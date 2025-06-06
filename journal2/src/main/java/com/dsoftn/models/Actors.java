package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.CONSTANTS;
import com.dsoftn.DIALOGS;
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
TABLE actors
    id INTEGER PRIMARY KEY AUTOINCREMENT
    nick TEXT NOT NULL - short name of the actor
    name TEXT NOT NULL - full name of the actor
    description TEXT NOT NULL - description of the actor
    description_styled TEXT NOT NULL - description of the actor with style
    default_actor INTEGER NOT NULL - 0 or 1 - It determines if actor will be automatically added to new blocks
    created TEXT NOT NULL - date in format for JSON
    updated TEXT NOT NULL - date in format for JSON
    default_attachment INTEGER - default attachment id
RELATED PROPERTIES
    Attachments
 */

public class Actors implements IModelRepository<Actor>, ICustomEventListener {
    // Variables

    private Map<Integer, Actor> data = new LinkedHashMap<>(); // <id, Actor>
    private boolean isLoaded = false;

    // Constructor

    public Actors() {
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

        if (relation.getBaseModel() == ModelEnum.ACTOR) {
            Actor actor = getEntity(relation.getBaseID());
            if (actor != null) {
                actor.onCustomEvent(event);
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
                    ModelEnum.ACTOR,
                    TaskStateEnum.STARTED
                )
            );
        });

        boolean result = true;

        SQLiteDB db = OBJECTS.DATABASE;
        if (db.isConnected() == false) { loadFailed(); return false; }

        // Find number of rows
        Integer rowCount = db.getRowCount("actors");
        if (rowCount == null) {
            UError.error("Actors.load: Failed to load actors", "Failed to get row count");
            loadFailed();
            return false;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM actors");
            if (stmt == null) {
                UError.error("Actors.load: Failed to load actors", "Statement is unexpectedly null");
                loadFailed();
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Actors.load: Failed to load actors", "Result set is unexpectedly null");
                loadFailed();
                return false;
            }

            int currentRow = 1;

            while (rs.next()) {
                Actor actor = new Actor();
                result = actor.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Actors.load: Failed to load actors", "Loading actor failed");
                    result = false;
                    continue;
                }
                
                // Add block
                add(actor);

                // Send progress event
                Integer progressPercent = UNumbers.getPercentIfHasNoRemainder(rowCount, currentRow);
                if (progressPercent != null) {
                    Platform.runLater(() -> {
                        OBJECTS.EVENT_HANDLER.fireEvent(
                            new TaskStateEvent(ModelEnum.ACTOR, TaskStateEnum.EXECUTING, progressPercent)
                        );
                    });
                }
                currentRow++;
            }

        } catch (Exception e) {
            UError.exception("Actors.load: Failed to load actors", e);
            loadFailed();
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Actors.load: Failed to close result set", e);
                    loadFailed();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Actors.load: Failed to close statement", e);
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
                OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(ModelEnum.ACTOR, TaskStateEnum.COMPLETED));
            });
            isLoaded = true;
        }

        return result;
    }

    private void loadFailed() {
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ModelEnum.ACTOR,
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
    public Actor getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Actor> getEntityAll() {
        List<Actor> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Actor entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Actor entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Actor entity) {
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

    public boolean confirmDelete(int id) {
        Actor actor = getEntity(id);
        if (actor == null) return false;

        if (msgConfirmDelete(actor)) {
            return true;
        }

        return false;
    }

    public List<Actor> getActorsListFromIDs(List<Integer> actorIDs) {
        List<Actor> actors = new ArrayList<>();
        for (Integer actorID : actorIDs) {
            Actor actor = getEntity(actorID);
            if (actor != null) actors.add(actor);
        }

        return actors;
    }

    public List<Actor> getActorsListFromRelations(List<Relation> relations) {
        List<Integer> actorIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ModelEnum.ACTOR) {
                actorIDs.add(relation.getRelatedID());
            }
        }

        return getActorsListFromIDs(actorIDs);
    }

    public boolean canBeDeleted(Actor actor) {
        if (actor == null) return false;

        // Check if actor is used in any relation
        if (OBJECTS.RELATIONS.getRelationsList(ModelEnum.BLOCK, null, ModelEnum.ACTOR, actor.getID()).size() > 0) {
            return false;
        }

        return true;
    }

    // Private methods

    private boolean msgConfirmDelete(Actor actor) {
        if (actor == null) return false;
        if (!canBeDeleted(actor)) {
            // Msg cannot be deleted
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(CONSTANTS.PRIMARY_STAGE);
            msgBoxController.setTitleText(OBJECTS.SETTINGS.getl("text_Delete"));
            msgBoxController.setHeaderText(OBJECTS.SETTINGS.getl("text_DeleteActor"));
            msgBoxController.setHeaderIcon(MsgBoxIcon.WARNING);
            msgBoxController.setContentText(OBJECTS.SETTINGS.getl("ActorDelete_CannotBeDeleted").replace("#1", actor.getFriendlyName()));
            msgBoxController.setContentIcon(MsgBoxIcon.DELETE);
            msgBoxController.setButtons(MsgBoxButton.OK);
            msgBoxController.setDefaultButton(MsgBoxButton.OK);
            msgBoxController.startMe();

            return false;
        }

        MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(CONSTANTS.PRIMARY_STAGE);
        msgBoxController.setTitleText(OBJECTS.SETTINGS.getl("text_Delete"));
        msgBoxController.setHeaderText(OBJECTS.SETTINGS.getl("text_DeleteActor"));
        msgBoxController.setHeaderIcon(MsgBoxIcon.QUESTION);
        msgBoxController.setContentText(OBJECTS.SETTINGS.getl("ActorDelete_ConfirmDelete").replace("#1", actor.getFriendlyName()));
        msgBoxController.setContentIcon(MsgBoxIcon.DELETE);
        msgBoxController.setButtons(MsgBoxButton.YES, MsgBoxButton.NO, MsgBoxButton.CANCEL);
        msgBoxController.setDefaultButton(MsgBoxButton.NO);
        msgBoxController.startMe();

        return msgBoxController.getSelectedButton() == MsgBoxButton.YES;
    }


}
