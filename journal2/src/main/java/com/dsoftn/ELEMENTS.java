package com.dsoftn;

import com.dsoftn.models.Block;
import com.dsoftn.controllers.elements.BlockGeneralController;
import com.dsoftn.controllers.elements.SelectionController;
import com.dsoftn.utils.UError;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;


public class ELEMENTS {

    public static BlockGeneralController getBlockGeneralController(Block block) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/BlockGeneral.fxml"));
        
        try {
            VBox root = loader.load();
            BlockGeneralController controller = loader.getController();
            controller.setRoot(root);
            controller.setBlock(block);
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getBlockGeneralController: Failed to load element", e);
            return null;
        }
    }

    public static SelectionController getSelectionController() {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/Selection.fxml"));
        
        try {
            VBox root = loader.load();
            SelectionController controller = loader.getController();
            controller.setRoot(root);
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getSelectionController: Failed to load element", e);
            return null;
        }
    }


}
