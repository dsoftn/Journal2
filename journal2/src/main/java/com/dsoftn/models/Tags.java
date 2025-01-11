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
TABLE tags
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the tag
    description TEXT NOT NULL - description of the tag
    scope INTEGER NOT NULL - ScopeEnum combined value
    created TEXT NOT NULL - date in format for JSON
RELATED PROPERTIES:
    Tags
 */
public class Tags implements IModelRepository<Tag> {
    // Variables

    private Map<Integer, Tag> data = new LinkedHashMap<>(); // <id, Tag>

    // Interface methods

    @Override
    public boolean load() {
        boolean result = true;

        SQLiteDB db = new SQLiteDB();
        if (db.isConnected() == false) return false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM tags");
            if (stmt == null) {
                UError.error("Tags.load: Failed to load tags", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Tags.load: Failed to load tags", "Result set is unexpectedly null");
                return false;
            }

            while (rs.next()) {
                Tag tag = new Tag();
                result = tag.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Tags.load: Failed to load tag", "Loading tag failed");
                    result = false;
                    continue;
                }
                
                // Add tag
                add(tag);
            }

        } catch (Exception e) {
            UError.exception("Tags.load: Failed to load tags", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Tags.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Tags.load: Failed to close statement", e);
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
    public Tag getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Tag> getEntityAll() {
        List<Tag> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Tag entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Tag entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Tag entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.remove(entity.getID());
        return true;
    }

    // Public methods

    public List<Tag> getTagsListFromIDs(List<Integer> tagIDs) {
        List<Tag> tags = new ArrayList<>();
        for (Integer tagID : tagIDs) {
            Tag tag = getEntity(tagID);
            if (tag != null) tags.add(tag);
        }

        return tags;
    }

    public List<Tag> getTagsListFromRelations(List<Relation> relations) {
        List<Integer> tagIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ScopeEnum.TAG) {
                tagIDs.add(relation.getRelatedID());
            }
        }

        return getTagsListFromIDs(tagIDs);
    }

}
