package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.enums.models.AttachmentTypeEnum;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.events.TagAddedEvent;
import com.dsoftn.events.TagDeletedEvent;
import com.dsoftn.events.TagUpdatedEvent;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UList;

import javafx.event.Event;
import javafx.scene.image.Image;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;


public class Tag implements IModelEntity, ICustomEventListener {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private String name = "";
    private String description = "";
    private List<Integer> relatedTags = new ArrayList<>();
    private List<Integer> relatedAttachments = new ArrayList<>();
    private int defaultAttachment = CONSTANTS.INVALID_ID;
    private int scope = ModelEnum.ALL.getValue();
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);

    // Constructors
    
    public Tag() {
        OBJECTS.EVENT_HANDLER.register(
        this,
        RelationAddedEvent.RELATION_ADDED_EVENT,
        RelationUpdatedEvent.RELATION_UPDATED_EVENT,
        RelationDeletedEvent.RELATION_DELETED_EVENT
        );
    }

    // Event listeners

    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof RelationAddedEvent || event instanceof RelationUpdatedEvent || event instanceof RelationDeletedEvent) {
            Relation newRelation = null;
            Relation oldRelation = null;
            if (event instanceof RelationAddedEvent) {
                RelationAddedEvent relationAddedEvent = (RelationAddedEvent) event;
                // If relation is part of loop event then no need to update because this object caused the event
                if (relationAddedEvent.isLoopEvent()) { return; }

                newRelation = relationAddedEvent.getRelation();
            }
            else if (event instanceof RelationUpdatedEvent) {
                RelationUpdatedEvent relationUpdatedEvent = (RelationUpdatedEvent) event;
                // If relation is part of loop event then no need to update because this object caused the event
                if (relationUpdatedEvent.isLoopEvent()) { return; }

                newRelation = relationUpdatedEvent.getNewRelation();
                oldRelation = relationUpdatedEvent.getOldRelation();
            }
            else {
                RelationDeletedEvent relationDeletedEvent = (RelationDeletedEvent) event;
                // If relation is part of loop event then no need to update because this object caused the event
                if (relationDeletedEvent.isLoopEvent()) { return; }
                
                newRelation = relationDeletedEvent.getRelation();
            }
            
            onRelationEvents(newRelation);
            onRelationEvents(oldRelation);
        }
    }

    private void onRelationEvents(Relation relation) {
        if (relation == null) {
            return;
        }

        // Check if relation belongs to this block
        if (relation.getBaseModel() != ModelEnum.TAG && relation.getBaseID() != this.id) {
            return;
        }

        List<Integer>  newRelatedTags = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.TAG, this.id, ModelEnum.TAG).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedAttachments = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.TAG, this.id, ModelEnum.ATTACHMENT).stream().map(Relation::getRelatedID).collect(Collectors.toList());

        relatedTags = newRelatedTags;
        relatedAttachments = newRelatedAttachments;
        update();
    }

    // Interface methods
    
    @Override
    public Integer getID() {
        return this.id;
    }

    @Override
    public boolean load(Integer id) {
        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = db.preparedStatement("SELECT * FROM tags WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Tag.load: Failed to load tag", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Tag.load: Failed to load tag", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Tag.load: Failed to load tag", e);
            return false;
        }
        finally {
            if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
        return false;
    }

    public boolean loadFromResultSet(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");
            this.description = rs.getString("description");
            this.scope = rs.getInt("scope");
            this.created = rs.getString("created");
            this.defaultAttachment = rs.getInt("default_attachment");

            this.setRelatedTags(OBJECTS.TAGS.getTagsListFromRelations(
                OBJECTS.RELATIONS.getRelationsList(ModelEnum.TAG, this.id, ModelEnum.TAG)
            ));

            this.setRelatedAttachments(OBJECTS.ATTACHMENTS.getAttachmentsListFromRelations(
                OBJECTS.RELATIONS.getRelationsList(ModelEnum.TAG, this.id, ModelEnum.ATTACHMENT)
            ));

            return isValid();
        } catch (Exception e) {
            UError.exception("Tag.loadFromResultSet: Failed to load tag from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return  this.name != null &&
                this.description != null &&
                this.created != null;
    }

    @Override
    public boolean add() {
        // Check if tag can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO tags " + 
                "(name, description, scope, created, default_attachment) " + 
                "VALUES (?, ?, ?, ?)",
                this.name,
                this.description,
                this.scope,
                this.created,
                this.defaultAttachment);

            if (stmt == null) {
                UError.error("Tag.add: Failed to add tag", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Tag.add: Failed to write tag to database", "Adding tag failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.TAGS.add(this)) {
                UError.error("Tag.add: Failed to add tag to repository", "Adding tag to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new TagAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Tag.add: Failed to add tag", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (this.id != CONSTANTS.INVALID_ID) return false;
        if (this.created == null || this.created.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (OBJECTS.TAGS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if tag can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE tags " + 
                "SET name = ?, description = ?, scope = ?, created = ? default_attachment = ?" + 
                "WHERE id = ?",
                this.name,
                this.description,
                this.scope,
                this.created,
                this.defaultAttachment,
                this.id);

            if (stmt == null) {
                UError.error("Tag.update: Failed to update tag", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Tag.update: Failed to write tag to database", "Updating tag failed");
                return false;
            }

            Tag oldTag = OBJECTS.TAGS.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.TAGS.update(this)) {
                UError.error("Tag.update: Failed to update tag in repository", "Updating tag in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new TagUpdatedEvent(oldTag, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Tag.update: Failed to update tag", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeUpdated() {
        if (this.id == CONSTANTS.INVALID_ID) return false;
        if (this.created == null || this.created.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (!OBJECTS.TAGS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if tag can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM tags " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Tag.delete: Failed to delete tag", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Tag.delete: Failed to delete tag from database", "Deleting tag failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.TAGS.delete(this)) {
                UError.error("Tag.delete: Failed to delete tag from repository", "Deleting tag from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new TagDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Tag.delete: Failed to delete tag", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeDeleted() {
        if (this.id == CONSTANTS.INVALID_ID) return false;
        if (!OBJECTS.TAGS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public IModelEntity duplicateModel() {
        Tag newTag = new Tag();

        newTag.id = this.id;
        newTag.name = this.name;
        newTag.description = this.description;
        newTag.scope = this.scope;
        newTag.created = this.created;
        newTag.defaultAttachment = this.defaultAttachment;

        newTag.relatedTags = UList.deepCopy(this.relatedTags);
        newTag.relatedAttachments = UList.deepCopy(this.relatedAttachments);

        return newTag;
    }

    public Tag duplicate() {
        Tag block = (Tag) this.duplicateModel();
        return block;
    }

    @Override
    public String getImagePath() {
        if (defaultAttachment != CONSTANTS.INVALID_ID && OBJECTS.ATTACHMENTS.getEntity(defaultAttachment).getType() == AttachmentTypeEnum.IMAGE) {
            if (OBJECTS.ATTACHMENTS.prepare(OBJECTS.ATTACHMENTS.getEntity(defaultAttachment))) {
                return OBJECTS.ATTACHMENTS.getEntity(defaultAttachment).getFilePath();
            }
        }

        for (Integer attachmentID : relatedAttachments) {
            Attachment attachment = OBJECTS.ATTACHMENTS.getEntity(attachmentID);
            if (attachment.getType() == AttachmentTypeEnum.IMAGE) {
                if (!OBJECTS.ATTACHMENTS.prepare(attachment)) continue;
                return attachment.getFilePath();
            }
        }

        return null;
    }

    @Override
    public Image getGenericImage() {
        return new Image(getClass().getResourceAsStream("/images/tag_generic.png"));
    }

    @Override
    public String getFriendlyName() {
        return  OBJECTS.SETTINGS.getl("Tag_FriendlyName")
                .replace("#1", String.valueOf(id))
                .replace("#2", name);
    }

    @Override
    public String getTooltipString() {
        return  OBJECTS.SETTINGS.getl("Tag_Tooltip")
                .replace("#1", String.valueOf(id))
                .replace("#2", name)
                .replace("#3", description)
                .replace("#4", created);
    }


    // Getters
    
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Tag> getRelatedTags() {
        return OBJECTS.TAGS.getTagsListFromIDs(this.relatedTags);
    }

    public List<Integer> getRelatedTagsIDs() {
        return this.relatedTags;
    }

    public List<Attachment> getRelatedAttachments() {
        return OBJECTS.ATTACHMENTS.getAttachmentsListFromIDs(this.relatedAttachments);
    }

    public List<Integer> getRelatedAttachmentsIDs() {
        return this.relatedAttachments;
    }

    public int getDefaultAttachment() {
        return this.defaultAttachment;
    }

    public int getScope() {
        return this.scope;
    }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Tag.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Tag.getCreatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR_JSON() {
        return this.created;
    }

    // Setters

    public void setID(Integer id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setRelatedTags(List<Tag> relatedTags) {
        this.relatedTags = relatedTags.stream().map(Tag::getID).collect(Collectors.toList());
    }

    public void setRelatedAttachments(List<Attachment> relatedAttachments) {
        this.relatedAttachments = relatedAttachments.stream().map(Attachment::getID).collect(Collectors.toList());
    }

    public void setDefaultAttachment(int defaultAttachment) {
        this.defaultAttachment = defaultAttachment;
    }

    public void setScope(ModelEnum... scopes) {
        this.scope = ModelEnum.combineValues(scopes);
    }

    public void setScope(int scope) { this.scope = scope; }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Tag.setCreated: Failed to parse date", e);
        }
    }

    public void setCreated() {
        setCreated(LocalDateTime.now());
    }

    public void setCreatedSTR_JSON(String created) { this.created = created; }

}
