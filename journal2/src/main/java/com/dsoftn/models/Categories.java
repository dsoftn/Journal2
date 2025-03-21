package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.Event;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.OBJECTS;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;
import com.dsoftn.events.TaskStateEvent;


/*
TABLE categories
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the category
    description TEXT NOT NULL - description of the category
    parent INTEGER NOT NULL - id of the parent category
    created TEXT NOT NULL - date in format for JSON
    default_attachment INTEGER NOT NULL - id of the default attachment
RELATED TO:
    Categories    
    Tags
    Attachments
 */
public class Categories implements IModelRepository<Category>, ICustomEventListener {
    // Variables

    private Map<Integer, Category> data = new LinkedHashMap<>(); // <id, Category>
    private boolean isLoaded = false;

    // Constructor

    public Categories() {
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

        if (relation.getBaseModel() == ModelEnum.CATEGORY) {
            Category category = getEntity(relation.getBaseID());
            if (category != null) {
                category.onCustomEvent(event);
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
                    ModelEnum.CATEGORY,
                    TaskStateEnum.STARTED
                )
            );
        });

        boolean result = true;

        SQLiteDB db = OBJECTS.DATABASE;
        if (db.isConnected() == false) { loadFailed(); return false; }

        // Find number of rows
        Integer rowCount = db.getRowCount("categories");
        if (rowCount == null) {
            UError.error("Categories.load: Failed to load categories", "Failed to get row count");
            loadFailed();
            return false;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM categories");
            if (stmt == null) {
                UError.error("Categories.load: Failed to load categories", "Statement is unexpectedly null");
                loadFailed();
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Categories.load: Failed to load categories", "Result set is unexpectedly null");
                loadFailed();
                return false;
            }
            
            int currentRow = 1;

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

                // Send progress event
                Integer progressPercent = UNumbers.getPercentIfHasNoRemainder(rowCount, currentRow);
                if (progressPercent != null) {
                    Platform.runLater(() -> {
                        OBJECTS.EVENT_HANDLER.fireEvent(
                            new TaskStateEvent(ModelEnum.CATEGORY, TaskStateEnum.EXECUTING, progressPercent)
                        );
                    });
                }
                currentRow++;
            }

        } catch (Exception e) {
            UError.exception("Categories.load: Failed to load categories", e);
            loadFailed();
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Categories.load: Failed to close result set", e);
                    loadFailed();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Categories.load: Failed to close statement", e);
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
                OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(ModelEnum.CATEGORY, TaskStateEnum.COMPLETED));
            });
            isLoaded = true;
        }

        return result;
    }

    private void loadFailed() {
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ModelEnum.CATEGORY,
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

    @Override
    public boolean isModelLoaded() {
        return isLoaded;
    }

    // Public methods

    public List<Category> getCategoriesListFromIDs(List<Integer> categoryIDs) {
        List<Category> categories = new ArrayList<>();
        for (Integer categoryID : categoryIDs) {
            Category category = getEntity(categoryID);
            if (category != null) categories.add(category);
        }

        return categories;
    }

    public List<Category> getCategoriesListFromRelations(List<Relation> relations) {
        List<Integer> categoryIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ModelEnum.CATEGORY) {
                categoryIDs.add(relation.getRelatedID());
            }
        }

        return getCategoriesListFromIDs(categoryIDs);
    }

}
