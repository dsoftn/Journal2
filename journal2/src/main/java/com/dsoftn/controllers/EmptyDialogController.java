package com.dsoftn.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

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
                if (bounds == null) {
                    continue;
                }

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

    private String myName = UJavaFX.getUniqueId();
    private String mySettingsName = "EmptyDialog";

    Stage stage = null;
    boolean framelessWindow = false; // true = window is frameless
    Map<String, Double> mousePos = null; // Used to resize window if window is frameless
    boolean resizeEnabled = false; // Used to enable window resizing when window is frameless
    double minSize = 50;
    List<Node> dragNodes = new ArrayList<>();
    boolean dialogPinned = false;
    IElementController myContentController = null;

    // FXML widgets
    @FXML
    VBox vBoxTitle; // Title
        @FXML
        Button btnPin;
        @FXML
        Label lblTitle;
        @FXML
        Button btnClose;
        @FXML
        Region regTitleSpacer;
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
        // Close button
        Image imgClose = new Image(getClass().getResourceAsStream("/images/close.png"));
        ImageView imgCloseView = new ImageView(imgClose);
        imgCloseView.setPreserveRatio(true);
        imgCloseView.setFitHeight(30);
        btnClose.setGraphic(imgCloseView);
        // Pin button
        Image imgPin = new Image(getClass().getResourceAsStream("/images/pin_red.png"));
        ImageView imgPinView = new ImageView(imgPin);
        imgPinView.setPreserveRatio(true);
        imgPinView.setFitHeight(30);
        btnPin.setGraphic(imgPinView);
    }


    // Implementation of IBaseController

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;

        stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !dialogPinned) {
                closeMe();
            }
        });
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void startMe() {
        if (stage == null) {
            UError.error("EmptyDialogController.startMe: stage is null");
            return;
        }

        stage.setOnCloseRequest(event -> {
            closeMe(event);
        });

        // Ensure that root always responds to mouse moved events even if mouse is over widgets like Buttons or ListView
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            onRootMouseMoved(event);
        });
        
        stage.show();
    }

    @Override
    public void closeMe() {
        saveStageGeometry();
        stage.close();
    }

    public void closeMe(WindowEvent event) {
        if (myContentController != null) {
            if (!myContentController.canBeClosed()) {
                event.consume();
                return;
            }
        }

        closeMe();
    }

    // Public methods

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setWindowBehavior(WindowBehavior windowStyle) {
        resetDragNodes();
        switch (windowStyle) {
            case ACTOR_SELECT_STANDARD:
                setFramelessWindow(true);
                setResizeEnabled(true);
                setMiniTitleEnabled(false);
                lblTitle.setText(OBJECTS.SETTINGS.getl("text_SelectActors"));
                mySettingsName = "EmptyDialog_" + windowStyle.toString();
                setStageGeometry();
                break;
            default:
                setFramelessWindow(false);
                setResizeEnabled(false);
                setMiniTitleEnabled(false);
                lblTitle.setText("---");
                break;
        }
    }

    public void setDragNodes(List<Node> nodes) {
        dragNodes.clear();

        for (Node node : nodes) {
            dragNodes.add(node);
        }

        setMiniTitleEnabled(false);
    }

    public void resetDragNodes() {
        dragNodes.clear();
        dragNodes.add(regTitle);
        dragNodes.add(lblTitle);
        setMiniTitleEnabled(true);
    }

    public void setContent(VBox content) {
        vBoxContent.getChildren().clear();
        vBoxContent.getChildren().add(content);
    }

    public void setFramelessWindow(boolean framelessWindow) {
        this.framelessWindow = framelessWindow;

        if (framelessWindow) {
            stage.initStyle(StageStyle.UNDECORATED);
            ancRoot.getStyleClass().remove("empty-dialog-root");
            ancRoot.getStyleClass().add("empty-dialog-root");
        }
        else {
            stage.initStyle(StageStyle.DECORATED);
            ancRoot.getStyleClass().remove("empty-dialog-root");
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

    public void setDialogPinned(Boolean dialogPinned) {
        if (dialogPinned == null) {
            this.dialogPinned = !this.dialogPinned;
            dialogPinned = this.dialogPinned;
        }

        if (dialogPinned) {
            Image imgPin = new Image(getClass().getResourceAsStream("/images/unpin.png"));            
            ImageView imgPinView = new ImageView(imgPin);
            imgPinView.setPreserveRatio(true);
            imgPinView.setFitHeight(30);
            btnPin.setGraphic(imgPinView);
        }
        else {
            Image imgPin = new Image(getClass().getResourceAsStream("/images/pin_red.png"));
            ImageView imgPinView = new ImageView(imgPin);
            imgPinView.setPreserveRatio(true);
            imgPinView.setFitHeight(30);
            btnPin.setGraphic(imgPinView);
        }
        
        this.dialogPinned = dialogPinned;
    }

    public void setContentController(IElementController contentController) {
        myContentController = contentController;
    }

    // Private methods

    private void setStageGeometry() {
        if (stage == null) {
            return;
        }
        UJavaFX.setStageGeometry(mySettingsName, stage);
    }

    private void saveStageGeometry() {
        if (stage == null) {
            return;
        }
        UJavaFX.saveStageGeometry(mySettingsName, stage);
    }

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
            // If cursor is not NORMAl change to default cursor
            if (ancRoot.getCursor() != Cursor.DEFAULT) {
                ancRoot.setCursor(Cursor.DEFAULT);
            }
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

    public void onBtnCloseAction(ActionEvent event) {
        closeMe();
    }

    public void onBtnPinAction(ActionEvent event) {
        setDialogPinned(null);
    }


}
