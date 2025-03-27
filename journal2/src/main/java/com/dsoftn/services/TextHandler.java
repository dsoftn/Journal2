package com.dsoftn.services;

import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.utils.UError;


public class TextHandler {
    public record Msg(String message) {}

    // Variables
    private RTWidget txtWidget = null;
    private TextEditToolbarController toolbarController = null;

    // Constructor
    public TextHandler(RTWidget txtWidget, TextEditToolbarController toolbarController) {
        this.toolbarController = toolbarController;
        toolbarController.setTextHandler(this);
        this.txtWidget = txtWidget;
        this.txtWidget.setTextHandler(this);
        this.txtWidget.setupWidget();
        msgForToolbar(txtWidget.getCss());
        msgForToolbar(txtWidget.getParagraphCss());
    }

    // Public methods
    public void msgFromToolbar(String messageSTRING) {
        String[] messageParts = messageSTRING.split("\n");
        // Check if message messageSTRING length is even
        if (messageParts.length % 2 != 0) {
            UError.error("TextHandler.messageReceived: Message messageSTRING length is odd.", "Message messageSTRING length is odd.");
            return;
        }

        for (int i = 0; i < messageParts.length; i = i + 2) {
            if (i + 1 >= messageParts.length) {
                break;
            }

            // Process information from toolbar

            if (messageParts[i].equals(TextToolbarActionEnum.FOCUS_TO_TEXT.name())) {
                txtWidget.requestFocus();
            }
            else if (messageParts[i].equals(TextToolbarActionEnum.UNDO.name())) {
                // TODO Implement undo
            }
            else if (messageParts[i].equals(TextToolbarActionEnum.REDO.name())) {
                // TODO Implement redo
            }

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
    }

    public void msgFromWidget(StyleSheetChar styleSheet) {
        // Forward message to toolbar
        msgForToolbar(styleSheet);
    }

    public void msgFromWidget(StyleSheetParagraph styleSheet) {
        // Forward message to toolbar
        msgForToolbar(styleSheet);
    }

    
    // Private methods

    // Messages for toolbar
    private void msgForToolbar(String messageSTRING) {
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
