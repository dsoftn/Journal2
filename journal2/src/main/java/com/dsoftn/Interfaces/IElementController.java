package com.dsoftn.Interfaces;

import javafx.scene.layout.VBox;

public interface IElementController {

    public VBox getRoot();
    
    public void setRoot(VBox root);

    public void addToLayout(VBox layout);
    public void addToLayout(VBox layout, int insertIntoIndex);

    public void removeFromLayout();
    public void removeFromLayout(VBox layout);

}
