package com.dsoftn.Interfaces;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public interface IElementController {

    public void setStage(Stage stage);

    public Stage getStage();
    
    public VBox getRoot();

    public void setParentController(IBaseController parentController);
    public IBaseController getParentController();

    public void setRoot(VBox root);

    public void addToLayout(VBox layout);
    public void addToLayout(VBox layout, int insertIntoIndex);

    public void removeFromLayout();
    public void removeFromLayout(VBox layout);

    public String getMyName();

    public void calculateData();

}
