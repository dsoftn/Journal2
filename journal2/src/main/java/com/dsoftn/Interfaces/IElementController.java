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

    default boolean canBeClosed() { return true; }

    public void calculateData();

    public void closeMe();

    default void message(String senderID, String receiverID, String messageSTRING) {}

    default void beforeShowing() {}

}
