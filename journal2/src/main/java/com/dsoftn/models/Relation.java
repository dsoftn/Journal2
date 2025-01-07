package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;


public class Relation implements IModelEntity<Relation> {
    // Properties
    private Integer id = CONSTANTS.INVALID_ID;
    private Integer baseModel = ScopeEnum.NONE.getValue();
    private Integer baseID = CONSTANTS.INVALID_ID;
    private Integer relatedModel = ScopeEnum.NONE.getValue();
    private Integer relatedID = CONSTANTS.INVALID_ID;
    private String description = "";
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);

    // Constructors
    
    public Relation() {}

    // Interface methods
    
    @Override
    public Integer getID() {
        return this.id;
    }

    @Override
    public boolean load(Integer id) {
        SQLiteDB db = new SQLiteDB();
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
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
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
            return true;
        } catch (Exception e) {
            UError.exception("Relation.loadFromResultSet: Failed to load relation from result set", e);
            return false;
        }
    }

    @Override
    public boolean add() {
        // Check if relation can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
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
            OBJECTS.EVENT_HANDLER.fireEvent(new RelationAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Relation.add: Failed to add relation", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
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
        // Check if relation can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
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
            OBJECTS.EVENT_HANDLER.fireEvent(new RelationUpdatedEvent(oldRelation, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Relation.update: Failed to update relation", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
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
        // Check if relation can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
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
            OBJECTS.EVENT_HANDLER.fireEvent(new RelationDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Relation.delete: Failed to delete relation", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
    }

    @Override
    public boolean canBeDeleted() {
        if (this.id == CONSTANTS.INVALID_ID) return false;
        if (!OBJECTS.RELATIONS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public Relation duplicate() {
        Relation newRelation = new Relation();
        newRelation.setID(this.id);
        newRelation.setBaseModel(this.baseModel);
        newRelation.setBaseID(this.baseID);
        newRelation.setRelatedModel(this.relatedModel);
        newRelation.setRelatedID(this.relatedID);
        newRelation.setDescription(this.description);
        newRelation.setCreatedSTR_JSON(this.getCreatedSTR_JSON());
        return newRelation;
    }

    // Getters
    
    public ScopeEnum getBaseModel() {
        return ScopeEnum.fromInteger(this.baseModel);
    }

    public int getBaseID() {
        return this.baseID;
    }

    public ScopeEnum getRelatedModel() {
        return ScopeEnum.fromInteger(this.relatedModel);
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

    public void setBaseModel(ScopeEnum baseModel) { this.baseModel = baseModel.getValue(); }
    public void setBaseModel(Integer baseModel) {
        ScopeEnum scope = ScopeEnum.fromInteger(baseModel);
        if (scope == null || scope == ScopeEnum.NONE || scope == ScopeEnum.ALL) {
            UError.error("Relation.setBaseModel: Failed to set base model", "Invalid base model");
            return;
        }

        this.baseModel = baseModel;
    }

    public void setBaseID(Integer baseID) { this.baseID = baseID; }

    public void setRelatedModel(ScopeEnum relatedModel) { this.relatedModel = relatedModel.getValue(); }
    public void setRelatedModel(Integer relatedModel) {
        ScopeEnum scope = ScopeEnum.fromInteger(relatedModel);
        if (scope == null || scope == ScopeEnum.NONE || scope == ScopeEnum.ALL) {
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
