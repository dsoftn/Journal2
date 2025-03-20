package com.dsoftn.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.models.*;
import com.dsoftn.utils.UFile;

import javafx.scene.image.Image;

public class SelectionData {

    public class Item {
        private String name = "";
        private int id = CONSTANTS.INVALID_ID;
        private String tooltip = "";
        private String imagePath = "";
        private String genericImageResourcePath = null;
        private Boolean selected = null;
    
        // Constructors
        public Item(String name, int id, String tooltip, String imagePath, String genericImageResourcePath, Boolean selected) {
            this.name = name;
            this.id = id;
            this.tooltip = tooltip;
            this.imagePath = imagePath;
            this.genericImageResourcePath = genericImageResourcePath;
            this.selected = selected;
        }

        // Getters and setters
        public String getName() { return name; }
        public int getId() { return id; }
        public String getTooltip() { return tooltip; }
        public String getImagePath() { return imagePath; }
        public Boolean isSelected() { return selected; }
        public String getGenericImageResourcePath() { return genericImageResourcePath; }
    
        public void setName(String name) { this.name = name; }
        public void setId(int id) { this.id = id; }
        public void setTooltip(String tooltip) { this.tooltip = tooltip; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
        public void setGenericImageResourcePath(String genericImageResourcePath) { this.genericImageResourcePath = genericImageResourcePath; }
        public void setSelected(Boolean selected) { this.selected = selected; }

        // Public methods
        public String getShortName(int maxLength) {
            if (name.length() > maxLength) {
                return name.substring(0, maxLength - 3) + "...";
            } else {
                return name;
            }
        }

        public Image getImage() {
            if (imagePath != null && imagePath.length() > 0) {
                if (!UFile.isFile(imagePath)) return null;
                return new Image(imagePath);
            }
            else {
                if (genericImageResourcePath != null) {
                    return new Image(getClass().getResourceAsStream(genericImageResourcePath));
                }
                else {
                    return null;
                }
            }
        }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return  Objects.equals(name, item.name) &&
                Objects.equals(id, item.id) &&
                Objects.equals(tooltip, item.tooltip) &&
                Objects.equals(imagePath, item.imagePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, tooltip, imagePath);
    }

    }

    // Variables
    private List<Item> itemsAll = null;
    private List<Item> itemsLast = null;
    private List<Item> itemsMost = null;
    private List<Item> itemsIgnored = new ArrayList<>();
    private List<Item> itemsSelected = new ArrayList<>();
    private ModelEnum baseModel = ModelEnum.NONE;
    private ModelEnum relatedModel = ModelEnum.NONE;
    private Integer maxLastCount = OBJECTS.SETTINGS.getvINTEGER("MaxLastCount");
    private Integer maxMostCount = OBJECTS.SETTINGS.getvINTEGER("MaxMostCount");

    // Constructors

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
        if (itemsAll != null) return itemsAll;

        itemsAll = calculateAllItems();
        return itemsAll;
    }

    public List<Item> getClipItems() {
        return calculateClipItems();
    }

    public void setAllItems(List<Item> items) {
        this.itemsAll = items;
    }

    public List<Item> getLastItems() {
        if (itemsLast != null) return itemsLast;

        itemsLast = calculateLastItems();
        return itemsLast;
    }

    public void setLastItems(List<Item> items) {
        this.itemsLast = items;
    }

    public List<Item> getMostItems() {
        if (itemsMost != null) return itemsMost;

        itemsMost = calculateMostItems();
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

    public void setSelectedItems(List<Integer> itemsIDs, ModelEnum model) {
        this.itemsSelected = new ArrayList<>();
        for (int itemID : itemsIDs) {
            this.itemsSelected.add(getItemEntity(getEntity(itemID, model)));
        }
    }

    public Integer getMaxLastCount() {
        return maxLastCount;
    }

    public void setMaxLastCount(Integer count) {
        this.maxLastCount = count;
    }

    public Integer getMaxMostCount() {
        return maxMostCount;
    }

    public void setMaxMostCount(Integer count) {
        this.maxMostCount = count;
    }

    // Public methods

    public void addSelectedItem(Item item) {
        if (item == null) return;
        if (itemsSelected.contains(item)) return;
        itemsSelected.add(item);
    }

    public void removeSelectedItem(Item item) {
        if (item == null) return;
        if (!itemsSelected.contains(item)) return;
        itemsSelected.remove(item);
    }

    public Item getItem(int id) {
        for (Item item : itemsAll) {
            if (item.getId() == id) return item;
        }
        return null;
    }

    // Private methods

    private List<Item> calculateAllItems() {
        if (relatedModel == null || relatedModel == ModelEnum.NONE) return this.itemsLast;

        ModelEnum  model = this.relatedModel;
        
        List<Item> items = new ArrayList<>();
        Item item = null;
        switch (model) {
            case BLOCK:
                for (Block modelOBJ : OBJECTS.BLOCKS.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case DEFINITION:
                for (Definition modelOBJ : OBJECTS.DEFINITIONS.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case ATTACHMENT:
                for (Attachment modelOBJ : OBJECTS.ATTACHMENTS.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case CATEGORY:
                for (Category modelOBJ : OBJECTS.CATEGORIES.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case TAG:
                for (Tag modelOBJ : OBJECTS.TAGS.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case RELATION:
                for (Relation modelOBJ : OBJECTS.RELATIONS.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case ACTOR:
                for (Actor modelOBJ : OBJECTS.ACTORS.getEntityAll()) {
                    item = getItemEntity((IModelEntity) modelOBJ);
                    if (item == null) continue;
                    items.add(item);
                }
                break;
            case DEF_VARIANT:
            case ALL:
            default:
                break;
        }
    
        return items;
    }

    private List<Item> calculateClipItems() {
        List<Integer> itemsInClip = OBJECTS.CLIP.getIDs(relatedModel);

        if (itemsInClip == null || itemsInClip.isEmpty()) return new ArrayList<Item>();

        List<Item> itemsClip = new ArrayList<>();

        for (int clipID : itemsInClip) {
            Item newItem = getItemEntity(getEntity(clipID, relatedModel));
            if (newItem == null) continue;
            itemsClip.add(newItem);
        }

        return itemsClip;
    }

    private IModelEntity getEntity(int id, ModelEnum model) {
        switch (model) {
            case BLOCK:
                return OBJECTS.BLOCKS.getEntity(id);
            case DEFINITION:
                return OBJECTS.DEFINITIONS.getEntity(id);
            case ATTACHMENT:
                return OBJECTS.ATTACHMENTS.getEntity(id);
            case CATEGORY:
                return OBJECTS.CATEGORIES.getEntity(id);
            case TAG:
                return OBJECTS.TAGS.getEntity(id);
            case RELATION:
                return OBJECTS.RELATIONS.getEntity(id);
            case ACTOR:
                return OBJECTS.ACTORS.getEntity(id);
            case DEF_VARIANT:
            case ALL:
            default:
                return null;
        }
    }

    public Item getItemEntity(IModelEntity modelObject) {
        if (modelObject == null) return null;

        Item newItem = new Item(
            modelObject.getFriendlyName(),
            modelObject.getID(),
            modelObject.getTooltipString(),
            modelObject.getImagePath(),
            modelObject.getGenericImageResourcePath(),
            null);
        
        boolean selected = this.itemsSelected.contains(newItem);
        newItem.setSelected(selected);
        
        if (itemsIgnored.contains(newItem)) return null;
        
        return newItem;
    }

    private List<Item> calculateLastItems() {
        return getLastAndMostItemsHelper(OBJECTS.RELATIONS.getLastUsed(baseModel, null, relatedModel, null));
    }

    private List<Item> calculateMostItems() {
        return getLastAndMostItemsHelper(OBJECTS.RELATIONS.getMostUsed(baseModel, null, relatedModel, null));
    }

    private List<Item> getLastAndMostItemsHelper(List<Integer> listOfIntegers) {
        if (baseModel == null || baseModel == ModelEnum.NONE) return this.itemsLast;
        if (relatedModel == null || relatedModel == ModelEnum.NONE) return this.itemsLast;
        
        List<Item> items = new ArrayList<>();

        int counter = 0;
        for (int entityID : listOfIntegers) {
            Item newItem = getItemEntity(getEntity(entityID, relatedModel));
            if (itemsIgnored.contains(newItem)) continue;
            items.add(newItem);
            counter++;
            if (maxLastCount != null && counter >= maxLastCount) break;
        }

        return items;
    }

}
