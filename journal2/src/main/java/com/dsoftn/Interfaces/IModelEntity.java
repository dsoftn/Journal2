package com.dsoftn.Interfaces;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public interface IModelEntity {

    public Integer getID();

    public boolean load(Integer id);

    public boolean isValid();

    public boolean add();
    public boolean canBeAdded();

    public boolean update();
    public boolean canBeUpdated();

    public boolean delete();
    public boolean canBeDeleted();

    public IModelEntity duplicateModel();

    public String getImagePath();
    
    public Image getGenericImage();

    default ImageView getImageAny(double width, double height) {
        Image image = null;

        if (getImagePath() != null && !getImagePath().isEmpty()) {
            image = new Image(getImagePath());
        }
        else {
            image = getGenericImage();
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        return imageView;
    }

    public String getFriendlyName();

    public String getTooltipString();

    public void ignoreEvents(boolean ignore);

}
