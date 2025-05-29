package com.dsoftn.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.CONSTANTS;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MoveResizeWindow {

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

        public static void updateCursor(MousePosition position, AnchorPane ancRoot, boolean resizeEnabled, boolean moveEnabled) {

            Cursor cursor = Cursor.DEFAULT;
            switch (position) {
                case TOP_LEFT:
                case BOTTOM_RIGHT:
                    if (!resizeEnabled) break;
                    cursor = Cursor.NW_RESIZE;
                    break;
                case TOP_RIGHT:
                case BOTTOM_LEFT:
                    if (!resizeEnabled) break;
                    cursor = Cursor.NE_RESIZE;
                    break;
                case TOP:
                case BOTTOM:
                    if (!resizeEnabled) break;
                    cursor = Cursor.V_RESIZE;
                    break;
                case LEFT:
                case RIGHT:
                    if (!resizeEnabled) break;
                    cursor = Cursor.H_RESIZE;
                    break;
                case DRAG:
                    if (!moveEnabled) break;
                    cursor = Cursor.MOVE;
                    break;
                default:
                    cursor = Cursor.DEFAULT;
            }

            ancRoot.setCursor(cursor);
        }
    

    }

    // Variables
    private Popup popup;
    private Stage stage;
    private AnchorPane ancRoot;
    private Map<String, Double> mousePos = null; // Used to resize window
    private boolean resize = true;
    private boolean move = true;
    private double minSize = 50;
    private List<Node> dragNodes = new ArrayList<>();
    private boolean isPopup = false;
    private boolean isStage = false;


    // Constructor
    public MoveResizeWindow(Popup popup, Node... dragNodes) {
        isPopup = true;
        this.popup = popup;
        this.ancRoot = (AnchorPane) popup.getScene().getRoot();
        this.dragNodes.addAll(List.of(dragNodes));

        ancRoot.setMaxHeight(Double.MAX_VALUE);
        ancRoot.setMaxWidth(Double.MAX_VALUE);

        // Setup events
        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            onRootMouseMoved(event);
        });

        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            onRootMousePressed(event);
        });

        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            onRootMouseDragged(event);
        });

        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            onRootMouseReleased(event);
        });
    }

    public MoveResizeWindow(Popup popup, List<Node> dragNodes) {
        this(popup, dragNodes.toArray(new Node[0]));
    }

    public MoveResizeWindow(Stage stage, Node... dragNodes) {
        isStage = true;
        this.stage = stage;
        this.ancRoot = (AnchorPane) stage.getScene().getRoot();
        this.dragNodes.addAll(List.of(dragNodes));

        ancRoot.setMaxHeight(Double.MAX_VALUE);
        ancRoot.setMaxWidth(Double.MAX_VALUE);

        // Setup events
        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            onRootMouseMoved(event);
        });

        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            onRootMousePressed(event);
        });

        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            onRootMouseDragged(event);
        });

        ancRoot.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            onRootMouseReleased(event);
        });
    }

    public MoveResizeWindow(Stage stage, List<Node> dragNodes) {
        this(stage, dragNodes.toArray(new Node[0]));
    }

    // Public methods

    public void enableWindowResize(boolean resize) {
        this.resize = resize;
    }

    public void enableWindowMove(boolean move) {
        this.move = move;
    }

    // Move / Resize events

    private void setStageGeometry(double posX, double posY, double width, double height) {
        if (isStage) {
            stage.setX(posX);
            stage.setY(posY);
            stage.setWidth(width);
            stage.setHeight(height);
        } else if (isPopup) {
            popup.setX(posX);
            popup.setY(posY);
            ancRoot.setPrefHeight(height);
            ancRoot.setPrefWidth(width);
        }
    }

    private boolean isStageInBounds(double posX, double posY, double width, double height) {
        if (posX < 0 || posY < 0 || width < minSize || height < minSize) return false;

        if (posX + width > Screen.getPrimary().getBounds().getWidth() ||
            posY + height > Screen.getPrimary().getBounds().getHeight()) {
            return false;
        }
        return true;
    }

    public void onRootMouseMoved(MouseEvent event) {
        if (!resize && !move) {
            // If cursor is not NORMAl change to default cursor
            if (ancRoot.getCursor() != Cursor.DEFAULT) {
                ancRoot.setCursor(Cursor.DEFAULT);
            }
            return;
        }

        MousePosition mouseType = MousePosition.getMousePosition(ancRoot, event, dragNodes);

        if (mousePos == null) {
            MousePosition.updateCursor(mouseType, ancRoot, resize, move);
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
                if (!resize) break;
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW") - (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") - (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case TOP_RIGHT:
                if (!resize) break;
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") - (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case BOTTOM_LEFT:
                if (!resize) break;
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") - (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") + (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case BOTTOM_RIGHT:
                if (!resize) break;
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH") + (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case TOP:
                if (!resize) break;
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY") + (event.getScreenY() - mousePos.get("mouseY"));
                newRootW = mousePos.get("rootW");
                newRootH = mousePos.get("rootH") - (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case BOTTOM:
                if (!resize) break;
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW");
                newRootH = mousePos.get("rootH") + (event.getScreenY() - mousePos.get("mouseY"));

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case LEFT:
                if (!resize) break;
                newRootX = mousePos.get("rootX") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") - (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH");

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;
            case RIGHT:
                if (!resize) break;
                newRootX = mousePos.get("rootX");
                newRootY = mousePos.get("rootY");
                newRootW = mousePos.get("rootW") + (event.getScreenX() - mousePos.get("mouseX"));
                newRootH = mousePos.get("rootH");

                if (isStageInBounds(newRootX, newRootY, newRootW, newRootH)) setStageGeometry(newRootX, newRootY, newRootW, newRootH);

                break;    
            case DRAG:
                if (!move) break;
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
        if (!resize && !move) {
            return;
        }

        MousePosition mouseType = MousePosition.getMousePosition(ancRoot, event, dragNodes);

        if (!MousePosition.isSizeable(mouseType)) {
            return;
        }

        mousePos = new HashMap<String, Double>();
        if (isStage) {
            mousePos.put("rootX", stage.getX());
            mousePos.put("rootY", stage.getY());
            mousePos.put("rootW", stage.getWidth());
            mousePos.put("rootH", stage.getHeight());
        } else if (isPopup) {
            mousePos.put("rootX", popup.getX());
            mousePos.put("rootY", popup.getY());
            mousePos.put("rootW", popup.getWidth());
            mousePos.put("rootH", popup.getHeight());
        }
        mousePos.put("mouseX", event.getScreenX());
        mousePos.put("mouseY", event.getScreenY());
        mousePos.put("action", mouseType.getValue());
        
    }

    public void onRootMouseReleased(MouseEvent event) {
        if (!resize && !move) {
            return;
        }

        if (mousePos == null) {
            return;
        }

        mousePos = null;
    }


}
