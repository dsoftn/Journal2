package com.dsoftn.controllers.pop_up_windows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.controllers.elements.RTSettingsController;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.services.MoveResizeWindow;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.UJavaFX;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.awt.MouseInfo;



public class RTSettingsPopup implements ICustomEventListener {
    // Variables
    private final Consumer<Boolean> onExitCallback;
    private Popup popup;
    private String settingsName = null;
    private List<Node> dragNodes = new ArrayList<>();
    private TextHandler.Behavior behavior = null;
    private RTSettingsController rtSettingsController = null;

    // Constructor
    public RTSettingsPopup(Consumer<Boolean> onExitCallback, TextHandler.Behavior behavior) {
        this.settingsName = "RTSettingsPopup_" + behavior.toString();
        this.behavior = behavior;
        this.onExitCallback = onExitCallback;
        this.popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);

        // Register for event
        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);
    }

    // Implement ICustomEventListener interface
    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof TaskStateEvent) {
            TaskStateEvent taskEvent = (TaskStateEvent) event;
            if (!taskEvent.getID().equals(rtSettingsController.getMyName())) { return; }

            if (taskEvent.getState() == TaskStateEnum.COMPLETED) {
                if (taskEvent.getMessage().equals("CLOSE")) {
                    onExitCallback.accept(false);
                    popup.hide();
                }
                else if (taskEvent.getMessage().equals("CANCEL")) {
                    onExitCallback.accept(false);
                    popup.hide();
                }
                else if (taskEvent.getMessage().equals("APPLY")) {
                    onExitCallback.accept(true);
                    popup.hide();
                }
            }
        }
    }


    // Public Methods
    public void startMe(Window ownerWindow, Double x, Double y) {
        if (x == null || y == null) {
            java.awt.Point mousePos = MouseInfo.getPointerInfo().getLocation();
            if (x == null) {
                x = mousePos.getX();
            }
            if (y == null) {
                y = mousePos.getY();
            }
        }
        
        rtSettingsController = ELEMENTS.getRTSettingsController(behavior);
        rtSettingsController.setOwnerWindow(ownerWindow);
        AnchorPane root = rtSettingsController.getAnchorPaneRoot();

        dragNodes = rtSettingsController.getDragNodes();
        popup.getScene().setRoot(root);
        
        // Enable dragging
        new MoveResizeWindow(popup, dragNodes);

        popup.onHidingProperty().set(event -> {
            UJavaFX.savePopupGeometry(settingsName, popup);
        });

        popup.show(ownerWindow, x, y);
        UJavaFX.setPopupGeometry(settingsName, popup);
    }

    public void startMe(Window ownerWindow) {
        startMe(ownerWindow, null, null);
    }


}
