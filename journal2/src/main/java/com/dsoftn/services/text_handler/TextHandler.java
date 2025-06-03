package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.elements.FormatCharController;
import com.dsoftn.controllers.elements.FormatParagraphController;
import com.dsoftn.controllers.elements.RTSettingsController;
import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.USettings;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;


public class TextHandler {
    public enum Behavior {
        BLOCK_NAME(1),
        ACTOR_DESCRIPTION(2);

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
    private Stage stage = null;
    private RTWidget rtWidget = null;
    private TextEditToolbarController toolbarController = null;
    private UndoHandler undoHandler = new UndoHandler();
    private Marker marker = null;
    private String receiverID = null;
    private ContextMenu contextRTWidgetMenu = new ContextMenu();
    private Map<String, MenuItem> contextRTWidgetMenuItems = new LinkedHashMap<>();
    private Behavior behavior = null;
    private RTSettingsController rtSettingsController = null;

    // Constructor
    public TextHandler(RTWidget txtWidget, TextEditToolbarController toolbarController, Behavior behavior, Stage stage) {
        this.stage = stage;
        this.toolbarController = toolbarController;
        if (toolbarController != null) {
            toolbarController.setTextHandler(this);
        }

        this.rtWidget = txtWidget;
        this.behavior = behavior;

        marker = new Marker(txtWidget, this);
        marker.updateSettings();
        
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

    public void updateSettings() {
        marker.updateSettings();
        rtWidget.ac.updateSettings();
    }

    public boolean canBeClosed() {
        if (toolbarController != null) {
            if (!toolbarController.canBeClosed()) {
                return false;
            }
        }

        if (rtWidget != null) {
            if (!rtWidget.canBeClosed()) {
                return false;
            }
        }

        return true;
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

    public void showRTWidgetContextMenu(Event event) {
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


        if (event == null) {
            rtWidget.getCaretBounds().ifPresent(bounds -> {
                double screenX = bounds.getMinX();
                double screenY = bounds.getMaxY();
                Platform.runLater(() -> {
                    contextRTWidgetMenu.show(rtWidget, screenX, screenY);
                });
            });
        } else if (event instanceof ContextMenuEvent) {
            contextRTWidgetMenu.show(rtWidget, ((ContextMenuEvent) event).getScreenX(), ((ContextMenuEvent) event).getScreenY());
        } else if (event instanceof MouseEvent) {
            contextRTWidgetMenu.show(rtWidget, ((MouseEvent) event).getScreenX(), ((MouseEvent) event).getScreenY());
        }
    }

    private void createRTWidgetContextMenu() {
        // Separator
        List<String> separatorAfterItem = List.of();
        
        // Cut
        contextRTWidgetMenuItems.put("CUT", getCMItemCut());
        // Copy
        contextRTWidgetMenuItems.put("COPY", getCMItemCopy());
        // Paste
        contextRTWidgetMenuItems.put("PASTE", getCMItemPaste());
        // Select All
        contextRTWidgetMenuItems.put("SELECT_ALL", getSelectAll());
        // Format Text
        contextRTWidgetMenuItems.put("FORMAT_TEXT", getCMItemFormatText());
        // Format Paragraph
        contextRTWidgetMenuItems.put("FORMAT_PARAGRAPH", getCMItemFormatParagraph());
        // Options
        contextRTWidgetMenuItems.put("OPTIONS", getCMIItemOptions());

        List<String> allowedItems = new ArrayList<>();
        
        // BEHAVIOR - selection of menu items
        if (behavior == Behavior.BLOCK_NAME) {
            separatorAfterItem = List.of("SELECT_ALL", "FORMAT_PARAGRAPH");
            allowedItems = List.of("CUT", "COPY", "PASTE", "SELECT_ALL", "FORMAT_TEXT", "FORMAT_PARAGRAPH", "OPTIONS");
        } else if (behavior == Behavior.ACTOR_DESCRIPTION) {
            separatorAfterItem = List.of("SELECT_ALL", "FORMAT_PARAGRAPH");
            allowedItems = List.of("CUT", "COPY", "PASTE", "SELECT_ALL", "FORMAT_TEXT", "FORMAT_PARAGRAPH", "OPTIONS");
        }

        if (rtWidget.isReadOnly()) {
            separatorAfterItem = List.of("SELECT_ALL");
            allowedItems = List.of("COPY", "SELECT_ALL", "OPTIONS");
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

    private MenuItem getSelectAll() {
        MenuItem selectAll = new MenuItem(OBJECTS.SETTINGS.getl("text_SelectAll"));
        Image imgSelectAll = new Image(getClass().getResourceAsStream("/images/select_all.png"));
        ImageView imgSelectAllView = new ImageView(imgSelectAll);
        imgSelectAllView.setPreserveRatio(true);
        imgSelectAllView.setFitHeight(20);
        selectAll.setGraphic(imgSelectAllView);
        selectAll.setOnAction(event -> {
            msgForWidget("SELECT_ALL");
        });

        return selectAll;
    }

    private MenuItem getCMItemFormatText() {
        MenuItem formatText = new MenuItem(OBJECTS.SETTINGS.getl("text_FormatText"));
        Image imgFormatText = new Image(getClass().getResourceAsStream("/images/text_format.png"));
        ImageView imgFormatTextView = new ImageView(imgFormatText);
        imgFormatTextView.setPreserveRatio(true);
        imgFormatTextView.setFitHeight(20);
        formatText.setGraphic(imgFormatTextView);
        formatText.setOnAction(event -> {
            StyleSheetChar defCharCss = new StyleSheetChar();
            defCharCss.setCss(USettings.getAppOrUserSettingsItem("CssDefaultTextStyle", behavior).getValueSTRING());
            FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, defCharCss, rtWidget.getCssChar(), this::onTextFormatExitCallback);
            formatChar.setTitle(OBJECTS.SETTINGS.getl("text_FormatText"));
            formatChar.startMe();
        });

        return formatText;
    }

    private void onTextFormatExitCallback(StyleSheetChar result) {
        if (result != null) {
            msgForWidget(result);
            msgForToolbar(result);
        }
    }

    private MenuItem getCMItemFormatParagraph() {
        MenuItem formatParagraph = new MenuItem(OBJECTS.SETTINGS.getl("text_FormatParagraph"));
        Image imgFormatParagraph = new Image(getClass().getResourceAsStream("/images/paragraph_format.png"));
        ImageView imgFormatParagraphView = new ImageView(imgFormatParagraph);
        imgFormatParagraphView.setPreserveRatio(true);
        imgFormatParagraphView.setFitHeight(20);
        formatParagraph.setGraphic(imgFormatParagraphView);
        formatParagraph.setOnAction(event -> {
            StyleSheetParagraph defParCss = new StyleSheetParagraph();
            defParCss.setCss(USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", behavior).getValueSTRING());
            FormatParagraphController formatParagraphController = DIALOGS.getFormatParagraphController(stage, behavior, defParCss, rtWidget.getParagraphCss(), rtWidget.getCssChar(), this::onParagraphFormatExitCallback);
            formatParagraphController.setTitle(OBJECTS.SETTINGS.getl("text_FormatParagraph"));
            formatParagraphController.startMe();
        });

        return formatParagraph;
    }

    private void onParagraphFormatExitCallback(StyleSheetParagraph result) {
        if (result != null) {
            msgForWidget(result);
            msgForToolbar(result);
        }
    }



    private MenuItem getCMIItemOptions() {
        MenuItem options = new MenuItem(OBJECTS.SETTINGS.getl("text_Options"));
        Image imgOptions = new Image(getClass().getResourceAsStream("/images/options_bw.png"));
        ImageView imgOptionsView = new ImageView(imgOptions);
        imgOptionsView.setPreserveRatio(true);
        imgOptionsView.setFitHeight(20);
        options.setGraphic(imgOptionsView);
        options.setOnAction(event -> {
            showRTSettings();
        });

        return options;
    }

    private SeparatorMenuItem getCMItemSeparator() {
        return new SeparatorMenuItem();
    }


    private void setBehavior() {
        this.rtWidget.ac = new ACHandler(rtWidget, this);
        this.rtWidget.ac.updateSettings();
        createRTWidgetContextMenu();

        switch (behavior) {
            case BLOCK_NAME:
                HBox.setHgrow(rtWidget, Priority.ALWAYS);
                StyleSheetChar css = new StyleSheetChar();
                css.setCss(USettings.getAppOrUserSettingsItem("CssDefaultTextStyle", behavior, "-fx-font-family: 'Arial';-fx-font-size: 30px;").getValueSTRING());
                rtWidget.setCssChar(css);
                StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
                cssParagraph.setCss(USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", behavior, "-fx-text-alignment: center;").getValueSTRING());
                rtWidget.setParagraphCss(cssParagraph);
                rtWidget.setMinTextWidgetHeight(USettings.getAppOrUserSettingsItem("MinRTWidgetHeight", behavior, 40).getValueINT());
                rtWidget.setMaxNumberOfParagraphs(USettings.getAppOrUserSettingsItem("MaxNumberOfParagraphs", behavior, 5).getValueINT());
                break;
            case ACTOR_DESCRIPTION:
                HBox.setHgrow(rtWidget, Priority.ALWAYS);
                StyleSheetChar cssActor = new StyleSheetChar();
                cssActor.setCss(USettings.getAppOrUserSettingsItem("CssDefaultTextStyle", behavior, "-fx-font-family: 'Arial';-fx-font-size: 20px;").getValueSTRING());
                rtWidget.setCssChar(cssActor);
                StyleSheetParagraph cssParagraphActor = new StyleSheetParagraph();
                cssParagraphActor.setCss(USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", behavior, "-fx-text-alignment: left;").getValueSTRING());
                rtWidget.setParagraphCss(cssParagraphActor);
                rtWidget.setMinTextWidgetHeight(USettings.getAppOrUserSettingsItem("MinRTWidgetHeight", behavior, 40).getValueINT());
                rtWidget.setMaxNumberOfParagraphs(USettings.getAppOrUserSettingsItem("MaxNumberOfParagraphs", behavior, 0).getValueINT());
                if (toolbarController != null) {
                    toolbarController.setToolbarSectionVisible(TextEditToolbarController.ToolbarSectionsEnum.CLIPBOARD, false);
                    toolbarController.setToolbarSectionVisible(TextEditToolbarController.ToolbarSectionsEnum.INSERT, false);
                    toolbarController.setToolbarSectionVisible(TextEditToolbarController.ToolbarSectionsEnum.FIND_REPLACE, false);
                    toolbarController.setToolbarSectionVisible(TextEditToolbarController.ToolbarSectionsEnum.ALIGNMENT, false);
                }
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
        // this.rtWidget.setOnContextMenuRequested(event -> {
        //     hideRTWidgetContextMenu();
        //     showRTWidgetContextMenu(event);
        // });
    }

    private void showRTSettings() {
        rtSettingsController = DIALOGS.getRTSettingsController(stage, behavior, this::onRTSettingsExit);
        rtSettingsController.startMe();
    }

    private void onRTSettingsExit(Boolean result) {
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
        rtSettingsController = null;
    }

}
