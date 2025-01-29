package com.dsoftn.controllers;

import com.dsoftn.CONSTANTS;
import com.dsoftn.Interfaces.IBaseController;

import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;


public class BlockGeneralController implements IBaseController {

        // Variables
    private Stage stage;


    // Interface IBaseController methods

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void startMe() {
        // Set 
        
        stage.show();
    }

    @Override
    public void closeMe() {
        stage.close();
    }


}
