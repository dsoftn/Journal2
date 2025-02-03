package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.OBJECTS;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;

import javafx.application.Platform;
import javafx.event.Event;

import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.utils.UNumbers;

/*
TABLE blocks
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the block
    date TEXT NOT NULL - date in format for JSON
    text TEXT NOT NULL - text of the block
    text_style TEXT NOT NULL - text style of the block
    block_type INTEGER NOT NULL - BlockTypeEnum value
    created TEXT NOT NULL - date in format for JSON
    updated TEXT NOT NULL - date in format for JSON
    default_attachment INTEGER - default attachment id
RELATED PROPERTIES
    Attachments
    Categories
    Tags
    Blocks
    Actors
 */

public class Blocks implements IModelRepository<Block>, ICustomEventListener {
    // Variables

    private Map<Integer, Block> data = new LinkedHashMap<>(); // <id, Block>
    private boolean isLoaded = false;

    // Constructor

    public Blocks() {
        OBJECTS.EVENT_HANDLER.register(
            this,
            RelationAddedEvent.RELATION_ADDED_EVENT,
            RelationUpdatedEvent.RELATION_UPDATED_EVENT,
            RelationDeletedEvent.RELATION_DELETED_EVENT
        );
    }

    // Event handlers

    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof RelationAddedEvent || event instanceof RelationUpdatedEvent || event instanceof RelationDeletedEvent) {
            Relation newRelation = null;
            Relation oldRelation = null;
            if (event instanceof RelationAddedEvent) {
                RelationAddedEvent relationEvent = (RelationAddedEvent) event;
                newRelation = relationEvent.getRelation();
            }
            else if (event instanceof RelationUpdatedEvent) {
                RelationUpdatedEvent relationEvent = (RelationUpdatedEvent) event;
                oldRelation = relationEvent.getOldRelation();
                newRelation = relationEvent.getNewRelation();
            }
            else if (event instanceof RelationDeletedEvent) {
                RelationDeletedEvent relationEvent = (RelationDeletedEvent) event;
                oldRelation = relationEvent.getRelation();
            }

            onRelationEvents(newRelation, event);
            onRelationEvents(oldRelation, event);

        }
    }

    private void onRelationEvents(Relation relation, Event event) {
        if (relation == null) {
            return;
        }

        if (relation.getBaseModel() == ModelEnum.BLOCK) {
            Block block = getEntity(relation.getBaseID());
            if (block != null) {
                block.onCustomEvent(event);
            }
        }
    }

    // Interface methods

    @Override
    public boolean load() {
        // Send start event
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ModelEnum.BLOCK,
                    TaskStateEnum.STARTED
                )
            );
        });

        boolean result = true;

        SQLiteDB db = OBJECTS.DATABASE;
        if (db.isConnected() == false) { loadFailed(); return false; }

        // Find number of rows
        Integer rowCount = db.getRowCount("blocks");
        if (rowCount == null) {
            UError.error("Blocks.load: Failed to load blocks", "Failed to get row count");
            loadFailed();
            return false;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM blocks");
            if (stmt == null) {
                UError.error("Blocks.load: Failed to load blocks", "Statement is unexpectedly null");
                loadFailed();
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Blocks.load: Failed to load blocks", "Result set is unexpectedly null");
                loadFailed();
                return false;
            }

            int currentRow = 1;

            while (rs.next()) {
                Block block = new Block();
                result = block.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Blocks.load: Failed to load block", "Loading block failed");
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
                            new TaskStateEvent(ModelEnum.BLOCK, TaskStateEnum.EXECUTING, progressPercent)
                        );
                    });
                }
                currentRow++;
            }

        } catch (Exception e) {
            UError.exception("Blocks.load: Failed to load blocks", e);
            loadFailed();
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Blocks.load: Failed to close result set", e);
                    loadFailed();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Blocks.load: Failed to close statement", e);
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
                OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(ModelEnum.BLOCK, TaskStateEnum.COMPLETED));
            });
            isLoaded = true;
        }

        return result;
    }

    private void loadFailed() {
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ModelEnum.BLOCK,
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
    public Block getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Block> getEntityAll() {
        List<Block> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Block entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Block entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Block entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.remove(entity.getID());
        return true;
    }

    @Override
    public boolean isModelLoaded() {
        return isLoaded;
    }

    // Public methods

    public List<Block> getBlocksListFromIDs(List<Integer> blockIDs) {
        List<Block> blocks = new ArrayList<>();
        for (Integer blockID : blockIDs) {
            Block block = getEntity(blockID);
            if (block != null) blocks.add(block);
        }

        return blocks;
    }

    public List<Block> getBlocksListFromRelations(List<Relation> relations) {
        List<Integer> blockIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ModelEnum.BLOCK) {
                blockIDs.add(relation.getRelatedID());
            }
        }

        return getBlocksListFromIDs(blockIDs);
    }

}
