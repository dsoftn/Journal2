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
import com.dsoftn.enums.models.SourceTypeEnum;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UList;

import javafx.event.Event;
import javafx.scene.image.Image;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.events.DefinitionAddedEvent;
import com.dsoftn.events.DefinitionUpdatedEvent;
import com.dsoftn.events.DefinitionDeletedEvent;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;
import com.dsoftn.utils.UString;


public class Definition implements IModelEntity<Definition>, ICustomEventListener {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private String name = "";
    private String description = "";
    private String source = "";
    private int sourceType = SourceTypeEnum.UNDEFINED.getValue();
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private String updated = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private List<Integer> relatedDefinitions = new ArrayList<Integer>();
    private List<Integer> relatedCategories = new ArrayList<Integer>();
    private List<Integer> relatedTags = new ArrayList<Integer>();
    private List<Integer> relatedAttachments = new ArrayList<Integer>();
    private List<Integer> relatedBlocks = new ArrayList<Integer>();
    private int defaultAttachment = CONSTANTS.INVALID_ID;
    private String variants = "";

    // Constructors

    public Definition() {
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

        // Check if relation belongs to this definition
        if (relation.getBaseModel() != ModelEnum.DEFINITION && relation.getBaseID() != this.id) {
            return;
        }

        List<Integer>  newRelatedAttachments = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.DEFINITION, this.id, ModelEnum.ATTACHMENT).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedBlocks = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.DEFINITION, this.id, ModelEnum.BLOCK).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedCategories = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.DEFINITION, this.id, ModelEnum.CATEGORY).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedTags = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.DEFINITION, this.id, ModelEnum.TAG).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedDefinitions = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.DEFINITION, this.id, ModelEnum.DEFINITION).stream().map(Relation::getRelatedID).collect(Collectors.toList());

        relatedAttachments = newRelatedAttachments;
        relatedBlocks = newRelatedBlocks;
        relatedCategories = newRelatedCategories;
        relatedTags = newRelatedTags;
        relatedDefinitions = newRelatedDefinitions;
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
            stmt = db.preparedStatement("SELECT * FROM definitions WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Definition.load: Failed to load definition", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Definition.load: Failed to load definition", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Definition.load: Failed to load definition", e);
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
            this.source = rs.getString("source");
            this.sourceType = rs.getInt("source_type");
            this.created = rs.getString("created");
            this.updated = rs.getString("updated");
            this.defaultAttachment = rs.getInt("default_attachment");

            this.setRelatedCategories(
                OBJECTS.CATEGORIES.getCategoriesListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.DEFINITION, this.id, ModelEnum.CATEGORY))
                );

            this.setRelatedTags(
                OBJECTS.TAGS.getTagsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.DEFINITION, this.id, ModelEnum.TAG))
            );

            this.setRelatedAttachments(
                OBJECTS.ATTACHMENTS.getAttachmentsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.DEFINITION, this.id, ModelEnum.ATTACHMENT))
            );

            this.setRelatedBlocks(
                OBJECTS.BLOCKS.getBlocksListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.DEFINITION, this.id, ModelEnum.BLOCK))
            );

            this.setRelatedDefinitions(
                OBJECTS.DEFINITIONS.getDefinitionsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.DEFINITION, this.id, ModelEnum.DEFINITION))
            );

            // Variants
            this.setVariants(
                OBJECTS.DEFINITIONS_VARIANTS.getVariantsWordList(this.id)
            );

            return isValid();
        } catch (Exception e) {
            UError.exception("Definition.loadFromResultSet: Failed to load definition from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return  this.name != null &&
                this.description != null &&
                this.source != null &&
                this.created != null &&
                this.updated != null;
    }

    @Override
    public boolean add() {
        // Check if definition can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO definitions " + 
                "(name, description, source, source_type, created, updated, default_attachment) " + 
                "VALUES (?,?,?,?,?,?,?)",
                this.name,
                this.description,
                this.source,
                this.sourceType,
                this.created,
                this.updated,
                this.defaultAttachment);

            if (stmt == null) {
                UError.error("Definition.add: Failed to add definition", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Definition.add: Failed to write definition to database", "Adding definition failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.DEFINITIONS.add(this)) {
                UError.error("Definition.add: Failed to add definition to repository", "Adding definition to repository failed");
                return false;
            }

            // Add Variants
            if (!OBJECTS.DEFINITIONS_VARIANTS.updateVariantsDefinitionAdd(this)) {
                UError.error("Definition.add: Failed to update definition variants", "Updating definition variants failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new DefinitionAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Definition.add: Failed to add definition", e);
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
        if (OBJECTS.DEFINITIONS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if definition can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE definitions " + 
                "SET name = ?, description = ?, source = ?, source_type = ?, created = ?, updated = ?, default_attachment = ? " + 
                "WHERE id = ?",
                this.name,
                this.description,
                this.source,
                this.sourceType,
                this.created,
                this.updated,
                this.defaultAttachment,
                this.id);

            if (stmt == null) {
                UError.error("Definition.update: Failed to update definition", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Definition.update: Failed to write  to database", "Updating definition failed");
                return false;
            }

            Definition oldDefinition = OBJECTS.DEFINITIONS.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.DEFINITIONS.update(this)) {
                UError.error("Definition.update: Failed to update definition in repository", "Updating definition in repository failed");
                return false;
            }

            // Update Variants
            if (!OBJECTS.DEFINITIONS_VARIANTS.updateVariantsDefinitionUpdate(this)) {
                UError.error("Definition.add: Failed to update definition variants", "Updating definition variants failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new DefinitionUpdatedEvent(oldDefinition, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Definition.update: Failed to update definition", e);
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
        if (!OBJECTS.DEFINITIONS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if definition can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM definitions " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Definition.delete: Failed to delete definition", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Definition.delete: Failed to delete definition from database", "Deleting definition failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.DEFINITIONS.delete(this)) {
                UError.error("Definition.delete: Failed to delete definition from repository", "Deleting definition from repository failed");
                return false;
            }

            // Delete Variants
            if (!OBJECTS.DEFINITIONS_VARIANTS.updateVariantsDefinitionDelete(this)) {
                UError.error("Definition.add: Failed to update definition variants", "Updating definition variants failed");
                return false;
            }
            
            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new DefinitionDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Definition.delete: Failed to delete definition", e);
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
        if (!OBJECTS.DEFINITIONS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public Definition duplicate() {
        Definition definition = new Definition();
        definition.id = this.id;
        definition.name = this.name;
        definition.description = this.description;
        definition.source = this.source;
        definition.sourceType = this.sourceType;
        definition.created = this.created;
        definition.updated = this.updated;
        definition.defaultAttachment = this.defaultAttachment;
        definition.variants = this.variants;

        definition.relatedCategories = UList.deepCopy(this.relatedCategories);
        definition.relatedTags = UList.deepCopy(this.relatedTags);
        definition.relatedAttachments = UList.deepCopy(this.relatedAttachments);
        definition.relatedBlocks = UList.deepCopy(this.relatedBlocks);
        definition.relatedDefinitions = UList.deepCopy(this.relatedDefinitions);

        return definition;
    }

    @Override
    public String getImagePath() {
        if (defaultAttachment != CONSTANTS.INVALID_ID && OBJECTS.ATTACHMENTS.getEntity(defaultAttachment).getType() == AttachmentTypeEnum.IMAGE) {
            return OBJECTS.ATTACHMENTS.getEntity(defaultAttachment).getFilePath();
        }

        for (Integer attachmentID : relatedAttachments) {
            Attachment attachment = OBJECTS.ATTACHMENTS.getEntity(attachmentID);
            if (attachment.getType() == AttachmentTypeEnum.IMAGE) {
                return attachment.getFilePath();
            }
        }

        return null;
    }

    @Override
    public Image getGenericImage() {
        return new Image(getClass().getResourceAsStream("/images/definition_generic.png"));
    }

    @Override
    public String getFriendlyName() {
        return  OBJECTS.SETTINGS.getl("Definition_FriendlyName")
                .replace("#1", String.valueOf(id))
                .replace("#2", name);
    }

    @Override
    public String getTooltipString() {
        return  OBJECTS.SETTINGS.getl("Definition_Tooltip")
                .replace("#1", String.valueOf(id))
                .replace("#2", name)
                .replace("#3", description)
                .replace("#4", source + " (" + SourceTypeEnum.fromInteger(this.sourceType).toString() + ")")
                .replace("#5", String.valueOf(relatedAttachments.size()))
                .replace("#6", created)
                .replace("#7", updated);
    }

    // Getters

    public String getName() { return this.name; }
    
    public String getDescription() { return this.description; }

    public String getSource() { return this.source; }

    public SourceTypeEnum getSourceType() { return SourceTypeEnum.fromInteger(this.sourceType); }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Definition.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Definition.getCreatedSTR: Failed to parse date", e);
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
            UError.exception("Definition.getUpdatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getUpdatedSTR() {
        try {
            return LocalDateTime.parse(this.updated, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Definition.getUpdatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getUpdatedSTR_JSON() {
        return this.updated;
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

    public List<Definition> getRelatedDefinitions() {
        return OBJECTS.DEFINITIONS.getDefinitionsListFromIDs(this.relatedDefinitions);
    }

    public List<Integer> getRelatedDefinitionsIDs() {
        return this.relatedDefinitions;
    }
    
    public int getDefaultAttachment() { return this.defaultAttachment; }

    public List<Block> getRelatedBlocks() {
        return OBJECTS.BLOCKS.getBlocksListFromIDs(this.relatedBlocks);
    }

    public List<Integer> getRelatedBlocksIDs() {
        return this.relatedBlocks;
    }

    public String getVariants() {
        return this.variants;
    }

    public List<String> getVariantsWordsList() {
        return UString.splitAndStrip(this.variants, "\n");
    }

    // Setters

    public void setID(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }
    
    public void setDescription(String description) { this.description = description; }

    public void setSource(String source) { this.source = source; }

    public void setSourceType(SourceTypeEnum sourceType) { this.sourceType = sourceType.getValue(); }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Definition.setCreated: Failed to parse date", e);
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
            UError.exception("Definition.setUpdated: Failed to parse date", e);
        }
    }

    public void setUpdated() {
        setUpdated(LocalDateTime.now());
    }

    public void setUpdatedSTR_JSON(String updated) { this.updated = updated; }

    public void setRelatedCategories(List<Category> categories) {
        this.relatedCategories = categories.stream().map((Category category) -> category.getID()).collect(Collectors.toList());
    }

    public void setRelatedTags(List<Tag> tags) {
        this.relatedTags = tags.stream().map((Tag tag) -> tag.getID()).collect(Collectors.toList());
    }
    
    public void setRelatedAttachments(List<Attachment> attachments) {
        this.relatedAttachments = attachments.stream().map((Attachment attachment) -> attachment.getID()).collect(Collectors.toList());
    }

    public void setRelatedDefinitions(List<Definition> definitions) {
        this.relatedDefinitions = definitions.stream().map((Definition definition) -> definition.getID()).collect(Collectors.toList());
    }
    
    public void setDefaultAttachment(int defaultAttachment) { this.defaultAttachment = defaultAttachment; }

    public void setDefaultAttachment(Attachment defaultAttachment) { this.defaultAttachment = defaultAttachment.getID(); }

    public void setRelatedBlocks(List<Block> relatedBlocks) {
        this.relatedBlocks = relatedBlocks.stream().map((Block block) -> block.getID()).collect(Collectors.toList());
    }

    public void setVariants(String variantsString) {
        this.variants = variantsString;
    }

    public void setVariants(List<String> variantsWordList) {
        this.variants = variantsWordList.stream().collect(Collectors.joining("\n"));
    }

}
