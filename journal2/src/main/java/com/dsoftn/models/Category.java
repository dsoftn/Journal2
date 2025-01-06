package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.utils.UString;
import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;

import com.dsoftn.events.CategoryAddedEvent;
import com.dsoftn.events.CategoryUpdatedEvent;
import com.dsoftn.events.CategoryDeletedEvent;


public class Category implements IModelEntity{
    // Properties
    private Integer id = 0;
    private String name = "";
    private String description = "";
    private String relatedCategories = "";
    private Integer parent = 0;
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);

    // Constructors
    
    public Category() {}

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
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
        return false;
    }

    public boolean loadFromResultSet(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.name = rs.getString("name");
            this.description = rs.getString("description");
            this.relatedCategories = rs.getString("related_categories");
            this.parent = rs.getInt("parent");
            this.created = rs.getString("created");
            return true;
        } catch (Exception e) {
            UError.exception("Category.loadFromResultSet: Failed to load category from result set", e);
            return false;
        }
    }

    @Override
    public boolean add() {
        // Check if category can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO categories " + 
                "(name, description, related_categories, parent, created) " + 
                "VALUES (?, ?, ?, ?, ?)",
                this.name,
                this.description,
                this.relatedCategories,
                this.parent,
                this.created);

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
            OBJECTS.EVENT_HANDLER.fireEvent(new CategoryAddedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Category.add: Failed to add category", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (this.created == null || this.created.isEmpty()) return false;
        if (OBJECTS.CATEGORIES.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if category can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE categories " + 
                "SET name = ?, description = ?, related_categories = ?, parent = ?, created = ? " + 
                "WHERE id = ?",
                this.name,
                this.description,
                this.relatedCategories,
                this.parent,
                this.created,
                this.id);

            if (stmt == null) {
                UError.error("Category.update: Failed to update category", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Category.update: Failed to write category to database", "Updating category failed");
                return false;
            }

            // Update in repository
            if (!OBJECTS.CATEGORIES.update(this)) {
                UError.error("Category.update: Failed to update category in repository", "Updating category in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new CategoryUpdatedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Category.update: Failed to update category", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
    }

    @Override
    public boolean canBeUpdated() {
        if (this.id == 0) return false;
        if (this.created == null || this.created.isEmpty()) return false;
        if (!OBJECTS.CATEGORIES.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if category can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
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
            OBJECTS.EVENT_HANDLER.fireEvent(new CategoryDeletedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Category.delete: Failed to delete category", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
    }

    @Override
    public boolean canBeDeleted() {
        if (this.id == 0) return false;
        if (!OBJECTS.CATEGORIES.isExists(this.id)) return false;
        return true;
    }

    // Getters
    
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Category> getRelatedCategories() {
        return getRelatedCategoriesList();
    }

    private List<Category> getRelatedCategoriesList() {
        List<String> ids = UString.splitAndStrip(this.relatedCategories, ",");
        List<Category> categories = new ArrayList<Category>();

        for (String id : ids) {
            if (UNumbers.isStringInteger(id)) {
                Category category = OBJECTS.CATEGORIES.getEntity(UNumbers.toInteger(id));
                if (category != null) {
                    categories.add(category);
                }
            }
        }
        return categories;
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
        this.relatedCategories = String.join(",", relatedCategories.stream().map((Category category) -> String.valueOf(category.getID())).collect(Collectors.toList()));
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

}
