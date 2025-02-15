package com.dsoftn.controllers.elements;

import com.dsoftn.CONSTANTS;
import com.dsoftn.DIALOGS;
import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.controllers.EmptyDialogController;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    private boolean showRestore = false;
    private boolean showClose = true;
    private boolean showOptions = true;
    private boolean showReadOnly = true;
    private boolean readOnly = false;

    // FXML variables

    @FXML
    private VBox vBoxRoot;

    @FXML
    private HBox hBoxTitle;
        @FXML
        private ImageView imgReadOnly;
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
        setupWidgetsText();

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
            showMinimize = false;
            showRestore = true;
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
            minimized = false;
            showMinimize = true;
            showRestore = false;
        }

        adjustWidgets(block);
    }

    public boolean isMinimized() {
        return this.minimized;
    }

    public void canShowMinimizeAndRestore(boolean show) {
        this.showMinimize = show;
        showNode(btnMinimize, show);
        this.showRestore = show;
        showNode(btnRestore, show);
    }

    public void canShowClose(boolean show) {
        this.showClose = show;
        showNode(btnClose, show);
    }

    public void canShowOptions(boolean show) {
        this.showOptions = show;
        showNode(btnOptions, show);
    }

    public void canShowReadOnly(boolean show) {
        this.showReadOnly = show;
        showNode(imgReadOnly, show);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        setImgReadOnly();
    }

    public boolean isReadOnly() { return readOnly; }

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
        // Read only
        setImgReadOnly();
        // ID
        setLblID(block);
        // Actors
        setBtnActors(block);
        // Block Type
        setBtnType(block);
        // Date
        setBtnDate(block);
        // Name
        setBtnName(block);
    }

    // Private methods

    private void setupWidgetsText() {
        UJavaFX.setTooltip(btnActors, OBJECTS.SETTINGS.getl("tt_BlockGeneral_btnActors"));
        
        String bType = OBJECTS.SETTINGS.isLanguageKeyExists(block.getBlockType().toString()) ? OBJECTS.SETTINGS.getl(block.getBlockType().toString()) : block.getBlockType().toString();
        UJavaFX.setTooltip(
            btnType,
            OBJECTS.SETTINGS.getl("tt_BlockGeneral_btnType_Text"),
            OBJECTS.SETTINGS.getl("tt_BlockGeneral_btnType_Title").replace("#1", bType),
            new Image(getClass().getResourceAsStream("/images/block_type_general.png")),
            null,null
        );

        UJavaFX.setTooltip(btnDate, OBJECTS.SETTINGS.getl("tt_BlockGeneral_btnDate"));

        UJavaFX.setTooltip(btnName, OBJECTS.SETTINGS.getl("tt_BlockGeneral_btnName"));


    }

    private void adjustWidgets(Block block) {
        // Block Title
        if (block.getName() == null || block.getName().isEmpty()) {
            showNode(hBoxName, false);
        }
        else {
            showNode(hBoxName, true);
        }
        
        // Related attachments
        if (block.getRelatedAttachmentsIDs().size() > 0) {
            showNode(hBoxAttachments, true);
        }
        else {
            showNode(hBoxAttachments, false);
        }

        // Related blocks
        if (block.getRelatedBlocksIDs().size() > 0) {
            showNode(hBoxBlocks, true);
        }
        else {
            showNode(hBoxBlocks, false);
        }

        // Button minimize
        showNode(btnMinimize, showMinimize);
        // Button restore
        showNode(btnRestore, showRestore);
        // Button close
        showNode(btnClose, showClose);
        // Button options
        showNode(btnOptions, showOptions);
        // ImageView read only
        showNode(imgReadOnly, showReadOnly);
    }

    private void showNode(Node node, boolean show) {
        node.setVisible(show);
        node.setManaged(show);
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


    private void setImgReadOnly() {
        if (readOnly) {
            Image imgRO = new Image(getClass().getResourceAsStream("/images/read_only.png"));
            imgReadOnly.setImage(imgRO);
            UJavaFX.setTooltip(imgReadOnly, OBJECTS.SETTINGS.getl("tt_BlockGeneral_imgReadOnly_True"));
            }
        else {
            Image imgRO = new Image(getClass().getResourceAsStream("/images/write.png"));
            imgReadOnly.setImage(imgRO);
            UJavaFX.setTooltip(imgReadOnly, OBJECTS.SETTINGS.getl("tt_BlockGeneral_imgReadOnly_False"));
        }
    }

    private void setLblID(Block block) {
        if (block.getID() != CONSTANTS.INVALID_ID) {
            lblID.setText("ID: " + block.getID());
            UJavaFX.setTooltip(
                lblID,
                OBJECTS.SETTINGS.getl("tt_BlockGeneral_lblID_Valid"),
                "ID: " + block.getID(),
                new Image(getClass().getResourceAsStream("/images/id.png")),
                70,70);
        }
        else {
            lblID.setText("ID: ?");
            UJavaFX.setTooltip(
                lblID,
                OBJECTS.SETTINGS.getl("tt_BlockGeneral_lblID_Invalid"),
                "ID: " + block.getID(),
                new Image(getClass().getResourceAsStream("/images/error.png")),
                70,70);
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
        String name = block.getBlockType().toString();
        if (OBJECTS.SETTINGS.isLanguageKeyExists(name)) {
            name = OBJECTS.SETTINGS.getl(name);
        }

        btnType.setText(name);
        Image image = new Image(block.getBlockType().getImagePath());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(iconSize);
        btnType.setGraphic(imageView);
    }

    private void setBtnDate(Block block) {
        btnDate.setText(block.getDateSTR());
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

    private void showReadOnlyMessage() {
        MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
        msgBoxController.setTitleText(OBJECTS.SETTINGS.getl("text_ReadOnly"));
        msgBoxController.setHeaderText(OBJECTS.SETTINGS.getl("MsgBox_BlockReadOnly_Header"));
        msgBoxController.setHeaderIcon(MsgBoxIcon.READ_ONLY);
        msgBoxController.setContentText(OBJECTS.SETTINGS.getl("MsgBox_BlockReadOnly_Content"));
        msgBoxController.setContentIcon(MsgBoxIcon.BLOCK_GENERIC);
        msgBoxController.setButtons(MsgBoxButton.OK);
        msgBoxController.setDefaultButton(MsgBoxButton.OK);
        msgBoxController.startMe();
    }

    // FXML Events

    @FXML
    public void onBtnCloseAction(ActionEvent event) {
        closeMe();
    }

    @FXML
    public void onBtnRestoreAction(ActionEvent event) {
        minimize(false);
    }

    @FXML
    public void onBtnMinimizeAction(ActionEvent event) {
        minimize(true);
    }

    @FXML
    public void onImgReadOnlyMouseClicked(MouseEvent event) {
        setReadOnly(!readOnly);
    }

    @FXML
    public void onBtnActorsAction(ActionEvent event) {
        if (readOnly) {
            showReadOnlyMessage();
            return;
        }

        EmptyDialogController emptyDialogController = DIALOGS.getEmptyDialogController_FRAMELESS(stage, WindowBehavior.ACTOR_SELECT_STANDARD);
        SelectionController selectionController = ELEMENTS.getSelectionController(ModelEnum.BLOCK, ModelEnum.ACTOR, stage, this.myName + "ACTORS");

        selectionController.disableSections(SelectionController.Section.CLIPBOARD);
        selectionController.setParentController(emptyDialogController);
        selectionController.setSelectedItems(block.getRelatedActorsIDs(), ModelEnum.ACTOR);
        emptyDialogController.setContent(selectionController.getRoot());
        emptyDialogController.setContentController(selectionController);

        emptyDialogController.startMe();
    }

}
