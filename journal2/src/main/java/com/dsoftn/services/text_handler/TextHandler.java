package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.controllers.pop_up_windows.RTSettingsPopup;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.USettings;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
    private RTSettingsPopup rtSettingsPopup = null;

    // Constructor
    public TextHandler(RTWidget txtWidget, TextEditToolbarController toolbarController, Behavior behavior) {
        this.toolbarController = toolbarController;
        if (toolbarController != null) {
            toolbarController.setTextHandler(this);
        }

        this.rtWidget = txtWidget;
        this.behavior = behavior;

        marker = new Marker(txtWidget, this);
        
        setBehavior();
    }

    // Public methods
    public Behavior getBehavior() { return behavior; }
    
    public void enableAutoComplete(boolean enable) {
        rtWidget.ac.enableAutoComplete(enable);
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
        } else if (messageSTRING.startsWith("INSERT_MODE:")) {
            msgForToolbar(messageSTRING);
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
        // Separator
        List<String> separatorAfterItem = List.of("PASTE");
        
        // Cut
        contextRTWidgetMenuItems.put("CUT", getCMItemCut());
        // Copy
        contextRTWidgetMenuItems.put("COPY", getCMItemCopy());
        // Paste
        contextRTWidgetMenuItems.put("PASTE", getCMItemPaste());
        // Options
        contextRTWidgetMenuItems.put("OPTIONS", getCMIItemOptions());

        List<String> allowedItems = new ArrayList<>();
        
        // BEHAVIOR - selection of menu items
        if (behavior == Behavior.BLOCK_NAME_ENTER) {
            allowedItems = List.of("CUT", "COPY", "PASTE", "OPTIONS");
        }
        else if (behavior == Behavior.BLOCK_NAME_SHOW) {
            allowedItems = List.of("COPY", "OPTIONS");
        }

        for (String item : allowedItems) {
            contextRTWidgetMenu.getItems().add(contextRTWidgetMenuItems.get(item));
            if (separatorAfterItem.contains(item)) {
                contextRTWidgetMenu.getItems().add(getCMItemSeparator());
            }
        }

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

    private MenuItem getCMIItemOptions() {
        MenuItem options = new MenuItem(OBJECTS.SETTINGS.getl("text_Options"));
        Image imgOptions = new Image(getClass().getResourceAsStream("/images/options_bw.png"));
        ImageView imgOptionsView = new ImageView(imgOptions);
        imgOptionsView.setPreserveRatio(true);
        imgOptionsView.setFitHeight(20);
        options.setGraphic(imgOptionsView);
        options.setOnAction(event -> {
            showRTSettingsPopup();
        });

        return options;
    }

    private SeparatorMenuItem getCMItemSeparator() {
        return new SeparatorMenuItem();
    }


    private void setBehavior() {
        this.rtWidget.ac = new ACHandler(rtWidget, this);
        createRTWidgetContextMenu();

        switch (behavior) {
            case BLOCK_NAME_ENTER:
                // rtWidget.setBehavior(Behavior.BLOCK_NAME_ENTER);
                HBox.setHgrow(rtWidget, Priority.ALWAYS);
                StyleSheetChar css = new StyleSheetChar();
                css.setCss(USettings.getAppOrUserSettingsItem("CssDefaultTextStyle", behavior, "-fx-font-family: 'Arial';-fx-font-size: 30px;").getValueSTRING());
                rtWidget.setCssChar(css);
                // rtWidget.setStyle(css.getCss());
                StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
                cssParagraph.setCss(USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", behavior, "-fx-text-alignment: center;").getValueSTRING());
                rtWidget.setParagraphCss(cssParagraph);
                rtWidget.setMinTextWidgetHeight(USettings.getAppOrUserSettingsItem("MinRTWidgetHeight", behavior, 40).getValueINT());
                rtWidget.setMaxNumberOfParagraphs(USettings.getAppOrUserSettingsItem("MaxNumberOfParagraphs", behavior, 5).getValueINT());
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

    private void showRTSettingsPopup() {
        rtSettingsPopup = new RTSettingsPopup(this::onRTSettingsPopupExit, behavior);
        rtSettingsPopup.startMe(rtWidget.getScene().getWindow());
    }

    private void onRTSettingsPopupExit(Boolean result) {
        if (result) {
            boolean hasAc = rtWidget.ac.hasCurrentAC();
            rtWidget.ac.removeCurrentAC();
            marker.updateSettings();
            rtWidget.ac.updateSettings();

            for (int i = 0; i < rtWidget.cssStyles.size(); i++) {
                rtWidget.setStyle(i, i + 1, rtWidget.cssStyles.get(i).getCss());
            }

            marker.mark();
            if (hasAc) {
                rtWidget.ac.showAC(rtWidget.getCaretPosition(), rtWidget.getParagraphTextNoAC(rtWidget.getCurrentParagraph()));
            }
        }
        rtSettingsPopup = null;
    }

}
