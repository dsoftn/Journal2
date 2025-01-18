package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.event.Event;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.events.AttachmentAddedEvent;
import com.dsoftn.events.AttachmentUpdatedEvent;
import com.dsoftn.events.AttachmentDeletedEvent;
import com.dsoftn.events.BlockAddedEvent;
import com.dsoftn.events.BlockUpdatedEvent;
import com.dsoftn.events.BlockDeletedEvent;
import com.dsoftn.events.CategoryAddedEvent;
import com.dsoftn.events.CategoryUpdatedEvent;
import com.dsoftn.events.CategoryDeletedEvent;
import com.dsoftn.events.TagAddedEvent;
import com.dsoftn.events.TagUpdatedEvent;
import com.dsoftn.models.enums.ScopeEnum;
import com.dsoftn.events.TagDeletedEvent;


/*
TABLE relations
    id INTEGER PRIMARY KEY AUTOINCREMENT
    base_model INTEGER NOT NULL - type of the base model (ScopeEnum value)
    base_id INTEGER NOT NULL - id of the base model
    related_model INTEGER NOT NULL - type of the related model (ScopeEnum value)
    related_id INTEGER NOT NULL - id of the related model
    description TEXT NOT NULL - description of the relation
    created TEXT NOT NULL - date in format for JSON
 */
public class Relations implements IModelRepository<Relation>, ICustomEventListener {
    // Variables

    private Map<Integer, Relation> data = new LinkedHashMap<>(); // <id, Relation>

    private Map<String, List<Relation>> dataByScopeAndID = new LinkedHashMap<>(); // <scope + id, List<Relation>>

    // Constructor

    public Relations() {
        OBJECTS.EVENT_HANDLER.register(
            this,
            AttachmentAddedEvent.ATTACHMENT_ADDED_EVENT,
            AttachmentUpdatedEvent.ATTACHMENT_UPDATED_EVENT,
            AttachmentDeletedEvent.ATTACHMENT_DELETED_EVENT,
            BlockAddedEvent.BLOCK_ADDED_EVENT,
            BlockUpdatedEvent.BLOCK_UPDATED_EVENT,
            BlockDeletedEvent.BLOCK_DELETED_EVENT,
            CategoryAddedEvent.CATEGORY_ADDED_EVENT,
            CategoryUpdatedEvent.CATEGORY_UPDATED_EVENT,
            CategoryDeletedEvent.CATEGORY_DELETED_EVENT,
            TagAddedEvent.TAG_ADDED_EVENT,
            TagUpdatedEvent.TAG_UPDATED_EVENT,
            TagDeletedEvent.TAG_DELETED_EVENT
        );
    }

    // Interface ICustomEventListener methods

    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof AttachmentAddedEvent) {
            onAttachmentAddedEvent(event);
        }
        else if (event instanceof AttachmentUpdatedEvent) {
            onAttachmentUpdatedEvent(event);
        }
        else if (event instanceof AttachmentDeletedEvent) {
            onAttachmentDeletedEvent(event);
        }
        else if (event instanceof BlockAddedEvent) {
            onBlockAddedEvent(event);
        }
        else if (event instanceof BlockUpdatedEvent) {
            onBlockUpdatedEvent(event);
        }
        else if (event instanceof BlockDeletedEvent) {
            onBlockDeletedEvent(event);
        }
        else if (event instanceof CategoryAddedEvent) {
            onCategoryAddedEvent(event);
        }
        else if (event instanceof CategoryUpdatedEvent) {
            onCategoryUpdatedEvent(event);
        }
        else if (event instanceof CategoryDeletedEvent) {
            onCategoryDeletedEvent(event);
        }
        else if (event instanceof TagAddedEvent) {
            onTagAddedEvent(event);
        }
        else if (event instanceof TagUpdatedEvent) {
            onTagUpdatedEvent(event);
        }
        else if (event instanceof TagDeletedEvent) {
            onTagDeletedEvent(event);
        }

    }

    private void onAttachmentAddedEvent(Event event) {
        AttachmentAddedEvent attachmentAddedEvent = (AttachmentAddedEvent) event;
        Attachment attachment = attachmentAddedEvent.getAttachment();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.ATTACHMENT, attachment.getID());

        if (dataByScopeAndID.containsKey(scopeAndIDKey)) {
            UError.error(
                "Relations.onCustomEvent.onAttachmentAddedEvent: Key in 'scopeAndId' Map already exist",
                "Event received: AttachmentAddedEvent",
                "Attachment ID: " + attachment.getID(),
                "Scope and ID key: " + scopeAndIDKey,
                "Internal error occurred, related Attachments will be added but data structure may be corrupted !",
                CONSTANTS.DATA_STRUCTURE_CORRUPTED_MESSAGE_STRING);
                // Deleting existing relations
                dataByScopeAndID.remove(scopeAndIDKey);
        }

        for (int relatedAttachmentID : attachment.getRelatedAttachmentsIDs()) {
            Relation relation = new Relation();
            relation.setBaseModel(ScopeEnum.ATTACHMENT);
            relation.setBaseID(attachment.getID());
            relation.setRelatedModel(ScopeEnum.ATTACHMENT);
            relation.setRelatedID(relatedAttachmentID);

            relation.add(true);
        }
    }

    private void onAttachmentUpdatedEvent(Event event) {
        AttachmentUpdatedEvent attachmentUpdatedEvent = (AttachmentUpdatedEvent) event;
        Attachment attachment = attachmentUpdatedEvent.getNewAttachment();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.ATTACHMENT, attachment.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        // Delete all relations that new attachment does not have
        List<Relation> relationsToRemove = new ArrayList<>();
        List<Integer> relationsToAdd = new ArrayList<>();
        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            if (relation.getRelatedModel() != ScopeEnum.ATTACHMENT) continue;

            if (!attachment.getRelatedAttachmentsIDs().contains(relation.getRelatedID())) {
                relationsToRemove.add(relation);
            }
            else {
                relationsToAdd.add(relation.getRelatedID());
            }
        }

        for (Relation relation : relationsToRemove) {
            relation.delete(true);
        }

        // Add new relations that new attachment has
        for (int relatedAttachmentID : attachment.getRelatedAttachmentsIDs()) {
            if (relationsToAdd.contains(relatedAttachmentID)) continue;

            relationsToAdd.add(relatedAttachmentID);

            Relation relation = new Relation();
            relation.setBaseModel(ScopeEnum.ATTACHMENT);
            relation.setBaseID(attachment.getID());
            relation.setRelatedModel(ScopeEnum.ATTACHMENT);
            relation.setRelatedID(relatedAttachmentID);

            relation.add(true);
        }
    }

    private void onAttachmentDeletedEvent(Event event) {
        AttachmentDeletedEvent attachmentDeletedEvent = (AttachmentDeletedEvent) event;
        Attachment attachment = attachmentDeletedEvent.getAttachment();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.ATTACHMENT, attachment.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        // Delete all relations that new attachment does not have
        List<Relation> relationsToRemove = new ArrayList<>();
        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            relationsToRemove.add(relation);
        }

        for (Relation relation : relationsToRemove) {
            relation.delete(true);
        }

        // Delete key from dataByScopeAndID
        dataByScopeAndID.remove(scopeAndIDKey);
    }

    private void onBlockAddedEvent(Event event) {
        BlockAddedEvent blockAddedEvent = (BlockAddedEvent) event;
        Block block = blockAddedEvent.getBlock();
        
        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.BLOCK, block.getID());

        if (dataByScopeAndID.containsKey(scopeAndIDKey)) {
            UError.error(
                "Relations.onCustomEvent.onBlockAddedEvent: Key in 'scopeAndId' Map already exist",
                "Event received: BlockAddedEvent",
                "Block ID: " + block.getID(),
                "Scope and ID key: " + scopeAndIDKey,
                "Internal error occurred, related Block Objects will be added but data structure may be corrupted !",
                CONSTANTS.DATA_STRUCTURE_CORRUPTED_MESSAGE_STRING);
                // Deleting existing relations
                dataByScopeAndID.remove(scopeAndIDKey);
        }

        List<ScopeEnum> modelsToProcess = new ArrayList<>();
        modelsToProcess.add(ScopeEnum.ATTACHMENT);
        modelsToProcess.add(ScopeEnum.CATEGORY);
        modelsToProcess.add(ScopeEnum.TAG);
        modelsToProcess.add(ScopeEnum.BLOCK);

        for (ScopeEnum model : modelsToProcess) {
            List<Integer> relatedModelIDs = new ArrayList<>();
            if (model == ScopeEnum.ATTACHMENT) relatedModelIDs = block.getRelatedAttachmentsIDs();
            if (model == ScopeEnum.CATEGORY) relatedModelIDs = block.getRelatedCategoriesIDs();
            if (model == ScopeEnum.TAG) relatedModelIDs = block.getRelatedTagsIDs();
            if (model == ScopeEnum.BLOCK) relatedModelIDs = block.getRelatedBlocksIDs();

            for (int relatedID : relatedModelIDs) {
                Relation relation = new Relation();
                relation.setBaseModel(ScopeEnum.BLOCK);
                relation.setBaseID(block.getID());
                relation.setRelatedModel(model);
                relation.setRelatedID(relatedID);

                relation.add(true);
            }
        }

    }

    private void onBlockUpdatedEvent(Event event) {
        BlockUpdatedEvent blockUpdatedEvent = (BlockUpdatedEvent) event;
        Block block = blockUpdatedEvent.getNewBlock();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.BLOCK, block.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        List<ScopeEnum> modelsToProcess = new ArrayList<>();
        modelsToProcess.add(ScopeEnum.ATTACHMENT);
        modelsToProcess.add(ScopeEnum.CATEGORY);
        modelsToProcess.add(ScopeEnum.TAG);
        modelsToProcess.add(ScopeEnum.BLOCK);

        for (ScopeEnum model : modelsToProcess) {
            List<Integer> relatedModelIDs = new ArrayList<>();
            if (model == ScopeEnum.ATTACHMENT) relatedModelIDs = block.getRelatedAttachmentsIDs();
            if (model == ScopeEnum.CATEGORY) relatedModelIDs = block.getRelatedCategoriesIDs();
            if (model == ScopeEnum.TAG) relatedModelIDs = block.getRelatedTagsIDs();
            if (model == ScopeEnum.BLOCK) relatedModelIDs = block.getRelatedBlocksIDs();

            // Delete not needed
            List<Relation> relationsToRemove = new ArrayList<>();
            List<Integer> relationsToAdd = new ArrayList<>();
            for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
                if (relation.getRelatedModel() != model) continue;

                if (!relatedModelIDs.contains(relation.getRelatedID())) {
                    relationsToRemove.add(relation);
                }
                else {
                    relationsToAdd.add(relation.getRelatedID());
                }
            }

            for (Relation relation : relationsToRemove) {
                relation.delete(true);
            }

            // Add new
            for (int relatedID : relatedModelIDs) {
                if (relationsToAdd.contains(relatedID)) continue;

                relationsToAdd.add(relatedID);

                Relation relation = new Relation();
                relation.setBaseModel(ScopeEnum.BLOCK);
                relation.setBaseID(block.getID());
                relation.setRelatedModel(model);
                relation.setRelatedID(relatedID);

                relation.add(true);
            }
        }
    }

    private void onBlockDeletedEvent(Event event) {
        BlockDeletedEvent BlockDeletedEvent = (BlockDeletedEvent) event;
        Block block = BlockDeletedEvent.getBlock();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.BLOCK, block.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        // Delete all relations
        List<Relation> relationsToRemove = new ArrayList<>();
        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            relationsToRemove.add(relation);
        }

        for (Relation relation : relationsToRemove) {
            relation.delete(true);
        }

        // Delete key from dataByScopeAndID
        dataByScopeAndID.remove(scopeAndIDKey);
    }

    private void onCategoryAddedEvent(Event event) {
        CategoryAddedEvent categoryAddedEvent = (CategoryAddedEvent) event;
        Category category = categoryAddedEvent.getCategory();
        
        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.CATEGORY, category.getID());

        if (dataByScopeAndID.containsKey(scopeAndIDKey)) {
            UError.error(
                "Relations.onCustomEvent.onCategoryAddedEvent: Key in 'scopeAndId' Map already exist",
                "Event received: CategoryAddedEvent",
                "Category ID: " + category.getID(),
                "Scope and ID key: " + scopeAndIDKey,
                "Internal error occurred, related Category Objects will be added but data structure may be corrupted !",
                CONSTANTS.DATA_STRUCTURE_CORRUPTED_MESSAGE_STRING);
                // Deleting existing relations
                dataByScopeAndID.remove(scopeAndIDKey);
        }

        List<ScopeEnum> modelsToProcess = new ArrayList<>();
        modelsToProcess.add(ScopeEnum.CATEGORY);
        modelsToProcess.add(ScopeEnum.TAG);

        for (ScopeEnum model : modelsToProcess) {
            List<Integer> relatedModelIDs = new ArrayList<>();
            if (model == ScopeEnum.CATEGORY) relatedModelIDs = category.getRelatedCategoriesIDs();
            if (model == ScopeEnum.TAG) relatedModelIDs = category.getRelatedTagsIDs();

            for (int relatedID : relatedModelIDs) {
                Relation relation = new Relation();
                relation.setBaseModel(ScopeEnum.CATEGORY);
                relation.setBaseID(category.getID());
                relation.setRelatedModel(model);
                relation.setRelatedID(relatedID);

                relation.add(true);
            }
        }

    }

    private void onCategoryUpdatedEvent(Event event) {
        CategoryUpdatedEvent categoryUpdatedEvent = (CategoryUpdatedEvent) event;
        Category category = categoryUpdatedEvent.getNewCategory();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.CATEGORY, category.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        List<ScopeEnum> modelsToProcess = new ArrayList<>();
        modelsToProcess.add(ScopeEnum.CATEGORY);
        modelsToProcess.add(ScopeEnum.TAG);

        for (ScopeEnum model : modelsToProcess) {
            List<Integer> relatedModelIDs = new ArrayList<>();
            if (model == ScopeEnum.CATEGORY) relatedModelIDs = category.getRelatedCategoriesIDs();
            if (model == ScopeEnum.TAG) relatedModelIDs = category.getRelatedTagsIDs();

            // Delete not needed
            List<Relation> relationsToRemove = new ArrayList<>();
            List<Integer> relationsToAdd = new ArrayList<>();
            for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
                if (relation.getRelatedModel() != model) continue;

                if (!relatedModelIDs.contains(relation.getRelatedID())) {
                    relationsToRemove.add(relation);
                }
                else {
                    relationsToAdd.add(relation.getRelatedID());
                }
            }

            for (Relation relation : relationsToRemove) {
                relation.delete(true);
            }

            // Add new
            for (int relatedID : relatedModelIDs) {
                if (relationsToAdd.contains(relatedID)) continue;

                relationsToAdd.add(relatedID);

                Relation relation = new Relation();
                relation.setBaseModel(ScopeEnum.CATEGORY);
                relation.setBaseID(category.getID());
                relation.setRelatedModel(model);
                relation.setRelatedID(relatedID);

                relation.add(true);
            }
        }
    }

    private void onCategoryDeletedEvent(Event event) {
        CategoryDeletedEvent categoryDeletedEvent = (CategoryDeletedEvent) event;
        Category category = categoryDeletedEvent.getCategory();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.CATEGORY, category.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        // Delete all relations
        List<Relation> relationsToRemove = new ArrayList<>();
        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            relationsToRemove.add(relation);
        }

        for (Relation relation : relationsToRemove) {
            relation.delete(true);
        }

        // Delete key from dataByScopeAndID
        dataByScopeAndID.remove(scopeAndIDKey);
    }

    private void onTagAddedEvent(Event event) {
        TagAddedEvent tagAddedEvent = (TagAddedEvent) event;
        Tag tag = tagAddedEvent.getTag();
        
        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.TAG, tag.getID());

        if (dataByScopeAndID.containsKey(scopeAndIDKey)) {
            UError.error(
                "Relations.onCustomEvent.onTagAddedEvent: Key in 'scopeAndId' Map already exist",
                "Event received: TagAddedEvent",
                "Tag ID: " + tag.getID(),
                "Scope and ID key: " + scopeAndIDKey,
                "Internal error occurred, related Tag Objects will be added but data structure may be corrupted !",
                CONSTANTS.DATA_STRUCTURE_CORRUPTED_MESSAGE_STRING);
                // Deleting existing relations
                dataByScopeAndID.remove(scopeAndIDKey);
        }

        List<ScopeEnum> modelsToProcess = new ArrayList<>();
        modelsToProcess.add(ScopeEnum.TAG);

        for (ScopeEnum model : modelsToProcess) {
            List<Integer> relatedModelIDs = new ArrayList<>();
            if (model == ScopeEnum.TAG) relatedModelIDs = tag.getRelatedTagsIDs();

            for (int relatedID : relatedModelIDs) {
                Relation relation = new Relation();
                relation.setBaseModel(ScopeEnum.TAG);
                relation.setBaseID(tag.getID());
                relation.setRelatedModel(model);
                relation.setRelatedID(relatedID);

                relation.add(true);
            }
        }

    }

    private void onTagUpdatedEvent(Event event) {
        TagUpdatedEvent tagUpdatedEvent = (TagUpdatedEvent) event;
        Tag tag = tagUpdatedEvent.getNewTag();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.TAG, tag.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        List<ScopeEnum> modelsToProcess = new ArrayList<>();
        modelsToProcess.add(ScopeEnum.TAG);

        for (ScopeEnum model : modelsToProcess) {
            List<Integer> relatedModelIDs = new ArrayList<>();
            if (model == ScopeEnum.TAG) relatedModelIDs = tag.getRelatedTagsIDs();

            // Delete not needed
            List<Relation> relationsToRemove = new ArrayList<>();
            List<Integer> relationsToAdd = new ArrayList<>();
            for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
                if (relation.getRelatedModel() != model) continue;

                if (!relatedModelIDs.contains(relation.getRelatedID())) {
                    relationsToRemove.add(relation);
                }
                else {
                    relationsToAdd.add(relation.getRelatedID());
                }
            }

            for (Relation relation : relationsToRemove) {
                relation.delete(true);
            }

            // Add new
            for (int relatedID : relatedModelIDs) {
                if (relationsToAdd.contains(relatedID)) continue;

                relationsToAdd.add(relatedID);

                Relation relation = new Relation();
                relation.setBaseModel(ScopeEnum.TAG);
                relation.setBaseID(tag.getID());
                relation.setRelatedModel(model);
                relation.setRelatedID(relatedID);

                relation.add(true);
            }
        }
    }

    private void onTagDeletedEvent(Event event) {
        TagDeletedEvent tagDeletedEvent = (TagDeletedEvent) event;
        Tag tag = tagDeletedEvent.getTag();

        String scopeAndIDKey = getScopeAndIdKey(ScopeEnum.TAG, tag.getID());

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            // Add key to dataByScopeAndID
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        // Delete all relations
        List<Relation> relationsToRemove = new ArrayList<>();
        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            relationsToRemove.add(relation);
        }

        for (Relation relation : relationsToRemove) {
            relation.delete(true);
        }

        // Delete key from dataByScopeAndID
        dataByScopeAndID.remove(scopeAndIDKey);
    }

    // Interface IModelRepository<Relation> methods

    @Override
    public boolean load() {
        boolean result = true;

        SQLiteDB db = new SQLiteDB();
        if (db.isConnected() == false) return false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM relations");
            if (stmt == null) {
                UError.error("Relations.load: Failed to load relations", "Statement is unexpectedly null");
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("Relations.load: Failed to load relations", "Result set is unexpectedly null");
                return false;
            }

            while (rs.next()) {
                Relation relation = new Relation();
                result = relation.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("Relations.load: Failed to load relation", "Loading relation failed");
                    result = false;
                    continue;
                }
                
                // Add relation
                add(relation);
            }

        } catch (Exception e) {
            UError.exception("Relations.load: Failed to load relations", e);
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("Relations.load: Failed to close result set", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("Relations.load: Failed to close statement", e);
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
    public Relation getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<Relation> getEntityAll() {
        List<Relation> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(Relation entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        // Add relation to data and dataByScopeAndID
        String scopeAndIDKey = getScopeAndIdKey(entity);

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
        }

        data.put(entity.getID(), entity);
        dataByScopeAndID.get(scopeAndIDKey).add(entity);
        
        return true;
    }

    @Override
    public boolean update(Relation entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        // Update data and dataByScopeAndID
        String scopeAndIDKey = getScopeAndIdKey(entity);

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            UError.error("Relations.update: Relation does not exist", "Relation does not exist in 'scopeAndId' Map");
            return false;
        }

        data.put(entity.getID(), entity);

        rebuildScopeAndIdMap();

        return true;
    }

    @Override
    public boolean delete(Relation entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        // Remove from data and dataByScopeAndID
        String scopeAndIDKey = getScopeAndIdKey(entity);

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
            UError.error("Relations.delete: Relation does not exist", "Relation does not exist in 'scopeAndId' Map");
            return false;
        }

        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            if (relation.getID() == entity.getID()) {
                dataByScopeAndID.get(scopeAndIDKey).remove(relation);
                break;
            }
        }

        data.remove(entity.getID());
        
        return true;
    }

    // Public methods

    public List<Relation> getScopeAndIdList(ScopeEnum baseModel, Integer baseID, ScopeEnum relatedModel) {
        String scopeAndIDKey = getScopeAndIdKey(baseModel, baseID);
        List<Relation> list = new ArrayList<>();

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) return list;

        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            if (relation.getRelatedModel() == relatedModel) {
                list.add(relation);
            }
        }

        return list;
    }

    public List<Relation> getScopeAndIdList(ScopeEnum baseModel, Integer baseID) {
        String scopeAndIDKey = getScopeAndIdKey(baseModel, baseID);
        List<Relation> list = new ArrayList<>();

        if (!dataByScopeAndID.containsKey(scopeAndIDKey)) return list;

        for (Relation relation : dataByScopeAndID.get(scopeAndIDKey)) {
            list.add(relation);
        }

        return list;
    }

    /**
     * Check if relation with same base model, base id, related model and related id exists
     */
    public boolean isSameRelationExists(Relation entity) {
        for (Relation relation : data.values()) {
            if (relation.equals(entity)) return true;
        }
        return false;
    }

    /**
     * Get relations list
     * @param baseModel - use ScopeEnum.ALL or null  to get all base models
     * @param baseID - use null to get all base ids
     * @param relatedModel - use ScopeEnum.ALL or null to get all related models
     * @param relatedID - use null to get all related ids
     * @return
     */
    public List<Relation> getRelationsList(ScopeEnum baseModel, Integer baseID, ScopeEnum relatedModel, Integer relatedID) {
        List<Relation> list = new ArrayList<>();

        if (baseModel == null) baseModel = ScopeEnum.ALL;
        if (relatedModel == null) relatedModel = ScopeEnum.ALL;

        for (Map.Entry<Integer, Relation> entry : data.entrySet()) {
            Relation relation = entry.getValue();

            if (baseModel != ScopeEnum.ALL && relation.getBaseModel() != baseModel) continue;
            if (baseID != null && relation.getBaseID() != baseID) continue;
            if (relatedModel != ScopeEnum.ALL && relation.getRelatedModel() != relatedModel) continue;
            if (relatedID != null && relation.getRelatedID() != relatedID) continue;

            list.add(relation);
        }

        return list;
    }

    public List<Relation> getRelationsList(ScopeEnum baseModel, Integer baseID, ScopeEnum relatedModel) {
        return getScopeAndIdList(baseModel, baseID, relatedModel);
    }

    public List<Relation> getRelationsList(ScopeEnum baseModel, Integer baseID) {
        return getScopeAndIdList(baseModel, baseID);
    }

    public List<Relation> getRelationsList(ScopeEnum baseModel) {
        return getRelationsList(baseModel, null, null, null);
    }

    // Private methods

    private String getScopeAndIdKey(Relation relation) {
        return relation.getBaseModel().toString() + ";" + String.valueOf(relation.getBaseID());
    }

    private String getScopeAndIdKey(ScopeEnum baseModel, Integer baseID) {
        return baseModel.toString() + ";" + String.valueOf(baseID);
    }

    private void rebuildScopeAndIdMap() {
        dataByScopeAndID.clear();
        for (Relation relation : data.values()) {
            String scopeAndIDKey = getScopeAndIdKey(relation);
            if (!dataByScopeAndID.containsKey(scopeAndIDKey)) {
                dataByScopeAndID.put(scopeAndIDKey, new ArrayList<>());
            }
            dataByScopeAndID.get(scopeAndIDKey).add(relation);
        }
    }

}
