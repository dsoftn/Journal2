package com.dsoftn.services;

import com.dsoftn.controllers.elements.TextEditToolbarController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.models.StyleSheet;


public class TextHandler {
    // Variables
    private RTWidget txtWidget = null;
    private TextEditToolbarController toolbarController = null;

    // Constructor
    public TextHandler(RTWidget txtWidget, TextEditToolbarController toolbarController) {
        this.toolbarController = toolbarController;
        toolbarController.setTextHandler(this);
        this.txtWidget = txtWidget;
        this.txtWidget.setupWidget();

        messageSent(txtWidget.getCss());
    }

    // Public methods
    public void messageReceived(String messageSTRING) {
        String[] messageParts = messageSTRING.split("\n");

        for (int i = 0; i < messageParts.length; i = i + 2) {
            if (i + 1 >= messageParts.length) {
                break;
            }

            if (messageParts[i].equals(TextToolbarActionEnum.FOCUS_TO_TEXT.name())) {
                txtWidget.requestFocus();
            }

        }
    }

    public void messageReceived(StyleSheet styleSheet) {
        txtWidget.setCss(styleSheet);
    }

    
    // Private methods
    private void messageSent(String messageSTRING) {
        // Send message to toolbar
        toolbarController.messageReceived(messageSTRING);
    }

    private void messageSent(StyleSheet styleSheet) {
        // Send message to toolbar
        toolbarController.messageReceived(styleSheet);
    }





}
