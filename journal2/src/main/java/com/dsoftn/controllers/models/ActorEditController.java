package com.dsoftn.controllers.models;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.DIALOGS;
import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.models.Actor;
import com.dsoftn.services.MoveResizeWindow;
import com.dsoftn.services.RTWText;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ActorEditController implements IBaseController, ICustomEventListener {
    // Variables
    private String myName = UJavaFX.getUniqueId();
    private String settingsName = "ActorEdit";
    private Stage stage = null;
    private Actor actor = null;
    private RTWidget rtwDescription = new RTWidget();
    private TextHandler textHandler = null;

    // FXML variables
    // Title
    @FXML
    private ImageView imgActor;
    @FXML
    private Label lblTitle;
    @FXML
    private ImageView imgNew;
    @FXML
    private Button btnClose;
    // Actor info - name
    @FXML
    private Label lblName;
    @FXML
    private TextField txtName;
    // Actor info - nick
    @FXML
    private Label lblNick;
    @FXML
    private TextField txtNick;
    // Info box
    @FXML
    private HBox hBoxInfo;
    @FXML
    private Label lblInfo;
    // Description
    @FXML
    private Label lblDescription;
    @FXML
    private HBox hBoxToolbarPlaceholder;
    @FXML
    private VBox vBoxRTWidgetPlaceholder;
    // Attachments
    @FXML
    private Label lblAttachments;
    @FXML
    private FlowPane flowAttachments;
    // Default actor
    @FXML
    private CheckBox chkDefault;
    // Buttons
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnCancel;

    // Interface ICustomEventListener methods

    @Override
    public void onCustomEvent(Event event) {
        
    }

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
        setupData(actor);
        setupWidgetsText();
        setupWidgetsAppearance();

        // Set Window as undecorated
        stage.initStyle(StageStyle.UNDECORATED);

        // Make window draggable and resizable
        List<Node> dragNodes = new ArrayList<>();
        dragNodes.add(lblTitle);
        dragNodes.add(imgActor);
        dragNodes.add(imgNew);
        new MoveResizeWindow(stage, dragNodes);

        // Handle font size
        UJavaFX.handleNodeFontSizeChange(txtName, settingsName + "_FontSize", "txtName", 16);
        UJavaFX.handleNodeFontSizeChange(txtNick, settingsName + "_FontSize", "txtNick", 16);
        UJavaFX.handleNodeFontSizeChange(chkDefault, settingsName + "_FontSize", "chkDefault", 14);

        // Close on ESC 
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (textHandler.canBeClosed()) {
                    onCloseDialog(event);
                } else {
                    event.consume();
                }
            }
        });

        // Load stage geometry
        UJavaFX.setStageGeometry(settingsName, stage);
        
        stage.show();
    }

    @Override
    public void closeMe() {
        UJavaFX.saveStageGeometry(settingsName, stage);
        stage.close();
    }

    // Public methods

    public void setActor(Actor actor) {
        if (actor == null) {
            this.actor = null;
            return;
        }

        this.actor = actor.duplicate();
    }

    // Private methods

    private void setupWidgetsText() {
        if (actor == null) {
            lblTitle.setText(OBJECTS.SETTINGS.getl("ActorAdd_Title"));
        } else {
            lblTitle.setText(OBJECTS.SETTINGS.getl("ActorEdit_Title").replace("#1", String.valueOf(actor.getID())));
        }
        
        lblName.setText(OBJECTS.SETTINGS.getl("text_Name"));
        lblNick.setText(OBJECTS.SETTINGS.getl("text_Nick"));
        lblInfo.setText("");
        lblDescription.setText(OBJECTS.SETTINGS.getl("text_Description"));
        lblAttachments.setText(OBJECTS.SETTINGS.getl("text_Attachments"));
        chkDefault.setText(OBJECTS.SETTINGS.getl("text_MakeThisAsDefaultActorForNewBlocks"));
        btnUpdate.setText(OBJECTS.SETTINGS.getl("text_Update"));
        btnAdd.setText(OBJECTS.SETTINGS.getl("text_Add"));
        btnCancel.setText(OBJECTS.SETTINGS.getl("text_Cancel"));
    }

    private void setupWidgetsAppearance() {
        if (actor == null) {
            // Disable update button
            btnUpdate.setVisible(false);
            btnUpdate.setManaged(false);
            // Disable actor image
            imgActor.setVisible(false);
            imgActor.setManaged(false);
            // Add image for new actor
            imgNew.setImage(new Image(getClass().getResourceAsStream("/images/new.png")));
        } else {
            // Disable add button
            btnAdd.setVisible(false);
            btnAdd.setManaged(false);
            // Disable new image
            imgNew.setVisible(false);
            imgNew.setManaged(false);
            // Set actor image
            ImageView actorImage = actor.getImageAny(40, 40);
            imgActor.setImage(actorImage.getImage());
        }

        // Add image to close button
        Image imgClose = new Image(getClass().getResourceAsStream("/images/close.png"));
        ImageView imgCloseView = new ImageView(imgClose);
        imgCloseView.setPreserveRatio(true);
        imgCloseView.setFitHeight(30);
        btnClose.setGraphic(imgCloseView);

        // Hide info box
        hBoxInfo.setVisible(false);
        hBoxInfo.setManaged(false);

        // Add Toolbar
        TextEditToolbarController toolbarController = ELEMENTS.getTextEditToolbarController(stage);
        toolbarController.setReceiverID(myName);
        hBoxToolbarPlaceholder.getChildren().add(toolbarController.getRoot());

        // Add RTWidget
        vBoxRTWidgetPlaceholder.getChildren().add(rtwDescription);

        // Create TextHandler
        textHandler = new TextHandler(rtwDescription, toolbarController, TextHandler.Behavior.ACTOR_DESCRIPTION, stage);
        textHandler.setReceiverID(myName);
    }

    private void setupData(Actor actor) {
        if (actor == null) {
            return;
        }

        txtName.setText(actor.getName());
        txtNick.setText(actor.getNick());
        rtwDescription.setRTWTextObject(new RTWText(actor.getDescriptionStyled()));
        chkDefault.setSelected(actor.isDefaultActor());
    }

    private boolean msgConfirmClose() {
        MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
        msgBoxController.setTitleText(OBJECTS.SETTINGS.getl("text_Close"));
        msgBoxController.setHeaderText(OBJECTS.SETTINGS.getl("text_DataNotSaved"));
        msgBoxController.setHeaderIcon(MsgBoxIcon.WARNING);
        msgBoxController.setContentText(OBJECTS.SETTINGS.getl("ActorClose_DataNotSavedWarning"));
        msgBoxController.setContentIcon(MsgBoxIcon.SAVE);
        msgBoxController.setButtons(MsgBoxButton.YES, MsgBoxButton.NO, MsgBoxButton.CANCEL);
        msgBoxController.setDefaultButton(MsgBoxButton.NO);
        msgBoxController.startMe();

        if (msgBoxController.getSelectedButton() == MsgBoxButton.YES) {
            if (!isValid()) {
                msgDataNotValid();
                return false;
            } else {
                saveData();
                return true;
            }
        } else if (msgBoxController.getSelectedButton() == MsgBoxButton.NO) {
            return true;
        } else {
            return false;
        }
    }

    private String invalidDataText() {
        if (txtName.getText().isEmpty()) {
            return OBJECTS.SETTINGS.getl("ActorEdit_NameCannotBeEmpty");
        }

        if (actor != null) {
            List<Actor> actors = OBJECTS.ACTORS.getEntityAll();
            for (int i = 0; i < actors.size(); i++) {
                Actor otherActor = actors.get(i);
                if (otherActor.getNick().equals(txtNick.getText()) && otherActor.getName().equals(txtName.getText())) {
                    return OBJECTS.SETTINGS.getl("ActorEdit_NickAndNameAlreadyExists");
                }
            }
        }

        return "";
    }
    
    private void msgDataNotValid() {
        MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
        msgBoxController.setTitleText(OBJECTS.SETTINGS.getl("text_InvalidData"));
        msgBoxController.setHeaderText(OBJECTS.SETTINGS.getl("text_InvalidData"));
        msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
        msgBoxController.setContentText(invalidDataText());
        msgBoxController.setContentIcon(MsgBoxIcon.INVALID_DATA);
        msgBoxController.setButtons(MsgBoxButton.OK);
        msgBoxController.setDefaultButton(MsgBoxButton.OK);
        msgBoxController.startMe();
    }

    private boolean isValid() {
        return invalidDataText().isEmpty();
    }

    private boolean isChanged() {
        if (actor == null) {
            return !txtName.getText().isEmpty() || !txtNick.getText().isEmpty() || !rtwDescription.getTextPlain().isEmpty();
        }

        return !actor.getName().equals(txtName.getText()) ||
                !actor.getNick().equals(txtNick.getText()) ||
                !actor.getDescriptionStyled().equals(rtwDescription.getRTWTextObject().getStyledText()) ||
                actor.isDefaultActor() != chkDefault.isSelected();
    }

    private boolean saveData() {
        if (!isValid()) {
            msgDataNotValid();
            return false;
        }

        if (actor == null) {
            actor = new Actor();
            actor.setNick(txtNick.getText());
            actor.setName(txtName.getText());
            actor.setDescriptionStyled(rtwDescription.getRTWTextObject().getStyledText());
            actor.setDefaultActor(chkDefault.isSelected());

            if (!actor.add()) {
                UError.error("ActorEditController.saveData: Failed to add actor", "Adding actor failed");
                return false;
            }
        } else {
            actor.setNick(txtNick.getText());
            actor.setName(txtName.getText());
            actor.setDescriptionStyled(rtwDescription.getRTWTextObject().getStyledText());
            actor.setDefaultActor(chkDefault.isSelected());

            if (!actor.update()) {
                UError.error("ActorEditController.saveData: Failed to update actor", "Updating actor failed");
                return false;
            }
        }

        return true;
    }

    // FXML methods
    @FXML
    private void onBtnCloseAction() {
        onCloseDialog(null);
    }

    private void onCloseDialog(Event event) {
        if (isChanged()) {
            if (!msgConfirmClose()) {
                return;
            } else {
                if (event != null) { event.consume(); }
                closeMe();
            }
        }

        if (event != null) { event.consume(); }
        closeMe();
    }

    @FXML
    private void onBtnUpdateAction() {
        if (!saveData()) {
            return;
        }

        closeMe();
    }

    @FXML
    private void onBtnAddAction() {
        if (!saveData()) {
            return;
        }

        closeMe();
    }

    @FXML
    private void onBtnCancelAction() {
        onCloseDialog(null);
    }


}
