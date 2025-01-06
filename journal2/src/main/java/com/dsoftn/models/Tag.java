package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.events.TagAddedEvent;
import com.dsoftn.events.TagDeletedEvent;
import com.dsoftn.events.TagUpdatedEvent;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.utils.UString;
import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;


public class Tag implements IModelEntity{
    // Properties
    private Integer id = 0;
    private String name = "";
    private String description = "";
    private String relatedTags = "";
    private Integer scope = ScopeEnum.ALL.getValue();
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);

    // Constructors
    
    public Tag() {}

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
            this.relatedTags = rs.getString("related_tags");
            this.scope = rs.getInt("scope");
            this.created = rs.getString("created");
            return true;
        } catch (Exception e) {
            UError.exception("Tag.loadFromResultSet: Failed to load tag from result set", e);
            return false;
        }
    }

    @Override
    public boolean add() {
        // Check if tag can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO tags " + 
                "(name, description, related_tags, scope, created) " + 
                "VALUES (?, ?, ?, ?, ?)",
                this.name,
                this.description,
                this.relatedTags,
                this.scope,
                this.created);

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
            OBJECTS.EVENT_HANDLER.fireEvent(new TagAddedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Tag.add: Failed to add tag", e);
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
        if (OBJECTS.TAGS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if tag can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE tags " + 
                "SET name = ?, description = ?, related_tags = ?, scope = ?, created = ? " + 
                "WHERE id = ?",
                this.name,
                this.description,
                this.relatedTags,
                this.scope,
                this.created,
                this.id);

            if (stmt == null) {
                UError.error("Tag.update: Failed to update tag", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Tag.update: Failed to write tag to database", "Updating tag failed");
                return false;
            }

            // Update in repository
            if (!OBJECTS.TAGS.update(this)) {
                UError.error("Tag.update: Failed to update tag in repository", "Updating tag in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new TagUpdatedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Tag.update: Failed to update tag", e);
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
        if (!OBJECTS.TAGS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if tag can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
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
            OBJECTS.EVENT_HANDLER.fireEvent(new TagDeletedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Tag.delete: Failed to delete tag", e);
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
        if (!OBJECTS.TAGS.isExists(this.id)) return false;
        return true;
    }

    // Getters
    
    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Tag> getRelatedTags() {
        return getRelatedTagsList();
    }

    private List<Tag> getRelatedTagsList() {
        List<String> ids = UString.splitAndStrip(this.relatedTags, ",");
        List<Tag> tags = new ArrayList<Tag>();

        for (String id : ids) {
            if (UNumbers.isStringInteger(id)) {
                Tag tag = OBJECTS.TAGS.getEntity(UNumbers.toInteger(id));
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        return tags;
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
        this.relatedTags = String.join(",", relatedTags.stream().map((Tag tag) -> String.valueOf(tag.getID())).collect(Collectors.toList()));
    }

    public void setScope(ScopeEnum... scopes) {
        this.scope = ScopeEnum.combineValues(scopes);
    }

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

    


}
