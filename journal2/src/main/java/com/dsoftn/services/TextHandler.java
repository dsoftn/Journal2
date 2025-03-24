package com.dsoftn.services;

import com.dsoftn.controllers.elements.TextEditToolbarController;
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
    }

    // Public methods
    public void messageReceived(String messageSTRING) {
        // TODO Auto-generated method stub
    }

    public void messageReceived(StyleSheet styleSheet) {
        // TODO Auto-generated method stub
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
