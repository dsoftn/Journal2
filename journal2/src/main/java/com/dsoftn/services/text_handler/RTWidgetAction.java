package com.dsoftn.services.text_handler;

import com.dsoftn.CONSTANTS;

import javafx.scene.input.KeyEvent;

public class RTWidgetAction {
    // ActionType enum
    public enum ActionType {
        TYPED_CHAR, // Single character typed by user
        PASTE, // Styled or plain text pasted
        DELETE, // Text deleted
        COPY, // Text copied
        CUT, // Text cut
        SELECT_ALL,
        UNDO,
        REDO,
        SET_FUTURE_CHAR_STYLE, // Set style for future characters
        FORMAT_STRING_SELECTION, // Format selected text
        FORMAT_PARAGRAPH_SELECTION, // Format selected paragraph(s)
        INSERT_PRESSED,
        ENTER_PRESSED,
        BACKSPACE_PRESSED
        ;
    }

    // Variables
    private ActionType actionType = null;
    private int globalStart = CONSTANTS.INVALID_ID;
    private int globalEnd = CONSTANTS.INVALID_ID;
    private KeyEvent keyEvent = null;

    // Constructor
    public RTWidgetAction(ActionType actionType) {
        this.actionType = actionType;
    }

    public RTWidgetAction(ActionType actionType, KeyEvent keyEvent) {
        this.actionType = actionType;
        this.keyEvent = keyEvent;
    }

    // Getters and setters
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public int getGlobalStart() { return globalStart; }
    public void setGlobalStart(int globalStart) { this.globalStart = globalStart; }
    public int getGlobalEnd() { return globalEnd; }
    public void setGlobalEnd(int globalEnd) { this.globalEnd = globalEnd; }
    public KeyEvent getKeyEvent() { return keyEvent; }
    public void setKeyEvent(KeyEvent keyEvent) { this.keyEvent = keyEvent; }

}
