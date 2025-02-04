package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.time.LocalDateTime;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.BlockTypeEnum;
import com.dsoftn.Interfaces.IBlockBaseEntity;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UList;

import javafx.event.Event;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;

import com.dsoftn.events.BlockAddedEvent;
import com.dsoftn.events.BlockUpdatedEvent;
import com.dsoftn.events.BlockDeletedEvent;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;


public class Block implements IModelEntity<Block>, ICustomEventListener {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private String name = "";
    private String date = CONSTANTS.INVALID_DATETIME_STRING;
    private String text = "";
    private String textStyle = "";
    private int draft = 1; // 0 = false, 1 = true
    private int blockType = BlockTypeEnum.UNDEFINED.getValue();
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private String updated = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private List<Integer> relatedCategories = new ArrayList<Integer>();
    private List<Integer> relatedTags = new ArrayList<Integer>();
    private List<Integer> relatedAttachments = new ArrayList<Integer>();
    private int defaultAttachment = CONSTANTS.INVALID_ID;
    private List<Integer> relatedBlocks = new ArrayList<Integer>();
    private List<Integer> relatedActors = new ArrayList<Integer>();

    // Constructors

    public Block() {
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

        // Check if relation belongs to this block
        if (relation.getBaseModel() != ModelEnum.BLOCK && relation.getBaseID() != this.id) {
            return;
        }

        List<Integer>  newRelatedAttachments = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.BLOCK, this.id, ModelEnum.ATTACHMENT).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedBlocks = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.BLOCK, this.id, ModelEnum.BLOCK).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedCategories = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.BLOCK, this.id, ModelEnum.CATEGORY).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedTags = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.BLOCK, this.id, ModelEnum.TAG).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        List<Integer>  newRelatedActors = OBJECTS.RELATIONS.getScopeAndIdList(ModelEnum.BLOCK, this.id, ModelEnum.ACTOR).stream().map(Relation::getRelatedID).collect(Collectors.toList());

        relatedAttachments = newRelatedAttachments;
        relatedBlocks = newRelatedBlocks;
        relatedCategories = newRelatedCategories;
        relatedTags = newRelatedTags;
        relatedActors = newRelatedActors;

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
            this.date = rs.getString("date");
            this.text = rs.getString("text");
            this.textStyle = rs.getString("text_style");
            this.draft = rs.getInt("draft");
            this.blockType = rs.getInt("block_type");
            this.created = rs.getString("created");
            this.updated = rs.getString("updated");
            this.defaultAttachment = rs.getInt("default_attachment");

            this.setRelatedCategories(
                OBJECTS.CATEGORIES.getCategoriesListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.BLOCK, this.id, ModelEnum.CATEGORY))
                );

            this.setRelatedTags(
                OBJECTS.TAGS.getTagsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.BLOCK, this.id, ModelEnum.TAG))
            );

            this.setRelatedAttachments(
                OBJECTS.ATTACHMENTS.getAttachmentsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.BLOCK, this.id, ModelEnum.ATTACHMENT))
            );

            this.setRelatedBlocks(
                OBJECTS.BLOCKS.getBlocksListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.BLOCK, this.id, ModelEnum.BLOCK))
            );

            this.setRelatedActors(
                OBJECTS.ACTORS.getActorsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ModelEnum.BLOCK, this.id, ModelEnum.ACTOR))
            );

            return isValid();
        } catch (Exception e) {
            UError.exception("Block.loadFromResultSet: Failed to load block from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return  this.name != null &&
                this.date != null &&
                this.text != null &&
                this.textStyle != null &&
                this.created != null &&
                this.updated != null;
    }

    @Override
    public boolean add() {
        // Check if block can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO blocks " + 
                "(name, date, text, text_style, draft, block_type, created, updated, default_attachment) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                this.name,
                this.date,
                this.text,
                this.textStyle,
                this.draft,
                this.blockType,
                this.created,
                this.updated,
                this.defaultAttachment);

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
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Block.add: Failed to add block", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (this.date == null || this.date.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (this.created == null || this.created.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (this.updated == null || this.updated.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (OBJECTS.BLOCKS.isExists(this.id)) return false;
        if (this.blockType == BlockTypeEnum.UNDEFINED.getValue()) return false;
        
        return true;
    }

    @Override
    public boolean update() {
        // Check if block can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE blocks " + 
                "SET name = ?, date = ?, text = ?, text_style = ?, draft = ?, block_type = ?, created = ?, updated = ?, default_attachment = ? " + 
                "WHERE id = ?",
                this.name,
                this.date,
                this.text,
                this.textStyle,
                this.draft,
                this.blockType,
                this.created,
                this.updated,
                this.defaultAttachment,
                this.id);

            if (stmt == null) {
                UError.error("Block.update: Failed to update block", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Block.update: Failed to write block to database", "Updating block failed");
                return false;
            }

            Block oldBlock = OBJECTS.BLOCKS.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.BLOCKS.update(this)) {
                UError.error("Block.update: Failed to update block in repository", "Updating block in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockUpdatedEvent(oldBlock, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Block.update: Failed to update block", e);
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
        if (this.date == null || this.date.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (this.created == null || this.created.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (this.updated == null || this.updated.equals(CONSTANTS.INVALID_DATETIME_STRING)) return false;
        if (!OBJECTS.BLOCKS.isExists(this.id)) return false;
        if (this.blockType == BlockTypeEnum.UNDEFINED.getValue()) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if block can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
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
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Block.delete: Failed to delete block", e);
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
        if (!OBJECTS.BLOCKS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public Block duplicate() {
        Block block = new Block();
        block.setID(this.id);
        block.setName(this.name);
        block.setDateSTR_JSON(this.getDateSTR_JSON());
        block.setText(this.text);
        block.setTextStyle(this.textStyle);
        block.setDraft(this.draft);
        block.setBlockType(BlockTypeEnum.fromInteger(this.blockType));
        block.setCreatedSTR_JSON(this.getCreatedSTR_JSON());
        block.setUpdatedSTR_JSON(this.getUpdatedSTR_JSON());
        block.setDefaultAttachment(this.defaultAttachment);

        block.setRelatedCategories(this.getRelatedCategories());
        block.setRelatedTags(this.getRelatedTags());
        block.setRelatedAttachments(this.getRelatedAttachments());
        block.setRelatedBlocks(this.getRelatedBlocks());
        block.setRelatedActors(this.getRelatedActors());

        return block;
    }

    // Public methods

    public IBlockBaseEntity getBlockTypeObject() {
        switch (getBlockType()) {
            case UNDEFINED: return null;
            
            case DIARY: return OBJECTS.BLOCKS_DIARY.getEntityFromBase(this);
            case BUDGET: return null;
            case CONTACT: return null;
            case COMPANY: return null;
            case MOVIE: return null;
            case MUSIC: return null;
            case BOOK: return null;
            case GAME: return null;
            case APP: return null;
            case WEBSITE: return null;
            case VIDEO: return null;
            case REMAINDER: return null;
            case QUOTE: return null;
            case HEALTH: return null;
            case RECIPE: return null;
            case TRAVEL: return null;
            case EVENT: return null;
            case WORKOUT: return null;
            case PASSWORD: return null;

        }

        return null;
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

    public String getTextStyle() { return this.textStyle; }

    public boolean getDraft() { return this.draft == 1; }

    public BlockTypeEnum getBlockType() { return BlockTypeEnum.fromInteger(this.blockType); }

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
    
    public int getDefaultAttachment() { return this.defaultAttachment; }

    public List<Block> getRelatedBlocks() {
        return OBJECTS.BLOCKS.getBlocksListFromIDs(this.relatedBlocks);
    }

    public List<Integer> getRelatedBlocksIDs() {
        return this.relatedBlocks;
    }

    public List<Actor> getRelatedActors() {
        return OBJECTS.ACTORS.getActorsListFromIDs(this.relatedActors);
    }

    public List<Integer> getRelatedActorsIDs() {
        return this.relatedActors;
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

    public void setDateSTR_JSON(String date) { this.date = date; }

    public void setText(String text) { this.text = text; }

    public void setTextStyle(String textStyle) { this.textStyle = textStyle; }

    public void setDraft(boolean draft) { this.draft = draft ? 1 : 0; }

    public void setDraft(Integer draft) { this.draft = draft; }

    public void setBlockType(BlockTypeEnum blockType) { this.blockType = blockType.getValue(); }

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

    public void setCreatedSTR_JSON(String created) { this.created = created; }

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
    
    public void setDefaultAttachment(int defaultAttachment) { this.defaultAttachment = defaultAttachment; }

    public void setDefaultAttachment(Attachment defaultAttachment) { this.defaultAttachment = defaultAttachment.getID(); }

    public void setRelatedBlocks(List<Block> relatedBlocks) {
        this.relatedBlocks = relatedBlocks.stream().map((Block block) -> block.getID()).collect(Collectors.toList());
    }

    public void setRelatedActors(List<Actor> relatedActors) {
        this.relatedActors = relatedActors.stream().map((Actor actor) -> actor.getID()).collect(Collectors.toList());
    }


    // Overrides methods "equals()" and "hashCode()"

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        Block other = (Block) obj;

        return  this.getID() == other.getID() &&
                this.getName().equals(other.getName()) &&
                this.getDateSTR_JSON().equals(other.getDateSTR_JSON()) &&
                this.getText().equals(other.getText()) &&
                this.getTextStyle().equals(other.getTextStyle()) &&
                this.getDraft() == other.getDraft() &&
                this.getBlockType() == other.getBlockType() &&
                this.getCreatedSTR_JSON().equals(other.getCreatedSTR_JSON()) &&
                this.getUpdatedSTR_JSON().equals(other.getUpdatedSTR_JSON()) &&
                UList.hasSameElements(this.getRelatedCategoriesIDs(), other.getRelatedCategoriesIDs()) &&
                UList.hasSameElements(this.getRelatedTagsIDs(), other.getRelatedTagsIDs()) &&
                UList.hasSameElements(this.getRelatedAttachmentsIDs(), other.getRelatedAttachmentsIDs()) &&
                this.getDefaultAttachment() == other.getDefaultAttachment() &&
                UList.hasSameElements(this.getRelatedBlocksIDs(), other.getRelatedBlocksIDs()) &&
                UList.hasSameElements(this.getRelatedActorsIDs(), other.getRelatedActorsIDs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.id,
            this.name,
            this.date,
            this.text,
            this.textStyle,
            this.draft,
            this.blockType,
            this.created,
            this.updated,
            this.defaultAttachment,
            new HashSet<>(this.getRelatedCategoriesIDs()),
            new HashSet<>(this.getRelatedTagsIDs()),
            new HashSet<>(this.getRelatedAttachmentsIDs()),
            new HashSet<>(this.getRelatedBlocksIDs()),
            new HashSet<>(this.getRelatedActorsIDs())
        );
    }

}
