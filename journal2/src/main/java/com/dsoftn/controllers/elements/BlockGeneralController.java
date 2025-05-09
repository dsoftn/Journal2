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
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.MessageEvent;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.Actor;
import com.dsoftn.models.Block;
import com.dsoftn.services.RTWText;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.SelectionData;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.UDate;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UList;
import com.dsoftn.utils.UString;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private RTWidget rtwBlockName = new RTWidget(); // Rich text widget for block name
    private TextHandler textHandlerBlockName = null;

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
        private DatePicker dpDate;
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
        else if (event instanceof TaskStateEvent) {
            TaskStateEvent taskEvent = (TaskStateEvent) event;
            if (!taskEvent.getID().startsWith(this.myName)) { return; }

            if (taskEvent.getID().endsWith("BLOCK_NAME")) {
                // Change Block name
                if (taskEvent.getState() == TaskStateEnum.COMPLETED) {
                    if (taskEvent.getMessage().startsWith("SAVE:")) {
                        // Save block name
                        block.setName(taskEvent.getRTWText().getPlainText());
                        block.setNameStyle(taskEvent.getRTWText().getStyledText());
                        setBtnName(block);
                    }
                }
            }
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
        setupWidgetsAppearance();

        OBJECTS.EVENT_HANDLER.register(
            this,
            MessageEvent.RESULT_EVENT,
            TaskStateEvent.TASK_STATE_EVENT
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
        setDpDate(block);
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

        UJavaFX.setTooltip(btnName, OBJECTS.SETTINGS.getl("tt_BlockGeneral_btnName"));

        // Fix DatePicker Displaying dates
        dpDate.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return CONSTANTS.DATE_FORMATTER.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string, CONSTANTS.DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        UError.exception("BlockGeneralController.dpDate.fromString: Failed to parse date", e);
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });



    }

    private void setupWidgetsAppearance() {
        // Block name
        textHandlerBlockName = new TextHandler(rtwBlockName, null, TextHandler.Behavior.BLOCK_NAME_SHOW);
        rtwBlockName.setRTWTextObject(new RTWText(block.getNameStyle()));
        hBoxName.getChildren().add(rtwBlockName);
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

    private void setDpDate(Block block) {
        if (block.getDateOBJ() == null) {
            UJavaFX.setTooltip(dpDate, OBJECTS.SETTINGS.getl("tt_BlockGeneral_dpDateEmpty"));
        } else {
            UJavaFX.setTooltip(dpDate, UDate.getPeriodString(LocalDate.now(), block.getDateOBJ()), UDate.getDateWithWeekdayAndMonthName(block.getDateOBJ()), new Image(getClass().getResourceAsStream("/images/date.png")), 30, 30);
        }

        dpDate.setValue(block.getDateOBJ());
    }

    private void setBtnName(Block block) {
        if (block.getName() == null || block.getName().isEmpty()) {
            btnName.setText(OBJECTS.SETTINGS.getl("text_SetName"));
            hBoxName.setVisible(false);
            hBoxName.setManaged(false);
            return;
        }

        btnName.setText(OBJECTS.SETTINGS.getl("text_ChangeName"));
        hBoxName.setVisible(true);
        hBoxName.setManaged(true);
        rtwBlockName.setTextStyled(block.getNameStyle());
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
        selectActors();
    }

    private void selectActors() {
        if (readOnly) {
            showReadOnlyMessage();
            return;
        }

        OBJECTS.CLIP.setIDs(ModelEnum.ACTOR, 1);
        OBJECTS.CLIP.addIDs(ModelEnum.ACTOR, 2);

        System.out.println(OBJECTS.CLIP.getIDs(ModelEnum.ACTOR));

        EmptyDialogController emptyDialogController = DIALOGS.getEmptyDialogController_FRAMELESS(stage, WindowBehavior.ACTOR_SELECT_STANDARD);
        SelectionController selectionController = ELEMENTS.getSelectionController(ModelEnum.BLOCK, ModelEnum.ACTOR, stage, this.myName + "ACTORS");

        selectionController.setParentController(emptyDialogController);
        selectionController.setSelectedItems(block.getRelatedActorsIDs(), ModelEnum.ACTOR);
        emptyDialogController.setContent(selectionController.getRoot());
        emptyDialogController.setContentController(selectionController);

        emptyDialogController.startMe();
    }

    @FXML
    public void onBtnActorsClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            // Show Context Menu
            ContextMenu contextMenu = new ContextMenu();

            // Select actors
            MenuItem select = new MenuItem(OBJECTS.SETTINGS.getl("text_SelectActors"));
            Image imgSelect = new Image(getClass().getResourceAsStream("/images/select.png"));
            ImageView imgSelectView = new ImageView(imgSelect);
            imgSelectView.setPreserveRatio(true);
            imgSelectView.setFitHeight(20);
            select.setGraphic(imgSelectView);

            SeparatorMenuItem separator = new SeparatorMenuItem();
            
            // Copy added actors in block
            MenuItem copy = new MenuItem(OBJECTS.SETTINGS.getl("text_Copy"));
            Image imgCopy = new Image(getClass().getResourceAsStream("/images/copy.png"));
            ImageView imgCopyView = new ImageView(imgCopy);
            imgCopyView.setPreserveRatio(true);
            imgCopyView.setFitHeight(20);
            copy.setGraphic(imgCopyView);

            // Paste actors from clipboard in block
            MenuItem paste = new MenuItem(OBJECTS.SETTINGS.getl("text_Paste"));
            Image imgPaste = new Image(getClass().getResourceAsStream("/images/paste.png"));
            ImageView imgPasteView = new ImageView(imgPaste);
            imgPasteView.setPreserveRatio(true);
            imgPasteView.setFitHeight(20);
            paste.setGraphic(imgPasteView);

            contextMenu.getItems().addAll(select, separator, copy, paste);

            select.setOnAction(event1 -> {
                selectActors();
            });

            copy.setOnAction(event1 -> {
                OBJECTS.CLIP.setIDs(ModelEnum.ACTOR, block.getRelatedActorsIDs());
            });

            paste.setOnAction(event1 -> {
                List<Actor> actorIDs = new ArrayList<>();
                for (int actorID : OBJECTS.CLIP.getIDs(ModelEnum.ACTOR)) {
                    if (OBJECTS.ACTORS.isExists(actorID)) {
                        actorIDs.add(OBJECTS.ACTORS.getEntity(actorID));
                    }
                }
                block.setRelatedActors(actorIDs);
                setBtnActors(block);
            });

            // Disable unnecessary items
            if (block.getRelatedActorsIDs().size() == 0) {
                copy.setDisable(true);
            }
            if (OBJECTS.CLIP.getIDs(ModelEnum.ACTOR).size() == 0) {
                paste.setDisable(true);
            }

            contextMenu.show(btnActors, event.getScreenX(), event.getScreenY());
        }
    }

    @FXML
    public void onDpDateAction(ActionEvent event) {
        changeDate();
        setDpDate(block);
    }

    private void changeDate() {
        if (readOnly) {
            showReadOnlyMessage();
            return;
        }

        block.setDate(dpDate.getValue());
    }

    @FXML
    public void onBtnNameAction(ActionEvent event) {
        changeName();
    }

    private void changeName() {
        if (readOnly) {
            showReadOnlyMessage();
            return;
        }

        EmptyDialogController emptyDialogController = DIALOGS.getEmptyDialogController_FRAMELESS(stage, WindowBehavior.BLOCK_NAME_ENTER);
        TextInputController textInputController = ELEMENTS.getTextInputController(stage, TextHandler.Behavior.BLOCK_NAME_ENTER, myName);

        textInputController.setParentController(emptyDialogController);
        textInputController.setReceiverID(myName + "BLOCK_NAME");
        emptyDialogController.setContent(textInputController.getRoot());
        emptyDialogController.setContentController(textInputController);

        OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(textInputController.getMyName(), new RTWText(block.getNameStyle()), "SET TEXT"));

        emptyDialogController.startMe();
    }


}
