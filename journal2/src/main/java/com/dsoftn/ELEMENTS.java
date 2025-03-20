package com.dsoftn;

import com.dsoftn.models.Block;
import com.dsoftn.services.SelectionData;
import com.dsoftn.controllers.elements.BlockGeneralController;
import com.dsoftn.controllers.elements.SelectionController;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.utils.UError;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ELEMENTS {

    public static BlockGeneralController getBlockGeneralController(Block block, Stage parentStage) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/BlockGeneral.fxml"));
        
        try {
            VBox root = loader.load();
            BlockGeneralController controller = loader.getController();
            controller.setRoot(root);
            controller.setBlock(block);
            controller.setStage(parentStage);
            controller.calculateData();
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getBlockGeneralController: Failed to load element", e);
            return null;
        }
    }

    public static SelectionController getSelectionController(ModelEnum baseModel, ModelEnum relatedModel, Stage parentStage, String receiverID) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/Selection.fxml"));
        
        try {
            VBox root = loader.load();
            SelectionController controller = loader.getController();
            controller.setRoot(root);
            controller.setStage(parentStage);
            controller.setData(new SelectionData(baseModel, relatedModel));
            controller.setReceiverID(receiverID);
            controller.calculateData();
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getSelectionController: Failed to load element", e);
            return null;
        }
    }


}
