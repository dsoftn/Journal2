package com.dsoftn.services.text_handler;

import com.dsoftn.CONSTANTS;
import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;

import javafx.application.Platform;


public class TextHandler {
    public record Msg(String message) {}

    // Variables
    private String myName = UJavaFX.getUniqueId();
    private RTWidget txtWidget = null;
    private TextEditToolbarController toolbarController = null;
    private UndoHandler undoHandler = new UndoHandler();
    private Marker marker = null;

    // Constructor
    public TextHandler(RTWidget txtWidget, TextEditToolbarController toolbarController) {
        marker = new Marker(txtWidget, this);
        this.toolbarController = toolbarController;
        toolbarController.setTextHandler(this);
        this.txtWidget = txtWidget;
        this.txtWidget.setTextHandler(this);
        this.txtWidget.setupWidget();
        msgForToolbar(txtWidget.getCssChar());
        msgForToolbar(txtWidget.getParagraphCss());
        msgForToolbar("UNDO:" + undoHandler.canUndo());
        msgForToolbar("REDO:" + undoHandler.canRedo());
    }

    // Public methods
    public void msgFromToolbar(String messageSTRING) {
        if (messageSTRING.equals(TextToolbarActionEnum.FOCUS_TO_TEXT.name())) {
            txtWidget.requestFocus();
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.UNDO.name())) {
            txtWidget.busy = true;
            txtWidget.ignoreCaretPositionChange = true;
            txtWidget.ignoreTextChangePERMANENT = true;
            msgForToolbar(TextToolbarActionEnum.FIND_CLOSE.name());
            undoHandler.undo(txtWidget);
            Platform.runLater(() -> {
                txtWidget.ignoreCaretPositionChange = false;
                txtWidget.ignoreTextChangePERMANENT = false;
                txtWidget.busy = false;
            });
            txtWidget.requestFocus();
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.REDO.name())) {
            txtWidget.busy = true;
            txtWidget.ignoreCaretPositionChange = true;
            txtWidget.ignoreTextChangePERMANENT = true;
            msgForToolbar(TextToolbarActionEnum.FIND_CLOSE.name());
            undoHandler.redo(txtWidget);
            Platform.runLater(() -> {
                txtWidget.ignoreCaretPositionChange = false;
                txtWidget.ignoreTextChangePERMANENT = false;
                txtWidget.busy = false;
            });
            txtWidget.requestFocus();
        }
        else if (messageSTRING.equals("CUT")) {
            msgForWidget("CUT");
            txtWidget.requestFocus();
        }
        else if (messageSTRING.equals("COPY")) {
            msgForWidget("COPY");
            txtWidget.requestFocus();
        }
        else if (messageSTRING.equals("PASTE")) {
            msgForWidget("PASTE");
            txtWidget.requestFocus();
        }
        else if (messageSTRING.startsWith("FIND/REPLACE ACTION:FIND CLOSED")) {
            marker.unMarkFindReplace();
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
        else if (messageSTRING.equals(TextToolbarActionEnum.FIND_UP.name())) {
            marker.findReplaceUP();
        }
        else if (messageSTRING.equals(TextToolbarActionEnum.FIND_DOWN.name())) {
            marker.findReplaceDOWN();
        }

    }

    public void msgFromToolbar(StyleSheetChar styleSheet) {
        // Forward message to widget
        msgForWidget(styleSheet);
        txtWidget.requestFocus();
    }

    public void msgFromToolbar(StyleSheetParagraph styleSheet) {
        // Forward message to widget
        msgForWidget(styleSheet);
        txtWidget.requestFocus();
    }

    public void msgFromWidget(String messageSTRING) {
        // Process information from widget
        if (messageSTRING.equals("TAKE_SNAPSHOT")) {
            undoHandler.addSnapshot(txtWidget);
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
    }

    public void msgFromWidget(StyleSheetChar styleSheet) {
        // Forward message to toolbar
        txtWidget.ac.updateStyleSheet(styleSheet);
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
        toolbarController.msgFromHandler(messageSTRING);
    }
    private void msgForToolbar(StyleSheetChar styleSheet) {
        // Send message to toolbar
        toolbarController.msgFromHandler(styleSheet);
    }

    private void msgForToolbar(StyleSheetParagraph styleSheet) {
        // Send message to toolbar
        toolbarController.msgFromHandler(styleSheet);
    }

    // Messages for Widget
    private void msgForWidget(String messageSTRING) {
        // Send message to widget
        txtWidget.msgFromHandler(messageSTRING);
    }
    private void msgForWidget(StyleSheetChar styleSheet) {
        // Send message to widget
        txtWidget.msgFromHandler(styleSheet);
    }

    private void msgForWidget(StyleSheetParagraph styleSheet) {
        // Send message to widget
        txtWidget.msgFromHandler(styleSheet);
    }



}
