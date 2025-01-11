package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;


/*
TABLE attachments
    id INTEGER PRIMARY KEY AUTOINCREMENT
    name TEXT NOT NULL - name of the attachment
    description TEXT NOT NULL - description of the attachment
    type INTEGER NOT NULL - AttachmentTypeEnum value
    is_supported INTEGER NOT NULL - 0 or 1
    created TEXT NOT NULL - date in format for JSON
    file_path TEXT NOT NULL - path to attachment file
    file_size INTEGER NOT NULL - file size in bytes
    file_created TEXT NOT NULL - date in format for JSON
    file_modified TEXT NOT NULL - date in format for JSON
    file_accessed TEXT NOT NULL - date in format for JSON
RELATED PROPERTIES:
    Attachments
 */
public class Attachments implements IModelRepository<Attachment> {
    // Variables

    private Map<Integer, Attachment> data = new LinkedHashMap<>(); // <id, Attachment>

    // Interface methods

    @Override
    public boolean load() {
        boolean result = true;

        SQLiteDB db = new SQLiteDB();
        if (db.isConnected() == false) return false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM attachments");
            if (stmt == null) {
                UError.error("Attachments.load: Failed to load attachments", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Attachments.load: Failed to load attachments", "Result set is unexpectedly null");
                return false;
            }

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
            }

        } catch (Exception e) {
            UError.exception("Attachments.load: Failed to load attachments", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Attachments.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Attachments.load: Failed to close statement", e);
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

    // Public methods

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
            if (relation.getRelatedModel() == ScopeEnum.ATTACHMENT) {
                attachmentIDs.add(relation.getRelatedID());
            }
        }

        return getAttachmentsListFromIDs(attachmentIDs);
    }

}
