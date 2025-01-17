package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.events.AttachmentAddedEvent;
import com.dsoftn.events.AttachmentDeletedEvent;
import com.dsoftn.events.AttachmentUpdatedEvent;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;

import javafx.event.Event;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.events.RelationAddedEvent;
import com.dsoftn.events.RelationDeletedEvent;
import com.dsoftn.events.RelationUpdatedEvent;


public class Attachment implements IModelEntity<Attachment>, ICustomEventListener {
    // Properties
    private Integer id = CONSTANTS.INVALID_ID;
    private String name = "";
    private String description = "";
    private Integer type = AttachmentTypeEnum.UNDEFINED.getValue();
    private Integer isSupported = 0;
    private String source = "";
    private Integer sourceType = SourceTypeEnum.UNDEFINED.getValue();
    private Integer downloaded = 0;
    private List<Integer> relatedAttachments = new ArrayList<>();
    private String created = LocalDateTime.now().format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    private String filePath = "";
    private Integer fileSize = CONSTANTS.INVALID_SIZE;
    private String fileCreated = CONSTANTS.INVALID_DATETIME_STRING;
    private String fileModified = CONSTANTS.INVALID_DATETIME_STRING;
    private String fileAccessed = CONSTANTS.INVALID_DATETIME_STRING;

    // Constructors
    
    public Attachment() {
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
                newRelation = relationAddedEvent.getRelation();
            }
            else if (event instanceof RelationUpdatedEvent) {
                RelationUpdatedEvent relationUpdatedEvent = (RelationUpdatedEvent) event;
                newRelation = relationUpdatedEvent.getNewRelation();
                oldRelation = relationUpdatedEvent.getOldRelation();
            }
            else {
                RelationDeletedEvent relationDeletedEvent = (RelationDeletedEvent) event;
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

        // Check if relation belongs to this attachment
        if (relation.getBaseModel() != ScopeEnum.ATTACHMENT && relation.getBaseID() != this.id) {
            return;
        }

        List<Integer>  newRelatedAttachments = OBJECTS.RELATIONS.getScopeAndIdList(ScopeEnum.ATTACHMENT, this.id, ScopeEnum.ATTACHMENT).stream().map(Relation::getRelatedID).collect(Collectors.toList());
        relatedAttachments = newRelatedAttachments;
        update();
    }

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
            stmt = db.preparedStatement("SELECT * FROM attachments WHERE id = ?", id);
            if (stmt == null) {
                UError.error("Attachment.load: Failed to load attachment", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Attachment.load: Failed to load attachment", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("Attachment.load: Failed to load attachment", e);
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
            this.description = rs.getString("description");
            this.type = rs.getInt("type");
            this.isSupported = rs.getInt("is_supported");
            this.source = rs.getString("source");
            this.sourceType = rs.getInt("source_type");
            this.downloaded = rs.getInt("downloaded");
            this.created = rs.getString("created");
            this.filePath = rs.getString("file_path");
            this.fileSize = rs.getInt("file_size");
            this.fileCreated = rs.getString("file_created");
            this.fileModified = rs.getString("file_modified");
            this.fileAccessed = rs.getString("file_accessed");

            this.setRelatedAttachments(
                OBJECTS.ATTACHMENTS.getAttachmentsListFromRelations(
                    OBJECTS.RELATIONS.getRelationsList(ScopeEnum.ATTACHMENT, this.id, ScopeEnum.ATTACHMENT)));

            return true;
        } catch (Exception e) {
            UError.exception("Attachment.loadFromResultSet: Failed to load attachment from result set", e);
            return false;
        }
    }

    @Override
    public boolean add() {
        // Check if the attachment can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO attachments " + 
                "(name, description, type, is_supported, source, source_type, downloaded, file_path, file_size, file_created, file_modified, file_accessed, created) " + 
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                this.name,
                this.description,
                this.type,
                this.isSupported,
                this.source,
                this.sourceType,
                this.downloaded,
                this.filePath,
                this.fileSize,
                this.fileCreated,
                this.fileModified,
                this.fileAccessed,
                this.created);

            if (stmt == null) {
                UError.error("Attachment.add: Failed to add attachment", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("Attachment.add: Failed to write attachment to database", "Adding attachment failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.ATTACHMENTS.add(this)) {
                UError.error("Attachment.add: Failed to add attachment to repository", "Adding attachment to repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new AttachmentAddedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Attachment.add: Failed to add attachment", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.disconnect();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (this.created == null || this.created.isEmpty()) return false;
        if (OBJECTS.ATTACHMENTS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Check if the attachment can be updated
        if (!canBeUpdated()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Update in database
            stmt = db.preparedStatement(
                "UPDATE attachments " + 
                "SET name = ?, description = ?, type = ?, is_supported = ?, source = ?, source_type = ?, downloaded = ?, file_path = ?, file_size = ?, file_created = ?, file_modified = ?, file_accessed = ?, created = ? " + 
                "WHERE id = ?",
                this.name,
                this.description,
                this.type,
                this.isSupported,
                this.source,
                this.sourceType,
                this.downloaded,
                this.filePath,
                this.fileSize,
                this.fileCreated,
                this.fileModified,
                this.fileAccessed,
                this.created,
                this.id);

            if (stmt == null) {
                UError.error("Attachment.update: Failed to update attachment", "Statement is unexpectedly null");
                return false;
            }
            if (!db.update(stmt)) {
                UError.error("Attachment.update: Failed to write attachment to database", "Updating attachment failed");
                return false;
            }

            Attachment oldAttachment = OBJECTS.ATTACHMENTS.getEntity(this.id).duplicate();

            // Update in repository
            if (!OBJECTS.ATTACHMENTS.update(this)) {
                UError.error("Attachment.update: Failed to update attachment in repository", "Updating attachment in repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new AttachmentUpdatedEvent(oldAttachment, this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Attachment.update: Failed to update attachment", e);
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
        if (this.created == null || this.created.isEmpty()) return false;
        if (!OBJECTS.ATTACHMENTS.isExists(this.id)) return false;

        return true;
    }

    @Override
    public boolean delete() {
        // Check if the attachment can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = new SQLiteDB();
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM attachments " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("Attachment.delete: Failed to delete attachment", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("Attachment.delete: Failed to delete attachment from database", "Deleting attachment failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.ATTACHMENTS.delete(this)) {
                UError.error("Attachment.delete: Failed to delete attachment from repository", "Deleting attachment from repository failed");
                return false;
            }

            // Send event
            OBJECTS.EVENT_HANDLER.fireEvent(new AttachmentDeletedEvent(this.duplicate()));

            return true;

        } catch (Exception e) {
            UError.exception("Attachment.delete: Failed to delete attachment", e);
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
        if (!OBJECTS.ATTACHMENTS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public Attachment duplicate() {
        Attachment newAttachment = new Attachment();
        newAttachment.setID(this.id);
        newAttachment.setName(this.name);
        newAttachment.setDescription(this.description);
        newAttachment.setType(AttachmentTypeEnum.fromInteger(this.type));
        newAttachment.setIsSupported(this.isSupported);
        newAttachment.setSource(this.source);
        newAttachment.setSourceType(SourceTypeEnum.fromInteger(this.sourceType));
        newAttachment.setDownloaded(this.isDownloaded());
        newAttachment.setFilePath(this.filePath);
        newAttachment.setFileSize(this.fileSize);
        newAttachment.setFileCreatedSTR_JSON(getFileCreatedSTR_JSON());
        newAttachment.setFileModifiedSTR_JSON(getFileModifiedSTR_JSON());
        newAttachment.setFileAccessedSTR_JSON(getFileAccessedSTR_JSON());
        newAttachment.setCreatedSTR_JSON(getCreatedSTR_JSON());
        
        List<Integer> relAtt = new ArrayList<>();
        for (Integer i : this.relatedAttachments) {
            relAtt.add(i);
        }
        newAttachment.setRelatedAttachmentsFromIDsList(relAtt);

        return newAttachment;
    }

    // Getters
    
    public String getName() { return this.name; }

    public String getDescription() { return this.description; }

    public AttachmentTypeEnum getType() { return AttachmentTypeEnum.fromInteger(this.type); }

    public boolean getIsSupported() { return this.isSupported == 1; }

    public String getSource () { return this.source; }

    public SourceTypeEnum getSourceType () { return SourceTypeEnum.fromInteger(this.sourceType); }

    public boolean isDownloaded() { return this.downloaded == 1; }

    public String getFilePath() { return this.filePath; }

    public int getFileSize() { return this.fileSize; }

    public LocalDateTime getFileCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.fileCreated, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.getFileCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }
    
    public String getFileCreatedSTR() {
        try {
            return LocalDateTime.parse(this.fileCreated, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Attachment.getFileCreatedSTR: Failed to parse date", e);
            return null;
        }
    }
    
    public String getFileCreatedSTR_JSON() { return this.fileCreated; }

    public LocalDateTime getFileModifiedOBJ() {
        try {
            return LocalDateTime.parse(this.fileModified, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.getFileModifiedOBJ: Failed to parse date", e);
            return null;
        }
    }
    
    public String getFileModifiedSTR() {
        try {
            return LocalDateTime.parse(this.fileModified, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Attachment.getFileModifiedSTR: Failed to parse date", e);
            return null;
        }
    }
    
    public String getFileModifiedSTR_JSON() { return this.fileModified; }

    public LocalDateTime getFileAccessedOBJ() {
        try {
            return LocalDateTime.parse(this.fileAccessed, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.getFileAccessedOBJ: Failed to parse date", e);
            return null;
        }
    }
    
    public String getFileAccessedSTR() {
        try {
            return LocalDateTime.parse(this.fileAccessed, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Attachment.getFileAccessedSTR: Failed to parse date", e);
            return null;
        }
    }
    
    public String getFileAccessedSTR_JSON() { return this.fileAccessed; }

    public LocalDateTime getCreatedOBJ() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.getCreatedOBJ: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR() {
        try {
            return LocalDateTime.parse(this.created, CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON).format(CONSTANTS.DATE_TIME_FORMATTER);
        } catch (Exception e) {
            UError.exception("Attachment.getCreatedSTR: Failed to parse date", e);
            return null;
        }
    }

    public String getCreatedSTR_JSON() {
        return this.created;
    }

    public List<Attachment> getRelatedAttachments() {
        return OBJECTS.ATTACHMENTS.getAttachmentsListFromIDs(this.relatedAttachments);
    }

    public List<Integer> getRelatedAttachmentsIDs() {
        return this.relatedAttachments;
    }

    // Setters

    public void setID(Integer id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setType(AttachmentTypeEnum type) { this.type = type.getInteger(); }

    public void setIsSupported(boolean isSupported) { this.isSupported = isSupported ? 1 : 0; }

    public void setSource(String source) { this.source = source; }

    public void setSourceType(SourceTypeEnum sourceType) { this.sourceType = sourceType.getValue(); }

    public void setDownloaded(boolean downloaded) { this.downloaded = downloaded ? 1 : 0; }
    
    public void setIsSupported(Integer isSupported) { this.isSupported = isSupported == 1 ? 1 : 0; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public void setFileSize(Integer fileSize) { this.fileSize = fileSize; }

    public void setFileCreated(LocalDateTime fileCreated) { this.fileCreated = fileCreated.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON); }
    
    public void setFileCreated() { setFileCreated(LocalDateTime.now()); }
    
    public void setFileCreated(String fileCreated) {
        try {
            this.fileCreated = LocalDateTime.parse(fileCreated, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.setFileCreated: Failed to parse date", e);
        }
    }

    public void  setFileCreatedSTR_JSON(String fileCreated) { this.fileCreated = fileCreated; }

    public void setFileModified(LocalDateTime fileModified) { this.fileModified = fileModified.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON); }
    
    public void setFileModified() { setFileModified(LocalDateTime.now()); }
    
    public void setFileModified(String fileModified) {
        try {
            this.fileModified = LocalDateTime.parse(fileModified, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.setFileModified: Failed to parse date", e);
        }
    }

    public void setFileModifiedSTR_JSON(String fileModified) { this.fileModified = fileModified; }

    public void setFileAccessed(LocalDateTime fileAccessed) { this.fileAccessed = fileAccessed.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON); }
    
    public void setFileAccessed() { setFileAccessed(LocalDateTime.now()); }
    
    public void setFileAccessed(String fileAccessed) {
        try {
            this.fileAccessed = LocalDateTime.parse(fileAccessed, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.setFileAccessed: Failed to parse date", e);
        }
    }

    public void setFileAccessedSTR_JSON(String fileAccessed) { this.fileAccessed = fileAccessed; }

    public void setCreated(LocalDateTime created) {
        this.created = created.format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
    }
    
    public void setCreated(String created) {
        try {
            this.created = LocalDateTime.parse(created, CONSTANTS.DATE_TIME_FORMATTER).format(CONSTANTS.DATE_TIME_FORMATTER_FOR_JSON);
        } catch (Exception e) {
            UError.exception("Attachment.setCreated: Failed to parse date", e);
        }
    }
    
    public void setCreated() {
        setCreated(LocalDateTime.now());
    }

    public void setCreatedSTR_JSON(String created) { this.created = created; }

    public void setRelatedAttachments(List<Attachment> relatedAttachments) {
        List<Integer> ids = relatedAttachments.stream().map((Attachment attachment) -> attachment.getID()).collect(Collectors.toList());
        this.relatedAttachments = ids;
    }
    public void setRelatedAttachmentsFromIDsList(List<Integer> relatedAttachments) { this.relatedAttachments = relatedAttachments; }

}
