package com.dsoftn.controllers.pop_up_windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.dsoftn.OBJECTS;
import com.dsoftn.services.MoveResizeWindow;
import com.dsoftn.utils.URichText;

public class ColorPopup {
    // Variables
    private final Consumer<String> onColorSelectedCallback;
    private final Popup popup;
    TextField txtInfo = new TextField();
    private MoveResizeWindow moveResizeWindow = null;
    private List<Node> dragNodes = new ArrayList<>();

    // Constructor
    public ColorPopup(Consumer<String> onColorSelectedCallback) {
        this.onColorSelectedCallback = onColorSelectedCallback;
        this.popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
    }

    // Methods
    public void startMe(Window ownerWindow, Double x, Double y) {
        if (x == null || y == null) {
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            if (x == null) {
                x = mousePos.getX();
            }
            if (y == null) {
                y = mousePos.getY();
            }
        }
        
        AnchorPane root = createColorPickerContent(onColorSelectedCallback);

        popup.getScene().getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        popup.getScene().setRoot(root);
        
        // Enable dragging
        moveResizeWindow = new MoveResizeWindow(popup, dragNodes);
        moveResizeWindow.enableWindowResize(false);
        
        popup.show(ownerWindow, x, y);
    }

    public void startMe(Window ownerWindow) {
        startMe(ownerWindow, null, null);
    }

    public AnchorPane createColorPickerContent(Consumer<String> onColorSelectedCallback) {
        Map<String, Color> baseColors = new LinkedHashMap<>();
        baseColors.put("Red", Color.RED);
        baseColors.put("Green", Color.GREEN);
        baseColors.put("Blue", Color.BLUE);
        baseColors.put("Yellow", Color.YELLOW);
        baseColors.put("Orange", Color.ORANGE);
        baseColors.put("Purple", Color.PURPLE);
        baseColors.put("Brown", Color.BROWN);
        baseColors.put("Black", Color.BLACK);

        VBox rows = new VBox(5);
        rows.setPadding(new Insets(10));

        for (Color baseColor : baseColors.values()) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER_LEFT);

            for (int i = 0; i < 10; i++) {
                double factor = i * 0.09;
                Color shade = baseColor.interpolate(Color.WHITE, factor);

                Region colorBox = new Region();
                colorBox.setPrefSize(24, 24);
                colorBox.setStyle("-fx-background-color: " + URichText.colorToHexString(shade) + "; -fx-border-color: black; -fx-border-radius: 3; -fx-background-radius: 3;");

                colorBox.setOnMouseEntered(e -> {
                    colorBox.setScaleX(1.3);
                    colorBox.setScaleY(1.3);
                    // txtInfo.setText(URichText.colorToHexString(shade));
                });
                colorBox.setOnMouseExited(e -> {
                    colorBox.setScaleX(1.0);
                    colorBox.setScaleY(1.0);
                });

                colorBox.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        ContextMenu contextMenu = new ContextMenu();

                        MenuItem copy = new MenuItem(OBJECTS.SETTINGS.getl("text_Copy"));
                        Image imgCopy = new Image(getClass().getResourceAsStream("/images/copy.png"));
                        ImageView imgCopyView = new ImageView(imgCopy);
                        imgCopyView.setPreserveRatio(true);
                        imgCopyView.setFitHeight(20);
                        copy.setGraphic(imgCopyView);
                        copy.setOnAction(event -> {
                            OBJECTS.CLIP.setClipText(URichText.colorToHexString(shade));
                        });

                        contextMenu.getItems().add(copy);
                        contextMenu.show(colorBox, e.getScreenX(), e.getScreenY());
                        return;
                    }
                    onColorSelectedCallback.accept(URichText.colorToHexString(shade));
                    popup.hide();
                });

                row.getChildren().add(colorBox);
            }

            rows.getChildren().add(row);
        }

        // Title label
        Label lblTitle = new Label(OBJECTS.SETTINGS.getl("text_PickAColor"));
        lblTitle.setStyle("-fx-text-fill: #00a6fb;-fx-font-size: 18px;");
        lblTitle.setAlignment(Pos.CENTER);

        // Transparent color
        Button btnTransparent = new Button(OBJECTS.SETTINGS.getl("text_Transparent"));
        btnTransparent.setOnAction(e -> {
            onColorSelectedCallback.accept("transparent");
            popup.hide();
        });
        btnTransparent.getStyleClass().add("button-default");

        // Info
        HBox infoBox = new HBox(5);

        Region infoColor = new Region();

        HBox.setHgrow(infoColor, Priority.ALWAYS);
        infoColor.setStyle("-fx-background-color: transparent; -fx-border-color: gray; -fx-border-radius: 5; -fx-background-radius: 5;");
        infoColor.setOnMouseClicked(e -> {
            onColorSelectedCallback.accept(txtInfo.getText());
            popup.hide();
        });
        
        txtInfo.setMaxWidth(130);
        txtInfo.getStyleClass().add("text-field-default");
        txtInfo.setStyle("-fx-font-size: 18px;-fx-alignment: center;");
        txtInfo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                infoColor.setStyle("-fx-background-color: transparent;");
            } else {
                infoColor.setStyle("-fx-background-color: " + newValue + ";");
            }
        });
        txtInfo.setText("#FFFFFF");

        txtInfo.setOnContextMenuRequested(e -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem paste = new MenuItem(OBJECTS.SETTINGS.getl("text_Paste"));
            Image imgPaste = new Image(getClass().getResourceAsStream("/images/paste.png"));
            ImageView imgPasteView = new ImageView(imgPaste);
            imgPasteView.setPreserveRatio(true);
            imgPasteView.setFitHeight(20);
            paste.setGraphic(imgPasteView);
            paste.setOnAction(event -> {
                txtInfo.setText(OBJECTS.CLIP.getClipText());
            });
            contextMenu.getItems().add(paste);
            contextMenu.show(txtInfo, e.getScreenX(), e.getScreenY());
        });
        
        infoBox.getChildren().add(txtInfo);
        infoBox.getChildren().add(infoColor);

        // Effects
        HBox effectsBox = new HBox(5);
        effectsBox.setAlignment(Pos.CENTER_LEFT);
        effectsBox.setPadding(new Insets(10));
        Label lblEffects = new Label(OBJECTS.SETTINGS.getl("text_Effects"));
        lblEffects.setStyle("-fx-text-fill: #00a6fb;-fx-font-size: 16px;");
        ComboBox<String> cmbEffects = new ComboBox<>();
        cmbEffects.getStyleClass().add("combo-box-default");
        cmbEffects.getItems().addAll(getEffectsMap().keySet());
        cmbEffects.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                txtInfo.setText("#FFFFFF");
            } else {
                txtInfo.setText(getEffectsMap().get(newText));
            }
        });
        effectsBox.getChildren().addAll(lblEffects, cmbEffects);

        VBox vbox = new VBox(10, lblTitle, btnTransparent, rows, infoBox, effectsBox);
        dragNodes.add(lblTitle);

        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(vbox, 5.0);
        AnchorPane.setBottomAnchor(vbox, 5.0);
        AnchorPane.setLeftAnchor(vbox, 5.0);
        AnchorPane.setRightAnchor(vbox, 5.0);
        
        root.getChildren().add(vbox);

        root.setStyle("-fx-background-color: black; -fx-border-color: gray;");

        return root;
    }

    // Private methods
    private Map<String, String> getEffectsMap() {
        Map<String, String> effectsMap = new LinkedHashMap<>();

        effectsMap.put(OBJECTS.SETTINGS.getl("text_DEFAULT"), "");
        // Linear Gradients
        effectsMap.put("Linear: Steel Blue Gradient", "linear-gradient(from 0% 0% to 100% 100%, #1c1c3c, #003366);");
        effectsMap.put("Linear: Cool Gray Gradient", "linear-gradient(from 0% 0% to 100% 0%, #2e2e2e, #5e4e4e);");
        effectsMap.put("Linear: Midnight Fade", "linear-gradient(from 0% 0% to 0% 100%, #000000, #1a1a1a);");
        effectsMap.put("Linear: Dark Teal Shift", "linear-gradient(from 0% 0% to 100% 100%, #003333, #001a1a);");
        effectsMap.put("Linear: Shadow Purple", "linear-gradient(from 0% 0% to 100% 100%, #2b1a3f, #1a0f2f);");
        effectsMap.put("Linear: Obsidian Veil", "linear-gradient(from 0% 0% to 0% 100%, #1e1e1e, #2a2a2a);");
        effectsMap.put("Linear: Charcoal Flow", "linear-gradient(from 0% 0% to 100% 0%, #1f1f1f, #3a3a3a);");
        effectsMap.put("Linear: Deep Space", "linear-gradient(from 0% 0% to 100% 100%, #000010, #202030);");
        effectsMap.put("Linear: Crimson Edge", "linear-gradient(from 0% 0% to 100% 100%, #1a0000, #330000);");
        effectsMap.put("Linear: Electric Blue", "linear-gradient(from 0% 0% to 100% 100%, #001f33, #004466);");
        effectsMap.put("Linear: Lava Overlay", "linear-gradient(from 0% 0% to 100% 100%, #330000, #661111);");
        effectsMap.put("Linear: Neon Glow", "linear-gradient(from 0% 0% to 100% 0%, #0f0f0f, #00ffff22);");
        effectsMap.put("Linear: Stormy Sky", "linear-gradient(from 0% 0% to 0% 100%, #2e2e2e, #1e1e1e, #0f0f0f);");
        effectsMap.put("Linear: Blue Midnight", "linear-gradient(from 0% 0% to 100% 0%, #111122, #222244);");
        effectsMap.put("Linear: Minty Core", "linear-gradient(from 0% 0% to 100% 100%, #98FB98, #87CEEB);");
        // Radial Gradients
        effectsMap.put("Radial: Center Glow", "radial-gradient(radius 80%, #1e1e1e, #000000);");
        effectsMap.put("Radial: Purple Burst", "radial-gradient(radius 80%, #2e1a3c, #0f0a1f);");
        effectsMap.put("Radial: Dark Nebula", "radial-gradient(radius 100%, #101010, #000000);");
        effectsMap.put("Radial: Cyber Green", "radial-gradient(radius 70%, #002b00, #000000);");
        effectsMap.put("Radial: Lava Core", "radial-gradient(radius 90%, #330000, #000000);");
        effectsMap.put("Radial: Dim Spotlight", "radial-gradient(radius 90%, #222222, #111111);");
        effectsMap.put("Radial: Ashen Pulse", "radial-gradient(radius 80%, #2b2b2b, #0a0a0a);");
        effectsMap.put("Radial: Ocean Ring", "radial-gradient(radius 80%, #001a33, #000000);");
        effectsMap.put("Radial: Night Flame", "radial-gradient(radius 100%, #1f0000, #000000);");
        effectsMap.put("Radial: Twilight Zone", "radial-gradient(radius 80%, #1a1a3a, #000010);");
        effectsMap.put("Radial: Core Blue", "radial-gradient(radius 90%, #000033, #000000);");
        effectsMap.put("Radial: Infra Red", "radial-gradient(radius 90%, #330011, #000000);");
        effectsMap.put("Radial: Shadow Wave", "radial-gradient(radius 100%, #000000, #222222);");
        effectsMap.put("Radial: Toxic Glow", "radial-gradient(radius 80%, #003300, #000000);");
        effectsMap.put("Radial: Emerald Aura", "radial-gradient(radius 90%, #002b1f, #000000);");
        // Combined
        effectsMap.put("Combined: Glare Overlay", "radial-gradient(radius 90%, #1a1a1a, transparent), linear-gradient(from 0% 0% to 100% 0%, #000000, #222222);");
        effectsMap.put("Combined: Nebula Layer", "radial-gradient(radius 70%, #220022, transparent), linear-gradient(from 0% 0% to 100% 100%, #000000, #110011);");
        effectsMap.put("Combined: Midnight Stream", "linear-gradient(from 0% 0% to 100% 100%, #1a1a1a, #0f0f0f), radial-gradient(radius 60%, #222222, transparent);");
        effectsMap.put("Combined: Ghost Fade", "linear-gradient(from 0% 0% to 0% 100%, #111111, transparent), radial-gradient(radius 70%, #222222, #000000);");
        effectsMap.put("Combined: Dark Reflection", "radial-gradient(radius 100%, #1e1e1e, transparent), linear-gradient(from 0% 0% to 100% 100%, #000000, #1e1e1e);");
        effectsMap.put("Combined: Icy Core", "radial-gradient(radius 90%, #003344, #000000), linear-gradient(from 0% 0% to 100% 100%, #000000, #002233);");
        effectsMap.put("Combined: Black Mirror", "linear-gradient(from 0% 0% to 100% 0%, #282828, #000000), radial-gradient(radius 90%, #222222, transparent);");
        effectsMap.put("Combined: Haunted Night", "linear-gradient(from 0% 0% to 100% 100%, #1a0000, #330000), radial-gradient(radius 70%, #220000, transparent);");
        effectsMap.put("Combined: Deep Forest", "linear-gradient(from 0% 0% to 0% 100%, #002200, #001100), radial-gradient(radius 80%, #003300, transparent);");
        effectsMap.put("Combined: Blue Phantom", "radial-gradient(radius 70%, #000033, #000000), linear-gradient(from 0% 0% to 100% 0%, #111122, #222244);");
        effectsMap.put("Combined: Glow Grid", "radial-gradient(radius 70%, #00ffff11, transparent), linear-gradient(from 0% 0% to 100% 0%, #0f0f0f, #1a1a1a);");
        effectsMap.put("Combined: Black Gold", "linear-gradient(from 0% 0% to 100% 100%, #2a2a2a, #000000), radial-gradient(radius 90%, #332211, transparent);");
        effectsMap.put("Combined: Sapphire Fog", "radial-gradient(radius 80%, #001a33, transparent), linear-gradient(from 0% 0% to 100% 0%, #0f0f0f, #1e1e2e);");
        effectsMap.put("Combined: Electric Fade", "linear-gradient(from 0% 0% to 100% 0%, #111111, #333333), radial-gradient(radius 80%, #00ffff22, transparent);");
        effectsMap.put("Combined: Twilight Blend", "radial-gradient(radius 80%, #220022, transparent), linear-gradient(from 0% 0% to 100% 100%, #111111, #222222);");

        return effectsMap;
    }

}
