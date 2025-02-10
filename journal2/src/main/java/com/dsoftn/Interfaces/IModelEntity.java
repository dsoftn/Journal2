package com.dsoftn.Interfaces;

import javafx.scene.image.Image;

public interface IModelEntity<T> {

    public Integer getID();

    public boolean load(Integer id);

    public boolean isValid();

    public boolean add();
    public boolean canBeAdded();

    public boolean update();
    public boolean canBeUpdated();

    public boolean delete();
    public boolean canBeDeleted();

    public T duplicate();

    public String getImagePath();
    
    public Image getGenericImage();

    public String getFriendlyName();

    public String getTooltipString();

}
