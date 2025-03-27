package com.dsoftn.services;

import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.models.StyleSheetChar;
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
        this.txtWidget.setupWidget();
        this.txtWidget.setUpdateToolbar(() -> {
            msgForToolbar(txtWidget.getCss());
        });

        msgForToolbar(txtWidget.getCss());
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
            else if (messageParts[i].equals(TextToolbarActionEnum.ALIGNMENT.name())) {
                // get current paragraph
                int paragraphIndex = txtWidget.getCurrentParagraph();
                txtWidget.setParagraphStyle(paragraphIndex, "-fx-text-alignment: " + messageParts[i + 1] + ";-fx-border-with: 1px;-fx-border-color: red;-fx-border-radius: 5px;-fx-padding: 10px;");
                txtWidget.setStyle(2, 5, "-fx-border-with: 1px;");
            }

        }
    }

    public void msgFromToolbar(StyleSheetChar styleSheet) {
        txtWidget.setCss(styleSheet);
    }

    public void msgFromWidget(String messageSTRING) {
        // Process information from widget
    }

    public void msgFromWidget(StyleSheetChar styleSheet) {
        // Process information from widget
    }

    
    // Private methods

    // Messages for toolbar
    private void msgForToolbar(String messageSTRING) {
        // Send message to toolbar
        toolbarController.messageReceived(messageSTRING);
    }
    private void msgForToolbar(StyleSheetChar styleSheet) {
        // Send message to toolbar
        toolbarController.messageReceived(styleSheet);
    }

    // Messages for Widget
    private void msgForWidget(String messageSTRING) {
        // Send message to widget
    }
    private void msgForWidget(StyleSheetChar styleSheet) {
        // Send message to widget
    }



}
