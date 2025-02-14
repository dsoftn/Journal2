package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;

import javafx.scene.image.Image;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;


public class Relation implements IModelEntity {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private int baseModel = ModelEnum.NONE.getValue();
    private int baseID = CONSTANTS.INVALID_ID;
    private int relatedModel = ModelEnum.NONE.getValue();
    private int relatedID = CONSTANTS.INVALID_ID;
    private String description = "";
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);

    private boolean eventsIgnored = false;

    // Constructors
    
    public Relation() {}

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
            stmt = db.preparedStatement("SELECT * FROM relations WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Relation.load: Failed to load relation", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Relation.load: Failed to load relation", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Relation.load: Failed to load relation", e);
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
            this.baseModel = rs.getInt("base_model");
            this.baseID = rs.getInt("base_id");
            this.relatedModel = rs.getInt("related_model");
            this.relatedID = rs.getInt("related_id");
            this.description = rs.getString("description");
            this.created = rs.getString("created");
            
            return isValid();
        } catch (Exception e) {
            UError.exception("Relation.loadFromResultSet: Failed to load relation from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return  this.description != null &&
                this.created != null;
    }

    @Override
    public boolean add() {
        return add(false);
    }

    public boolean add(boolean isLoopEvent) {
        // Check if relation can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO relations " + 
                "(base_model, base_id, related_model, related_id, description, created) " + 
                "VALUES (?, ?, ?, ?, ?, ?)",
                this.baseModel,
                this.baseID,
                this.relatedModel,
                this.relatedID,
                this.description,
                this.created);

            if (stmt == null) {
                UError.error("Relation.add: Failed to add relation", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Relation.add: Failed to write relation to database", "Adding relation failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.RELATIONS.add(this)) {
                UError.error("Relation.add: Failed to add relation to repository", "Adding relation to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new RelationAddedEvent(this.duplicate(), isLoopEvent));

            return true;

        } catch (Exception e) {
            UError.exception("Relation.add: Failed to add relation", e);
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
        if (OBJECTS.RELATIONS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        return update(false);
    }

    public boolean update(boolean isLoopEvent) {
        // Check if relation can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE relations " + 
                "SET base_model = ?, base_id = ?, related_model = ?, related_id = ?, description = ?, created = ? " + 
                "WHERE id = ?",
                this.baseModel,
                this.baseID,
                this.relatedModel,
                this.relatedID,
                this.description,
                this.created,
                this.id);

            if (stmt == null) {
                UError.error("Relation.update: Failed to update relation", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Relation.update: Failed to write relation to database", "Updating relation failed");
                return false;
            }

            Relation oldRelation = OBJECTS.RELATIONS.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.RELATIONS.update(this)) {
                UError.error("Relation.update: Failed to update relation in repository", "Updating relation in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new RelationUpdatedEvent(oldRelation, this.duplicate(), isLoopEvent));

            return true;

        } catch (Exception e) {
            UError.exception("Relation.update: Failed to update relation", e);
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
        if (!OBJECTS.RELATIONS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        return delete(false);
    }

    public boolean delete(boolean isLoopEvent) {
        // Check if relation can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM relations " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Relation.delete: Failed to delete relation", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Relation.delete: Failed to delete relation from database", "Deleting relation failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.RELATIONS.delete(this)) {
                UError.error("Relation.delete: Failed to delete relation from repository", "Deleting relation from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new RelationDeletedEvent(this.duplicate(), isLoopEvent));

            return true;

        } catch (Exception e) {
            UError.exception("Relation.delete: Failed to delete relation", e);
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
        if (!OBJECTS.RELATIONS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public IModelEntity duplicateModel() {
        Relation newRelation = new Relation();
        newRelation.id = this.id;
        newRelation.baseModel = this.baseModel;
        newRelation.baseID = this.baseID;
        newRelation.relatedModel = this.relatedModel;
        newRelation.relatedID = this.relatedID;
        newRelation.description = this.description;
        newRelation.created = this.created;
        
        return newRelation;
    }

    public Relation duplicate() {
        Relation block = (Relation) this.duplicateModel();
        return block;
    }

    @Override
    public String getImagePath() {
        return null;
    }

    @Override
    public Image getGenericImage() {
        return new Image(getClass().getResourceAsStream("/images/relation_generic.png"));
    }

    @Override
    public String getFriendlyName() {
        String baseName = getFriendlyNameFromModel(this.baseModel, this.baseID);
        String relatedName = getFriendlyNameFromModel(this.relatedModel, this.relatedID);
        
        return  OBJECTS.SETTINGS.getl("Relation_FriendlyName")
                .replace("#1", String.valueOf(id))
                .replace("#2", baseName)
                .replace("#3", relatedName);
    }

    private String getFriendlyNameFromModel(int model, int id) {
        if (model == ModelEnum.BLOCK.getValue()) return OBJECTS.BLOCKS.getEntity(id).getFriendlyName();
        if (model == ModelEnum.DEFINITION.getValue()) return OBJECTS.DEFINITIONS.getEntity(id).getFriendlyName();
        if (model == ModelEnum.CATEGORY.getValue()) return OBJECTS.CATEGORIES.getEntity(id).getFriendlyName();
        if (model == ModelEnum.ACTOR.getValue()) return OBJECTS.ACTORS.getEntity(id).getFriendlyName();
        if (model == ModelEnum.ATTACHMENT.getValue()) return OBJECTS.ATTACHMENTS.getEntity(id).getFriendlyName();
        if (model == ModelEnum.TAG.getValue()) return OBJECTS.TAGS.getEntity(id).getFriendlyName();
        return "?";
    }

    @Override
    public String getTooltipString() {
        String baseName = getFriendlyNameFromModel(this.baseModel, this.baseID);
        String relatedName = getFriendlyNameFromModel(this.relatedModel, this.relatedID);
        
        return  OBJECTS.SETTINGS.getl("Relation_Tooltip")
                .replace("#1", String.valueOf(id))
                .replace("#2", baseName)
                .replace("#3", relatedName);
    }

    @Override
    public void ignoreEvents(boolean ignore) { this.eventsIgnored = ignore; }

    
    // Getters
    
    public ModelEnum getBaseModel() {
        return ModelEnum.fromInteger(this.baseModel);
    }

    public int getBaseID() {
        return this.baseID;
    }

    public ModelEnum getRelatedModel() {
        return ModelEnum.fromInteger(this.relatedModel);
    }

    public int getRelatedID() {
        return this.relatedID;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Relation.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Relation.getCreatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR_JSON() {
        return this.created;
    }

    // Setters

    public void setID(Integer id) { this.id = id; }

    public void setBaseModel(ModelEnum baseModel) { this.baseModel = baseModel.getValue(); }
    public void setBaseModel(Integer baseModel) {
        ModelEnum scope = ModelEnum.fromInteger(baseModel);
        if (scope == null || scope == ModelEnum.NONE || scope == ModelEnum.ALL) {
            UError.error("Relation.setBaseModel: Failed to set base model", "Invalid base model");
            return;
        }

        this.baseModel = baseModel;
    }

    public void setBaseID(Integer baseID) { this.baseID = baseID; }

    public void setRelatedModel(ModelEnum relatedModel) { this.relatedModel = relatedModel.getValue(); }
    public void setRelatedModel(Integer relatedModel) {
        ModelEnum scope = ModelEnum.fromInteger(relatedModel);
        if (scope == null || scope == ModelEnum.NONE || scope == ModelEnum.ALL) {
            UError.error("Relation.setRelatedModel: Failed to set related model", "Invalid related model");
            return;
        }

        this.relatedModel = relatedModel;
    }

    public void setRelatedID(Integer relatedID) { this.relatedID = relatedID; }

    public void setDescription(String description) { this.description = description; }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Relation.setCreated: Failed to parse date", e);
        }
    }

    public void setCreated() {
        setCreated(LocalDateTime.now());
    }

    public void setCreatedSTR_JSON(String created) {
        this.created = created;
    }

    // Overrides

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Relation) {
            return this.baseModel == ( (Relation) obj).baseModel
            && this.baseID == ( (Relation) obj).baseID
            && this.relatedModel == ( (Relation) obj).relatedModel
            && this.relatedID == ( (Relation) obj).relatedID;
        }
        return false;
    }

}
