package com.dsoftn.utils;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.pop_up_windows.ColorPopup;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

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

    public static Task<Boolean> taskStartWithResult(Supplier<Boolean> supplierToExecute, String eventID) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return supplierToExecute.get();
            }
        };

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                boolean result = task.getValue();
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        eventID,
                        result ? TaskStateEnum.COMPLETED : TaskStateEnum.CANCELED,
                        ""
                    )
                );
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

        new Thread(task).start();

        return task;
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

    public static void setPopupGeometry(String keyName, Popup popup) {
        if (!OBJECTS.SETTINGS.isAppSettingExists(keyName)) {
            OBJECTS.SETTINGS.addAppSettings(keyName, "", true);
        }

        Map<String, Double> geometry = getStageGeometryMap(OBJECTS.SETTINGS.getAppSTRING(keyName));

        if (geometry.containsKey("PosX")) {
            popup.setX(geometry.get("PosX"));
        }
        if (geometry.containsKey("PosY")) {
            popup.setY(geometry.get("PosY"));
        }
        if (geometry.containsKey("Width")) {
            popup.setWidth(geometry.get("Width"));
        }
        if (geometry.containsKey("Height")) {
            popup.setHeight(geometry.get("Height"));
        }
    }

    public static void savePopupGeometry(String keyName, Popup popup) {
        if (!OBJECTS.SETTINGS.isAppSettingExists(keyName)) {
            OBJECTS.SETTINGS.addAppSettings(keyName, "", true);
        }

        Map<String, Double> geometry = getStageGeometryMap(OBJECTS.SETTINGS.getAppSTRING(keyName));

        geometry.put("PosX", popup.getX());
        geometry.put("PosY", popup.getY());
        geometry.put("Width", popup.getWidth());
        geometry.put("Height", popup.getHeight());

        OBJECTS.SETTINGS.setApp(keyName, getStageGeometryString(geometry));
    }


    public static void setStageGeometry(String keyName, Stage stage) {
        if (!OBJECTS.SETTINGS.isAppSettingExists(keyName)) {
            OBJECTS.SETTINGS.addAppSettings(keyName, "", true);
        }

        Map<String, Double> geometry = getStageGeometryMap(OBJECTS.SETTINGS.getAppSTRING(keyName));

        if (geometry.containsKey("PosX")) {
            stage.setX(geometry.get("PosX"));
        }
        if (geometry.containsKey("PosY")) {
            stage.setY(geometry.get("PosY"));
        }
        if (geometry.containsKey("Width")) {
            stage.setWidth(geometry.get("Width"));
        }
        if (geometry.containsKey("Height")) {
            stage.setHeight(geometry.get("Height"));
        }
        
        if (geometry.containsKey("status")) {
            // 0=normal, 1=maximized, 2=minimized
            if (geometry.get("status") == 1.0) {
                stage.setMaximized(true);
            }
            else if (geometry.get("status") == 2.0) {
                stage.setIconified(true);
            }
        }
    }
    
    public static void saveStageGeometry(String keyName, Stage stage) {
        if (!OBJECTS.SETTINGS.isAppSettingExists(keyName)) {
            OBJECTS.SETTINGS.addAppSettings(keyName, "", true);
        }

        Map<String, Double> geometry = getStageGeometryMap(OBJECTS.SETTINGS.getAppSTRING(keyName));

        if (stage.isMaximized()) {
            geometry.put("status", 1.0);
            OBJECTS.SETTINGS.setApp(keyName, getStageGeometryString(geometry));
            return;
        }
        if (stage.isIconified()) {
            geometry.put("status", 2.0);
            OBJECTS.SETTINGS.setApp(keyName, getStageGeometryString(geometry));
            return;
        }
        
        geometry.put("status", 0.0);
        geometry.put("PosX", stage.getX());
        geometry.put("PosY", stage.getY());
        geometry.put("Width", stage.getWidth());
        geometry.put("Height", stage.getHeight());

        OBJECTS.SETTINGS.setApp(keyName, getStageGeometryString(geometry));
    }

    private static Map<String, Double> getStageGeometryMap(String geometryString) {
        Map<String, Double> result = new LinkedHashMap<>();
        if (geometryString == null || geometryString.isEmpty()) {
            return result;
        }

        String[] parts = geometryString.split("\n");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                result.put(keyValue[0], Double.parseDouble(keyValue[1]));
            }
        }
        return result;
    }

    private static String getStageGeometryString(Map<String, Double> geometryMap) {
        String result = "";
        for (Map.Entry<String, Double> entry : geometryMap.entrySet()) {
            result += entry.getKey() + "=" + entry.getValue() + "\n";
        }
        return result;
    }

    public static boolean isImagesContentEqual(Image img1, Image img2) {
    if (img1 == null || img2 == null) {
        // If both are null, they are equal
        return img1 == img2;
    }
    
    // Check if dimensions are the same
    if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
        return false;
    }

    // Check each pixel
    PixelReader pr1 = img1.getPixelReader();
    PixelReader pr2 = img2.getPixelReader();

    for (int y = 0; y < img1.getHeight(); y++) {
        for (int x = 0; x < img1.getWidth(); x++) {
            if (pr1.getArgb(x, y) != pr2.getArgb(x, y)) {
                return false;
            }
        }
    }
    
    return true;

    }

    /**
     * Displays a color picker popup
     * @param ownerWindow - The window that will own the popup
     * @param onColorSelectedCallback - The callback that will be called when a color is selected
     */
    public static void showColorPickerPopUp(Window ownerWindow, Consumer<String> onColorSelectedCallback) {
        ColorPopup colorPopup = new ColorPopup(color -> {
            onColorSelectedCallback.accept(color);
        });

        colorPopup.startMe(ownerWindow);
    }

    /**
     * Returns list of basic fonts from all available fonts, no italic, bold, condensed, extended, light, medium, semi, thin, extra, etc.
     * @param allFonts - List of fonts, if null then it will use all available fonts from system
     * @return List of basic fonts
     */
    public static List<String> getBasicFonts(List<String> allFonts) {
        if (allFonts == null) {
            allFonts = getAllFonts();
        }

        List<String> fonts = new ArrayList<>();

        for (String fontName : allFonts) {
            String fontBaseName = fontName.split(" ")[0];

            boolean found = false;
            for (String font : fonts) {
                if (font.startsWith(fontBaseName)) {
                    found = true;
                    if (fontName.toLowerCase().contains("regular")) { // If font name contains "Regular" then it is the default font for the font family, so we use it instead of the other fonts with the same name but different styles (e.g. "Regular", "Italic", "Bold", etc.)
                        fonts.set(fonts.indexOf(font), fontName);
                        break;
                    }

                    if (fontName.length() < font.length() && !fontName.toLowerCase().contains("regular")) {
                        fonts.set(fonts.indexOf(font), fontName);
                        break;
                    }
                }
            }
            if (!found) {
                fonts.add(fontName);
            }
        }

        return fonts;
    }

    public static List<String> getAllFonts() {
        return Font.getFontNames();
    }



}
