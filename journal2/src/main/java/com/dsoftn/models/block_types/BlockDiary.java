package com.dsoftn.models.block_types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

import com.dsoftn.Interfaces.IBlockBaseEntity;
import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.enums.models.BlockTypeEnum;
import com.dsoftn.enums.models.DefaultAttachmentShowPolicy;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UString;

import javafx.scene.image.Image;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;

import com.dsoftn.events.BlockTypeAddedEvent;
import com.dsoftn.events.BlockTypeUpdatedEvent;
import com.dsoftn.events.BlockTypeDeletedEvent;
import com.dsoftn.models.Block;


public class BlockDiary implements IModelEntity, IBlockBaseEntity {
    // Properties
    private int id = CONSTANTS.INVALID_ID;
    private int baseBlockID = CONSTANTS.INVALID_ID;
    private int showDefAttachment = DefaultAttachmentShowPolicy.DEFAULT.getValue();
    
    private String text = "";
    private String textStyle = "";

    // Variables

    private final BlockTypeEnum blockType = BlockTypeEnum.DIARY;
    private Block baseBlock = null;

    // Constructors

    public BlockDiary() {
        this.setBaseBlock(new Block());
    }

    public BlockDiary(Block baseBlock) {
        this.setBaseBlock(baseBlock.duplicate());
    }

    
    // Interface IModelEntity methods

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
            stmt = db.preparedStatement("SELECT * FROM blocks_diary WHERE id = ?", id);
            if (stmt == null) {
                UError.error("BlockDiary.load: Failed to load block_diary", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("BlockDiary.load: Failed to load block_diary", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("BlockDiary.load: Failed to load block_diary", e);
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
            this.baseBlockID = rs.getInt("base_block_id");
            this.showDefAttachment = rs.getInt("show_def_attachment");
            this.text = rs.getString("text");
            this.textStyle = rs.getString("text_style");

            // Set base block
            if (OBJECTS.BLOCKS.isExists(this.baseBlockID)) {
                this.baseBlock = OBJECTS.BLOCKS.getEntity(this.baseBlockID);
            }
            else {
                this.baseBlock = null;
                return false;
            }

            return isValid();
        } catch (Exception e) {
            UError.exception("BlockDiary.loadFromResultSet: Failed to load block_diary from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return  this.text != null &&
                this.textStyle != null;
    }

    @Override
    public boolean add() {
        // Check if can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO blocks_diary " + 
                "(base_block_id, show_def_attachment, text, text_style) " + 
                "VALUES (?, ?, ?, ?)",
                this.baseBlockID,
                this.showDefAttachment,
                this.text,
                this.textStyle
            );

            if (stmt == null) {
                UError.error("BlockDiary.add: Failed to add block_diary", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("BlockDiary.add: Failed to write block_diary to database", "Adding block_diary failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.BLOCKS_DIARY.add(this)) {
                UError.error("BlockDiary.add: Failed to add block_diary to repository", "Adding block_diary to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockTypeAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("BlockDiary.add: Failed to add block_diary", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (OBJECTS.BLOCKS_DIARY.isExists(this.id)) return false;
        
        return true;
    }

    @Override
    public boolean update() {
        // Check if can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE blocks_diary " + 
                "SET base_block_id = ?, show_def_attachment = ?, text = ?, text_style = ? " + 
                "WHERE id = ?",
                this.baseBlockID,
                this.showDefAttachment,
                this.text,
                this.textStyle,
                this.id
                );

            if (stmt == null) {
                UError.error("BlockDiary.update: Failed to update block_diary", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("BlockDiary.update: Failed to write block_diary to database", "Updating block_diary failed");
                return false;
            }

            BlockDiary oldBlock = OBJECTS.BLOCKS_DIARY.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.BLOCKS_DIARY.update(this)) {
                UError.error("BlockDiary.update: Failed to update block_diary in repository", "Updating block_diary in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockTypeUpdatedEvent(oldBlock, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("BlockDiary.update: Failed to update block_diary", e);
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
        if (this.baseBlockID == CONSTANTS.INVALID_ID) return false;
        if (!OBJECTS.BLOCKS_DIARY.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM blocks_diary " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("BlockDiary.delete: Failed to delete block_diary", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("BlockDiary.delete: Failed to delete block_diary from database", "Deleting block_diary failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.BLOCKS_DIARY.delete(this)) {
                UError.error("BlockDiary.delete: Failed to delete block_diary from repository", "Deleting block_diary from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new BlockTypeDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("BlockDiary.delete: Failed to delete block_diary", e);
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
        if (!OBJECTS.BLOCKS_DIARY.isExists(this.id)) return false;

        return true;
    }

    @Override
    public IModelEntity duplicateModel() {
        BlockDiary block = new BlockDiary();
        block.id = this.id;
        block.baseBlock = this.baseBlock == null ? null : this.baseBlock.duplicate();
        block.baseBlockID = this.baseBlockID;
        block.showDefAttachment = this.showDefAttachment;
        block.text = this.text;
        block.textStyle = this.textStyle;

        return block;
    }

    public BlockDiary duplicate() {
        BlockDiary block = (BlockDiary) this.duplicateModel();
        return block;
    }

    @Override
    public String getImagePath() {
        Block bb = OBJECTS.BLOCKS.getEntity(this.baseBlockID);
        return bb == null ? null : bb.getImagePath();
    }

    @Override
    public Image getGenericImage() {
        return new Image(getClass().getResourceAsStream("/images/block_diary_generic.png"));
    }

    @Override
    public String getFriendlyName() {
        String result = OBJECTS.SETTINGS.getl("BlockDiary_FriendlyName");
        Block bb = OBJECTS.BLOCKS.getEntity(this.baseBlockID);
        if (bb == null) {
            return  result
                    .replace("#1", String.valueOf(this.id))
                    .replace("#2", "?")
                    .replace("#3", "?");
        }
        else {
            String name = bb.getName();
            if (name == null || name.isEmpty()) {
                List<String> textList = UString.splitAndStrip(this.text, "\n");
                if (textList.size() > 0) {
                    name = textList.get(0);
                }
            }
            return  result
                    .replace("#1", String.valueOf(this.id))
                    .replace("#2", bb.getDateSTR()
                    .replace("#3", name));
        }
    }

    @Override
    public String getTooltipString() {
        String baseBlockTT = this.baseBlock == null ? "?" : this.baseBlock.getTooltipString();

        return OBJECTS.SETTINGS.getl("BlockDiary_Tooltip")
                .replace("#1", String.valueOf(this.id))
                .replace("#2", this.text)
                .replace("#3", baseBlockTT);
    }

    // Interface IBlockBaseEntity methods

    @Override
    public boolean saveBlockAndBase() {
        if (this.baseBlock == null) {
            UError.error("BlockDiary.saveBlockAndBase: Failed to save " + this.blockType.toString(), "Base block is unexpectedly null");
            return false;
        }

        if (this.baseBlock.getBlockType() != this.blockType) {
            UError.error("BlockDiary.saveBlockAndBase: Failed to save " + this.blockType.toString(), "Base block's block type does not match");
            return false;
        }

        // First save base block
        this.baseBlock.setText(getTextForBaseBlock());
        this.baseBlock.setTextStyle(getTextStyleForBaseBlock());

        if (this.baseBlock.getID() == CONSTANTS.INVALID_ID) {
            if (!this.baseBlock.add()) {
                UError.error("BlockDiary.saveBlockAndBase: Failed to save " + this.blockType.toString(), "Adding base block failed");
                return false;
            }
        }
        else {
            if (!this.baseBlock.update()) {
                UError.error("BlockDiary.saveBlockAndBase: Failed to save " + this.blockType.toString(), "Updating base block failed");
                return false;
            }
        }

        this.baseBlockID = this.baseBlock.getID();

        // Then save this block

        if (this.id == CONSTANTS.INVALID_ID) {
            // Add
            if (!this.add()) {
                UError.error("BlockDiary.saveBlockAndBase: Failed to save block_diary", "Adding block_diary failed");
                return false;
            }
        } else {
            // Update
            if (!this.update()) {
                    UError.error("BlockDiary.saveBlockAndBase: Failed to save block_diary", "Updating block_diary failed");
                    return false;
            }
        }

        return true;
    }

    @Override
    public boolean deleteBlockAndBase() {
        if (this.baseBlock == null) {
            UError.error("BlockDiary.deleteBlockAndBase: Failed to delete " + this.blockType.toString(), "Base block is unexpectedly null");
            return false;
        }

        if (this.id == CONSTANTS.INVALID_ID) {
            UError.error("BlockDiary.deleteBlockAndBase: Failed to delete " + this.blockType.toString(), "ID is unexpectedly invalid");
            return false;
        }

        if (this.baseBlock.getID() == CONSTANTS.INVALID_ID) {
            UError.error("BlockDiary.deleteBlockAndBase: Failed to delete " + this.blockType.toString(), "Base block ID is unexpectedly invalid");
            return false;
        }

        boolean result = true;

        result = this.baseBlock.delete();
        if (!result) {
            UError.error("BlockDiary.deleteBlockAndBase: Failed to delete block_diary", "Deleting base block failed");
            return false;
        }

        result = this.delete();
        if (!result) {
            UError.error("BlockDiary.deleteBlockAndBase: Failed to delete block_diary", "Deleting block_diary failed");
            return false;
        }

        return result;
    }

    @Override
    public BlockTypeEnum getBlockType() { return this.blockType; }

    @Override
    public Integer getBaseBlockID() { return this.baseBlockID; }
    
    @Override
    public Block getBaseBlock() { return baseBlock; }

    @Override
    public void setBaseBlock(Block baseBlock) {
        if (baseBlock == null) {
            this.baseBlockID = CONSTANTS.INVALID_ID;
            this.baseBlock = baseBlock;
            return;
        }

        if (baseBlock.getID() == CONSTANTS.INVALID_ID) {
            baseBlock.setBlockType(this.blockType);
        }

        if (baseBlock.getID() != this.baseBlockID) {
            if (baseBlock.getID() != CONSTANTS.INVALID_ID && OBJECTS.BLOCKS_DIARY.isBaseExists(baseBlock)) {
                UError.error(
                    "BlockDiary.setBaseBlock: Failed to set base block",
                    "Base block already belongs to another " + this.blockType.toString(),
                    "Base block ID: " + baseBlock.getID() + "  belongs to block_diary ID: " + OBJECTS.BLOCKS_DIARY.getEntityFromBase(baseBlock).getID()
                );
                return;
            }

            if (baseBlock.getBlockTypeObject() != null) {
                UError.error(
                    "BlockDiary.setBaseBlock: Failed to set base block",
                    "Base block already belongs to another block type",
                    "Base block ID: " + baseBlock.getID() + "  belongs to block type: " + baseBlock.getBlockType().toString()
                );
                return;
            }
        }

        this.baseBlockID = baseBlock.getID();
        baseBlock.setBlockType(this.blockType);
        baseBlock.setText(this.text);
        baseBlock.setTextStyle(this.textStyle);

        this.baseBlock = baseBlock;
    }
    
    @Override
    public String getTextForBaseBlock() { return createTextForBaseBlock(); }

    @Override
    public String getTextStyleForBaseBlock() { return createTextStyleForBaseBlock(); }


    // Private methods

    private String createTextForBaseBlock() {
        return this.text;
    }

    private String createTextStyleForBaseBlock() {
        return this.textStyle;
    }


    // Getters - General    

    public DefaultAttachmentShowPolicy getShowDefAttachment() { return DefaultAttachmentShowPolicy.fromInteger(this.showDefAttachment); }

    // Getters - Specific

    public String getText() { return this.text; }

    public String getTextStyle() { return this.textStyle; }


    // Setters - General

    public void setID(int id) { this.id = id; }

    public void setShowDefAttachment(DefaultAttachmentShowPolicy defaultAttachmentShowPolicy) { this.showDefAttachment = defaultAttachmentShowPolicy.getInteger(); }

    public void setShowDefAttachment(int showDefAttachment) { this.showDefAttachment = showDefAttachment; }

    // Setters - Specific

    public void setText(String text) {
        this.text = text;

        if (this.baseBlock != null) {
            this.baseBlock.setText(getTextForBaseBlock());
        }
    }

    public void setTextStyle(String textStyle) { this.textStyle = textStyle; }

    
    // Overrides methods "equals()" and "hashCode()"

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        BlockDiary other = (BlockDiary) obj;

        return  this.getID() == other.getID() &&
                this.getBaseBlockID() == other.getBaseBlockID() &&
                this.getBaseBlock().equals(other.getBaseBlock()) &&
                this.getShowDefAttachment() == other.getShowDefAttachment() &&
                this.getText().equals(other.getText()) &&
                this.getTextStyle().equals(other.getTextStyle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.id,
            this.baseBlockID,
            this.baseBlock,
            this.showDefAttachment,
            this.text,
            this.textStyle);
    }



}
