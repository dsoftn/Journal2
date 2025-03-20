package com.dsoftn.services;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.dsoftn.OBJECTS;
import com.dsoftn.enums.events.ClipboardActionEnum;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.utils.UString;


public class Clip {
    private Map<String, List<Integer>> clipMap = null;

    // Constructor

    public Clip() { }

    // Set new IDs

    public void setIDs(String clipModel, List<Integer> ids) {
        if (clipMap == null) {
            clipMap = new HashMap<>();
        }

        clipMap.put(clipModel, ids);

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.COPY, ModelEnum.fromName(clipModel), UString.joinListOfInteger(ids, ","));
        OBJECTS.EVENT_HANDLER.fireEvent(event);
    }

    public void setIDs(String clipModel, Integer id) {
        setIDs(clipModel, new ArrayList<Integer>() { { add(id); } });
    }

    public void setIDs(ModelEnum model, List<Integer> ids) {
        setIDs(model.toString(), ids);
    }

    public void setIDs(ModelEnum model, Integer id) {
        setIDs(model.toString(), new ArrayList<Integer>() { { add(id); } });
    }

    // Add new IDs

    public void addIDs(String clipModel, List<Integer> ids) {
        if (clipMap == null) {
            clipMap = new HashMap<>();
        }

        if (clipMap.containsKey(clipModel)) {
            clipMap.get(clipModel).addAll(ids);
        }
        else {
            clipMap.put(clipModel, ids);
        }
        
        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.ADD, ModelEnum.fromName(clipModel), UString.joinListOfInteger(ids, ","));
        OBJECTS.EVENT_HANDLER.fireEvent(event);
    }

    public void addIDs(String clipModel, Integer id) {
        addIDs(clipModel, new ArrayList<Integer>() { { add(id); } });
    }

    public void addIDs(ModelEnum model, List<Integer> ids) {
        addIDs(model.toString(), ids);
    }

    public void addIDs(ModelEnum model, Integer id) {
        addIDs(model.toString(), new ArrayList<Integer>() { { add(id); } });
    }

    // Get IDs

    public List<Integer> getIDs(String clipModel) {
        if (clipMap == null) {
            return null;
        }

        if (clipMap.containsKey(clipModel)) {
            return clipMap.get(clipModel);
        }

        return null;
    }

    public List<Integer> getIDs(ModelEnum model) {
        return getIDs(model.toString());
    }

    // Clear IDs

    public void clearIDs(String clipModel) {
        if (clipMap == null) {
            return;
        }

        if (clipMap.containsKey(clipModel)) {
            clipMap.remove(clipModel);
        }

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.DELETE_MODEL, ModelEnum.fromName(clipModel), null);
        OBJECTS.EVENT_HANDLER.fireEvent(event);
        
    }

    public void clearIDs(ModelEnum model) {
        clearIDs(model.toString());
    }

    public void clearIDs() {
        clipMap = null;

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.DELETE_ALL, null, null);
        OBJECTS.EVENT_HANDLER.fireEvent(event);
    }

    public void clear() {
        clearIDs();
    }

    // Count IDs

    public int countIDs(String clipModel) {
        if (clipMap == null) {
            return 0;
        }

        if (clipMap.containsKey(clipModel)) {
            return clipMap.get(clipModel).size();
        }

        return 0;
    }

    public int countIDs(ModelEnum model) {
        return countIDs(model.toString());
    }


}
