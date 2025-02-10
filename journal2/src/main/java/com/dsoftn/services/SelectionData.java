package com.dsoftn.services;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.CONSTANTS;
import com.dsoftn.enums.models.ModelEnum;

import javafx.scene.image.Image;

public class SelectionData {

    public class Item {
        private String name = "";
        private int id = CONSTANTS.INVALID_ID;
        private String tooltip = "";
        private Image image = null;
    
        public Item(String name, int id, String tooltip) {
            this.name = name;
            this.id = id;
            this.tooltip = tooltip;
        }
    
        public String getName() { return name; }
        public int getId() { return id; }
        public String getTooltip() { return tooltip; }
        public Image getImage() { return image; }
    
        public void setName(String name) { this.name = name; }
        public void setId(int id) { this.id = id; }
        public void setTooltip(String tooltip) { this.tooltip = tooltip; }
        public void setImage(Image image) { this.image = image; }

        // Public methods

        public String getShortName(int maxLength) {
            if (name.length() > maxLength) {
                return name.substring(0, maxLength - 3) + "...";
            } else {
                return name;
            }
        }
    }

    // Variables
    private List<Item> itemsAll = new ArrayList<>();
    private List<Item> itemsLast = new ArrayList<>();
    private List<Item> itemsMost = new ArrayList<>();
    private List<Item> itemsIgnored = new ArrayList<>();
    private List<Item> itemsSelected = new ArrayList<>();
    private ModelEnum baseModel = ModelEnum.NONE;
    private ModelEnum relatedModel = ModelEnum.NONE;

    // Constructors

    public SelectionData() {
        this.baseModel = ModelEnum.NONE;
    }

    public SelectionData(ModelEnum baseModel, ModelEnum relatedModel) {
        if (baseModel == null) baseModel = ModelEnum.NONE;
        if (relatedModel == null) relatedModel = ModelEnum.NONE;
        this.baseModel = baseModel;
        this.relatedModel = relatedModel;
    }


    // Public getters and setter methods

    public ModelEnum getBaseModel() {
        return baseModel;
    }

    public void setBaseModel(ModelEnum model) {
        if (model == null) model = ModelEnum.NONE;
        this.baseModel = model;
    }

    public ModelEnum getRelatedModel() {
        return relatedModel;
    }

    public void setRelatedModel(ModelEnum model) {
        if (model == null) model = ModelEnum.NONE;
        this.relatedModel = model;
    }

    public List<Item> getAllItems() {
        itemsAll = calculateAllItems(this.baseModel);
        return itemsAll;
    }

    public void setAllItems(List<Item> items) {
        this.itemsAll = items;
    }

    public List<Item> getLastItems() {
        itemsLast = calculateLastItems(this.baseModel);
        return itemsLast;
    }

    public void setLastItems(List<Item> items) {
        this.itemsLast = items;
    }

    public List<Item> getMostItems() {
        itemsMost = calculateMostItems(this.baseModel);
        return itemsMost;
    }

    public void setMostItems(List<Item> items) {
        this.itemsMost = items;
    }

    public List<Item> getIgnoredItems() {
        return itemsIgnored;
    }

    public void setIgnoredItems(List<Item> items) {
        this.itemsIgnored = items;
    }

    public List<Item> getSelectedItems() {
        return itemsSelected;
    }
    
    public void setSelectedItems(List<Item> items) {
        this.itemsSelected = items;
    }

    // Private methods

    private List<Item> calculateAllItems(ModelEnum model) {
        List<Item> items = new ArrayList<>();

        switch (model) {
            case BLOCK:
            case DEFINITION:
            case ATTACHMENT:
            case CATEGORY:
            case TAG:
            case RELATION:
            case DEF_VARIANT:
            case ACTOR:
            default:
                break;
        }
    
        return items;
    }

    private List<Item> calculateLastItems(ModelEnum model) {
        List<Item> items = new ArrayList<>();

        switch (model) {
            case BLOCK:
            case DEFINITION:
            case ATTACHMENT:
            case CATEGORY:
            case TAG:
            case RELATION:
            case DEF_VARIANT:
            case ACTOR:
            default:
                break;
        }
    
        return items;
    }
    
    private List<Item> calculateMostItems(ModelEnum model) {
        List<Item> items = new ArrayList<>();

        switch (model) {
            case BLOCK:
            case DEFINITION:
            case ATTACHMENT:
            case CATEGORY:
            case TAG:
            case RELATION:
            case DEF_VARIANT:
            case ACTOR:
            default:
                break;
        }
    
        return items;
    }

    private List<Item> calculateIgnoredItems(List<Integer> itemIDs, ModelEnum model) {
        List<Item> items = new ArrayList<>();

        switch (model) {
            case BLOCK:
            case DEFINITION:
            case ATTACHMENT:
            case CATEGORY:
            case TAG:
            case RELATION:
            case DEF_VARIANT:
            case ACTOR:
            default:
                break;
        }
    
        return items;
    }

    private List<Item> calculateSelectedItems(List<Integer> itemIDs, ModelEnum model) {
        List<Item> items = new ArrayList<>();

        switch (model) {
            case BLOCK:
            case DEFINITION:
            case ATTACHMENT:
            case CATEGORY:
            case TAG:
            case RELATION:
            case DEF_VARIANT:
            case ACTOR:
            default:
                break;
        }
    
        return items;
    }


}
