package com.dsoftn.controllers;

import com.dsoftn.CONSTANTS;
import com.dsoftn.DIALOGS;
import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.controllers.EmptyDialogController.WindowBehavior;
import com.dsoftn.controllers.elements.BlockGeneralController;
import com.dsoftn.controllers.elements.SelectionController;
import com.dsoftn.controllers.models.ActorEditController;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.models.Actor;
import com.dsoftn.models.Block;
import com.dsoftn.models.block_types.BlockDiary;
import com.dsoftn.utils.UJavaFX;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWinController implements IBaseController {

    // Variables
    
    private String myName = UJavaFX.getUniqueId();
    
    private Stage stage;

    //      ---   FXML Variables   ---

    // Menu
    @FXML
    MenuItem mnuNew;

    @FXML
    MenuItem mnuAddActor;

    // Areas
    @FXML
    ScrollPane scrPaneContentArea;
    @FXML
    AnchorPane anchorContentArea;
    @FXML
    VBox vBoxContentArea;
    
    @FXML
    ScrollPane scrPaneWorkArea;
    @FXML
    AnchorPane anchorWorkArea;
    @FXML
    VBox vBoxWorkArea;

    @FXML
    ScrollPane scrPaneNewsArea;
    @FXML
    AnchorPane anchorNewsArea;
    @FXML
    VBox vBoxNewsArea;

    // Status Bar



    // Interface IBaseController methods

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void startMe() {
        stage.setTitle(CONSTANTS.APPLICATION_NAME);

        setupWidgetsAppearance();

        stage.show();

        anchorWorkArea.prefWidthProperty().bind(scrPaneWorkArea.widthProperty());
    }

    @Override
    public void closeMe() {
        stage.close();
    }

    // Private methods

    /**
     * Scrolls to the specified element index
     * 
     * @param elementIndex - index of element
     * @param vBoxArea - VBox area to scroll in
     * @param scrPaneArea - ScrollPane area where vBoxArea is
     */
    private void scrollToElement(int elementIndex, VBox vBoxArea, ScrollPane scrPaneArea) {
        if (elementIndex == 0 || vBoxArea.getChildren().size() >= elementIndex + 1) {
            Platform.runLater(() -> {
                scrPaneArea.setVvalue(1);
            });

            return;
        }
    }

    private void setupWidgetsAppearance() {
        scrPaneWorkArea.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY();
            double contentHeight = scrPaneWorkArea.getContent().getBoundsInLocal().getHeight();
            double scrollDelta = deltaY / contentHeight;

            scrPaneWorkArea.setVvalue(
                clamp(scrPaneWorkArea.getVvalue() - scrollDelta, 0, 1)
            );
            event.consume();
        });
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // FXML event handlers

    @FXML
    public void onMnuNew() {
        if (!OBJECTS.BLOCKS.isExists(1)) {
            BlockDiary blockDiary = new BlockDiary();
            blockDiary.add();
        }
        // System.out.println(blockDiary.getID());
        // System.out.println(blockDiary.getBaseBlockID());

        // Actor ac1 = new Actor();
        // ac1.setName("Danijel Nisevic");
        // ac1.setNick("Dado");
        // ac1.add();
        // Actor ac2 = new Actor();
        // ac2.setName("Danica Nisevic");
        // ac2.setNick("Mama");
        // ac2.add();
        // Actor ac3 = new Actor();
        // ac3.setName("Vojislav Nisevic");
        // ac3.setNick("Vox");
        // ac3.add();

        // return;



        int loadBlockDiaryID = 1;

        BlockDiary blockDiary = OBJECTS.BLOCKS_DIARY.getEntity(loadBlockDiaryID).duplicate();

        BlockGeneralController blockController = ELEMENTS.getBlockGeneralController(blockDiary.getBaseBlock(), stage);

        blockController.addToLayout(vBoxWorkArea);
        scrollToElement(vBoxWorkArea.getChildren().size() - 1, vBoxWorkArea, scrPaneWorkArea);
        
    }

    @FXML
    public void onMnuAddActor() {
        ActorEditController actorEditController = DIALOGS.getActorEditController(stage, null);
        actorEditController.startMe();
    }

    @FXML
    public void onScrWorkingAreaMouseEnter() {
        // Show vertical scroll bar
        scrPaneWorkArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    @FXML
    public void onScrWorkingAreaMouseExit() {
        // Hide vertical scroll bar
        scrPaneWorkArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

}
