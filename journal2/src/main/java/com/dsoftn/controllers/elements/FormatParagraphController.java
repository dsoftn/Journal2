package com.dsoftn.controllers.elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.MoveResizeWindow;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UNumbers;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FormatParagraphController implements IBaseController, ICustomEventListener {
    //  Variables
    private String myName = UJavaFX.getUniqueId();
    private Stage stage = null;
    private Consumer<StyleSheetParagraph> onExitCallback;
    private String settingsName = null;
    private List<Node> dragNodes = new ArrayList<>();
    private StyleSheetParagraph originalParStyle = new StyleSheetParagraph();
    private StyleSheetParagraph curParStyle = new StyleSheetParagraph();
    private StyleSheetParagraph startingCurParStyle = new StyleSheetParagraph();
    private StyleSheetChar defaultCharStyle = new StyleSheetChar(true);
    private RTWidget sampleWidget = null;
    private String invalidEntry = "-fx-background-color: darkred;";
    private String changedEntry = "-fx-border-width: 2px; -fx-border-style: solid;";
    private String unchangedEntry = "-fx-border-width: 1; -fx-border-style: dashed;";
    private String curBGColor = null;
    private String curBorderColor = null;

    private List<String> listAlignment = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "left", "center", "right", "justify");
    private List<String> listBGRadius = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20");
    private List<String> listPadding = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "0", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30");
    private List<String> listBorderWidth = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    private List<String> listBorderStyle = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "solid", "dashed", "dotted");
    private List<String> listBorderRadius = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20");
    private Map<String, String> effectsMap = getEffectsMap();


    // FXML variables
    // Title
    @FXML
    private HBox hBoxTitle;
    @FXML
    private Label lblTitle;
    @FXML
    private Button btnClose;
    // Paragraph settings
    @FXML
    private Label lblParSettings;
    @FXML
    private Label lblAlignment;
    @FXML
    private ComboBox<String> cmbAlignment;
    @FXML
    private Label lblColorBG;
    @FXML
    private Button btnColorBG;
    @FXML
    private CheckBox chkColorBG;
    @FXML
    private Label lblBGRadius;
    @FXML
    private ComboBox<String> cmbBGRadius;
    @FXML
    private Label lblPadding;
    @FXML
    private ComboBox<String> cmbPadding;
    // Paragraph Border
    @FXML
    private Label lblBorder;
    @FXML
    private Label lblBorderColor;
    @FXML
    private Button btnBorderColor;
    @FXML
    private CheckBox chkBorderColor;
    @FXML
    private Label lblBorderWidth;
    @FXML
    private ComboBox<String> cmbBorderWidth;
    @FXML
    private Label lblBorderStyle;
    @FXML
    private ComboBox<String> cmbBorderStyle;
    @FXML
    private Label lblBorderRadius;
    @FXML
    private ComboBox<String> cmbBorderRadius;
    // Paragraph effects
    @FXML
    private Label lblParEffects;
    @FXML
    private Label lblEffects;
    @FXML
    private ComboBox<String> cmbEffects;
    // CSS
    @FXML
    private Label lblCss;
    @FXML
    private TextField txtCss;
    @FXML
    private Button btnCssCopy;
    @FXML
    private Button btnCssPaste;
    @FXML
    private Button btnCssDiscard;
    // Sample
    @FXML
    private Label lblSample;
    @FXML
    private VBox vBoxSample;
    // Buttons
    @FXML
    private Button btnApply;
    @FXML
    private Button btnCancel;

    // Interface ICustomEventListener methods
    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof ClipboardChangedEvent) {
            if (OBJECTS.CLIP.getClipText() != null && OBJECTS.CLIP.getClipText().startsWith("-fx-")) {
                btnCssPaste.setDisable(false);
                UJavaFX.setTooltip(btnCssPaste, OBJECTS.CLIP.getClipText(), OBJECTS.SETTINGS.getl("text_ClipboardContent"), new Image(getClass().getResourceAsStream("/images/clipboard.png")), 30, 30);
            } else {
                btnCssPaste.setDisable(true);
            }
        }
    }

    // Interface IElementController methods
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public void startMe() {
        // Register for ClipboardChanged event
        OBJECTS.EVENT_HANDLER.register(this, ClipboardChangedEvent.CLIPBOARD_CHANGED_EVENT);

        // Remove window border
        stage.initStyle(StageStyle.UNDECORATED);
        // Make window modal
        stage.initModality(Modality.WINDOW_MODAL);

        // Setup drag nodes
        dragNodes.add(lblTitle);

        // Setup stage
        UJavaFX.setStageGeometry(settingsName, stage);
        new MoveResizeWindow(stage, dragNodes);

        // Setup widgets
        setupWidgetsText();
        setupWidgetsAppearance();
        updateCss(curParStyle);

        stage.show();
    }

    @Override
    public void closeMe() {
        OBJECTS.EVENT_HANDLER.unregister(this);
        UJavaFX.saveStageGeometry(settingsName, stage);
        stage.close();
    }

    public void setOnExitCallback(Consumer<StyleSheetParagraph> onExitCallback) {
        this.onExitCallback = onExitCallback;
    }

    public void setBehavior(TextHandler.Behavior behavior) {
        settingsName = "FormatParagraph_" + behavior.toString();
    }

    public void setOldStyleSheet(StyleSheetParagraph oldStyleSheet) {
        originalParStyle = oldStyleSheet.duplicate();
    }

    public void setNewStyleSheet(StyleSheetParagraph newStyleSheet) {
        curParStyle = newStyleSheet.duplicate();
        startingCurParStyle = newStyleSheet.duplicate();
    }

    public void setDefaultCharStyle(StyleSheetChar defaultCharStyle) {
        this.defaultCharStyle = defaultCharStyle.duplicate();
    }

    // Public methods

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    // Private methods
    private Map<String, String> getEffectsMap() {
        Map<String, String> effectsMap = new LinkedHashMap<>();
        effectsMap.put(OBJECTS.SETTINGS.getl("text_DEFAULT"), OBJECTS.SETTINGS.getl("text_DEFAULT"));

        effectsMap.put("Dropshadow: Neon Blue Shadow", "-fx-effect: dropshadow(gaussian, #00FFFF, 5, 0.5, 0, 1);");
        effectsMap.put("Dropshadow: Golden Glow", "-fx-effect: dropshadow(gaussian, #FFD700, 7, 0.3, 0, 2);");
        effectsMap.put("Dropshadow: Cyber Pink", "-fx-effect: dropshadow(gaussian, #FF69B4, 8, 0.4, 1, 1);");
        effectsMap.put("Dropshadow: Electric Indigo", "-fx-effect: dropshadow(gaussian, #8A2BE2, 9, 0.6, 0, 2);");
        effectsMap.put("Dropshadow: Toxic Green", "-fx-effect: dropshadow(gaussian, #7FFF00, 10, 0.7, 1, 1);");
        effectsMap.put("Dropshadow: Soft White Glow", "-fx-effect: dropshadow(gaussian, white, 4, 0.2, 1, 1);");
        effectsMap.put("Dropshadow: Lava Orange", "-fx-effect: dropshadow(gaussian, #FF4500, 6, 0.6, 2, 2);");
        effectsMap.put("Dropshadow: Sky Shine", "-fx-effect: dropshadow(gaussian, #87CEFA, 12, 0.4, 0, 4);");
        effectsMap.put("Dropshadow: Mint Shadow", "-fx-effect: dropshadow(gaussian, #98FF98, 6, 0.3, 1, 1);");
        effectsMap.put("Dropshadow: Deep Red Glow", "-fx-effect: dropshadow(gaussian, #DC143C, 8, 0.5, 0, 3);");
        effectsMap.put("Dropshadow: Purple Pulse", "-fx-effect: dropshadow(gaussian, #9932CC, 7, 0.5, 2, 2);");
        effectsMap.put("Dropshadow: Cyan Pulse", "-fx-effect: dropshadow(gaussian, #00FFFF, 5, 0.6, 0, 2);");
        effectsMap.put("Dropshadow: Golden Shimmer", "-fx-effect: dropshadow(gaussian, #FFD700, 6, 0.4, 1, 1);");
        effectsMap.put("Dropshadow: Red Pulse", "-fx-effect: dropshadow(gaussian, #DC143C, 8, 0.5, 0, 3);");

        effectsMap.put("Innershadow: Blue", "-fx-effect: innershadow(gaussian, #4444FF, 4, 0.5, 0, 0);");
        effectsMap.put("Innershadow: Green", "-fx-effect: innershadow(gaussian, #00FF7F, 6, 0.7, 2, 2);");
        effectsMap.put("Innershadow: Violet Pulse", "-fx-effect: innershadow(gaussian, #DA70D6, 5, 0.6, 1, 1);");
        effectsMap.put("Innershadow: Ice Blue", "-fx-effect: innershadow(gaussian, #00CED1, 4, 0.5, 0, 1);");
        effectsMap.put("Innershadow: Orange Tint", "-fx-effect: innershadow(gaussian, #FFA500, 6, 0.6, 1, 2);");
        effectsMap.put("Innershadow: Soft White", "-fx-effect: innershadow(gaussian, white, 3, 0.2, 0, 0);");
        effectsMap.put("Innershadow: Electric Purple", "-fx-effect: innershadow(gaussian, #9932CC, 7, 0.5, 2, 2);");
        effectsMap.put("Innershadow: Cyan Pulse", "-fx-effect: innershadow(gaussian, #00FFFF, 5, 0.6, 0, 2);");
        effectsMap.put("Innershadow: Golden Shimmer", "-fx-effect: innershadow(gaussian, #FFD700, 6, 0.4, 1, 1);");
        effectsMap.put("Innershadow: Toxic Green", "-fx-effect: innershadow(gaussian, #ADFF2F, 7, 0.6, 2, 1);");        
        effectsMap.put("Innershadow: Red Pulse", "-fx-effect: innershadow(gaussian, #DC143C, 8, 0.5, 0, 3);");
        effectsMap.put("Innershadow: Purple", "-fx-effect: innershadow(gaussian, #800080, 5, 0.6, 1, 1);");
        effectsMap.put("Innershadow: Pink", "-fx-effect: innershadow(gaussian, #FF69B4, 7, 0.5, 2, 2);");
        effectsMap.put("Innershadow: Yellow", "-fx-effect: innershadow(gaussian, #FFFF00, 6, 0.4, 1, 1);");
        effectsMap.put("Innershadow: Cyan", "-fx-effect: innershadow(gaussian, #00FFFF, 5, 0.6, 0, 2);");
        effectsMap.put("Innershadow: Magenta", "-fx-effect: innershadow(gaussian, #FF00FF, 7, 0.5, 2, 2);");
        effectsMap.put("Innershadow: Lime", "-fx-effect: innershadow(gaussian, #00FF00, 6, 0.4, 1, 1);");
        effectsMap.put("Innershadow: Orange", "-fx-effect: innershadow(gaussian, #FFA500, 7, 0.5, 2, 2);");

        effectsMap.put("Combined: Cyber Blue Inner + Glow", 
            "-fx-effect: innershadow(gaussian, #00FFFF, 4, 0.5, 0, 1), " +
                  "dropshadow(gaussian, #00FFFF, 8, 0.6, 0, 2);");

        effectsMap.put("Combined: Toxic Green Core + Glow", 
            "-fx-effect: innershadow(gaussian, #7FFF00, 5, 0.6, 1, 1), " +
                        "dropshadow(gaussian, #ADFF2F, 10, 0.7, 1, 1);");

        effectsMap.put("Combined: Violet Pulse + Shadow", 
            "-fx-effect: innershadow(gaussian, #DA70D6, 5, 0.5, 1, 1), " +
                        "dropshadow(gaussian, #9932CC, 9, 0.7, 1, 3);");

        effectsMap.put("Combined: Golden Inner + Outer", 
            "-fx-effect: innershadow(gaussian, #FFD700, 4, 0.4, 0, 1), " +
                        "dropshadow(gaussian, #FFA500, 8, 0.5, 0, 2);");

        effectsMap.put("Combined: Ice Blue Inner + Outer", 
            "-fx-effect: innershadow(gaussian, #00CED1, 5, 0.6, 1, 1), " +
                        "dropshadow(gaussian, #00FFFF, 9, 0.7, 0, 3);");

        effectsMap.put("Combined: Red Hot Core + Shadow", 
            "-fx-effect: innershadow(gaussian, #FF6347, 4, 0.5, 0, 0), " +
                        "dropshadow(gaussian, #FF0000, 8, 0.7, 0, 2);");

        effectsMap.put("Combined: Indigo Inner + Cyan Shadow", 
            "-fx-effect: innershadow(gaussian, #4B0082, 6, 0.6, 2, 2), " +
                        "dropshadow(gaussian, #00FFFF, 7, 0.6, 1, 3);");

        effectsMap.put("Combined: Minty Core + Sky Outer", 
            "-fx-effect: innershadow(gaussian, #98FB98, 5, 0.4, 0, 1), " +
                        "dropshadow(gaussian, #87CEEB, 9, 0.6, 1, 1);");

        effectsMap.put("Combined: Deep Pink Inner + Outer Glow", 
            "-fx-effect: innershadow(gaussian, #FF1493, 6, 0.5, 1, 2), " +
                        "dropshadow(gaussian, #FF69B4, 10, 0.6, 0, 2);");

        effectsMap.put("Combined: Soft White Inner + Subtle Glow", 
            "-fx-effect: innershadow(gaussian, white, 3, 0.2, 0, 0), " +
                        "dropshadow(gaussian, white, 4, 0.3, 1, 1);");

        effectsMap.put("Combined: Violet Pulse + Shadow", 
            "-fx-effect: innershadow(gaussian, #DA70D6, 5, 0.5, 1, 1), " +
                        "dropshadow(gaussian, #9932CC, 9, 0.7, 1, 3);");
        
        effectsMap.put("Combined: Golden Inner + Outer Glow", 
            "-fx-effect: innershadow(gaussian, #FFD700, 4, 0.4, 0, 1), " +
                        "dropshadow(gaussian, #FFA500, 8, 0.5, 0, 2);");

        effectsMap.put("Combined: Red Hot Core + Shadow", 
            "-fx-effect: innershadow(gaussian, #FF6347, 4, 0.5, 0, 0), " +
                        "dropshadow(gaussian, #DC143C, 9, 0.7, 0, 3);");

        effectsMap.put("Combined: Indigo Inner + Cyan Shadow", 
            "-fx-effect: innershadow(gaussian, #4B0082, 6, 0.6, 2, 2), " +
                        "dropshadow(gaussian, #00FFFF, 7, 0.6, 1, 3);");

        effectsMap.put("Combined: Minty Core + Sky Outer", 
            "-fx-effect: innershadow(gaussian, #98FB98, 5, 0.4, 0, 1), " +
                        "dropshadow(gaussian, #87CEEB, 9, 0.6, 1, 1);");

        effectsMap.put("Combined: Deep Pink Inner + Outer Glow", 
            "-fx-effect: innershadow(gaussian, #FF1493, 6, 0.5, 1, 2), " +
                        "dropshadow(gaussian, #FF69B4, 10, 0.6, 0, 2);");

        return effectsMap;
    }

    private void setupWidgetsText() {
        lblParSettings.setText(OBJECTS.SETTINGS.getl("text_ParagraphSettings"));
        lblAlignment.setText(OBJECTS.SETTINGS.getl("text_Alignment"));
        lblColorBG.setText(OBJECTS.SETTINGS.getl("text_Background"));
        chkColorBG.setText(OBJECTS.SETTINGS.getl("text_Default"));
        lblBGRadius.setText(OBJECTS.SETTINGS.getl("text_Radius"));
        lblPadding.setText(OBJECTS.SETTINGS.getl("text_Padding"));
        lblBorder.setText(OBJECTS.SETTINGS.getl("text_ParagraphBorder"));
        lblBorderColor.setText(OBJECTS.SETTINGS.getl("text_Color"));
        chkBorderColor.setText(OBJECTS.SETTINGS.getl("text_Default"));
        lblBorderWidth.setText(OBJECTS.SETTINGS.getl("text_Width"));
        lblBorderStyle.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblBorderRadius.setText(OBJECTS.SETTINGS.getl("text_Radius"));
        lblParEffects.setText(OBJECTS.SETTINGS.getl("text_ParagraphEffects"));
        lblEffects.setText(OBJECTS.SETTINGS.getl("text_Effects"));
        lblCss.setText(OBJECTS.SETTINGS.getl("text_CSSchanges"));
        btnCssCopy.setText(OBJECTS.SETTINGS.getl("text_Copy"));
        btnCssPaste.setText(OBJECTS.SETTINGS.getl("text_Paste"));
        btnCssDiscard.setText(OBJECTS.SETTINGS.getl("text_DiscardChanges"));
        lblSample.setText(OBJECTS.SETTINGS.getl("text_Sample"));
        btnApply.setText(OBJECTS.SETTINGS.getl("text_Apply"));
        btnCancel.setText(OBJECTS.SETTINGS.getl("text_Cancel"));
    }

    private void setupWidgetsAppearance() {
        if (lblTitle.getText().equals("TITLE")) {
            lblTitle.setText(OBJECTS.SETTINGS.getl("text_ParagraphFormatting"));
        }

        // Create sample widget
        sampleWidget = new RTWidget();
        sampleWidget.setMinTextWidgetHeight(35);
        sampleWidget.setReadOnly(true);
        sampleWidget.getStyleClass().add("rich-text");
        VBox.setVgrow(sampleWidget, javafx.scene.layout.Priority.ALWAYS);
        vBoxSample.getChildren().add(sampleWidget);
        String sampleText = "\n" + OBJECTS.SETTINGS.getl("text_ParagraphStyle");
        sampleWidget.setCssChar(defaultCharStyle.duplicate());
        sampleWidget.setTextPlain(sampleText);
        sampleWidget.setParagraphStyle(1, curParStyle.getCss());
        sampleWidget.setStyle(0, 2, "-fx-font-size: 1px;");
        sampleWidget.setStyle(2, sampleText.length() + 2, defaultCharStyle.getCss());

        // Title - close button
        Image imgClose = new Image(getClass().getResourceAsStream("/images/close.png"));
        ImageView imgCloseView = new ImageView(imgClose);
        imgCloseView.setPreserveRatio(true);
        imgCloseView.setFitHeight(25);
        btnClose.setGraphic(imgCloseView);

        // -----   Paragraph settings
        // Alignment
        cmbAlignment.getItems().addAll(listAlignment);
        cmbAlignment.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setAlignment(null);
            } else {
                curParStyle.setAlignment(newText);
            }
            updateSample();
        });
        // Background color
        btnColorBG.setOnAction(event -> {
            UJavaFX.showColorPickerPopUp(stage.getScene().getWindow(), color -> {
                curParStyle.setBgColor(color);
                btnColorBG.setStyle("-fx-background-color: " + color + ";");
                curBGColor = color;
                updateSample();
            });
        });

        chkColorBG.setOnAction(event -> {
            if (chkColorBG.isSelected()) {
                btnColorBG.setDisable(true);
                curParStyle.setBgColor(null);
                updateSample();
            } else {
                btnColorBG.setDisable(false);
                if (curBGColor != null) {
                    curParStyle.setBgColor(curBGColor);
                    btnColorBG.setStyle("-fx-background-color: " + curBGColor + ";");
                    updateSample();
                } else {
                    curParStyle.setBgColor("transparent");
                    btnColorBG.setStyle("-fx-background-color: transparent;");
                    updateSample();
                }
            }
        });
        // Background radius
        cmbBGRadius.getItems().addAll(listBGRadius);
        cmbBGRadius.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setBgRadius(null);
            } else {
                Integer num = UNumbers.toInteger(newText);
                if (num != null && num >= 0 && num <= 150) {
                    curParStyle.setBgRadius(Integer.parseInt(newText));
                    cmbBGRadius.setStyle("-fx-background-color: #051923;");
                } else {
                    curParStyle.setBgRadius(null);
                    cmbBGRadius.setStyle(invalidEntry);
                }
            }
            updateSample();
        });
        // Padding
        cmbPadding.getItems().addAll(listPadding);
        cmbPadding.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setPadding(null);
            } else {
                Integer num = UNumbers.toInteger(newText);
                if (num != null && num >= 0 && num <= 150) {
                    curParStyle.setPadding(newText);
                    cmbPadding.setStyle("-fx-background-color: #051923;");
                } else {
                    curParStyle.setPadding(null);
                    cmbPadding.setStyle(invalidEntry);
                }
            }
            updateSample();
        });
        // Border color
        btnBorderColor.setOnAction(event -> {
            UJavaFX.showColorPickerPopUp(stage.getScene().getWindow(), color -> {
                curParStyle.setBorderColor(color);
                btnBorderColor.setStyle("-fx-background-color: " + color + ";");
                curBorderColor = color;
                updateSample();
            });
        });

        chkBorderColor.setOnAction(event -> {
            if (chkBorderColor.isSelected()) {
                btnBorderColor.setDisable(true);
                curParStyle.setBorderColor(null);
                updateSample();
            } else {
                btnBorderColor.setDisable(false);
                if (curBorderColor != null) {
                    curParStyle.setBorderColor(curBorderColor);
                    btnBorderColor.setStyle("-fx-background-color: " + curBorderColor + ";");
                    updateSample();
                } else {
                    curParStyle.setBorderColor("transparent");
                    btnBorderColor.setStyle("-fx-background-color: transparent;");
                    updateSample();
                }
            }
        });
        // Border width
        cmbBorderWidth.getItems().addAll(listBorderWidth);
        cmbBorderWidth.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setBorderWidth(null);
            } else {
                Integer num = UNumbers.toInteger(newText);
                if (num != null && num >= 0 && num <= 150) {
                    curParStyle.setBorderWidth(Integer.parseInt(newText));
                    cmbBorderWidth.setStyle("-fx-background-color: #051923;");
                } else {
                    curParStyle.setBorderWidth(null);
                    cmbBorderWidth.setStyle(invalidEntry);
                }
            }
            updateSample();
        });
        // Border style
        cmbBorderStyle.getItems().addAll(listBorderStyle);
        cmbBorderStyle.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setBorderStyle(null);
            } else {
                curParStyle.setBorderStyle(newText);
            }
            updateSample();
        });
        // Border radius
        cmbBorderRadius.getItems().addAll(listBorderRadius);
        cmbBorderRadius.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setBorderRadius(null);
            } else {
                Integer num = UNumbers.toInteger(newText);
                if (num != null && num >= 0 && num <= 150) {
                    curParStyle.setBorderRadius(Integer.parseInt(newText));
                    cmbBorderRadius.setStyle("-fx-background-color: #051923;");
                } else {
                    curParStyle.setBorderRadius(null);
                    cmbBorderRadius.setStyle(invalidEntry);
                }
            }
            updateSample();
        });
        // Effects
        cmbEffects.getItems().addAll(effectsMap.keySet());
        cmbEffects.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curParStyle.setEffect(null);
            } else {
                curParStyle.setEffect(effectsMap.get(newText));
            }
            updateSample();
        });
        // CSS
        btnCssCopy.setDisable(true);
        btnCssDiscard.setDisable(true);
        if (OBJECTS.CLIP.getClipText() == null || (!OBJECTS.CLIP.getClipText().startsWith("-fx-") && !OBJECTS.CLIP.getClipText().startsWith("-rtfx-"))) {
            btnCssPaste.setDisable(true);
        }
        txtCss.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                btnCssCopy.setDisable(true);
                btnCssDiscard.setDisable(true);
            } else {
                btnCssCopy.setDisable(false);
                btnCssDiscard.setDisable(false);
            }
        });

    }

    private void updateCss(StyleSheetParagraph styleSheet) {
        // Alignment
        if (styleSheet.getAlignment() == null) {
            cmbAlignment.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbAlignment.setValue(styleSheet.getAlignment());
        }
        // Background color
        if (styleSheet.getBgColor() == null) {
            chkColorBG.setSelected(true);
            btnColorBG.setDisable(true);
            curBGColor = null;
        } else {
            btnColorBG.setStyle("-fx-background-color: " + styleSheet.getBgColor() + ";");
            chkColorBG.setSelected(false);
            btnColorBG.setDisable(false);
            curBGColor = styleSheet.getBgColor();
        }
        // Background radius
        if (styleSheet.getBgRadius() == null) {
            cmbBGRadius.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbBGRadius.setValue(styleSheet.getBgRadius().toString());
        }
        // Padding
        if (styleSheet.getPadding() == null) {
            cmbPadding.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbPadding.setValue(styleSheet.getPadding());
        }
        // Border color
        if (styleSheet.getBorderColor() == null) {
            chkBorderColor.setSelected(true);
            btnBorderColor.setDisable(true);
            curBorderColor = null;
        } else {
            btnBorderColor.setStyle("-fx-background-color: " + styleSheet.getBorderColor() + ";");
            chkBorderColor.setSelected(false);
            btnBorderColor.setDisable(false);
            curBorderColor = styleSheet.getBorderColor();
        }
        // Border width
        if (styleSheet.getBorderWidth() == null) {
            cmbBorderWidth.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbBorderWidth.setValue(styleSheet.getBorderWidth().toString());
        }
        // Border style
        if (styleSheet.getBorderStyle() == null) {
            cmbBorderStyle.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbBorderStyle.setValue(styleSheet.getBorderStyle());
        }
        // Border radius
        if (styleSheet.getBorderRadius() == null) {
            cmbBorderRadius.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbBorderRadius.setValue(styleSheet.getBorderRadius().toString());
        }
        // Effects
        if (styleSheet.getEffect() == null) {
            cmbEffects.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbEffects.setValue(styleSheet.getEffect());
        }

        txtCss.setText(styleSheet.getCss());
        updateSample();
    }

    private void updateSample() {
        txtCss.setText(curParStyle.getCss());
        sampleWidget.setParagraphStyle(1, curParStyle.getCss());
        repaintSelectedChoices();
    }

    private void repaintSelectedChoices() {
        if (curParStyle.equals(startingCurParStyle)) {
            btnApply.setDisable(true);
            txtCss.setStyle(unchangedEntry);
        } else {
            btnApply.setDisable(false);
            txtCss.setStyle(changedEntry);
        }
    }

    // FXML methods

    @FXML
    private void onBtnCloseAction() {
        onExitCallback.accept(null);
        closeMe();
    }

    @FXML
    private void onBtnApplyAction() {
        onExitCallback.accept(curParStyle);
        closeMe();
    }

    @FXML
    private void onBtnCancelAction() {
        onExitCallback.accept(null);
        closeMe();
    }

    @FXML
    private void onBtnCssCopyAction() {
        OBJECTS.CLIP.setClipText(txtCss.getText());
    }

    @FXML
    private void onBtnCssPasteAction() {
        String css = OBJECTS.CLIP.getClipText();
        if (css != null && css.startsWith("-fx-")) {
            curParStyle = new StyleSheetParagraph();
            curParStyle.setCss(css);
            updateCss(curParStyle);
        }
    }

    @FXML
    private void onBtnCssDiscardAction() {
        curParStyle =originalParStyle.duplicate();
        updateCss(curParStyle);
    }






}
