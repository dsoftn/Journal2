package com.dsoftn.controllers.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.services.MoveResizeWindow;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.text_handler.StyleSheetChar;
import com.dsoftn.services.text_handler.StyleSheetParagraph;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UNumbers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FormatCharController implements IBaseController, ICustomEventListener {
    //  Variables
    private String myName = UJavaFX.getUniqueId();
    private Stage stage = null;
    private Consumer<StyleSheetChar> onExitCallback;
    private String settingsName = null;
    private List<Node> dragNodes = new ArrayList<>();
    private StyleSheetChar originalCharStyle = new StyleSheetChar();
    private StyleSheetChar curCharStyle = new StyleSheetChar();
    private StyleSheetChar startingCurCharStyle = new StyleSheetChar();
    private boolean ignoreFontChange = false;
    private int sampleStart = 0;
    private int sampleEnd = 0;
    private final String DELIMITER = ".....";
    private RTWidget sampleWidget = null;
    private String invalidEntry = "-fx-background-color: darkred;";
    private String changedEntry = "-fx-border-width: 2px; -fx-border-style: solid;";
    private String unchangedEntry = "-fx-border-width: 1; -fx-border-style: dashed;";
    private String curFGColor = null;
    private String curBGColor = null;
    private String curStrokeColor = null;

    private ObservableList<String> listFontName;
    private FilteredList<String> filteredFonts;
    private PauseTransition fontChangeDelay = new PauseTransition(Duration.millis(200));
    private List<String> listFontSize = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42", "44", "46", "48", "50", "52", "54", "56", "58", "60", "62", "64", "66", "68", "70", "72", "74", "76", "78", "80", "82", "84", "86", "88", "90", "92", "94", "96", "98", "100", "110", "120", "130", "140", "150");
    private List<String> listFontBold = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), OBJECTS.SETTINGS.getl("text_Bold"), OBJECTS.SETTINGS.getl("text_Regular"));
    private List<String> listFontItalic = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), OBJECTS.SETTINGS.getl("text_Italic"), OBJECTS.SETTINGS.getl("text_Regular"));
    private List<String> listFontUnderline = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), OBJECTS.SETTINGS.getl("text_Underline"), OBJECTS.SETTINGS.getl("text_Regular"));
    private List<String> listFontStriketrought = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), OBJECTS.SETTINGS.getl("text_Strikethrough"), OBJECTS.SETTINGS.getl("text_Regular"));
    private List<String> listStrokeWidth = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    private List<String> listStrokeType = List.of(OBJECTS.SETTINGS.getl("text_DEFAULT"), "inside", "outside", "centered");


    

    // FXML variables
    // Title
    @FXML
    private HBox hBoxTitle;
    @FXML
    private Label lblTitle;
    @FXML
    private Button btnClose;
    // Font
    @FXML
    private Label lblFont;
    @FXML
    private Label lblFontName;
    @FXML
    private ComboBox<String> cmbFontName;
    @FXML
    private Label lblFontSize;
    @FXML
    private ComboBox<String> cmbFontSize;
    @FXML
    private Label lblFontBold;
    @FXML
    private ComboBox<String> cmbFontBold;
    @FXML
    private Label lblFontItalic;
    @FXML
    private ComboBox<String> cmbFontItalic;
    @FXML
    private Label lblFontUnderline;
    @FXML
    private ComboBox<String> cmbFontUnderline;
    @FXML
    private Label lblFontStriketrought;
    @FXML
    private ComboBox<String> cmbFontStriketrought;
    // Color
    @FXML
    private Label lblColor;
    @FXML
    private Label lblColorFG;
    @FXML
    private Button btnColorFG;
    @FXML
    private CheckBox chkColorFG;
    @FXML
    private Label lblColorBG;
    @FXML
    private Button btnColorBG;
    @FXML
    private CheckBox chkColorBG;
    // Stroke
    @FXML
    private Label lblStroke;
    @FXML
    private Label lblStrokeColor;
    @FXML
    private Button btnStrokeColor;
    @FXML
    private CheckBox chkStrokeColor;
    @FXML
    private Label lblStrokeWidth;
    @FXML
    private ComboBox<String> cmbStrokeWidth;
    @FXML
    private Label lblStrokeType;
    @FXML
    private ComboBox<String> cmbStrokeType;
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

        // Exit on ESC
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                onExitCallback.accept(null);
                closeMe();
            }
        });

        // Setup drag nodes
        dragNodes.add(lblTitle);

        // Setup stage
        UJavaFX.setStageGeometry(settingsName, stage);
        new MoveResizeWindow(stage, dragNodes);

        // Setup widgets
        setupWidgetsText();
        setupWidgetsAppearance();
        updateCss(curCharStyle);

        stage.show();
    }

    @Override
    public void closeMe() {
        OBJECTS.EVENT_HANDLER.unregister(this);
        UJavaFX.saveStageGeometry(settingsName, stage);
        stage.close();
    }

    public void setOnExitCallback(Consumer<StyleSheetChar> onExitCallback) {
        this.onExitCallback = onExitCallback;
    }

    public void setOldStyleSheet(StyleSheetChar oldStyleSheet) {
        originalCharStyle = oldStyleSheet.duplicate();
    }

    public void setNewStyleSheet(StyleSheetChar newStyleSheet) {
        curCharStyle = newStyleSheet.duplicate();
        startingCurCharStyle = newStyleSheet.duplicate();
    }
    
    public void setBehavior(TextHandler.Behavior behavior) {
        settingsName = "FormatChar_" + behavior.toString();
    }

    // Public methods

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    // Private methods
    private void setupWidgetsText() {
        lblFont.setText(OBJECTS.SETTINGS.getl("text_Font"));
        lblFontName.setText(OBJECTS.SETTINGS.getl("text_FontName"));
        lblFontSize.setText(OBJECTS.SETTINGS.getl("text_FontSize"));
        lblFontBold.setText(OBJECTS.SETTINGS.getl("text_Bold"));
        lblFontItalic.setText(OBJECTS.SETTINGS.getl("text_Italic"));
        lblFontUnderline.setText(OBJECTS.SETTINGS.getl("text_Underline"));
        lblFontStriketrought.setText(OBJECTS.SETTINGS.getl("text_Strikethrough"));
        lblColor.setText(OBJECTS.SETTINGS.getl("text_Color"));
        lblColorFG.setText(OBJECTS.SETTINGS.getl("text_Foreground"));
        lblColorBG.setText(OBJECTS.SETTINGS.getl("text_Background"));
        chkColorFG.setText(OBJECTS.SETTINGS.getl("text_Default"));
        chkColorBG.setText(OBJECTS.SETTINGS.getl("text_Default"));
        lblStroke.setText(OBJECTS.SETTINGS.getl("text_Stroke"));
        lblStrokeColor.setText(OBJECTS.SETTINGS.getl("text_StrokeColor"));
        chkStrokeColor.setText(OBJECTS.SETTINGS.getl("text_Default"));
        lblStrokeWidth.setText(OBJECTS.SETTINGS.getl("text_Width"));
        lblStrokeType.setText(OBJECTS.SETTINGS.getl("text_Type"));
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
            lblTitle.setText(OBJECTS.SETTINGS.getl("text_TextFormatting"));
        }

        // Create sample widget
        sampleWidget = new RTWidget(vBoxSample);
        sampleWidget.setMinTextWidgetHeight(35);
        sampleWidget.setReadOnly(true);
        sampleWidget.getStyleClass().add("rich-text");
        VBox.setVgrow(sampleWidget, javafx.scene.layout.Priority.ALWAYS);
        // vBoxSample.getChildren().add(sampleWidget);
        String sampleText = "\n" + OBJECTS.SETTINGS.getl("text_Before");
        int delimiterStart = sampleText.length() + 2;
        sampleText += DELIMITER;
        int delimiterEnd = delimiterStart + DELIMITER.length();
        sampleStart = sampleText.length() + 2;
        sampleText += OBJECTS.SETTINGS.getl("text_AfterChanges");
        sampleEnd = sampleText.length() + 2;
        StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
        cssParagraph.setAlignment("center");
        sampleWidget.setCssChar(originalCharStyle.duplicate());
        sampleWidget.setTextPlain(sampleText);
        sampleWidget.setParagraphStyle(1, cssParagraph.getCss());
        StyleSheetChar delimiterStyle = new StyleSheetChar();
        delimiterStyle.setFgColor("#ffffff");
        sampleWidget.setStyle(0, 2, "-fx-font-size: 1px;");
        sampleWidget.setStyle(delimiterStart, delimiterEnd, delimiterStyle.getCss());

        // Title - close button
        Image imgClose = new Image(getClass().getResourceAsStream("/images/close.png"));
        ImageView imgCloseView = new ImageView(imgClose);
        imgCloseView.setPreserveRatio(true);
        imgCloseView.setFitHeight(25);
        btnClose.setGraphic(imgCloseView);

        // Font Name
        listFontName = FXCollections.observableArrayList();
        listFontName.add(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        listFontName.addAll(UJavaFX.getAllFonts());
        filteredFonts = new FilteredList<>(listFontName, p -> true);

        ignoreFontChange = true;
        cmbFontName.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(String fontName, boolean empty) {
                super.updateItem(fontName, empty);
                if (empty || fontName == null) {
                    setText(null);
                    setFont(Font.getDefault());
                } else {
                    setText(fontName);
                    setFont(new Font(fontName, 14));
                }
            }
        });
        cmbFontName.setItems(filteredFonts);
        cmbFontName.getEditor().setStyle("-fx-font-size: 14px;");
        
        cmbFontName.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (ignoreFontChange) { return; }
            ignoreFontChange = true;
            
            fontChangeDelay.setOnFinished(e -> {
                if (listFontName.contains(newText) && !newText.isEmpty()) {
                    filteredFonts.setPredicate(f -> true);
                    if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                        curCharStyle.setFontName(null);
                    } else {
                        curCharStyle.setFontName(newText);
                    }
                    updateSample();
                } else {
                    filteredFonts.setPredicate(fontName -> fontName.toLowerCase().contains(newText.toLowerCase()));
                    if (!filteredFonts.isEmpty()) {
                        cmbFontName.show();
                    }
                }
                ignoreFontChange = false;
            });

            fontChangeDelay.playFromStart();
        });

        Platform.runLater(() -> {
            ignoreFontChange = false;
        });

        // Font Size
        cmbFontSize.setItems(FXCollections.observableArrayList(listFontSize));
        cmbFontSize.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setFontSize(null);
                cmbFontSize.setStyle("-fx-background-color: #051923;");
            } else {
                Integer num = UNumbers.toInteger(newText);
                if (num != null && num >= OBJECTS.SETTINGS.getvINTEGER("MinTextToolbarFontSize") && num <= OBJECTS.SETTINGS.getvINTEGER("MaxTextToolbarFontSize")) {
                    curCharStyle.setFontSize(Integer.parseInt(newText));
                    cmbFontSize.setStyle("-fx-background-color: #051923;");
                } else {
                    curCharStyle.setFontSize(null);
                    cmbFontSize.setStyle(invalidEntry);
                }
            }
            updateSample();
        });

        // Font Bold
        cmbFontBold.setItems(FXCollections.observableArrayList(listFontBold));
        cmbFontBold.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setBold(null);
            } else {
                if (newText.equals(OBJECTS.SETTINGS.getl("text_Bold"))) {
                    curCharStyle.setBold(true);
                } else {
                    curCharStyle.setBold(false);
                }
            }
            updateSample();
        });

        // Font Italic
        cmbFontItalic.setItems(FXCollections.observableArrayList(listFontItalic));
        cmbFontItalic.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setItalic(null);
            } else {
                if (newText.equals(OBJECTS.SETTINGS.getl("text_Italic"))) {
                    curCharStyle.setItalic(true);
                } else {
                    curCharStyle.setItalic(false);
                }
            }
            updateSample();
        });

        // Font Underline
        cmbFontUnderline.setItems(FXCollections.observableArrayList(listFontUnderline));
        cmbFontUnderline.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setUnderline(null);
            } else {
                if (newText.equals(OBJECTS.SETTINGS.getl("text_Underline"))) {
                    curCharStyle.setUnderline(true);
                } else {
                    curCharStyle.setUnderline(false);
                }
            }
            updateSample();
        });

        // Font Striketrought
        cmbFontStriketrought.setItems(FXCollections.observableArrayList(listFontStriketrought));
        cmbFontStriketrought.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setStrikethrough(null);
            } else {
                if (newText.equals(OBJECTS.SETTINGS.getl("text_Strikethrough"))) {
                    curCharStyle.setStrikethrough(true);
                } else {
                    curCharStyle.setStrikethrough(false);
                }
            }
            updateSample();
        });

        // Color
        btnColorFG.setOnAction(event -> {
            UJavaFX.showColorPickerPopUp(stage.getScene().getWindow(), color -> {
                curCharStyle.setFgColor(color);
                btnColorFG.setStyle("-fx-text-fill: " + color + ";");
                curFGColor = color;
                updateSample();
            });
        });

        chkColorFG.setOnAction(event -> {
            if (chkColorFG.isSelected()) {
                btnColorFG.setDisable(true);
                curCharStyle.setFgColor(null);
                updateSample();
            } else {
                btnColorFG.setDisable(false);
                if (curFGColor != null) {
                    curCharStyle.setFgColor(curFGColor);
                    btnColorFG.setStyle("-fx-text-fill: " + curFGColor + ";");
                    updateSample();
                } else {
                    curCharStyle.setFgColor("#ffff00");
                    btnColorFG.setStyle("-fx-text-fill: #ffff00;");
                    updateSample();
                }
            }
        });

        btnColorBG.setOnAction(event -> {
            UJavaFX.showColorPickerPopUp(stage.getScene().getWindow(), color -> {
                curCharStyle.setBgColor(color);
                btnColorBG.setStyle("-fx-background-color: " + color + ";");
                curBGColor = color;
                updateSample();
            });
        });

        chkColorBG.setOnAction(event -> {
            if (chkColorBG.isSelected()) {
                btnColorBG.setDisable(true);
                curCharStyle.setBgColor(null);
                updateSample();
            } else {
                btnColorBG.setDisable(false);
                if (curBGColor != null) {
                    curCharStyle.setBgColor(curBGColor);
                    btnColorBG.setStyle("-fx-background-color: " + curBGColor + ";");
                    updateSample();
                } else {
                    curCharStyle.setBgColor("transparent");
                    btnColorBG.setStyle("-fx-background-color: transparent;");
                    updateSample();
                }
            }
        });

        // Stroke Color
        btnStrokeColor.setOnAction(event -> {
            UJavaFX.showColorPickerPopUp(stage.getScene().getWindow(), color -> {
                curCharStyle.setStroke(color);
                btnStrokeColor.setStyle("-fx-background-color: " + color + ";");
                curStrokeColor = color;
                updateSample();
            });
        });

        chkStrokeColor.setOnAction(event -> {
            if (chkStrokeColor.isSelected()) {
                btnStrokeColor.setDisable(true);
                curCharStyle.setStroke(null);
                updateSample();
            } else {
                btnStrokeColor.setDisable(false);
                if (curStrokeColor != null) {
                    curCharStyle.setStroke(curStrokeColor);
                    btnStrokeColor.setStyle("-fx-background-color: " + curStrokeColor + ";");
                    updateSample();
                } else {
                    curCharStyle.setStroke("transparent");
                    btnStrokeColor.setStyle("-fx-background-color: transparent;");
                    updateSample();
                }
            }
        });

        // Stroke Width
        cmbStrokeWidth.setItems(FXCollections.observableArrayList(listStrokeWidth));
        cmbStrokeWidth.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setStrokeWidth(null);
            } else {
                Integer num = UNumbers.toInteger(newText);
                if (num != null && num >= 0 && num <= 150) {
                    curCharStyle.setStrokeWidth(Integer.parseInt(newText));
                    cmbStrokeWidth.setStyle("-fx-background-color: #051923;");
                } else {
                    curCharStyle.setStrokeWidth(null);
                    cmbStrokeWidth.setStyle(invalidEntry);
                }
            }
            updateSample();
        });

        // Stroke Type
        cmbStrokeType.setItems(FXCollections.observableArrayList(listStrokeType));
        cmbStrokeType.valueProperty().addListener((obs, oldText, newText) -> {
            if (newText.equals(OBJECTS.SETTINGS.getl("text_DEFAULT"))) {
                curCharStyle.setStrokeType(null);
            } else {
                curCharStyle.setStrokeType(newText);
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

    private void updateCss(StyleSheetChar styleSheet) {
        // Font Name
        if (styleSheet.getFontName() == null) {
            cmbFontName.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbFontName.setValue(styleSheet.getFontName());
        }
        // Font Size
        if (styleSheet.getFontSize() == null) {
            cmbFontSize.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbFontSize.setValue(styleSheet.getFontSize().toString());
        }
        // Font Bold
        if (styleSheet.isBold() == null) {
            cmbFontBold.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            if (styleSheet.isBold()) {
                cmbFontBold.setValue(OBJECTS.SETTINGS.getl("text_Bold"));
            } else {
                cmbFontBold.setValue(OBJECTS.SETTINGS.getl("text_Regular"));
            }
        }
        // Font Italic
        if (styleSheet.isItalic() == null) {
            cmbFontItalic.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            if (styleSheet.isItalic()) {
                cmbFontItalic.setValue(OBJECTS.SETTINGS.getl("text_Italic"));
            } else {
                cmbFontItalic.setValue(OBJECTS.SETTINGS.getl("text_Regular"));
            }
        }
        // Font Underline
        if (styleSheet.isUnderline() == null) {
            cmbFontUnderline.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            if (styleSheet.isUnderline()) {
                cmbFontUnderline.setValue(OBJECTS.SETTINGS.getl("text_Underline"));
            } else {
                cmbFontUnderline.setValue(OBJECTS.SETTINGS.getl("text_Regular"));
            }
        }
        // Font Striketrought
        if (styleSheet.isStrikethrough() == null) {
            cmbFontStriketrought.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            if (styleSheet.isStrikethrough()) {
                cmbFontStriketrought.setValue(OBJECTS.SETTINGS.getl("text_Strikethrough"));
            } else {
                cmbFontStriketrought.setValue(OBJECTS.SETTINGS.getl("text_Regular"));
            }
        }
        // Color
        if (styleSheet.getFgColor() == null) {
            chkColorFG.setSelected(true);
            btnColorFG.setDisable(true);
            curFGColor = null;
        } else {
            btnColorFG.setStyle("-fx-text-fill: " + styleSheet.getFgColor() + ";");
            chkColorFG.setSelected(false);
            btnColorFG.setDisable(false);
            curFGColor = styleSheet.getFgColor();
        }
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
        // Stroke
        if (styleSheet.getStroke() == null) {
            chkStrokeColor.setSelected(true);
            btnStrokeColor.setDisable(true);
            curStrokeColor = null;
        } else {
            btnStrokeColor.setStyle("-fx-background-color: " + styleSheet.getStroke() + ";");
            chkStrokeColor.setSelected(false);
            btnStrokeColor.setDisable(false);
            curStrokeColor = styleSheet.getStroke();
        }
        if (styleSheet.getStrokeWidth() == null) {
            cmbStrokeWidth.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbStrokeWidth.setValue(styleSheet.getStrokeWidth().toString());
        }
        if (styleSheet.getStrokeType() == null) {
            cmbStrokeType.setValue(OBJECTS.SETTINGS.getl("text_DEFAULT"));
        } else {
            cmbStrokeType.setValue(styleSheet.getStrokeType());
        }

        txtCss.setText(styleSheet.getCss());
        updateSample();
    }

    private void updateSample() {
        txtCss.setText(curCharStyle.getCss());
        StyleSheetChar css = originalCharStyle.duplicate();
        css.setCss(curCharStyle.getCss());
        sampleWidget.setStyle(sampleStart, sampleEnd, css.getCss());
        repaintSelectedChoices();
    }

    private void repaintSelectedChoices() {
        if (curCharStyle.equals(startingCurCharStyle)) {
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
        onExitCallback.accept(curCharStyle);
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
            curCharStyle = new StyleSheetChar();
            curCharStyle.setCss(css);
            updateCss(curCharStyle);
        }
    }

    @FXML
    private void onBtnCssDiscardAction() {
        curCharStyle = originalCharStyle.duplicate();
        updateCss(curCharStyle);
    }








}
