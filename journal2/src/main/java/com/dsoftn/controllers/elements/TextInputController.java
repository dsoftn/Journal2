package com.dsoftn.controllers.elements;

import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.TextHandler;
import com.dsoftn.utils.UJavaFX;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class TextInputController implements IElementController {

    public enum Behavior {
        BLOCK_NAME;
    }
    
    // Variables
    private Stage stage = null;
    private String myName = UJavaFX.getUniqueId();
    private String mySettingsName = "TextInput";
    private VBox root = null;
    private VBox vLayout = null;
    private String receiverID = null;
    private IBaseController parentController = null;
    private TextEditToolbarController toolbarController = null;
    private TextHandler textHandler = null;

    // FXML variables
    @FXML
    private HBox hBoxRichText; // Here goes rich text widget
        private RTWidget rTxtRichText = new RTWidget(); // Rich text widget

    @FXML
    private HBox hBoxControls;
        @FXML
        private Button btnOk;


    // Interface IElementController methods

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void setParentController(IBaseController parentController) {
        this.parentController = parentController;
    }

    @Override
    public IBaseController getParentController() {
        return parentController;
    }

    @Override
    public void setRoot(VBox root) {
        this.root = root;
    }

    @Override
    public void addToLayout(VBox layout) {
        layout.getChildren().add(root);
        vLayout = layout;
    }

    @Override
    public void addToLayout(VBox layout, int insertIntoIndex) {
        layout.getChildren().add(insertIntoIndex, root);
        vLayout = layout;
    }

    @Override
    public void removeFromLayout() {
        removeFromLayout(this.vLayout);
    }

    @Override
    public void removeFromLayout(VBox layout) {
        if (vLayout != null) {
            vLayout.getChildren().remove(root);
        }
    }

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public void beforeShowing() {
        rTxtRichText.requestFocus();
    }

    @Override
    public boolean canBeClosed() {
        if (toolbarController != null) {
            if (!toolbarController.canBeClosed()) {
                return false;
            }
        }   
        
        return true;
    }

    @Override
    public void calculateData() {
        // Add Rich text
        hBoxRichText.getChildren().add(rTxtRichText);
        HBox.setHgrow(rTxtRichText, Priority.ALWAYS);
        rTxtRichText.setMinTextWidgetHeight(OBJECTS.SETTINGS.getvINTEGER("BlockName_MinRichTextHeight"));

        // Create toolbar
        toolbarController = ELEMENTS.getTextEditToolbarController(stage);
        toolbarController.setReceiverID(myName);

        // Add Toolbar
        this.root.getChildren().add(0, toolbarController.getRoot());

        // Create TextHandler
        this.textHandler = new TextHandler(rTxtRichText, toolbarController);

        setupWidgetsText();
        setupWidgetsAppearance();
    }

    @Override
    public void closeMe() {
    }

    // Public methods

    public String getReceiverID() { return receiverID; }

    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public void setBehavior(Behavior behavior) {
        switch (behavior) {
            case BLOCK_NAME:
                StyleSheetChar css = new StyleSheetChar();
                css.setFontName(OBJECTS.SETTINGS.getvSTRING("BlockNameFontName"));
                css.setFontSize(OBJECTS.SETTINGS.getvINTEGER("BlockNameFontSize"));
                rTxtRichText.setCssChar(css);
                rTxtRichText.setMinTextWidgetHeight(OBJECTS.SETTINGS.getvINTEGER("BlockNameMinTextWidgetHeight"));
                break;
        }
    }

    // Private methods

    private void setupWidgetsText() {
        btnOk.setText(OBJECTS.SETTINGS.getl("text_Ok"));
    }

    private void setupWidgetsAppearance() {
    }



}
