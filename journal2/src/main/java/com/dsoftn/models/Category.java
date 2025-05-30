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

import com.dsoftn.events.CategoryAddedEvent;
import com.dsoftn.events.CategoryUpdatedEvent;
import com.dsoftn.events.CategoryDeletedEvent;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;


public class Category implements IModelEntity, ICustomEventListener {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private String name = "";
    private String description = "";
    private List<Integer> relatedCategories = new ArrayList<>();
    private List<Integer> relatedTags = new ArrayList<>();
    private List<Integer> relatedAttachments = new ArrayList<>();
    private int defaultAttachment = CONSTANTS.INVALID_ID;
    private int parent = CONSTANTS.INVALID_ID;
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);

    private boolean eventsIgnored = false;

    // Constructors
    
    public Category() {
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

        // Check if relation belongs to this block
        if (relation.getBaseModel() != ModelEnum.CATEGORY && relation.getBaseID() != this.id) {
            return;
        }

        List<Integer>  newRelatedCategories = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.CATEGORY, this.id, ModelEnum.CATEGORY).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedTags = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.CATEGORY, this.id, ModelEnum.TAG).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedAttachments = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.CATEGORY, this.id, ModelEnum.ATTACHMENT).stream().map(Relation::getRelatedID).collect(Collectors.toList());

        relatedCategories = newRelatedCategories;
        relatedTags = newRelatedTags;
        relatedAttachments = newRelatedAttachments;
        update();
    }

    // Interface IModelEntity methods
    
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
            stmt = db.preparedStatement("SELECT * FROM categories WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Category.load: Failed to load category", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Category.load: Failed to load category", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Category.load: Failed to load category", e);
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
            this.parent = rs.getInt("parent");
            this.created = rs.getString("created");
            this.defaultAttachment = rs.getInt("default_attachment");
            
            this.setRelatedCategories(OBJECTS.CATEGORIES.getCategoriesListFromRelations(
                OBJECTS.RELATIONS.getRelationsList(ModelEnum.CATEGORY, this.id, ModelEnum.CATEGORY)
            ));

            this.setRelatedTags(OBJECTS.TAGS.getTagsListFromRelations(
                OBJECTS.RELATIONS.getRelationsList(ModelEnum.CATEGORY, this.id, ModelEnum.TAG)
            ));
            
            return isValid();
        } catch (Exception e) {
            UError.exception("Category.loadFromResultSet: Failed to load category from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return  name != null &&
                description != null &&
                created != null;
    }

    @Override
    public boolean add() {
        // Check if category can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO categories " + 
                "(name, description, parent, created, default_attachment) " + 
                "VALUES (?, ?, ?, ?)",
                this.name,
                this.description,
                this.parent,
                this.created,
                this.defaultAttachment);

            if (stmt == null) {
                UError.error("Category.add: Failed to add category", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Category.add: Failed to write category to database", "Adding category failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.CATEGORIES.add(this)) {
                UError.error("Category.add: Failed to add category to repository", "Adding category to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new CategoryAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Category.add: Failed to add category", e);
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
        if (OBJECTS.CATEGORIES.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if category can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE categories " + 
                "SET name = ?, description = ?, parent = ?, created = ? default_attachment = ?" + 
                "WHERE id = ?",
                this.name,
                this.description,
                this.parent,
                this.created,
                this.defaultAttachment,
                this.id);

            if (stmt == null) {
                UError.error("Category.update: Failed to update category", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Category.update: Failed to write category to database", "Updating category failed");
                return false;
            }

            Category oldCategory = OBJECTS.CATEGORIES.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.CATEGORIES.update(this)) {
                UError.error("Category.update: Failed to update category in repository", "Updating category in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new CategoryUpdatedEvent(oldCategory, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Category.update: Failed to update category", e);
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
        if (!OBJECTS.CATEGORIES.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if category can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM categories " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Category.delete: Failed to delete category", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Category.delete: Failed to delete category from database", "Deleting category failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.CATEGORIES.delete(this)) {
                UError.error("Category.delete: Failed to delete category from repository", "Deleting category from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new CategoryDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Category.delete: Failed to delete category", e);
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
        if (!OBJECTS.CATEGORIES.isExists(this.id)) return false;
        return true;
    }

    @Override
    public IModelEntity duplicateModel() {
        Category newCategory = new Category();

        newCategory.id = this.id;
        newCategory.name = this.name;
        newCategory.description = this.description;
        newCategory.parent = this.parent;
        newCategory.created = this.created;
        newCategory.defaultAttachment = this.defaultAttachment;

        newCategory.relatedCategories = UList.deepCopy(this.relatedCategories);
        newCategory.relatedTags = UList.deepCopy(this.relatedTags);
        newCategory.relatedAttachments = UList.deepCopy(this.relatedAttachments);

        return newCategory;
    }

    public Category duplicate() {
        Category block = (Category) this.duplicateModel();
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
        return new Image(getClass().getResourceAsStream("/images/category_generic.png"));
    }

    @Override
    public String getGenericImageResourcePath() {
        return "/images/category_generic.png";
    }

    @Override
    public String getFriendlyName() {
        return  OBJECTS.SETTINGS.getl("Category_FriendlyName")
                .replace("#1", String.valueOf(id))
                .replace("#2", this.name);
    }

    @Override
    public String getTooltipString() {
        return  OBJECTS.SETTINGS.getl("Category_Tooltip")
                .replace("#1", String.valueOf(id))
                .replace("#2", name)
                .replace("#3", description)
                .replace("#4", created);
    }

    @Override
    public void ignoreEvents(boolean ignore) { this.eventsIgnored = ignore; }

    // Getters
    
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Category> getRelatedCategories() {
        return OBJECTS.CATEGORIES.getCategoriesListFromIDs(this.relatedCategories);
    }

    public List<Integer> getRelatedCategoriesIDs() {
        return this.relatedCategories;
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

    public Category getParent() {
        return OBJECTS.CATEGORIES.getEntity(this.parent);
    }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Category.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Category.getCreatedSTR: Failed to parse date", e);
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

    public void setRelatedCategories(List<Category> relatedCategories) {
        this.relatedCategories = relatedCategories.stream().map((Category category) -> category.getID()).collect(Collectors.toList());
    }

    public void setRelatedTags(List<Tag> relatedTags) {
        this.relatedTags = relatedTags.stream().map((Tag tag) -> tag.getID()).collect(Collectors.toList());
    }

    public void setRelatedAttachments(List<Attachment> relatedAttachments) {
        this.relatedAttachments = relatedAttachments.stream().map((Attachment attachment) -> attachment.getID()).collect(Collectors.toList());
    }

    public void setDefaultAttachment(int defaultAttachment) {
        this.defaultAttachment = defaultAttachment;
    }

    public void setParent(Category parent) {
        this.parent = parent.getID();
    }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Category.setCreated: Failed to parse date", e);
        }
    }

    public void setCreated() {
        setCreated(LocalDateTime.now());
    }

    public void setCreatedSTR_JSON(String created) {
        this.created = created;
    }

}
