package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;


/*
TABLE relations
    id INTEGER PRIMARY KEY AUTOINCREMENT
    base_model INTEGER NOT NULL - type of the base model (ScopeEnum value)
    base_id INTEGER NOT NULL - id of the base model
    related_model INTEGER NOT NULL - type of the related model (ScopeEnum value)
    related_id INTEGER NOT NULL - id of the related model
    description TEXT NOT NULL - description of the relation
    created TEXT NOT NULL - date in format for JSON
 */
public class Relations implements IModelRepository<Relation> {
    // Variables

    private Map<Integer, Relation> data = new LinkedHashMap<>(); // <id, Relation>

    // Interface methods

    @Override
    public boolean load() {
        boolean result = true;

        SQLiteDB db = new SQLiteDB();
        if (db.isConnected() == false) return false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM relations");
            if (stmt == null) {
                UError.error("Relations.load: Failed to load relations", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Relations.load: Failed to load relations", "Result set is unexpectedly null");
                return false;
            }

            while (rs.next()) {
                Relation relation = new Relation();
                result = relation.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Relations.load: Failed to load relation", "Loading relation failed");
                    result = false;
                    continue;
                }
                
                // Add relation
                add(relation);
            }

        } catch (Exception e) {
            UError.exception("Relations.load: Failed to load relations", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Relations.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Relations.load: Failed to close statement", e);
                }
            }
            db.disconnect();
        }

        return result;
    }

    @Override
    public int count() {
        return data.size();
    }

    @Override
    public boolean isExists(Integer entityID) {
        return data.containsKey(entityID);
    }

    @Override
    public Relation getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Relation> getEntityAll() {
        List<Relation> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Relation entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Relation entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Relation entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.remove(entity.getID());
        return true;
    }

    // Public methods

    /**
     * Check if relation with same base model, base id, related model and related id exists
     */
    public boolean isSameRelationExists(Relation entity) {
        for (Relation relation : data.values()) {
            if (relation.equals(entity)) return true;
        }
        return false;
    }

    /**
     * Get relations list
     * @param baseModel - use ScopeEnum.ALL or null  to get all base models
     * @param baseID - use null to get all base ids
     * @param relatedModel - use ScopeEnum.ALL or null to get all related models
     * @param relatedID - use null to get all related ids
     * @return
     */
    public List<Relation> getRelationsList(ScopeEnum baseModel, Integer baseID, ScopeEnum relatedModel, Integer relatedID) {
        List<Relation> list = new ArrayList<>();

        if (baseModel == null) baseModel = ScopeEnum.ALL;
        if (relatedModel == null) relatedModel = ScopeEnum.ALL;

        for (Map.Entry<Integer, Relation> entry : data.entrySet()) {
            Relation relation = entry.getValue();

            if (baseModel != ScopeEnum.ALL && relation.getBaseModel() != baseModel) continue;
            if (baseID != null && relation.getBaseID() != baseID) continue;
            if (relatedModel != ScopeEnum.ALL && relation.getRelatedModel() != relatedModel) continue;
            if (relatedID != null && relation.getRelatedID() != relatedID) continue;

            list.add(relation);
        }

        return list;
    }

    public List<Relation> getRelationsList(ScopeEnum baseModel, Integer baseID, ScopeEnum relatedModel) {
        return getRelationsList(baseModel, baseID, relatedModel, null);
    }

    public List<Relation> getRelationsList(ScopeEnum baseModel, Integer baseID) {
        return getRelationsList(baseModel, baseID, null, null);
    }

    public List<Relation> getRelationsList(ScopeEnum baseModel) {
        return getRelationsList(baseModel, null, null, null);
    }

}
