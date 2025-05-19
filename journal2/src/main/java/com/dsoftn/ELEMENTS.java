package com.dsoftn;

import com.dsoftn.models.Block;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.SelectionData;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.controllers.elements.BlockGeneralController;
import com.dsoftn.controllers.elements.FormatCharController;
import com.dsoftn.controllers.elements.RTSettingsController;
import com.dsoftn.controllers.elements.SelectionController;
import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.controllers.elements.TextInputController;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.utils.UError;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
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

    public static TextInputController getTextInputController(Stage parentStage, TextHandler.Behavior behavior, String receiverID) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/TextInput.fxml"));

        try {
            VBox root = loader.load();
            TextInputController controller = loader.getController();
            controller.setRoot(root);
            controller.setStage(parentStage);
            controller.setBehavior(behavior);
            controller.setReceiverID(receiverID);
            controller.calculateData();
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getTextInputController: Failed to load element", e);
            return null;
        }
    }

    public static TextEditToolbarController getTextEditToolbarController(Stage parentStage) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/TextEditToolbar.fxml"));
        
        try {
            VBox root = loader.load();
            TextEditToolbarController controller = loader.getController();
            controller.setRoot(root);
            controller.setStage(parentStage);
            controller.calculateData();
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getTextEditToolbarController: Failed to load element", e);
            return null;
        }
    }

    public static RTSettingsController getRTSettingsController(TextHandler.Behavior behavior) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/RTSettings.fxml"));
        
        try {
            AnchorPane root = loader.load();
            RTSettingsController controller = loader.getController();
            controller.setAnchorPaneRoot(root);
            controller.setBehavior(behavior);
            controller.calculateData();
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getRTSettingsController: Failed to load element", e);
            return null;
        }
    }

    public static FormatCharController getFormatCharController(StyleSheetChar oldStyleSheet, StyleSheetChar newStyleSheet) {
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/FormatChar.fxml"));
        
        try {
            AnchorPane root = loader.load();
            FormatCharController controller = loader.getController();
            controller.setAnchorPaneRoot(root);
            controller.calculateData(oldStyleSheet, newStyleSheet);
            return controller;
        } catch (Exception e) {
            UError.exception("ELEMENTS.getFormatCharController: Failed to load element", e);
            return null;
        }
    }


}
