package com.dsoftn.utils;

import com.dsoftn.OBJECTS;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UJavaFX {

    public static String getUniqueId() {
        return System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString();
    }

    public static void taskStart(Runnable runnableToExecute, String eventID) {
        // Start new task
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                runnableToExecute.run();
                return null;
            }
        };

        // When task is finished send event
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        eventID,
                        TaskStateEnum.COMPLETED,
                        ""
                ));
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        eventID,
                        TaskStateEnum.FAILED,
                        "Task.setOnFailed exception: " + e
                    )
                );
            });
        });

        // Start task
        new Thread(task).start();

    }

    public static void setTooltip(Node node, String text, String title, Image image, Integer imageMaxWidth, Integer imageMaxHeight) {
        if (imageMaxHeight == null && imageMaxWidth == null) {
            imageMaxWidth = 100;
            imageMaxHeight = 100;
        }

        Tooltip tooltip = new Tooltip();

        VBox root = new VBox(5);
        root.setStyle(OBJECTS.SETTINGS.getvSTRING("CssTooltip_Root"));

        HBox header = null;
        ImageView imgHeader = null;

        // Header image
        if (image != null) {
            imgHeader = new ImageView(image);
            imgHeader.setPreserveRatio(true);
            if (imageMaxHeight != null) {
                imgHeader.setFitHeight(imageMaxHeight);
            }
            if (imageMaxWidth != null) {
                imgHeader.setFitWidth(imageMaxWidth);
            }
            if (header == null) {
                header = new HBox(5);
                header.setAlignment(Pos.CENTER_LEFT);
            }
            header.getChildren().add(imgHeader);
        }

        // Header text
        if (title != null) {
            Label lblTitle = new Label(title);
            lblTitle.setWrapText(true);
            lblTitle.setStyle(OBJECTS.SETTINGS.getvSTRING("CssTooltip_Title"));
            if (header == null) {
                header = new HBox(5);
                header.setAlignment(Pos.CENTER_LEFT);
            }
            header.getChildren().add(lblTitle);
        }

        Label lblText = new Label(text);
        lblText.setWrapText(true);
        lblText.setStyle(OBJECTS.SETTINGS.getvSTRING("CssTooltip_Text"));

        if (header != null) {
            root.getChildren().add(header);
        }
        
        if (text != null) {
            root.getChildren().add(lblText);
        }

        root.setMinHeight(Region.USE_PREF_SIZE); // Expands to fit content if Label is wrapped

        tooltip.setGraphic(root);
        tooltip.setPrefHeight(root.getHeight());
        
        if (node instanceof Control) {
            ((Control) node).setTooltip(tooltip);
        } else {
            Tooltip.install(node, tooltip);
        }
    }

    public static void setTooltip(Node node, String text) {
        setTooltip(node, text, null, null, null, null);
    }


}
