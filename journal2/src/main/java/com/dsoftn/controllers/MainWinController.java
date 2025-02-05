package com.dsoftn.controllers;

import com.dsoftn.CONSTANTS;
import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.controllers.elements.BlockGeneralController;
import com.dsoftn.models.Block;
import com.dsoftn.models.block_types.BlockDiary;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWinController implements IBaseController {

    // Variables
    private Stage stage;

    // FXML Variables
    @FXML
    MenuItem mnuNew;
    @FXML
    VBox vBoxWorkArea;


    // Interface IBaseController methods

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void startMe() {
        stage.setTitle(CONSTANTS.APPLICATION_NAME);
        
        stage.show();
    }

    @Override
    public void closeMe() {
        stage.close();
    }

    @FXML
    public void onMnuNew() {
        BlockDiary blockDiary = new BlockDiary();
        blockDiary.saveBlockAndBase();

        BlockGeneralController block = ELEMENTS.getBlockGeneralController();

        vBoxWorkArea.getChildren().add(block.getRoot());
    }

}
