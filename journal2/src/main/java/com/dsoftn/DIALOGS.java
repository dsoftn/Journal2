package com.dsoftn;

import java.util.function.Consumer;

import com.dsoftn.controllers.EmptyDialogController;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.SplashScreenController;
import com.dsoftn.controllers.elements.FormatCharController;
import com.dsoftn.controllers.elements.FormatParagraphController;
import com.dsoftn.controllers.elements.RTSettingsController;
import com.dsoftn.controllers.models.ActorEditController;
import com.dsoftn.models.Actor;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.controllers.EmptyDialogController.WindowBehavior;
import com.dsoftn.utils.UError;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DIALOGS {

    public static MsgBoxController getMsgBoxController(Stage parentStage) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/MsgBox.fxml"));
        
        Parent root;
        try {
            root = loader.load();
            MsgBoxController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getMsgBoxController: Failed to load dialog", e);
            return null;
        }
    }

    public static SplashScreenController getSplashScreenController(Stage parentStage) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/SplashScreen.fxml"));
        
        Parent root;
        try {
            root = loader.load();
            SplashScreenController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getSplashScreenController: Failed to load dialog", e);
            return null;
        }
    }

    public static EmptyDialogController getEmptyDialogController_FRAMELESS(Stage parentStage, WindowBehavior windowBehavior) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/EmptyDialog.fxml"));
        
        Parent root;
        try {
            root = loader.load();
            EmptyDialogController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            controller.setWindowBehavior(windowBehavior);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getEmptyDialogController: Failed to load dialog", e);
            return null;
        }
    }

    public static RTSettingsController getRTSettingsController(Stage parentStage, TextHandler.Behavior behavior, Consumer<Boolean> onExitCallback) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/RTSettings.fxml"));
        Parent root;
        try {
            root = loader.load();
            RTSettingsController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            controller.setOnExitCallback(onExitCallback);
            controller.setBehavior(behavior);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getRTSettingsController: Failed to load dialog", e);
            return null;
        }
    }

    public static FormatCharController getFormatCharController(Stage parentStage, TextHandler.Behavior behavior, StyleSheetChar oldStyleSheet, StyleSheetChar newStyleSheet, Consumer<StyleSheetChar> onExitCallback) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/FormatChar.fxml"));
        Parent root;
        try {
            root = loader.load();
            FormatCharController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            controller.setBehavior(behavior);
            controller.setOnExitCallback(onExitCallback);
            controller.setOldStyleSheet(oldStyleSheet);
            controller.setNewStyleSheet(newStyleSheet);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getFormatCharController: Failed to load dialog", e);
            return null;
        }
    }

    public static FormatParagraphController getFormatParagraphController(Stage parentStage, TextHandler.Behavior behavior, StyleSheetParagraph oldStyleSheet, StyleSheetParagraph newStyleSheet, StyleSheetChar defaultCharStyle, Consumer<StyleSheetParagraph> onExitCallback) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/FormatParagraph.fxml"));
        Parent root;
        try {
            root = loader.load();
            FormatParagraphController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            controller.setBehavior(behavior);
            controller.setOnExitCallback(onExitCallback);
            controller.setOldStyleSheet(oldStyleSheet);
            controller.setNewStyleSheet(newStyleSheet);
            controller.setDefaultCharStyle(defaultCharStyle);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getFormatParagraphController: Failed to load dialog", e);
            return null;
        }
    }

    public static ActorEditController getActorEditController(Stage parentStage, Actor actor) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(DIALOGS.class.getResource("/fxml/ActorEdit.fxml"));
        Parent root;
        try {
            root = loader.load();
            ActorEditController controller = loader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            if (parentStage != null) {
                stage.initOwner(parentStage);
            }
            controller.setStage(stage);
            controller.setActor(actor);
            return controller;
        } catch (Exception e) {
            UError.exception("DIALOGS.getActorEditController: Failed to load dialog", e);
            return null;
        }
    }


}
