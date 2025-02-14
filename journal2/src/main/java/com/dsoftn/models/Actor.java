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
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UList;

import javafx.event.Event;
import javafx.scene.image.Image;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;

import com.dsoftn.events.ActorAddedEvent;
import com.dsoftn.events.ActorUpdatedEvent;
import com.dsoftn.events.ActorDeletedEvent;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;


public class Actor implements IModelEntity, ICustomEventListener {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private String nick = "";
    private String name = "";
    private String description = "";
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private String updated = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private List<Integer> relatedAttachments = new ArrayList<Integer>();
    private int defaultAttachment = CONSTANTS.INVALID_ID;

    private boolean eventsIgnored = false;

    // Constructors

    public Actor() {
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
        if (eventsIgnored) { return; }
        
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

        // Check if relation belongs to this actor
        if (relation.getBaseModel() != ModelEnum.ACTOR && relation.getBaseID() != this.id) {
            return;
        }

        List<Integer>  newRelatedAttachments = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.ACTOR, this.id, ModelEnum.ATTACHMENT).stream().map(Relation::getRelatedID).collect(Collectors.toList());

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
            stmt = db.preparedStatement("SELECT * FROM actors WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Actor.load: Failed to load actor", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Actor.load: Failed to load actor", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Actor.load: Failed to load actor", e);
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
            this.nick = rs.getString("nick");
            this.name = rs.getString("name");
            this.description = rs.getString("description");
            this.created = rs.getString("created");
            this.updated = rs.getString("updated");
            this.defaultAttachment = rs.getInt("default_attachment");

            this.setRelatedAttachments(
                OBJECTS.ATTACHMENTS.getAttachmentsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.ACTOR, this.id, ModelEnum.ATTACHMENT))
            );

            return isValid();
        } catch (Exception e) {
            UError.exception("Actor.loadFromResultSet: Failed to load actor from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return this.nick != null &&
                this.name != null &&
                this.description != null &&
                this.created != null &&
                this.updated != null;
    }

    @Override
    public boolean add() {
        // Check if actor can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO actors " + 
                "(nick, name, description, created, updated, default_attachment) " + 
                "VALUES (?, ?, ?, ?, ?, ?)",
                this.nick,
                this.name,
                this.description,
                this.created,
                this.updated,
                this.defaultAttachment);

            if (stmt == null) {
                UError.error("Actor.add: Failed to add actor", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Actor.add: Failed to write actor to database", "Adding actor failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.ACTORS.add(this)) {
                UError.error("Actor.add: Failed to add actor to repository", "Adding actor to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new ActorAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Actor.add: Failed to add actor", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (this.created == null || this.created.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (this.updated == null || this.updated.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (OBJECTS.ACTORS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if actor can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE actors " + 
                "SET nick = ?, name = ?, description = ?, created = ?, updated = ?, default_attachment = ? " + 
                "WHERE id = ?",
                this.nick,
                this.name,
                this.description,
                this.created,
                this.updated,
                this.defaultAttachment,
                this.id);

            if (stmt == null) {
                UError.error("Actor.update: Failed to update actor", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Actor.update: Failed to write actor to database", "Updating actor failed");
                return false;
            }

            Actor oldActor = OBJECTS.ACTORS.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.ACTORS.update(this)) {
                UError.error("Actor.update: Failed to update actor in repository", "Updating actor in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new ActorUpdatedEvent(oldActor, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Actor.update: Failed to update actor", e);
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
        if (this.updated == null || this.updated.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (!OBJECTS.ACTORS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if actor can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM actors " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Actor.delete: Failed to delete actor", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Actor.delete: Failed to delete actor from database", "Deleting actor failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.ACTORS.delete(this)) {
                UError.error("Actor.delete: Failed to delete actor from repository", "Deleting actor from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new ActorDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Actor.delete: Failed to delete actor", e);
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
        if (!OBJECTS.ACTORS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public IModelEntity duplicateModel() {
        Actor actor = new Actor();
        actor.id = this.id;
        actor.nick = this.nick;
        actor.name = this.name;
        actor.description = this.description;
        actor.created = this.created;
        actor.updated = this.updated;
        actor.defaultAttachment = this.defaultAttachment;

        actor.relatedAttachments = UList.deepCopy(this.relatedAttachments);

        return actor;
    }

    public Actor duplicate() {
        Actor block = (Actor) this.duplicateModel();
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
        return new Image(getClass().getResourceAsStream("/images/actor_generic.png"));
    }

    public String getGenericImageResourceURL() {
        return "/images/actor_generic.png";
    }

    @Override
    public String getFriendlyName() {
        return  OBJECTS.SETTINGS.getl("Actor_FriendlyName")
                .replace("#1", String.valueOf(id))
                .replace("#2", nick);
    }

    @Override
    public String getTooltipString() {
        return  OBJECTS.SETTINGS.getl("Actor_Tooltip")
                .replace("#1", String.valueOf(id))
                .replace("#2", nick)
                .replace("#3", name)
                .replace("#4", description)
                .replace("#5", created)
                .replace("#6", updated);
    }

    @Override
    public void ignoreEvents(boolean ignore) { this.eventsIgnored = ignore; }

    // Getters

    public String getNick() { return this.nick; }

    public String getName() { return this.name; }
    
    public String getDescription() { return this.description; }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Actor.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Actor.getCreatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR_JSON() {
        return this.created;
    }

    public LocalDateTime getUpdatedOBJ() {
        try {
            return LocalDateTime.parse(this.updated, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Actor.getUpdatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getUpdatedSTR() {
        try {
            return LocalDateTime.parse(this.updated, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Actor.getUpdatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getUpdatedSTR_JSON() {
        return this.updated;
    }

    public List<Attachment> getRelatedAttachments() {
        return OBJECTS.ATTACHMENTS.getAttachmentsListFromIDs(this.relatedAttachments);
    }

    public List<Integer> getRelatedAttachmentsIDs() {
        return this.relatedAttachments;
    }
    
    public int getDefaultAttachment() { return this.defaultAttachment; }

    // Setters

    public void setNick(String nick) { this.nick = nick; }

    public void setID(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }
    
    public void setDescription(String description) { this.description = description; }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Actor.setCreated: Failed to parse date", e);
        }
    }

    public void setCreated() {
        setCreated(LocalDateTime.now());
    }

    public void setCreatedSTR_JSON(String created) { this.created = created; }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setUpdated(String updated) {
        try {
            this.updated = LocalDateTime.parse(updated, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Actor.setUpdated: Failed to parse date", e);
        }
    }

    public void setUpdated() {
        setUpdated(LocalDateTime.now());
    }

    public void setUpdatedSTR_JSON(String updated) { this.updated = updated; }

    public void setRelatedAttachments(List<Attachment> attachments) {
        this.relatedAttachments = attachments.stream().map((Attachment attachment) -> attachment.getID()).collect(Collectors.toList());
    }
    
    public void setDefaultAttachment(int defaultAttachment) { this.defaultAttachment = defaultAttachment; }

    public void setDefaultAttachment(Attachment defaultAttachment) { this.defaultAttachment = defaultAttachment.getID(); }

}
