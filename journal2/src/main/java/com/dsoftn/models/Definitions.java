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


/*
 TABLE definitions
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the definition
    description TEXT NOT NULL - description of the definition
    source TEXT NOT NULL - source of the definition
    source_type INTEGER NOT NULL - SourceTypeEnum value
    created TEXT NOT NULL - date in format for JSON
    updated TEXT NOT NULL - date in format for JSON
    default_attachment INTEGER NOT NULL - id of the default attachment
RELATED PROPERTIES
    Attachments
    Categories
    Tags
    Definitions
    Blocks

TABLE definitions_variants
    id INTEGER PRIMARY KEY AUTOINCREMENT
    text TEXT NOT NULL - text of the variant
    definition_id INTEGER NOT NULL - id of the definition
    match_case INTEGER NOT NULL - 0 or 1
 */
public class Definitions implements IModelRepository<Definition>, ICustomEventListener {
    // Variables

    private Map<Integer, Definition> data = new LinkedHashMap<>();

    // Constructor

    public Definitions() {
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

        if (relation.getBaseModel() == ScopeEnum.DEFINITION) {
            Definition definition = getEntity(relation.getBaseID());
            if (definition != null) {
                definition.onCustomEvent(event);
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
            stmt = db.preparedStatement("SELECT * FROM definitions");
            if (stmt == null) {
                UError.error("Definitions.load: Failed to load definitions", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Definitions.load: Failed to load definitions", "Result set is unexpectedly null");
                return false;
            }

            while (rs.next()) {
                Definition definition = new Definition();
                result = definition.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Definitions.load: Failed to load definition", "Loading definition failed");
                    result = false;
                    continue;
                }
                
                // Add definition
                add(definition);
            }

        } catch (Exception e) {
            UError.exception("Definitions.load: Failed to load definitions", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Definitions.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Definitions.load: Failed to close statement", e);
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
    public Definition getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Definition> getEntityAll() {
        List<Definition> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Definition entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Definition entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Definition entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.remove(entity.getID());
        return true;
    }

    // Public methods

    public List<Definition> getDefinitionsListFromIDs(List<Integer> definitionIDs) {
        List<Definition> definitions = new ArrayList<>();
        for (Integer definitionID : definitionIDs) {
            Definition definition = getEntity(definitionID);
            if (definition != null) definitions.add(definition);
        }

        return definitions;
    }

    public List<Definition> getDefinitionsListFromRelations(List<Relation> relations) {
        List<Integer> definitionIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ScopeEnum.DEFINITION) {
                definitionIDs.add(relation.getRelatedID());
            }
        }

        return getDefinitionsListFromIDs(definitionIDs);
    }

}
