package com.dsoftn.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.dsoftn.OBJECTS;

public class ColorPopup {

    private final Consumer<String> onColorSelectedCallback;
    private final Popup popup;
    TextField txtInfo = new TextField();

    public ColorPopup(Consumer<String> onColorSelectedCallback) {
        this.onColorSelectedCallback = onColorSelectedCallback;
        this.popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
    }

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
        
        VBox content = createColorPickerContent(onColorSelectedCallback);
        popup.getScene().getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        content.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        popup.getScene().setRoot(content);
        popup.show(ownerWindow, x, y);
    }

    public void startMe(Window ownerWindow) {
        startMe(ownerWindow, null, null);
    }

    public VBox createColorPickerContent(Consumer<String> onColorSelectedCallback) {
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

            for (int i = 0; i < 7; i++) {
                double factor = i * 0.12;
                Color shade = baseColor.interpolate(Color.WHITE, factor);

                Region colorBox = new Region();
                colorBox.setPrefSize(24, 24);
                colorBox.setStyle("-fx-background-color: " + URichText.colorToHexString(shade) + "; -fx-border-color: black; -fx-border-radius: 3; -fx-background-radius: 3;");

                colorBox.setOnMouseEntered(e -> {
                    colorBox.setScaleX(1.3);
                    colorBox.setScaleY(1.3);
                    txtInfo.setText(URichText.colorToHexString(shade));
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



        VBox vbox = new VBox(10, lblTitle, btnTransparent, rows, infoBox);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: black; -fx-border-color: gray;");
        vbox.setAlignment(Pos.CENTER);

        return vbox;
    }


}
