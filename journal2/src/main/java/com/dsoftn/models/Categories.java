package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;


/*
TABLE categories
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the category
    description TEXT NOT NULL - description of the category
    related_categories TEXT NOT NULL - list of related category ids delimited by comma
    parent INTEGER NOT NULL - id of the parent category
    created TEXT NOT NULL - date in format for JSON
 */
public class Categories implements IModelRepository<Category> {
    // Variables

    private Map<Integer, Category> data = new LinkedHashMap<>(); // <id, Category>

    // Interface methods

    @Override
    public boolean load() {
        boolean result = true;

        SQLiteDB db = new SQLiteDB();
        if (db.isConnected() == false) return false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM categories");
            if (stmt == null) {
                UError.error("Categories.load: Failed to load categories", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Categories.load: Failed to load categories", "Result set is unexpectedly null");
                return false;
            }

            while (rs.next()) {
                Category category = new Category();
                result = category.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Categories.load: Failed to load category", "Loading category failed");
                    result = false;
                    continue;
                }
                
                // Add tag
                add(category);
            }

        } catch (Exception e) {
            UError.exception("Categories.load: Failed to load categories", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Categories.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Categories.load: Failed to close statement", e);
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
    public Category getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Category> getEntityAll() {
        List<Category> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Category entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Category entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Category entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.remove(entity.getID());
        return true;
    }

}
