package com.dsoftn.controllers.elements;

import com.dsoftn.CONSTANTS;
import com.dsoftn.DIALOGS;
import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.controllers.EmptyDialogController;
import com.dsoftn.controllers.EmptyDialogController.WindowBehavior;
import com.dsoftn.enums.models.BlockTypeEnum;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.events.MessageEvent;
import com.dsoftn.models.Actor;
import com.dsoftn.models.Block;
import com.dsoftn.services.SelectionData;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UList;
import com.dsoftn.utils.UString;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;

import org.fxmisc.richtext.InlineCssTextArea;


public class BlockGeneralController implements IBaseController, IElementController, ICustomEventListener {

    // Variables
    private String myName = UJavaFX.getUniqueId();
    private Stage stage = null;
    private IBaseController parentController = null;
    private VBox root = null;
    private Block block = null;

    private VBox vLayout = null;
    private int iconSize = 20;
    private boolean minimized = false;
    private boolean showMinimize = true;
    private  boolean showRestore = false;
    private boolean showClose = true;
    private boolean editable = true;

    // FXML variables

    @FXML
    private VBox vBoxRoot;

    @FXML
    private HBox hBoxTitle;
        @FXML
        private Label lblID;
        @FXML
        private Button btnActors;
        @FXML
        private Button btnType;
        @FXML
        private Button btnDate;
        @FXML
        private Button btnName;
        @FXML
        private HBox hBoxMessages; // Messages goes here
        @FXML
        private Button btnOptions;
        @FXML
        private Button btnMinimize;
        @FXML
        private Button btnRestore;
        @FXML
        private Button btnClose;

    @FXML
    private HBox hBoxHeader;
        @FXML
        private Button btnAddTag;
        @FXML
        private Button btnCategory;

    @FXML
    private HBox hBoxName;
        @FXML
        private Label lblName; // Block name in center

    @FXML
    private HBox hBoxContent; // Here goes block content

    @FXML
    private HBox hBoxAttachments;
        @FXML
        private HBox hBoxAttachmentsList; // Put here attachments, make sure that last entry is vBoxAttachmentsControl
            @FXML
            private VBox vBoxAttachmentsControl;
                @FXML
                private Button btnAddAttachment;
    
    @FXML
    private HBox hBoxBlocks;
        @FXML
        private HBox hBoxBlocksList; // Put here blocks, make sure that last entry is vBoxBlocksControl
            @FXML
            private VBox vBoxBlockControl;
                @FXML
                private Button btnAddBlock;

    @FXML
    private HBox hBoxFooter;
        @FXML
        private Button btnSave; // Save as completed block
        @FXML
        private Button btnSaveAsDraft; // Save as draft block
        @FXML
        private Button btnDelete;
        @FXML
        private Button btnRelateAttachment;
        @FXML
        private Button btnRelateBlock;
        @FXML
        private Button btnAddNewBlock;
    

    // Constructor
    public BlockGeneralController() {}

    public BlockGeneralController(Block block) {
        setBlock(block);
    }

    public void initialize() {
        // Setup icons
        ImageView imgOptions = new ImageView(new Image(getClass().getResourceAsStream("/images/options_bw.png")));
        imgOptions.setPreserveRatio(true);
        imgOptions.setFitHeight(iconSize);
        ImageView imgMinimize = new ImageView(new Image(getClass().getResourceAsStream("/images/minimize_bw.png")));
        imgMinimize.setPreserveRatio(true);
        imgMinimize.setFitHeight(iconSize);
        ImageView imgRestore = new ImageView(new Image(getClass().getResourceAsStream("/images/restore_bw.png")));
        imgRestore.setPreserveRatio(true);
        imgRestore.setFitHeight(iconSize);
        ImageView imgClose = new ImageView(new Image(getClass().getResourceAsStream("/images/close.png")));
        imgClose.setPreserveRatio(true);
        imgClose.setFitHeight(iconSize);
        ImageView imgAddAttachment = new ImageView(new Image(getClass().getResourceAsStream("/images/add_bw.png")));
        imgAddAttachment.setPreserveRatio(true);
        imgAddAttachment.setFitHeight(iconSize);
        ImageView imgAddBlock = new ImageView(new Image(getClass().getResourceAsStream("/images/add_bw.png")));
        imgAddBlock.setPreserveRatio(true);
        imgAddBlock.setFitHeight(iconSize);

        btnOptions.setGraphic(imgOptions);
        btnMinimize.setGraphic(imgMinimize);
        btnRestore.setGraphic(imgRestore);
        btnClose.setGraphic(imgClose);
        btnAddAttachment.setGraphic(imgAddAttachment);
        btnAddBlock.setGraphic(imgAddBlock);

    }

    // Interface ICustomEventListener methods

    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof MessageEvent) {
            MessageEvent messageEvent = (MessageEvent) event;
            eventMessage(messageEvent);
        }
    }

    private void eventMessage(MessageEvent messageEvent) {
        if (!messageEvent.getReceiverID().startsWith(myName)) {
            return;
        }

        if (messageEvent.getReceiverID().endsWith("ACTORS")) {
            block.setRelatedActors(
                OBJECTS.ACTORS.getActorsListFromIDs(
                UList.listStringToInteger(
                UString.splitAndStrip(messageEvent.getMessageSTRING(), ","))));

            setBtnActors(block);
            return;
        }
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
        return;
    }

    @Override
    public void closeMe() {
        if (stage != null) {
            stage.close();
        }

        removeFromLayout();
    }

    // Interface IElementController methods

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void setRoot(VBox root) {
        VBox.setVgrow(root, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(vBoxRoot, javafx.scene.layout.Priority.ALWAYS);
        this.root = root;
    }

    @Override
    public void setParentController(IBaseController parentController) { this.parentController = parentController; }

    @Override
    public IBaseController getParentController() { return parentController; }

    @Override
    public void addToLayout(VBox layout) {
        addToLayout(layout, layout.getChildren().size());
    }

    @Override
    public void addToLayout(VBox layout, int insertIntoIndex) {
        layout.getChildren().add(insertIntoIndex, vBoxRoot);

        if (OBJECTS.SETTINGS.getvBOOLEAN("AllowAnimateAddingBlockGeneral")) {
            animateAddingToLayout();
        }

        vLayout = layout;
    }

    @Override
    public void removeFromLayout() {
        removeFromLayout(this.vLayout);
    }

    @Override
    public void removeFromLayout(VBox layout) {
        startAnimatedRemovingFromLayout(layout);
    }

    @Override
    public void calculateData() {
        OBJECTS.EVENT_HANDLER.register(
            this,
            MessageEvent.RESULT_EVENT
            );
    }

    // Public methods

    public void minimize(boolean minimize) {
        if (minimize) {
            vBoxRoot.getChildren().clear();
            vBoxRoot.getChildren().add(hBoxTitle);
            minimized = true;
        }
        else {
            vBoxRoot.getChildren().clear();
            vBoxRoot.getChildren().add(hBoxTitle);
            vBoxRoot.getChildren().add(hBoxHeader);
            vBoxRoot.getChildren().add(hBoxName);
            vBoxRoot.getChildren().add(hBoxContent);
            vBoxRoot.getChildren().add(hBoxAttachments);
            vBoxRoot.getChildren().add(hBoxBlocks);
            vBoxRoot.getChildren().add(hBoxFooter);
            adjustWidgets(block);
            minimized = false;
        }
    }

    public boolean isMinimized() {
        return this.minimized;
    }

    public void canShowMinimize(boolean show) {
        this.showMinimize = show;
    }

    public void canShowRestore(boolean show) {
        this.showRestore = show;
    }

    public void canShowClose(boolean show) {
        this.showClose = show;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setBlock(Block block) {
        this.block = block;

        refresh();
    }

    public void refresh() {
        refresh(this.block);
    }

    public void refresh(Block block) {
        if (block == null) {
            return;
        }

        // Show all sections that need to be visible
        adjustWidgets(block);
        // Set background
        setBackground(block);
        // ID
        setLblID(block);
        // Actors
        setBtnActors(block);
        // Block Type
        setBtnType(block);
        // Date
        btnDate.setText(block.getDateSTR());
        // Name
        setBtnName(block);
    }

    // Private methods

    private void adjustWidgets(Block block) {
        // Related attachments
        if (block.getRelatedAttachmentsIDs().size() > 0) {
            hBoxAttachments.setVisible(true);
            hBoxAttachments.setManaged(true);
        }
        else {
            hBoxAttachments.setVisible(false);
            hBoxAttachments.setManaged(false);
        }

        // Related blocks
        if (block.getRelatedBlocksIDs().size() > 0) {
            hBoxBlocks.setVisible(true);
            hBoxBlocks.setManaged(true);
        }
        else {
            hBoxBlocks.setVisible(false);
            hBoxBlocks.setManaged(false);
        }

        // Button minimize
        btnMinimize.setVisible(showMinimize);
        btnMinimize.setManaged(showMinimize);
        // Button restore
        btnRestore.setVisible(showRestore);
        btnRestore.setManaged(showRestore);
        // Button close
        btnClose.setVisible(showClose);
        btnClose.setManaged(showClose);
    }

    private void setBackground(Block block) {
        String color = "CssBlockTypeBackground" + block.getBlockType().toString();
        if (OBJECTS.SETTINGS.isUserSettingExists(color)) {
            String bgStyle = OBJECTS.SETTINGS.getvSTRING(color);
            if (bgStyle.isEmpty()) {
                vBoxRoot.setStyle("-fx-background-color: transparent;");
            }
            else {
                List<String> bgStyleList = UString.splitAndStrip(bgStyle, "\n");
                for (String style : bgStyleList) {
                    vBoxRoot.setStyle(style);
                }
            }
        }
        else {
            vBoxRoot.setStyle("-fx-background-color: red;");
        }
    }

    private void setLblID(Block block) {
        if (block.getID() != CONSTANTS.INVALID_ID) {
            lblID.setText("ID: " + block.getID());
        }
        else {
            lblID.setText("ID: ?");
        }
    }

    private void setBtnActors(Block block) {
        String actors = "";
        actors = block.getRelatedActors().stream().map(Actor::getNick).collect(Collectors.joining(", "));

        if (actors.length() == 0) {
            Image image = new Image(getClass().getResourceAsStream("/images/actor_none.png"));
            btnActors.setText(OBJECTS.SETTINGS.getl("text_Actors"));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(iconSize);
            btnActors.setGraphic(imageView);
            return;
        }

        if (block.getRelatedActorsIDs().size() == 1) {
            Actor actor = block.getRelatedActors().get(0);

            btnActors.setText(actor.getNick());

            btnActors.setGraphic(actor.getImageAny(iconSize, iconSize));
            
            return;
        }

        btnActors.setText(actors);
        btnActors.setGraphic(null);
    }

    private void setBtnType(Block block) {
        btnType.setText(block.getBlockType().toString());
        Image image = new Image(block.getBlockType().getImagePath());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(iconSize);
        btnType.setGraphic(imageView);
    }

    private void setBtnName(Block block) {
        if (block.getName() == null || block.getName().isEmpty()) {
            btnName.setText(OBJECTS.SETTINGS.getl("text_SetName"));
            return;
        }

        btnName.setText(OBJECTS.SETTINGS.getl("text_ChangeName"));
    }


    private void animateAddingToLayout() {
        double duration = OBJECTS.SETTINGS.getvDOUBLE("AnimateAddingBlockGeneralDuration");

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(vBoxRoot.opacityProperty(), 0.01)
            ),
            new KeyFrame(Duration.seconds(duration), 
                new javafx.animation.KeyValue(vBoxRoot.opacityProperty(), 1)
            )
        );
    
        timeline.setCycleCount(1);

        timeline.play();
    }

    private void startAnimatedRemovingFromLayout(VBox vLayout) {
        if (OBJECTS.SETTINGS.getvBOOLEAN("AllowAnimateRemovingBlockGeneral") && vLayout != null) {
            animateRemovingFromLayout();
        }
        else {
            finishRemovingBlockFromLayout(vLayout);
        }
    }

    private void animateRemovingFromLayout() {
        double duration = OBJECTS.SETTINGS.getvDOUBLE("AnimateRemovingBlockGeneralDuration");

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(vBoxRoot.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.seconds(duration), 
                new javafx.animation.KeyValue(vBoxRoot.opacityProperty(), .01)
            )
        );
    
        timeline.setCycleCount(1);

        timeline.setOnFinished(event -> finishRemovingBlockFromLayout());

        timeline.play();
    }

    private void finishRemovingBlockFromLayout() {
        finishRemovingBlockFromLayout(this.vLayout);
    }
    
    private void finishRemovingBlockFromLayout(VBox vLayout) {
        if (vLayout != null) {
            vLayout.getChildren().remove(vBoxRoot);
        }
    }


    // FXML Events

    public void onBtnCloseAction(ActionEvent event) {
        closeMe();
    }

    public void onBtnRestoreAction(ActionEvent event) {
        minimize(false);
    }

    public void onBtnMinimizeAction(ActionEvent event) {
        minimize(true);
    }

    public void onBtnActorsAction(ActionEvent event) {
        EmptyDialogController emptyDialogController = DIALOGS.getEmptyDialogController_FRAMELESS(stage, WindowBehavior.ACTOR_SELECT_STANDARD);
        SelectionController selectionController = ELEMENTS.getSelectionController(ModelEnum.BLOCK, ModelEnum.ACTOR, stage, this.myName + "ACTORS");

        selectionController.disableSections(SelectionController.Section.CLIPBOARD);
        selectionController.setParentController(emptyDialogController);
        selectionController.setSelectedItems(block.getRelatedActorsIDs(), ModelEnum.ACTOR);
        emptyDialogController.setContent(selectionController.getRoot());

        emptyDialogController.startMe();
    }



}
