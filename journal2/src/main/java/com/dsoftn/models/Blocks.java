package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.OBJECTS;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;

import javafx.event.Event;

import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;
import com.dsoftn.models.enums.ScopeEnum;


/*
TABLE blocks
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the block
    date TEXT NOT NULL - date in format for JSON
    text TEXT NOT NULL - text of the block
    created TEXT NOT NULL - date in format for JSON
    updated TEXT NOT NULL - date in format for JSON
    default_attachment INTEGER - default attachment id
RELATED PROPERTIES
    Attachments
    Categories
    Tags
    Blocks
 */

public class Blocks implements IModelRepository<Block>, ICustomEventListener {
    // Variables

    private Map<Integer, Block> data = new LinkedHashMap<>(); // <id, Block>

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

        if (relation.getBaseModel() == ScopeEnum.BLOCK) {
            Block block = getEntity(relation.getBaseID());
            if (block != null) {
                block.onCustomEvent(event);
            }
        }
    }

    // Interface methods

    @Override
    public boolean load() {
        boolean result = true;

        SQLiteDB db = new SQLiteDB();
        if (db.isConnected() == false) return false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM blocks");
            if (stmt == null) {
                UError.error("Blocks.load: Failed to load blocks", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Blocks.load: Failed to load blocks", "Result set is unexpectedly null");
                return false;
            }

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
            }

        } catch (Exception e) {
            UError.exception("Blocks.load: Failed to load blocks", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Blocks.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Blocks.load: Failed to close statement", e);
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
            if (relation.getRelatedModel() == ScopeEnum.BLOCK) {
                blockIDs.add(relation.getRelatedID());
            }
        }

        return getBlocksListFromIDs(blockIDs);
    }

}
