package com.dsoftn.utils;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.awt.MouseInfo;
import java.awt.Point;

import java.util.function.Consumer;

public class ColorPopup {

    private final Consumer<String> onColorSelectedCallback;
    private final Popup popup;

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
        
        VBox content = createContent();
        popup.getScene().setRoot(content);
        popup.show(ownerWindow, x, y);
    }

    public void startMe(Window ownerWindow) {
        startMe(ownerWindow, null, null);
    }

    private VBox createContent() {
        // Predefinisane boje
        Color[] colors = {
                Color.RED, Color.GREEN, Color.BLUE,
                Color.YELLOW, Color.ORANGE, Color.PURPLE
        };

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            Region colorBox = new Region();
            colorBox.setPrefSize(30, 30);
            colorBox.setStyle("-fx-background-color: " + URichText.colorToHexString(color) + "; -fx-border-color: black;");

            colorBox.setOnMouseClicked(e -> {
                onColorSelectedCallback.accept(URichText.colorToHexString(color)); // Poziv callback metode
                popup.hide();
            });

            grid.add(colorBox, i % 3, i / 3);
        }

        // Custom boja sa ColorPicker-om
        Button customBtn = new Button("Custom...");
        customBtn.setMaxWidth(Double.MAX_VALUE);
        customBtn.setOnAction(e -> {
            ColorPicker colorPicker = new ColorPicker();
            colorPicker.setOnAction(event -> {
                onColorSelectedCallback.accept(URichText.colorToHexString(colorPicker.getValue()));
                popup.hide();
            });

            VBox customBox = new VBox(10, colorPicker);
            customBox.setPadding(new Insets(10));
            popup.getScene().setRoot(customBox);
        });

        VBox vbox = new VBox(10, grid, customBtn);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: black;");
        vbox.setAlignment(Pos.CENTER);
        return vbox;
    }

}
