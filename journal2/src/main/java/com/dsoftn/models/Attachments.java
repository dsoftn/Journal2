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
TABLE attachments
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the attachment
    description TEXT NOT NULL - description of the attachment
    type INTEGER NOT NULL - AttachmentTypeEnum value
    is_supported INTEGER NOT NULL - 0 or 1
    source TEXT NOT NULL - source of the attachment
    source_type INTEGER NOT NULL - SourceTypeEnum value
    downloaded INTEGER NOT NULL - 0 or 1
    created TEXT NOT NULL - date in format for JSON
    file_path TEXT NOT NULL - path to attachment file
    file_size INTEGER NOT NULL - file size in bytes
    file_created TEXT NOT NULL - date in format for JSON
    file_modified TEXT NOT NULL - date in format for JSON
    file_accessed TEXT NOT NULL - date in format for JSON
RELATED PROPERTIES:
    Attachments
 */
public class Attachments implements IModelRepository<Attachment>, ICustomEventListener {
    // Variables

    private Map<Integer, Attachment> data = new LinkedHashMap<>(); // <id, Attachment>
    private boolean isLoaded = false;

    // Constructor

    public Attachments() {
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

        if (relation.getBaseModel() == ModelEnum.ATTACHMENT) {
            Attachment attachment = getEntity(relation.getBaseID());
            if (attachment != null) {
                attachment.onCustomEvent(event);
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
                    ModelEnum.ATTACHMENT,
                    TaskStateEnum.STARTED
                )
            );
        });

        boolean result = true;

        SQLiteDB db = OBJECTS.DATABASE;
        if (db.isConnected() == false) { loadFailed(); return false; }

        // Find number of rows
        Integer rowCount = db.getRowCount("attachments");
        if (rowCount == null) {
            UError.error("Attachments.load: Failed to load attachments", "Failed to get row count");
            loadFailed();
            return false;
        }
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM attachments");
            if (stmt == null) {
                UError.error("Attachments.load: Failed to load attachments", "Statement is unexpectedly null");
                loadFailed();
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Attachments.load: Failed to load attachments", "Result set is unexpectedly null");
                loadFailed();
                return false;
            }

            int currentRow = 1;

            while (rs.next()) {
                Attachment attachment = new Attachment();
                result = attachment.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Attachments.load: Failed to load attachment", "Loading attachment failed");
                    result = false;
                    continue;
                }
                
                // Add attachment
                add(attachment);

                // Send progress event
                Integer progressPercent = UNumbers.getPercentIfHasNoRemainder(rowCount, currentRow);
                if (progressPercent != null) {
                    Platform.runLater(() -> {
                        OBJECTS.EVENT_HANDLER.fireEvent(
                            new TaskStateEvent(ModelEnum.ATTACHMENT, TaskStateEnum.EXECUTING, progressPercent)
                        );
                    });
                }
                currentRow++;
            }

        } catch (Exception e) {
            UError.exception("Attachments.load: Failed to load attachments", e);
            loadFailed();
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Attachments.load: Failed to close result set", e);
                    loadFailed();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Attachments.load: Failed to close statement", e);
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
                OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(ModelEnum.ATTACHMENT, TaskStateEnum.COMPLETED));
            });
            isLoaded = true;
        }

        return result;
    }

    private void loadFailed() {
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ModelEnum.ATTACHMENT,
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
    public Attachment getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Attachment> getEntityAll() {
        List<Attachment> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Attachment entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean update(Attachment entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        data.put(entity.getID(), entity);
        return true;
    }

    @Override
    public boolean delete(Attachment entity) {
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

    public boolean prepare(Attachment attachment) {
        // TODO prepare attachment in Attachemnst or Cashe
        return true;
    }

    public List<Attachment> getAttachmentsListFromIDs(List<Integer> attachmentIDs) {
        List<Attachment> attachments = new ArrayList<>();
        for (Integer attachmentID : attachmentIDs) {
            Attachment attachment = getEntity(attachmentID);
            if (attachment != null) attachments.add(attachment);
        }

        return attachments;
    }

    public List<Attachment> getAttachmentsListFromRelations(List<Relation> relations) {
        List<Integer> attachmentIDs = new ArrayList<>();
        for (Relation relation : relations) {
            if (relation.getRelatedModel() == ModelEnum.ATTACHMENT) {
                attachmentIDs.add(relation.getRelatedID());
            }
        }

        return getAttachmentsListFromIDs(attachmentIDs);
    }

}
