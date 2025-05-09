package com.dsoftn.services.text_handler;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.controllers.elements.TextEditToolbarController.AlignmentEnum;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.RTWidget;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class TextHandler {
    public enum Behavior {
        BLOCK_NAME_ENTER(1),
        BLOCK_NAME_SHOW(2);

        // Variables
        private final int value;

        // Constructor
        Behavior(int value) {
            this.value = value;
        }

        // Methods
        public int getValue() {
            return value;
        }

        public int getInteger() {
            return value;
        }

        public static Behavior fromInteger(int value) {
            for (Behavior type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return null;
        }

        public static Behavior fromName(String name) {
            for (Behavior type : values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    // Variables
    private RTWidget rtWidget = null;
    private TextEditToolbarController toolbarController = null;
    private UndoHandler undoHandler = new UndoHandler();
    private Marker marker = null;
    private String receiverID = null;
    private ContextMenu contextRTWidgetMenu = new ContextMenu();
    private Map<String, MenuItem> contextRTWidgetMenuItems = new LinkedHashMap<>();
    private Behavior behavior = null;

    // Constructor
    public TextHandler(RTWidget txtWidget, TextEditToolbarController toolbarController, Behavior behavior) {
        marker = new Marker(txtWidget, this);

        this.toolbarController = toolbarController;
        if (toolbarController != null) {
            toolbarController.setTextHandler(this);
        }

        this.rtWidget = txtWidget;
        this.behavior = behavior;
        
        setBehavior(this.behavior);
    }

    // Public methods
    public void enableAutoComplete(boolean enable) {
        rtWidget.ac.enableAutoComplete(enable);
    }

    public void enableMarkingIntegers(boolean enable) {
        marker.allowMarkingIntegers(enable);
    }

    public void enableMarkingDoubles(boolean enable) {
        marker.allowMarkingDoubles(enable);
    }

    public void enableMarkingDates(boolean enable) {
        marker.allowMarkingDates(enable);
    }

    public void enableMarkingTimes(boolean enable) {
        marker.allowMarkingTimes(enable);
    }

    public void enableMarkingSerbianMobileNumbers(boolean enable) {
        marker.allowMarkingSerbianMobileNumbers(enable);
    }

    public void enableMarkingSerbianLandlineNumbers(boolean enable) {
        marker.allowMarkingSerbianLandlineNumbers(enable);
    }

    public void enableMarkingInternationalPhoneNumbers(boolean enable) {
        marker.allowMarkingInternationalPhoneNumbers(enable);
    }
    
    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public void msgFromToolbar(String messageSTRING) {
        if (messageSTRING.equals(TextToolbarActionEnum.FOCUS_TO_TEXT.name())) {
            rtWidget.requestFocus();
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.UNDO.name())) {
            rtWidget.busy = true;
            rtWidget.ignoreCaretPositionChange = true;
            rtWidget.ignoreTextChangePERMANENT = true;
            msgForToolbar(TextToolbarActionEnum.FIND_CLOSE.name());
            undoHandler.undo(rtWidget);
            Platform.runLater(() -> {
                rtWidget.ignoreCaretPositionChange = false;
                rtWidget.ignoreTextChangePERMANENT = false;
                rtWidget.busy = false;
            });
            rtWidget.requestFocus();
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.REDO.name())) {
            rtWidget.busy = true;
            rtWidget.ignoreCaretPositionChange = true;
            rtWidget.ignoreTextChangePERMANENT = true;
            msgForToolbar(TextToolbarActionEnum.FIND_CLOSE.name());
            undoHandler.redo(rtWidget);
            Platform.runLater(() -> {
                rtWidget.ignoreCaretPositionChange = false;
                rtWidget.ignoreTextChangePERMANENT = false;
                rtWidget.busy = false;
            });
            rtWidget.requestFocus();
        }
        else if (messageSTRING.equals("CUT")) {
            msgForWidget("CUT");
            rtWidget.requestFocus();
        }
        else if (messageSTRING.equals("COPY")) {
            msgForWidget("COPY");
            rtWidget.requestFocus();
        }
        else if (messageSTRING.equals("PASTE")) {
            msgForWidget("PASTE");
            rtWidget.requestFocus();
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:FIND CLOSED")) {
            marker.unMarkFindReplace();
            marker.mark();
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:" + TextToolbarActionEnum.REPLACE_ONE.name())) {
            marker.findReplaceONE(messageSTRING);
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:" + TextToolbarActionEnum.REPLACE_ALL.name())) {
            marker.findReplaceALL(messageSTRING);
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:" + TextToolbarActionEnum.FIND_ALL.name())) {
            marker.mark(messageSTRING);
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:" + TextToolbarActionEnum.FIND_MATCH_CASE.name())) {
            marker.mark(messageSTRING);
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:" + TextToolbarActionEnum.FIND_WHOLE_WORDS.name())) {
            marker.mark(messageSTRING);
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.FIND_UP.name())) {
            marker.findReplaceUP();
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.FIND_DOWN.name())) {
            marker.findReplaceDOWN();
        }

    }

    public void msgFromToolbar(StyleSheetChar styleSheet) {
        // Forward message to widget
        if (!marker.findReplaceChangeStyle(styleSheet)) {
            msgForWidget(styleSheet);
            rtWidget.requestFocus();
        }
        marker.markRepeat();
    }

    public void msgFromToolbar(StyleSheetParagraph styleSheet) {
        // Forward message to widget
        msgForWidget(styleSheet);
        rtWidget.requestFocus();
        marker.markRepeat();
    }

    public void msgFromWidget(String messageSTRING) {
        // Process information from widget
        if (messageSTRING.equals("TAKE_SNAPSHOT")) {
            undoHandler.addSnapshot(rtWidget);
            msgForToolbar("UNDO:" + undoHandler.canUndo());
            msgForToolbar("REDO:" + undoHandler.canRedo());
        }
        else if (messageSTRING.equals("SELECTED: False")) {
            msgForToolbar("SELECTED: False");
        }
        else if (messageSTRING.equals("SELECTED: True")) {
            msgForToolbar("SELECTED: True");
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.UNDO.name())) {
            msgFromToolbar(messageSTRING);
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.REDO.name())) {
            msgFromToolbar(messageSTRING);
        }
        else if (messageSTRING.startsWith(TextToolbarActionEnum.FIND_SHOW.name())) {
            msgForToolbar(messageSTRING);
        }
        else if (messageSTRING.startsWith(TextToolbarActionEnum.REPLACE_SHOW.name())) {
            msgForToolbar(messageSTRING);
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.FIND_CLOSE.name())) {
            msgForToolbar(TextToolbarActionEnum.FIND_CLOSE.name());
        }
        else if (messageSTRING.equals("PARAGRAPHS_LIMIT_EXCEEDED")) {
            MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
            msgBoxController.setTitleText(OBJECTS.SETTINGS.getl("text_LimitExceeded"));
            msgBoxController.setHeaderText(OBJECTS.SETTINGS.getl("text_ParagraphsLimitExceeded"));
            msgBoxController.setHeaderIcon(MsgBoxController.MsgBoxIcon.LIMIT);
            msgBoxController.setContentText(OBJECTS.SETTINGS.getl("ParagraphsLimitExceededContent"));
            msgBoxController.setContentIcon(MsgBoxController.MsgBoxIcon.PARAGRAPH);
            msgBoxController.setButtons(MsgBoxController.MsgBoxButton.OK);
            msgBoxController.setDefaultButton(MsgBoxController.MsgBoxButton.OK);
            msgBoxController.startMe();
        }
        else if (messageSTRING.equals("SAVE: Ctrl+S")) {
            if (receiverID == null) return;
            // Save text
            TaskStateEvent event = new TaskStateEvent(receiverID, rtWidget.getRTWTextObject(), "SAVE: Ctrl+S");
            OBJECTS.EVENT_HANDLER.fireEvent(event);
        }
        else if (messageSTRING.equals("SAVE: Ctrl+Shift+S")) {
            if (receiverID == null) return;
            // Save text as draft
            TaskStateEvent event = new TaskStateEvent(receiverID, rtWidget.getRTWTextObject(), "SAVE: Ctrl+Shift+S");
            OBJECTS.EVENT_HANDLER.fireEvent(event);
        }
        else if (messageSTRING.equals("SAVE: Alt+ENTER")) {
            if (receiverID == null) return;
            // Save text as draft
            TaskStateEvent event = new TaskStateEvent(receiverID, rtWidget.getRTWTextObject(), "SAVE: Alt+ENTER");
            OBJECTS.EVENT_HANDLER.fireEvent(event);
        }
        else if (messageSTRING.equals("CONTEXT_MENU:SHOW")) {
            showRTWidgetContextMenu(null);
        }
        else if (messageSTRING.equals("CONTEXT_MENU:HIDE")) {
            hideRTWidgetContextMenu();
        }
    }

    public void msgFromWidget(StyleSheetChar styleSheet) {
        // Forward message to toolbar
        rtWidget.ac.updateStyleSheet(styleSheet);
        msgForToolbar(styleSheet);
    }

    public void msgFromWidget(StyleSheetParagraph styleSheet) {
        // Forward message to toolbar
        msgForToolbar(styleSheet);
    }

    
    // Private methods

    // Messages for toolbar
    public void msgForToolbar(String messageSTRING) {
        // Send message to toolbar
        if (toolbarController == null) return;
        toolbarController.msgFromHandler(messageSTRING);
    }
    
    private void msgForToolbar(StyleSheetChar styleSheet) {
        // Send message to toolbar
        if (toolbarController == null) return;
        toolbarController.msgFromHandler(styleSheet);
    }

    private void msgForToolbar(StyleSheetParagraph styleSheet) {
        // Send message to toolbar
        if (toolbarController == null) return;
        toolbarController.msgFromHandler(styleSheet);
    }

    // Messages for Widget
    private void msgForWidget(String messageSTRING) {
        // Send message to widget
        rtWidget.msgFromHandler(messageSTRING);
    }
    
    private void msgForWidget(StyleSheetChar styleSheet) {
        // Send message to widget
        rtWidget.msgFromHandler(styleSheet);
    }

    private void msgForWidget(StyleSheetParagraph styleSheet) {
        // Send message to widget
        rtWidget.msgFromHandler(styleSheet);
    }

    // RTWidget context menu
    private void hideRTWidgetContextMenu() {
        if (contextRTWidgetMenu.isShowing()) {
            contextRTWidgetMenu.hide();
        }
    }

    private void showRTWidgetContextMenu(ContextMenuEvent contextMenuEvent) {
        // Cut
        if (rtWidget.getSelectedText().isEmpty() || rtWidget.isReadOnly()) {
            contextRTWidgetMenuItems.get("CUT").setDisable(true);
        } else {
            contextRTWidgetMenuItems.get("CUT").setDisable(false);
        }
        // Copy
        if (rtWidget.getSelectedText().isEmpty()) {
            contextRTWidgetMenuItems.get("COPY").setDisable(true);
        } else {
            contextRTWidgetMenuItems.get("COPY").setDisable(false);
        }
        // Paste
        if (OBJECTS.CLIP.getStyledText().isEmpty() || rtWidget.isReadOnly()) {
            contextRTWidgetMenuItems.get("PASTE").setDisable(true);
        } else {
            contextRTWidgetMenuItems.get("PASTE").setDisable(false);
        }


        if (contextMenuEvent == null) {
            rtWidget.getCaretBounds().ifPresent(bounds -> {
                double screenX = bounds.getMinX();
                double screenY = bounds.getMaxY();
                Platform.runLater(() -> {
                    contextRTWidgetMenu.show(rtWidget, screenX, screenY);
                });
            });
        } else {
            contextRTWidgetMenu.show(rtWidget, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        }
    }

    private void createRTWidgetContextMenu() {
        // Cut
        contextRTWidgetMenuItems.put("CUT", getCMItemCut());
        contextRTWidgetMenu.getItems().add(contextRTWidgetMenuItems.get("CUT"));
        // Copy
        contextRTWidgetMenuItems.put("COPY", getCMItemCopy());
        contextRTWidgetMenu.getItems().add(contextRTWidgetMenuItems.get("COPY"));
        // Paste
        contextRTWidgetMenuItems.put("PASTE", getCMItemPaste());
        contextRTWidgetMenu.getItems().add(contextRTWidgetMenuItems.get("PASTE"));
    }

    private MenuItem getCMItemCut() {
        MenuItem cut = new MenuItem(OBJECTS.SETTINGS.getl("text_Cut"));
        Image imgCut = new Image(getClass().getResourceAsStream("/images/cut.png"));
        ImageView imgCutView = new ImageView(imgCut);
        imgCutView.setPreserveRatio(true);
        imgCutView.setFitHeight(20);
        cut.setGraphic(imgCutView);
        cut.setOnAction(event -> {
            msgForWidget("CUT");
        });
        
        return cut;
    }

    private MenuItem getCMItemCopy() {
        MenuItem copy = new MenuItem(OBJECTS.SETTINGS.getl("text_Copy"));
        Image imgCopy = new Image(getClass().getResourceAsStream("/images/copy.png"));
        ImageView imgCopyView = new ImageView(imgCopy);
        imgCopyView.setPreserveRatio(true);
        imgCopyView.setFitHeight(20);
        copy.setGraphic(imgCopyView);
        copy.setOnAction(event -> {
            msgForWidget("COPY");
        });

        return copy;
    }

    private MenuItem getCMItemPaste() {
        MenuItem paste = new MenuItem(OBJECTS.SETTINGS.getl("text_Paste"));
        Image imgPaste = new Image(getClass().getResourceAsStream("/images/paste.png"));
        ImageView imgPasteView = new ImageView(imgPaste);
        imgPasteView.setPreserveRatio(true);
        imgPasteView.setFitHeight(20);
        paste.setGraphic(imgPasteView);
        paste.setOnAction(event -> {
            msgForWidget("PASTE");
        });

        return paste;
    }


    private void setBehavior(Behavior behavior) {
        this.rtWidget.ac = new ACHandler(rtWidget);
        rtWidget.ac.textHandler = this;
        createRTWidgetContextMenu();

        switch (behavior) {
            case BLOCK_NAME_ENTER:
                // rtWidget.setBehavior(Behavior.BLOCK_NAME_ENTER);
                HBox.setHgrow(rtWidget, Priority.ALWAYS);
                StyleSheetChar css = new StyleSheetChar();
                css.setCss(OBJECTS.SETTINGS.getvSTRING("CssBlockName"));
                rtWidget.setCssChar(css);
                rtWidget.setStyle(css.getCss());
                StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
                cssParagraph.setAlignmentEnum(AlignmentEnum.CENTER);
                rtWidget.setParagraphCss(cssParagraph);
                rtWidget.setMinTextWidgetHeight(OBJECTS.SETTINGS.getvINTEGER("BlockName_MinRTWidgetHeight"));
                rtWidget.setMaxNumberOfParagraphs(OBJECTS.SETTINGS.getvINTEGER("BlockName_MaxNumberOfParagraphs"));
                break;
            case BLOCK_NAME_SHOW:
                // rtWidget.setBehavior(Behavior.BLOCK_NAME_SHOW);
                HBox.setHgrow(rtWidget, Priority.ALWAYS);
                rtWidget.setReadOnly(true);
                break;
            default:
                break;
        }

        this.rtWidget.setTextHandler(this);
        this.rtWidget.setupWidget();

        msgForToolbar(this.rtWidget.getCssChar());
        msgForToolbar(this.rtWidget.getParagraphCss());
        msgForToolbar("UNDO:" + undoHandler.canUndo());
        msgForToolbar("REDO:" + undoHandler.canRedo());

        // RTWidget context menu
        this.rtWidget.setOnContextMenuRequested(event -> {
            hideRTWidgetContextMenu();
            showRTWidgetContextMenu(event);
        });
    }


}
