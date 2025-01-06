package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;
import java.time.LocalDate;

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

import com.dsoftn.events.BlockAddedEvent;
import com.dsoftn.events.BlockUpdatedEvent;
import com.dsoftn.events.BlockDeletedEvent;


public class Block implements IModelEntity {
    // Properties
    private int id = 0;
    private String name = "";
    private String date = "";
    private String text = "";
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private String updated = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private String categories = "";
    private String tags = "";
    private String attachments = "";
    private int defaultAttachment = 0;
    private String relatedBlocks = "";

    // Constructors

    public Block() {}

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
            stmt = db.preparedStatement("SELECT * FROM blocks WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Block.load: Failed to load block", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Block.load: Failed to load block", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Block.load: Failed to load block", e);
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
            this.date = rs.getString("date");
            this.text = rs.getString("text");
            this.created = rs.getString("created");
            this.updated = rs.getString("updated");
            this.categories = rs.getString("categories");
            this.tags = rs.getString("tags");
            this.attachments = rs.getString("attachments");
            this.defaultAttachment = rs.getInt("default_attachment");
            this.relatedBlocks = rs.getString("related_blocks");
            return true;
        } catch (Exception e) {
            UError.exception("Block.loadFromResultSet: Failed to load block from result set", e);
            return false;
        }
    }

    @Override
    public boolean add() {
        // Check if block can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO blocks " + 
                "(name, date, text, created, updated, categories, tags, attachments, default_attachment, related_blocks) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                this.name,
                this.date,
                this.text,
                this.created,
                this.updated,
                this.categories,
                this.tags,
                this.attachments,
                this.defaultAttachment,
                this.relatedBlocks);

            if (stmt == null) {
                UError.error("Block.add: Failed to add block", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Block.add: Failed to write block to database", "Adding block failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.BLOCKS.add(this)) {
                UError.error("Block.add: Failed to add block to repository", "Adding block to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockAddedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Block.add: Failed to add block", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (this.date == null || this.date.isEmpty()) return false;
        if (this.created == null || this.created.isEmpty()) return false;
        if (this.updated == null || this.updated.isEmpty()) return false;
        if (OBJECTS.BLOCKS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if block can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE blocks " + 
                "SET name = ?, date = ?, text = ?, created = ?, updated = ?, categories = ?, tags = ?, attachments = ?, default_attachment = ?, related_blocks = ? " + 
                "WHERE id = ?",
                this.name,
                this.date,
                this.text,
                this.created,
                this.updated,
                this.categories,
                this.tags,
                this.attachments,
                this.defaultAttachment,
                this.relatedBlocks,
                this.id);

            if (stmt == null) {
                UError.error("Block.update: Failed to update block", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Block.update: Failed to write block to database", "Updating block failed");
                return false;
            }

            // Update in repository
            if (!OBJECTS.BLOCKS.update(this)) {
                UError.error("Block.update: Failed to update block in repository", "Updating block in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockUpdatedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Block.update: Failed to update block", e);
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
        if (this.date == null || this.date.isEmpty()) return false;
        if (this.created == null || this.created.isEmpty()) return false;
        if (this.updated == null || this.updated.isEmpty()) return false;
        if (!OBJECTS.BLOCKS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if block can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM blocks " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Block.delete: Failed to delete block", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Block.delete: Failed to delete block from database", "Deleting block failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.BLOCKS.delete(this)) {
                UError.error("Block.delete: Failed to delete block from repository", "Deleting block from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockDeletedEvent(this));

            return true;

        } catch (Exception e) {
            UError.exception("Block.delete: Failed to delete block", e);
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
        if (!OBJECTS.BLOCKS.isExists(this.id)) return false;
        return true;
    }


    // Getters

    public String getName() { return this.name; }
    
    public LocalDate getDateOBJ() {
        try {
            return LocalDate.parse(this.date, CONSTANTS.DATE_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Block.getDateOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getDateSTR() {
        try {
            return LocalDate.parse(this.date, CONSTANTS.DATE_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_FORMATTER);
        } catch (Exception e) {
            UError.exception("Block.getDateSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getDateSTR_JSON() {
        return this.date;
    }

    public String getText() { return this.text; }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Block.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Block.getCreatedSTR: Failed to parse date", e);
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
            UError.exception("Block.getUpdatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getUpdatedSTR() {
        try {
            return LocalDateTime.parse(this.updated, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Block.getUpdatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getUpdatedSTR_JSON() {
        return this.updated;
    }

    public List<Category> getCategories() {
        return getCategoriesList();
    }

    private List<Category> getCategoriesList() {
        List<String> ids = UString.splitAndStrip(this.categories, ",");
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

    public List<Tag> getTags() {
        return getTagsList();
    }

    private List<Tag> getTagsList() {
        List<String> ids = UString.splitAndStrip(this.tags, ",");
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

    public String getAttachments() {
        // TODO Return list of attachments
        return this.attachments;
    }
    
    public int getDefaultAttachment() { return this.defaultAttachment; }

    public List<Block> getRelatedBlocks() {
        return getRelatedBlocksList();
    }

    private List<Block> getRelatedBlocksList() {
        List<String> ids = UString.splitAndStrip(this.relatedBlocks, ",");
        List<Block> blocks = new ArrayList<Block>();

        for (String id : ids) {
            if (UNumbers.isStringInteger(id)) {
                Block block = OBJECTS.BLOCKS.getEntity(UNumbers.toInteger(id));
                if (block != null) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    // Setters

    public void setID(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }
    
    public void setDate(LocalDate date) {
        this.date = date.format(CONSTANTS.DATE_FORMATTER_FOR_JSON);
    }

    public void setDate(String date) {
        try {
            this.date = LocalDate.parse(date, CONSTANTS.DATE_FORMATTER).format(CONSTANTS.DATE_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Block.setDate: Failed to parse date", e);
        }
    }

    public void setText(String text) { this.text = text; }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Block.setCreated: Failed to parse date", e);
        }
    }

    public void setCreated() {
        setCreated(LocalDateTime.now());
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }

    public void setUpdated(String updated) {
        try {
            this.updated = LocalDateTime.parse(updated, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Block.setUpdated: Failed to parse date", e);
        }
    }

    public void setUpdated() {
        setUpdated(LocalDateTime.now());
    }

    public void setCategories(List<Category> categories) {
        this.categories = String.join(",", categories.stream().map((Category category) -> String.valueOf(category.getID())).collect(Collectors.toList()));
    }

    public void setTags(List<Tag> tags) {
        this.tags = String.join(",", tags.stream().map((Tag tag) -> String.valueOf(tag.getID())).collect(Collectors.toList()));
    }
    
    public void setAttachments(String attachments) {
        // TODO Set list of attachments
    }
    
    public void setDefaultAttachment(int defaultAttachment) { this.defaultAttachment = defaultAttachment; }

    public void setRelatedBlocks(List<Block> relatedBlocks) {
        this.relatedBlocks = String.join(",", relatedBlocks.stream().map((Block block) -> String.valueOf(block.getID())).collect(Collectors.toList()));
    }


}
