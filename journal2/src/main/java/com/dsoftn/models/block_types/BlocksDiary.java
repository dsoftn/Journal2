package com.dsoftn.models.block_types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IBlockBaseRepository;
import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.enums.models.ScopeEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.OBJECTS;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;

import javafx.application.Platform;

import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.Relation;
import com.dsoftn.models.Block;
import com.dsoftn.utils.UNumbers;

/*
TABLE blocks_diary
    id INTEGER PRIMARY KEY AUTOINCREMENT
    base_block_id INTEGER NOT NULL - id of the base block
    show_def_attachment INTEGER NOT NULL - -1 = DEFAULT, 0 = NO, 1 = YES
    text TEXT NOT NULL - text of the block
    text_style TEXT NOT NULL - style of the text
 */

public class BlocksDiary implements IModelRepository<BlockDiary>, IBlockBaseRepository<BlockDiary> {
    // Variables

    private Map<Integer, BlockDiary> data = new LinkedHashMap<>(); // <id, BlockDiary>
    private Map<Integer, BlockDiary> dataByBaseBlock = new LinkedHashMap<>(); // <base_block_id, BlockDiary>

    private boolean isLoaded = false;

    // Constructor

    public BlocksDiary() {
    }

    // Interface IModelRepository methods

    @Override
    public boolean load() {
        // Send start event
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ScopeEnum.BLOCK_TYPE,
                    TaskStateEnum.STARTED
                )
            );
        });

        boolean result = true;

        SQLiteDB db = OBJECTS.DATABASE;
        if (db.isConnected() == false) { loadFailed(); return false; }

        // Find number of rows
        Integer rowCount = db.getRowCount("blocks_diary");
        if (rowCount == null) {
            UError.error("BlocksDiary.load: Failed to load blocks_diary", "Failed to get row count");
            loadFailed();
            return false;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM blocks_diary");
            if (stmt == null) {
                UError.error("BlocksDiary.load: Failed to load blocks_diary", "Statement is unexpectedly null");
                loadFailed();
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("BlocksDiary.load: Failed to load blocks_diary", "Result set is unexpectedly null");
                loadFailed();
                return false;
            }

            int currentRow = 1;

            while (rs.next()) {
                BlockDiary block = new BlockDiary();
                result = block.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("BlocksDiary.load: Failed to load block_diary", "Loading block_diary failed");
                    result = false;
                    continue;
                }
                
                // Add block
                add(block);

                // Send progress event
                Integer progressPercent = UNumbers.getPercentIfHasNoRemainder(rowCount, currentRow);
                if (progressPercent != null) {
                    Platform.runLater(() -> {
                        OBJECTS.EVENT_HANDLER.fireEvent(
                            new TaskStateEvent(ScopeEnum.BLOCK_TYPE, TaskStateEnum.EXECUTING, progressPercent)
                        );
                    });
                }
                currentRow++;
            }

        } catch (Exception e) {
            UError.exception("BlocksDiary.load: Failed to load blocks_diary", e);
            loadFailed();
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("BlocksDiary.load: Failed to close result set", e);
                    loadFailed();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("BlocksDiary.load: Failed to close statement", e);
                    loadFailed();
                }
            }
            db.taskCompleted();
        }

        if (result == false) {
            loadFailed();
        }
        else {
            Platform.runLater(() -> { 
                OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(ScopeEnum.BLOCK_TYPE, TaskStateEnum.COMPLETED));
            });
            isLoaded = true;
        }

        return result;
    }

    private void loadFailed() {
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ScopeEnum.BLOCK_TYPE,
                    TaskStateEnum.FAILED
                )
            );
        });
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
    public BlockDiary getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<BlockDiary> getEntityAll() {
        List<BlockDiary> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(BlockDiary entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);

        dataByBaseBlock.put(entity.getBaseBlockID(), entity);

        return true;
    }

    @Override
    public boolean update(BlockDiary entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);

        dataByBaseBlock.put(entity.getBaseBlockID(), entity);

        return true;
    }

    @Override
    public boolean delete(BlockDiary entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.remove(entity.getID());

        dataByBaseBlock.remove(entity.getBaseBlockID());

        return true;
    }

    @Override
    public boolean isModelLoaded() {
        return isLoaded;
    }

    // Interface IBlockBaseRepository methods

    @Override
    public boolean isBaseExists(Integer baseBlockID) {
        return dataByBaseBlock.containsKey(baseBlockID);
    }

    @Override
    public boolean isBaseExists(Block baseBlock) {
        return isBaseExists(baseBlock.getID());
    }

    @Override
    public BlockDiary getEntityFromBase(Integer baseBlockID) {
        return dataByBaseBlock.get(baseBlockID);
    }

    @Override
    public BlockDiary getEntityFromBase(Block baseBlock) {
        return getEntityFromBase(baseBlock.getID());
    }

    // Public methods

    public List<BlockDiary> getBlocksListFromIDs(List<Integer> blockIDs) {
        List<BlockDiary> blocks = new ArrayList<>();
        for (Integer blockID : blockIDs) {
            BlockDiary block = getEntity(blockID);
            if (block != null) blocks.add(block);
        }

        return blocks;
    }

    public List<BlockDiary> getBlocksListFromRelations(List<Relation> relations) {
        List<Integer> blockIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ScopeEnum.BLOCK_TYPE) {
                blockIDs.add(relation.getRelatedID());
            }
        }

        return getBlocksListFromIDs(blockIDs);
    }

}
