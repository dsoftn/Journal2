package com.dsoftn.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.CONSTANTS;
import com.dsoftn.Interfaces.IBaseController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EmptyDialogController implements IBaseController {

    public enum MousePosition {
        IN_ROOT(1),
        TOP_LEFT(2),
        TOP_RIGHT(3),
        BOTTOM_LEFT(4),
        BOTTOM_RIGHT(5),
        TOP(6),
        RIGHT(7),
        BOTTOM(8),
        LEFT(9),
        OUT_ROOT(10),
        DRAG(11);

        private final double value;

        // Constructor
        MousePosition(int value) {
            this.value = value;
        }

        // Methods

        public double getValue() {
            return value;
        }

        public static MousePosition fromInteger(int value) {
            for (MousePosition type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return IN_ROOT;
        }

        public static MousePosition fromDouble(double value) {
            for (MousePosition type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return IN_ROOT;
        }

        public static MousePosition getMousePosition(AnchorPane ancRoot, double mouseX, double mouseY, List<Node> dragNodes) {
            double x = ancRoot.getScene().getWindow().getX();
            double y = ancRoot.getScene().getWindow().getY();
            double w = ancRoot.getWidth();
            double h = ancRoot.getHeight();

            if (mouseX < x || mouseY < y || mouseX > x + w || mouseY > y + h) {
                return OUT_ROOT;
            }

            if (isInDRAG(mouseX, mouseY, dragNodes)) {
                return DRAG;
            }

            if (mouseX - x <= CONSTANTS.DIALOG_MARGIN && mouseY - y <= CONSTANTS.DIALOG_MARGIN) {
                return TOP_LEFT;
            } else if ((x + w) - mouseX <= CONSTANTS.DIALOG_MARGIN && mouseY - y <= CONSTANTS.DIALOG_MARGIN) {
                return TOP_RIGHT;
            } else if (mouseX - x <= CONSTANTS.DIALOG_MARGIN && (y + h) - mouseY <= CONSTANTS.DIALOG_MARGIN) {
                return BOTTOM_LEFT;
            } else if ((x + w) - mouseX <= CONSTANTS.DIALOG_MARGIN && (y + h) - mouseY <= CONSTANTS.DIALOG_MARGIN) {
                return BOTTOM_RIGHT;
            } else if (mouseX - x <= CONSTANTS.DIALOG_MARGIN) {
                return LEFT;
            } else if ((x + w) - mouseX <= CONSTANTS.DIALOG_MARGIN) {
                return RIGHT;
            } else if (mouseY - y <= CONSTANTS.DIALOG_MARGIN) {
                return TOP;
            } else if ((y + h) - mouseY <= CONSTANTS.DIALOG_MARGIN) {
                return BOTTOM;
            } else {
                return IN_ROOT;
            }
        }

        public static MousePosition getMousePosition(AnchorPane ancRoot, MouseEvent event, List<Node> dragNodes) {
            return getMousePosition(ancRoot, event.getScreenX(), event.getScreenY(), dragNodes);
        }

        public static boolean isInDRAG(double mouseX, double mouseY, List<Node> dragNodes) {
            if (dragNodes == null) {
                return false;
            }

            for (Node node : dragNodes) {
                Bounds bounds = node.localToScreen(node.getBoundsInLocal());
                if (mouseX >= bounds.getMinX() &&
                    mouseX <= bounds.getMaxX() &&
                    mouseY >= bounds.getMinY() &&
                    mouseY <= bounds.getMaxY()
                ) {
                    return true;
                }
            }
            
            return false;
        }

        public static boolean isSizeable(MousePosition position) {
            if (position == TOP_LEFT || 
                position == TOP_RIGHT || 
                position == BOTTOM_LEFT || 
                position == BOTTOM_RIGHT ||
                position == TOP ||
                position == RIGHT ||
                position == BOTTOM ||
                position == LEFT ||
                position == DRAG) {
                return true;
            }
            return false;
        }

        public static void updateCursor(MousePosition position, AnchorPane ancRoot) {

            Cursor cursor;
            switch (position) {
                case TOP_LEFT:
                case BOTTOM_RIGHT:
                    cursor = Cursor.NW_RESIZE;
                    break;
                case TOP_RIGHT:
                case BOTTOM_LEFT:
                    cursor = Cursor.NE_RESIZE;
                    break;
                case TOP:
                case BOTTOM:
                    cursor = Cursor.V_RESIZE;
                    break;
                case LEFT:
                case RIGHT:
                    cursor = Cursor.H_RESIZE;
                    break;
                case DRAG:
                    cursor = Cursor.MOVE;
                    break;
                default:
                    cursor = Cursor.DEFAULT;
            }

            ancRoot.setCursor(cursor);
        }
    

    }

    public enum WindowBehavior {
        // If you want to add new window behavior please update method "setWindowBehavior"
        DEFAULT(0),
        ACTOR_SELECT_STANDARD(1);

        private final int value;

        WindowBehavior(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static WindowBehavior fromInteger(int value) {
            for (WindowBehavior type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return DEFAULT;
        }
    }


    // Variables

    Stage stage = null;
    boolean framelessWindow = false; // true = window is frameless
    Map<String, Double> mousePos = null; // Used to resize window if window is frameless
    boolean resizeEnabled = false; // Used to enable window resizing when window is frameless
    double minSize = 50;
    List<Node> dragNodes = new ArrayList<>();

    // FXML widgets
    @FXML
    AnchorPane ancRoot;
    @FXML
    VBox vBoxContent;
    @FXML
    HBox hBoxMiniTitle;
    @FXML
    Region regTitle;

    // Constructors

    public EmptyDialogController() {}

    public void initialize() {
    }


    // Implementation of IBaseController

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void startMe() {
        stage.show();
    }

    @Override
    public void closeMe() {
        stage.close();
    }

    // Public methods

    public void setWindowBehavior(WindowBehavior windowStyle) {
        switch (windowStyle) {
            case ACTOR_SELECT_STANDARD:
                setFramelessWindow(true);
                setResizeEnabled(true);
                setMiniTitleEnabled(false);
                break;
            default:
                setFramelessWindow(false);
                setResizeEnabled(false);
                setMiniTitleEnabled(false);
                break;
        }
    }

    public void setDragNodes(Node... nodes) {
        dragNodes.clear();

        for (Node node : nodes) {
            dragNodes.add(node);
        }

        setMiniTitleEnabled(false);
    }

    public void resetDragNodes() {
        dragNodes.clear();
        dragNodes.add(regTitle);
        setMiniTitleEnabled(true);
        System.out.println("resetDragNodes");
    }

    public void setContent(VBox content) {
        vBoxContent.getChildren().clear();
        vBoxContent.getChildren().add(content);
    }

    public void setFramelessWindow(boolean framelessWindow) {
        this.framelessWindow = framelessWindow;

        if (framelessWindow) {
            stage.initStyle(StageStyle.UNDECORATED);
        }
        else {
            stage.initStyle(StageStyle.DECORATED);
        }
    }

    public void setResizeEnabled(boolean resizeEnabled) {
        this.resizeEnabled = resizeEnabled;
    }

    public void setMiniTitleEnabled(boolean dragWindowEnabled) {
        if (dragWindowEnabled) {
            if (hBoxMiniTitle.getChildren().isEmpty()) {
                hBoxMiniTitle.getChildren().add(regTitle);
            }
        }
        else {
            hBoxMiniTitle.getChildren().clear();
        }
    }

    // Private methods

    private boolean isStageInBounds(double posX, double posY, double width, double height) {
        if (posX < 0 || posY < 0 || width < minSize || height < minSize) return false;

        if (posX + width > Screen.getPrimary().getBounds().getWidth() ||
            posY + height > Screen.getPrimary().getBounds().getHeight()) {
            return false;
        }
        return true;
    }

    private void setStageGeometry(double posX, double posY, double width, double height) {
        stage.setX(posX);
        stage.setY(posY);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    // FXML Events

    public void onRootMouseMoved(MouseEvent event) {
        if (!resizeEnabled) {
            return;
        }

        MousePosition mouseType = MousePosition.getMousePosition(ancRoot, event, dragNodes);

        if (mousePos == null) {
            MousePosition.updateCursor(mouseType, ancRoot);
            return;
        }
    }

    public void onRootMouseDragged(MouseEvent event) {
        if (mousePos == null) {
            return;
        }

        MousePosition mouseType = MousePosition.fromDouble(mousePos.get("action"));

        double newRootX = 0;
        double newRootY = 0;
        double newRootW = 0;
        double newRootH = 0;

        switch (mouseType) {
            case TOP_LEFT:
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW") - (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") - (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case TOP_RIGHT:
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") - (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case BOTTOM_LEFT:
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") - (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") + (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case BOTTOM_RIGHT:
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") + (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case TOP:
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW");
                newRootH = mousePos.get("rootH") - (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case BOTTOM:
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW");
                newRootH = mousePos.get("rootH") + (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case LEFT:
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") - (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH");

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case RIGHT:
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH");

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;    
            case DRAG:
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW");
                newRootH = mousePos.get("rootH");

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case IN_ROOT:
                break;
            case OUT_ROOT:
                break;
            default:
                break;
        }

    }

    public void onRootMousePressed(MouseEvent event) {
        if (!resizeEnabled) {
            return;
        }

        MousePosition mouseType = MousePosition.getMousePosition(ancRoot, event, dragNodes);

        if (!MousePosition.isSizeable(mouseType)) {
            return;
        }

        mousePos = new HashMap<String, Double>();
        mousePos.put("rootX", stage.getX());
        mousePos.put("rootY", stage.getY());
        mousePos.put("rootW", stage.getWidth());
        mousePos.put("rootH", stage.getHeight());
        mousePos.put("mouseX", event.getScreenX());
        mousePos.put("mouseY", event.getScreenY());
        mousePos.put("action", mouseType.getValue());
        
    }

    public void onRootMouseReleased() {
        if (!resizeEnabled) {
            return;
        }

        if (mousePos == null) {
            return;
        }

        mousePos = null;
    }

}
