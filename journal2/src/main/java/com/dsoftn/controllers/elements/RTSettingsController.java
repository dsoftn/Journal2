package com.dsoftn.controllers.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.services.MoveResizeWindow;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.text_handler.StyleSheetChar;
import com.dsoftn.services.text_handler.StyleSheetParagraph;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.utils.USettings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.IntegerStringConverter;

public class RTSettingsController implements IBaseController {
    // Variables
    private Consumer<Boolean> onExitCallback;
    private Stage stage = null;
    private String myName = UJavaFX.getUniqueId();
    private String settingsName = null;
    private List<Node> dragNodes = new ArrayList<>();
    private TextHandler.Behavior behavior = null;
    private RTWidget rtwInitTextSample = null;
    private RTWidget rtwAcSample = null;
    private RTWidget rtwHlSample = null;
    private String invalidEntry = "-fx-background-color: darkred;";

    private String cssInitTextStyle = null;
    private String cssInitParStyle = null;
    private String cssAutoCompleteStyle = null;
    private String cssMarkedInteger = null;
    private String cssMarkedDouble = null;
    private String cssMarkedDate = null;
    private String cssMarkedTime = null;
    private String cssMarkedWebLink = null;
    private String cssMarkedEmail = null;
    private String cssMarkedSerbianMobileNumbers = null;
    private String cssMarkedSerbianLandlineNumbers = null;
    private String cssMarkedInternationalPhoneNumbers = null;

    // FXML variables
    @FXML
    private VBox vBoxRoot; // Root element, it is wrapped with AnchorPane in FXML
    @FXML
    private Button btnClose; // Close button

    @FXML
    private HBox hBoxTitle; // Title section
        @FXML
        private Label lblTitle; // Title label

    @FXML
    HBox hBoxInitText; // Default text section
        @FXML
        private Label lblInitText;
        @FXML
        private Button btnInitTextStyle;
        @FXML
        private Button btnInitParStyle;
        @FXML
        private Label lblInitMinHeight;
        @FXML
        private TextField txtInitMinHeight;
        @FXML
        private Label lblInitNumPar;
        @FXML
        private TextField txtInitNumPar;
        @FXML
        private Label lblPadding;
        @FXML
        private Label lblPaddingTop;
        @FXML
        private TextField txtPaddingTop;
        @FXML
        private Label lblPaddingRight;
        @FXML
        private TextField txtPaddingRight;
        @FXML
        private Label lblPaddingBottom;
        @FXML
        private TextField txtPaddingBottom;
        @FXML
        private Label lblPaddingLeft;
        @FXML
        private TextField txtPaddingLeft;

        @FXML
        private VBox vBoxInitTextSample; // Placeholder for RTWidget - Init text sample
    
    @FXML
    private VBox vBoxAC; // AutoComplete section
        @FXML
        private Label lblAcSubTitle;
        @FXML
        private CheckBox chkAcEnabled;
        @FXML
        private Label lblAcMaxRec;
        @FXML
        private Spinner<Integer> spnAcMaxRec;
        @FXML
        private Label lblAcMaxWords;
        @FXML
        private Spinner<Integer> spnAcMaxWords;
        @FXML
        private Label lblAcDelay;
        @FXML
        private Spinner<Integer> spnAcDelay;
        @FXML
        private Button btnAcStyle;
        @FXML
        private VBox vBoxAcSample; // Placeholder for RTWidget - AC sample

    @FXML
    private Separator sepAcHl; // Separator between AutoComplete and HighLighting

    @FXML
    private HBox hBoxHL; // HighLighting section
        @FXML
        private Label lblHlSubTitle;
        @FXML
        private CheckBox chkHlEnabled;
        @FXML
        private Label lblHlInt; // Integer
        @FXML
        private CheckBox chkHlInt;
        @FXML
        private Button btnHlInt;
        @FXML
        private Label lblHlDec; // Decimal
        @FXML
        private CheckBox chkHlDec;
        @FXML
        private Button btnHlDec;
        @FXML
        private Label lblHlDate; // Date
        @FXML
        private CheckBox chkHlDate;
        @FXML
        private Button btnHlDate;
        @FXML
        private Label lblHlTime; // Time
        @FXML
        private CheckBox chkHlTime;
        @FXML
        private Button btnHlTime;
        @FXML
        private Label lblHlWeb; // Web link
        @FXML
        private CheckBox chkHlWeb;
        @FXML
        private Button btnHlWeb;
        @FXML
        private Label lblHlMail; // E-Mail
        @FXML
        private CheckBox chkHlMail;
        @FXML
        private Button btnHlMail;
        @FXML
        private Label lblHlSMP; // Serbian mobile phone
        @FXML
        private CheckBox chkHlSMP;
        @FXML
        private Button btnHlSMP;
        @FXML
        private Label lblHlSLP; // Serbian landline phone
        @FXML
        private CheckBox chkHlSLP;
        @FXML
        private Button btnHlSLP;
        @FXML
        private Label lblHlIntPhone; // International phone
        @FXML
        private CheckBox chkHlIntPhone;
        @FXML
        private Button btnHlIntPhone;
        @FXML
        private VBox vBoxHlSample; // Placeholder for RTWidget - HL sample
    
    @FXML
    private Button btnDefault;
    @FXML
    private Button btnApply;
    @FXML
    private Button btnCancel;

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
        // Remove window border
        stage.initStyle(StageStyle.UNDECORATED);
        // Make window modal
        stage.initModality(Modality.WINDOW_MODAL);

        // Check if ESC is pressed
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                onExitCallback.accept(false);
                closeMe();
            }
        });

        // Setup drag nodes
        dragNodes.add(lblTitle);

        UJavaFX.setStageGeometry(settingsName, stage);
        new MoveResizeWindow(stage, dragNodes);

        setupWidgetsText();
        setupWidgetsAppearance();
        setupSpinnerValidators();
        setupData(behavior);
        setRtwInitTextSamplePadding();

        stage.show();
    }
    @Override
    public void closeMe() {
        UJavaFX.saveStageGeometry(settingsName, stage);
        stage.close();
    }
    
    public void setOnExitCallback(Consumer<Boolean> onExitCallback) {
        this.onExitCallback = onExitCallback;
    }

    // Public methods
    public void showACSection(boolean show) {
        vBoxAC.setVisible(show);
        vBoxAC.setManaged(show);
        sepAcHl.setVisible(show);
        sepAcHl.setManaged(show);
    }

    public void setBehavior(TextHandler.Behavior behavior) {
        this.behavior = behavior;
        settingsName = "RTSettings_" + behavior.name();
    }

    // Private methods
    private void setupWidgetsText() {
        lblTitle.setText(OBJECTS.SETTINGS.getl("RtSettings_Title_" + behavior.name()));
        lblInitText.setText(OBJECTS.SETTINGS.getl("RtSettings_lblInitText"));
        UJavaFX.setTooltip(lblInitText, OBJECTS.SETTINGS.getl("tt_RtSettings_InitText"), OBJECTS.SETTINGS.getl("tt_RtSettings_InitText_Title"), new Image(getClass().getResourceAsStream("/images/default.png")), 30, 30);
        btnInitTextStyle.setText(OBJECTS.SETTINGS.getl("text_TextStyle"));
        btnInitParStyle.setText(OBJECTS.SETTINGS.getl("text_ParagraphStyle"));
        lblInitMinHeight.setText(OBJECTS.SETTINGS.getl("text_MinTextBoxHeight"));
        UJavaFX.setTooltip(lblInitMinHeight, OBJECTS.SETTINGS.getl("tt_RtSettings_MinHeight"), OBJECTS.SETTINGS.getl("tt_RtSettings_MinHeight_Title"), new Image(getClass().getResourceAsStream("/images/height.png")), 30, 30);
        UJavaFX.setTooltip(txtInitMinHeight, OBJECTS.SETTINGS.getl("tt_RtSettings_MinHeight"), OBJECTS.SETTINGS.getl("tt_RtSettings_MinHeight_Title"), new Image(getClass().getResourceAsStream("/images/height.png")), 30, 30);
        lblInitNumPar.setText(OBJECTS.SETTINGS.getl("text_NumberOfParagraphsAllowed"));
        UJavaFX.setTooltip(lblInitNumPar, OBJECTS.SETTINGS.getl("tt_RtSettings_NumPar"), OBJECTS.SETTINGS.getl("tt_RtSettings_NumPar_Title"), new Image(getClass().getResourceAsStream("/images/paragraph.png")), 30, 30);
        UJavaFX.setTooltip(txtInitNumPar, OBJECTS.SETTINGS.getl("tt_RtSettings_NumPar"), OBJECTS.SETTINGS.getl("tt_RtSettings_NumPar_Title"), new Image(getClass().getResourceAsStream("/images/paragraph.png")), 30, 30);
        lblPadding.setText(OBJECTS.SETTINGS.getl("text_Padding"));
        UJavaFX.setTooltip(lblPadding, OBJECTS.SETTINGS.getl("tt_RtSettings_Padding"), OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Title"), new Image(getClass().getResourceAsStream("/images/padding.png")), 30, 30);
        lblPaddingTop.setText(OBJECTS.SETTINGS.getl("text_Top"));
        UJavaFX.setTooltip(lblPaddingTop, OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Top"), OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Top_Title"), new Image(getClass().getResourceAsStream("/images/move.png")), 30, 30);
        lblPaddingRight.setText(OBJECTS.SETTINGS.getl("text_Right"));
        UJavaFX.setTooltip(lblPaddingRight, OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Right"), OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Right_Title"), new Image(getClass().getResourceAsStream("/images/move.png")), 30, 30);
        lblPaddingBottom.setText(OBJECTS.SETTINGS.getl("text_Bottom"));
        UJavaFX.setTooltip(lblPaddingBottom, OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Bottom"), OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Bottom_Title"), new Image(getClass().getResourceAsStream("/images/move.png")), 30, 30);
        lblPaddingLeft.setText(OBJECTS.SETTINGS.getl("text_Left"));
        UJavaFX.setTooltip(lblPaddingLeft, OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Left"), OBJECTS.SETTINGS.getl("tt_RtSettings_Padding_Left_Title"), new Image(getClass().getResourceAsStream("/images/move.png")), 30, 30);

        lblAcSubTitle.setText(OBJECTS.SETTINGS.getl("text_AutoCompleteRecommendations"));
        chkAcEnabled.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        lblAcMaxRec.setText(OBJECTS.SETTINGS.getl("text_MaxNumberOfRecommendations"));
        UJavaFX.setTooltip(lblAcMaxRec, OBJECTS.SETTINGS.getl("tt_RtSettings_MaxRec"), OBJECTS.SETTINGS.getl("tt_RtSettings_MaxRec_Title"), new Image(getClass().getResourceAsStream("/images/recommendation.png")), 30, 30);
        UJavaFX.setTooltip(spnAcMaxRec, OBJECTS.SETTINGS.getl("tt_RtSettings_MaxRec"), OBJECTS.SETTINGS.getl("tt_RtSettings_MaxRec_Title"), new Image(getClass().getResourceAsStream("/images/recommendation.png")), 30, 30);
        lblAcMaxWords.setText(OBJECTS.SETTINGS.getl("text_MaxWords"));
        UJavaFX.setTooltip(lblAcMaxWords, OBJECTS.SETTINGS.getl("tt_RtSettings_MaxWords"), OBJECTS.SETTINGS.getl("tt_RtSettings_MaxWords_Title"), new Image(getClass().getResourceAsStream("/images/words.png")), 30, 30);
        UJavaFX.setTooltip(spnAcMaxWords, OBJECTS.SETTINGS.getl("tt_RtSettings_MaxWords"), OBJECTS.SETTINGS.getl("tt_RtSettings_MaxWords_Title"), new Image(getClass().getResourceAsStream("/images/words.png")), 30, 30);
        lblAcDelay.setText(OBJECTS.SETTINGS.getl("text_DelayMS"));
        UJavaFX.setTooltip(lblAcDelay, OBJECTS.SETTINGS.getl("tt_RtSettings_Delay"), OBJECTS.SETTINGS.getl("tt_RtSettings_Delay_Title"), new Image(getClass().getResourceAsStream("/images/delay.png")), 30, 30);
        UJavaFX.setTooltip(spnAcDelay, OBJECTS.SETTINGS.getl("tt_RtSettings_Delay"), OBJECTS.SETTINGS.getl("tt_RtSettings_Delay_Title"), new Image(getClass().getResourceAsStream("/images/delay.png")), 30, 30);
        btnAcStyle.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlSubTitle.setText(OBJECTS.SETTINGS.getl("text_HighLightingText"));
        chkHlEnabled.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        lblHlInt.setText(OBJECTS.SETTINGS.getl("text_Integer"));
        chkHlInt.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlInt.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlDec.setText(OBJECTS.SETTINGS.getl("text_Decimal"));
        chkHlDec.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlDec.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlDate.setText(OBJECTS.SETTINGS.getl("text_Date"));
        chkHlDate.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlDate.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlTime.setText(OBJECTS.SETTINGS.getl("text_Time"));
        chkHlTime.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlTime.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlWeb.setText(OBJECTS.SETTINGS.getl("text_WebLink"));
        chkHlWeb.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlWeb.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlMail.setText(OBJECTS.SETTINGS.getl("text_Email"));
        chkHlMail.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlMail.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlSMP.setText(OBJECTS.SETTINGS.getl("text_SerbianMobileNumber"));
        chkHlSMP.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlSMP.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlSLP.setText(OBJECTS.SETTINGS.getl("text_SerbianLandlineNumber"));
        chkHlSLP.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlSLP.setText(OBJECTS.SETTINGS.getl("text_Style"));
        lblHlIntPhone.setText(OBJECTS.SETTINGS.getl("text_InternationalPhoneNumber"));
        chkHlIntPhone.setText(OBJECTS.SETTINGS.getl("text_Enabled"));
        btnHlIntPhone.setText(OBJECTS.SETTINGS.getl("text_Style"));
        btnDefault.setText(OBJECTS.SETTINGS.getl("text_SetDefaultValues"));
        btnApply.setText(OBJECTS.SETTINGS.getl("text_Apply"));
        btnCancel.setText(OBJECTS.SETTINGS.getl("text_Cancel"));
    }

    private void setupWidgetsAppearance() {
        // Add RTWidgets
        rtwInitTextSample = new RTWidget(null);
        rtwInitTextSample.setReadOnly(true);
        rtwInitTextSample.getStyleClass().add("rich-text");
        VBox.setVgrow(rtwInitTextSample, javafx.scene.layout.Priority.ALWAYS);
        setRtwInitTextSamplePadding();
        vBoxInitTextSample.getChildren().add(rtwInitTextSample);

        rtwAcSample = new RTWidget(null);
        rtwAcSample.setMinTextWidgetHeight(35);
        rtwAcSample.setReadOnly(true);
        rtwAcSample.getStyleClass().add("rich-text");
        VBox.setVgrow(rtwAcSample, javafx.scene.layout.Priority.ALWAYS);
        vBoxAcSample.getChildren().add(rtwAcSample);
        
        rtwHlSample = new RTWidget(null);
        rtwHlSample.setMinTextWidgetHeight(35);
        rtwHlSample.setPadding(new Insets(0, 0, 0, 10));
        rtwHlSample.setReadOnly(true);
        rtwHlSample.getStyleClass().add("rich-text");
        VBox.setVgrow(rtwHlSample, javafx.scene.layout.Priority.ALWAYS);
        vBoxHlSample.getChildren().add(rtwHlSample);

        //  Add image to close button
        Image imgClose = new Image(getClass().getResourceAsStream("/images/close.png"));
        ImageView imgCloseView = new ImageView(imgClose);
        imgCloseView.setPreserveRatio(true);
        imgCloseView.setFitHeight(30);
        btnClose.setGraphic(imgCloseView);

        // Text Field for initial text max height
        txtInitMinHeight.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidMinHeight(newValue)) {
                lblInitMinHeight.setStyle("-fx-background-color: transparent;");
                txtInitMinHeight.setStyle("-fx-background-color: #051923;");
            }
        });

        // Text Field for initial text number of paragraphs
        txtInitNumPar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("0")) {
                txtInitNumPar.setText("MAX");
                return;
            }
            if (isValidNumPar(newValue)) {
                lblInitNumPar.setStyle("-fx-background-color: transparent;");
                txtInitNumPar.setStyle("-fx-background-color: #051923;");
            }
        });

        // Text Field for padding top
        txtPaddingTop.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidPaddingTop(newValue)) {
                lblPaddingTop.setStyle("-fx-background-color: transparent;");
                txtPaddingTop.setStyle("-fx-background-color: #051923;");
            }
            setRtwInitTextSamplePadding();
        });

        // Text Field for padding right
        txtPaddingRight.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidPaddingRight(newValue)) {
                lblPaddingRight.setStyle("-fx-background-color: transparent;");
                txtPaddingRight.setStyle("-fx-background-color: #051923;");
            }
            setRtwInitTextSamplePadding();
        });

        // Text Field for padding bottom
        txtPaddingBottom.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidPaddingBottom(newValue)) {
                lblPaddingBottom.setStyle("-fx-background-color: transparent;");
                txtPaddingBottom.setStyle("-fx-background-color: #051923;");
            }
            setRtwInitTextSamplePadding();
        });

        // Text Field for padding left
        txtPaddingLeft.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidPaddingLeft(newValue)) {
                lblPaddingLeft.setStyle("-fx-background-color: transparent;");
                txtPaddingLeft.setStyle("-fx-background-color: #051923;");
            }
            setRtwInitTextSamplePadding();
        });

        // Spinner for AutoComplete - Max recommendations
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendations").getMinINT(),
                OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendations").getMaxINT(),
                OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendations").getValueINT()
                );
        spnAcMaxRec.setValueFactory(valueFactory);
        
        // Spinner for AutoComplete - Max words
        valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendedWords").getMinINT(),
                OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendedWords").getMaxINT(),
                OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendedWords").getValueINT()
                );
        spnAcMaxWords.setValueFactory(valueFactory);

        // Spinner for AutoComplete - Delay
        valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                OBJECTS.SETTINGS.getUserSettingsItem("AutoCompleteDelay").getMinINT(),
                OBJECTS.SETTINGS.getUserSettingsItem("AutoCompleteDelay").getMaxINT(),
                OBJECTS.SETTINGS.getUserSettingsItem("AutoCompleteDelay").getValueINT()
                );
        spnAcDelay.setValueFactory(valueFactory);

    }

    private void setRtwInitTextSamplePadding() {
        rtwInitTextSample.setPadding(
            isValidPaddingTop(txtPaddingTop.getText()) ? Double.parseDouble(txtPaddingTop.getText()) : (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingTop", behavior).getValueINT(),
            isValidPaddingRight(txtPaddingRight.getText()) ? Double.parseDouble(txtPaddingRight.getText()) : (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingRight", behavior).getValueINT(),
            isValidPaddingBottom(txtPaddingBottom.getText()) ? Double.parseDouble(txtPaddingBottom.getText()) : (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingBottom", behavior).getValueINT(),
            isValidPaddingLeft(txtPaddingLeft.getText()) ? Double.parseDouble(txtPaddingLeft.getText()) : (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingLeft", behavior).getValueINT(),
            behavior
            );
    }


    private void setupSpinnerValidators() {
        TextFormatter<Integer> acMaxRecFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, change -> {
            String newText = change.getControlNewText();
            if (newText != null && isValidAcMaxRec(newText)) {
                lblAcMaxRec.setStyle("-fx-background-color: transparent;");
                spnAcMaxRec.setStyle("-fx-background-color: transparent;");
                updateSamples();
                return change;
            } else {
                return null;
            }
        });

        TextFormatter<Integer> acMaxWordsFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, change -> {
            String newText = change.getControlNewText();
            if (newText != null && isValidAcMaxWords(newText)) {
                lblAcMaxWords.setStyle("-fx-background-color: transparent;");
                spnAcMaxWords.setStyle("-fx-background-color: transparent;");
                return change;
            } else {
                return null;
            }
        });

        TextFormatter<Integer> acDelayFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, change -> {
            String newText = change.getControlNewText();
            if (newText != null && isValidAcDelay(newText)) {
                lblAcDelay.setStyle("-fx-background-color: transparent;");
                spnAcDelay.setStyle("-fx-background-color: transparent;");
                return change;
            } else {
                return null;
            }
        });

        // Set spinner validators
        spnAcMaxRec.getEditor().setTextFormatter(acMaxRecFormatter);
        spnAcMaxWords.getEditor().setTextFormatter(acMaxWordsFormatter);
        spnAcDelay.getEditor().setTextFormatter(acDelayFormatter);
    }

    private void setupData(TextHandler.Behavior behavior) {
        // Default text and paragraph style
        txtInitMinHeight.setText(USettings.getAppOrUserSettingsItem("MinRTWidgetHeight", behavior).getValueINT().toString());
        if (USettings.getAppOrUserSettingsItem("MaxNumberOfParagraphs", behavior).getValueINT() < 1) {
            txtInitNumPar.setText("MAX");
        } else {
            txtInitNumPar.setText(USettings.getAppOrUserSettingsItem("MaxNumberOfParagraphs", behavior).getValueINT().toString());
        }
        
        cssInitTextStyle = USettings.getAppOrUserSettingsItem("CssDefaultTextStyle", behavior).getValueSTRING();
        cssInitParStyle = USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", behavior).getValueSTRING();
        txtPaddingTop.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingTop", behavior).getValueINT().toString());
        txtPaddingRight.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingRight", behavior).getValueINT().toString());
        txtPaddingBottom.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingBottom", behavior).getValueINT().toString());
        txtPaddingLeft.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingLeft", behavior).getValueINT().toString());

        // AutoComplete
        chkAcEnabled.setSelected(USettings.getAppOrUserSettingsItem("AllowAutoComplete", behavior).getValueBOOLEAN());
        spnAcMaxRec.getEditor().setText(USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendations", behavior).getValueINT().toString());
        spnAcMaxWords.getEditor().setText(USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendedWords", behavior).getValueINT().toString());
        spnAcDelay.getEditor().setText(USettings.getAppOrUserSettingsItem("AutoCompleteDelay", behavior).getValueINT().toString());

        cssAutoCompleteStyle = USettings.getAppOrUserSettingsItem("CssAutoCompleteStyle", behavior).getValueSTRING();

        // HighLighting
        chkHlEnabled.setSelected(USettings.getAppOrUserSettingsItem("AllowMarking", behavior).getValueBOOLEAN());
        chkHlInt.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingIntegers", behavior).getValueBOOLEAN());
        chkHlDec.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingDoubles", behavior).getValueBOOLEAN());
        chkHlDate.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingDates", behavior).getValueBOOLEAN());
        chkHlTime.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingTimes", behavior).getValueBOOLEAN());
        chkHlWeb.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingWebLinks", behavior).getValueBOOLEAN());
        chkHlMail.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingEmails", behavior).getValueBOOLEAN());
        chkHlSMP.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingSerbianMobileNumbers", behavior).getValueBOOLEAN());
        chkHlSLP.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingSerbianLandlineNumbers", behavior).getValueBOOLEAN());
        chkHlIntPhone.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingInternationalPhoneNumbers", behavior).getValueBOOLEAN());

        cssMarkedInteger = USettings.getAppOrUserSettingsItem("CssMarkedInteger", behavior).getValueSTRING();
        cssMarkedDouble = USettings.getAppOrUserSettingsItem("CssMarkedDouble", behavior).getValueSTRING();
        cssMarkedDate = USettings.getAppOrUserSettingsItem("CssMarkedDate", behavior).getValueSTRING();
        cssMarkedTime = USettings.getAppOrUserSettingsItem("CssMarkedTime", behavior).getValueSTRING();
        cssMarkedWebLink = USettings.getAppOrUserSettingsItem("CssMarkedWebLink", behavior).getValueSTRING();
        cssMarkedEmail = USettings.getAppOrUserSettingsItem("CssMarkedEmail", behavior).getValueSTRING();
        cssMarkedSerbianMobileNumbers = USettings.getAppOrUserSettingsItem("CssMarkedSerbianMobileNumbers", behavior).getValueSTRING();
        cssMarkedSerbianLandlineNumbers = USettings.getAppOrUserSettingsItem("CssMarkedSerbianLandlineNumbers", behavior).getValueSTRING();
        cssMarkedInternationalPhoneNumbers = USettings.getAppOrUserSettingsItem("CssMarkedInternationalPhoneNumbers", behavior).getValueSTRING();

        updateSamples();
    }

    private void setupDefaultData(TextHandler.Behavior behavior) {
        // Default text and paragraph style
        txtInitMinHeight.setText(USettings.getAppOrUserSettingsItem("MinRTWidgetHeight", behavior).getDefaultValueINT().toString());
        txtInitNumPar.setText("MAX");
        txtPaddingTop.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingTop", behavior).getDefaultValueINT().toString());
        txtPaddingRight.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingRight", behavior).getDefaultValueINT().toString());
        txtPaddingBottom.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingBottom", behavior).getDefaultValueINT().toString());
        txtPaddingLeft.setText(USettings.getAppOrUserSettingsItem("RTWidgetPaddingLeft", behavior).getDefaultValueINT().toString());
        
        cssInitTextStyle = USettings.getAppOrUserSettingsItem("CssDefaultTextStyle", behavior).getDefaultValueSTRING();
        cssInitParStyle = USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", behavior).getDefaultValueSTRING();

        // AutoComplete
        chkAcEnabled.setSelected(USettings.getAppOrUserSettingsItem("AllowAutoComplete", behavior).getDefaultValueBOOLEAN());
        spnAcMaxRec.getEditor().setText(USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendations", behavior).getDefaultValueINT().toString());
        spnAcMaxWords.getEditor().setText(USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendedWords", behavior).getDefaultValueINT().toString());
        spnAcDelay.getEditor().setText(USettings.getAppOrUserSettingsItem("AutoCompleteDelay", behavior).getDefaultValueINT().toString());

        cssAutoCompleteStyle = USettings.getAppOrUserSettingsItem("CssAutoCompleteStyle", behavior).getDefaultValueSTRING();

        // HighLighting
        chkHlEnabled.setSelected(USettings.getAppOrUserSettingsItem("AllowMarking", behavior).getDefaultValueBOOLEAN());
        chkHlInt.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingIntegers", behavior).getDefaultValueBOOLEAN());
        chkHlDec.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingDoubles", behavior).getDefaultValueBOOLEAN());
        chkHlDate.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingDates", behavior).getDefaultValueBOOLEAN());
        chkHlTime.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingTimes", behavior).getDefaultValueBOOLEAN());
        chkHlWeb.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingWebLinks", behavior).getDefaultValueBOOLEAN());
        chkHlMail.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingEmails", behavior).getDefaultValueBOOLEAN());
        chkHlSMP.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingSerbianMobileNumbers", behavior).getDefaultValueBOOLEAN());
        chkHlSLP.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingSerbianLandlineNumbers", behavior).getDefaultValueBOOLEAN());
        chkHlIntPhone.setSelected(USettings.getAppOrUserSettingsItem("AllowMarkingInternationalPhoneNumbers", behavior).getDefaultValueBOOLEAN());

        cssMarkedInteger = USettings.getAppOrUserSettingsItem("CssMarkedInteger", behavior).getDefaultValueSTRING();
        cssMarkedDouble = USettings.getAppOrUserSettingsItem("CssMarkedDouble", behavior).getDefaultValueSTRING();
        cssMarkedDate = USettings.getAppOrUserSettingsItem("CssMarkedDate", behavior).getDefaultValueSTRING();
        cssMarkedTime = USettings.getAppOrUserSettingsItem("CssMarkedTime", behavior).getDefaultValueSTRING();
        cssMarkedWebLink = USettings.getAppOrUserSettingsItem("CssMarkedWebLink", behavior).getDefaultValueSTRING();
        cssMarkedEmail = USettings.getAppOrUserSettingsItem("CssMarkedEmail", behavior).getDefaultValueSTRING();
        cssMarkedSerbianMobileNumbers = USettings.getAppOrUserSettingsItem("CssMarkedSerbianMobileNumbers", behavior).getDefaultValueSTRING();
        cssMarkedSerbianLandlineNumbers = USettings.getAppOrUserSettingsItem("CssMarkedSerbianLandlineNumbers", behavior).getDefaultValueSTRING();
        cssMarkedInternationalPhoneNumbers = USettings.getAppOrUserSettingsItem("CssMarkedInternationalPhoneNumbers", behavior).getDefaultValueSTRING();

        updateSamples();
    }

    private boolean saveData() {
        if (!isValid()) return false;

        String behaviorName = "_" + behavior.name();

        // Default text and paragraph style
        OBJECTS.SETTINGS.setApp("MinRTWidgetHeight" + behaviorName, UNumbers.toInteger(txtInitMinHeight.getText()));
        if (txtInitNumPar.getText().equals("MAX")) {
            OBJECTS.SETTINGS.setApp("MaxNumberOfParagraphs" + behaviorName, 0);
        } else {
            OBJECTS.SETTINGS.setApp("MaxNumberOfParagraphs" + behaviorName, UNumbers.toInteger(txtInitNumPar.getText()));
        }
        OBJECTS.SETTINGS.setApp("RTWidgetPaddingTop" + behaviorName, UNumbers.toInteger(txtPaddingTop.getText()));
        OBJECTS.SETTINGS.setApp("RTWidgetPaddingRight" + behaviorName, UNumbers.toInteger(txtPaddingRight.getText()));
        OBJECTS.SETTINGS.setApp("RTWidgetPaddingBottom" + behaviorName, UNumbers.toInteger(txtPaddingBottom.getText()));
        OBJECTS.SETTINGS.setApp("RTWidgetPaddingLeft" + behaviorName, UNumbers.toInteger(txtPaddingLeft.getText()));

        OBJECTS.SETTINGS.setApp("CssDefaultTextStyle" + behaviorName, cssInitTextStyle);
        OBJECTS.SETTINGS.setApp("CssDefaultParagraphStyle" + behaviorName, cssInitParStyle);

        // AutoComplete
        OBJECTS.SETTINGS.setApp("AllowAutoComplete" + behaviorName, chkAcEnabled.isSelected());
        OBJECTS.SETTINGS.setApp("MaxAutoCompleteRecommendations" + behaviorName, spnAcMaxRec.getValue());
        OBJECTS.SETTINGS.setApp("MaxAutoCompleteRecommendedWords" + behaviorName, spnAcMaxWords.getValue());
        OBJECTS.SETTINGS.setApp("AutoCompleteDelay" + behaviorName, spnAcDelay.getValue());

        OBJECTS.SETTINGS.setApp("CssAutoCompleteStyle" + behaviorName, cssAutoCompleteStyle);

        // HighLighting
        OBJECTS.SETTINGS.setApp("AllowMarking" + behaviorName, chkHlEnabled.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingIntegers" + behaviorName, chkHlInt.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingDoubles" + behaviorName, chkHlDec.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingDates" + behaviorName, chkHlDate.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingTimes" + behaviorName, chkHlTime.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingWebLinks" + behaviorName, chkHlWeb.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingEmails" + behaviorName, chkHlMail.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingSerbianMobileNumbers" + behaviorName, chkHlSMP.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingSerbianLandlineNumbers" + behaviorName, chkHlSLP.isSelected());
        OBJECTS.SETTINGS.setApp("AllowMarkingInternationalPhoneNumbers" + behaviorName, chkHlIntPhone.isSelected());

        OBJECTS.SETTINGS.setApp("CssMarkedInteger" + behaviorName, cssMarkedInteger);
        OBJECTS.SETTINGS.setApp("CssMarkedDouble" + behaviorName, cssMarkedDouble);
        OBJECTS.SETTINGS.setApp("CssMarkedDate" + behaviorName, cssMarkedDate);
        OBJECTS.SETTINGS.setApp("CssMarkedTime" + behaviorName, cssMarkedTime);
        OBJECTS.SETTINGS.setApp("CssMarkedWebLink" + behaviorName, cssMarkedWebLink);
        OBJECTS.SETTINGS.setApp("CssMarkedEmail" + behaviorName, cssMarkedEmail);
        OBJECTS.SETTINGS.setApp("CssMarkedSerbianMobileNumbers" + behaviorName, cssMarkedSerbianMobileNumbers);
        OBJECTS.SETTINGS.setApp("CssMarkedSerbianLandlineNumbers" + behaviorName, cssMarkedSerbianLandlineNumbers);
        OBJECTS.SETTINGS.setApp("CssMarkedInternationalPhoneNumbers" + behaviorName, cssMarkedInternationalPhoneNumbers);

        return true;
    }

    private boolean isValid() {
        boolean success = true;

        // Default text and paragraph style
        if (!isValidMinHeight(null)) { success = false; }
        if (!isValidNumPar(null)) { success = false; }
        if (!isValidPaddingTop(null)) { success = false; }
        if (!isValidPaddingRight(null)) { success = false; }
        if (!isValidPaddingBottom(null)) { success = false; }
        if (!isValidPaddingLeft(null)) { success = false; }

        // AutoComplete
        if (!isValidAcMaxRec(null)) { success = false; }
        if (!isValidAcMaxWords(null)) { success = false; }
        if (!isValidAcDelay(null)) { success = false; }

        if (
            cssInitTextStyle == null ||
            cssInitParStyle == null ||
            cssAutoCompleteStyle == null ||
            cssMarkedInteger == null ||
            cssMarkedDouble == null ||
            cssMarkedDate == null ||
            cssMarkedTime == null ||
            cssMarkedWebLink == null ||
            cssMarkedEmail == null ||
            cssMarkedSerbianMobileNumbers == null ||
            cssMarkedSerbianLandlineNumbers == null ||
            cssMarkedInternationalPhoneNumbers == null
        )
        { success = false; }

        return success;
    }

    private boolean isValidMinHeight(String text) {
        if (text == null) text = txtInitMinHeight.getText();
        if (UNumbers.toInteger(text) == null || UNumbers.toInteger(text) < USettings.getAppOrUserSettingsItem("MinRTWidgetHeight", behavior).getMinINT()) {
            lblInitMinHeight.setStyle(invalidEntry);
            txtInitMinHeight.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidNumPar(String text) {
        if (text == null) text = txtInitNumPar.getText();
        if (text.equals("MAX")) return true;
        if (UNumbers.toInteger(text) == null || UNumbers.toInteger(text) < 1) {
            lblInitNumPar.setStyle(invalidEntry);
            txtInitNumPar.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidPaddingTop(String text) {
        if (text == null) text = txtPaddingTop.getText();
        if (UNumbers.toDouble(text) == null || UNumbers.toDouble(text) < OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingTop").getMinINT() || UNumbers.toDouble(text) > OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingTop").getMaxINT()) {
            lblPaddingTop.setStyle(invalidEntry);
            txtPaddingTop.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidPaddingRight(String text) {
        if (text == null) text = txtPaddingRight.getText();
        if (UNumbers.toDouble(text) == null || UNumbers.toDouble(text) < OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingRight").getMinINT() || UNumbers.toDouble(text) > OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingRight").getMaxINT()) {
            lblPaddingRight.setStyle(invalidEntry);
            txtPaddingRight.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidPaddingBottom(String text) {
        if (text == null) text = txtPaddingBottom.getText();
        if (UNumbers.toDouble(text) == null || UNumbers.toDouble(text) < OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingBottom").getMinINT() || UNumbers.toDouble(text) > OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingBottom").getMaxINT()) {
            lblPaddingBottom.setStyle(invalidEntry);
            txtPaddingBottom.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidPaddingLeft(String text) {
        if (text == null) text = txtPaddingLeft.getText();
        if (UNumbers.toDouble(text) == null || UNumbers.toDouble(text) < OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingLeft").getMinINT() || UNumbers.toDouble(text) > OBJECTS.SETTINGS.getUserSettingsItem("RTWidgetPaddingLeft").getMaxINT()) {
            lblPaddingLeft.setStyle(invalidEntry);
            txtPaddingLeft.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidAcMaxRec(String text) {
        if (text == null) text = spnAcMaxRec.getValue().toString();

        if (UNumbers.toInteger(text) == null ||
            UNumbers.toInteger(text) < OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendations").getMinINT() ||
            UNumbers.toInteger(text) > OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendations").getMaxINT()) {
            lblAcMaxRec.setStyle(invalidEntry);
            spnAcMaxRec.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidAcMaxWords(String text) {
        if (text == null) text = spnAcMaxWords.getValue().toString();

        if (UNumbers.toInteger(text) == null ||
            UNumbers.toInteger(text) < OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendedWords").getMinINT() ||
            UNumbers.toInteger(text) > OBJECTS.SETTINGS.getUserSettingsItem("MaxAutoCompleteRecommendedWords").getMaxINT()) {
            lblAcMaxWords.setStyle(invalidEntry);
            spnAcMaxWords.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private boolean isValidAcDelay(String text) {
        if (text == null) text = spnAcDelay.getValue().toString();

        if (UNumbers.toInteger(text) == null ||
            UNumbers.toInteger(text) < OBJECTS.SETTINGS.getUserSettingsItem("AutoCompleteDelay").getMinINT() ||
            UNumbers.toInteger(text) > OBJECTS.SETTINGS.getUserSettingsItem("AutoCompleteDelay").getMaxINT()) {
            lblAcDelay.setStyle(invalidEntry);
            spnAcDelay.setStyle(invalidEntry);
            return false;
        }
        return true;
    }

    private void updateSamples() {
        if (!isValid()) return;

        String text;
        StyleSheetChar initStyle = new StyleSheetChar(cssInitTextStyle);

        // Init text sample
        rtwInitTextSample.setParagraphCss(new StyleSheetParagraph(cssInitParStyle));
        rtwInitTextSample.setCssChar(new StyleSheetChar(cssInitTextStyle));
        rtwInitTextSample.setTextPlain(OBJECTS.SETTINGS.getl("RtTextSample_Init"));
        rtwInitTextSample.setStyle(1, OBJECTS.SETTINGS.getl("RtTextSample_Init").length(), initStyle.getCss());

        // AutoComplete sample
        StyleSheetParagraph acParStyle = new StyleSheetParagraph();
        acParStyle.setAlignment("center");
        rtwAcSample.setParagraphCss(new StyleSheetParagraph(acParStyle.getCss()));
        rtwAcSample.setCssChar(new StyleSheetChar(cssInitTextStyle));
        text = OBJECTS.SETTINGS.getl("RtTextSample_AC1") + " ";
        int startAc = text.length();
        String textAcSample = OBJECTS.SETTINGS.getl("RtTextSample_AC2").replace("#1", spnAcMaxRec.getValue().toString());
        text += textAcSample + " " + OBJECTS.SETTINGS.getl("RtTextSample_AC1").toLowerCase() + ".";
        int endAc = startAc + textAcSample.length() + 1;
        rtwAcSample.setTextPlain(text);
        rtwAcSample.setStyle(1, text.length(), initStyle.getCss());
        StyleSheetChar acStyle = initStyle.duplicate();
        acStyle.setCss(cssAutoCompleteStyle);
        if (chkAcEnabled.isSelected()) {
            rtwAcSample.setStyle(startAc, endAc, acStyle.getCss());
        }

        // HighLighting sample
        rtwHlSample.setCssChar(new StyleSheetChar(cssInitTextStyle));
        text = OBJECTS.SETTINGS.getl("text_Samples") + "\n";
        
        text += OBJECTS.SETTINGS.getl("text_Integer") + ": ";
        int posInt = text.length() + 2;
        text += "123\n";
        
        text += OBJECTS.SETTINGS.getl("text_Decimal") + ": ";
        int posDec = text.length() + 3;
        text += "3.14\n";

        text += OBJECTS.SETTINGS.getl("text_Date") + ": ";
        int posDate = text.length() + 4;
        text += "29.09.1975.\n";

        text += OBJECTS.SETTINGS.getl("text_Time") + ": ";
        int posTime = text.length() + 5;
        text += "12:34:56\n";

        text += OBJECTS.SETTINGS.getl("text_WebLink") + ": ";
        int posWeb = text.length() + 6;
        text += "https://www.google.com\n";

        text += OBJECTS.SETTINGS.getl("text_Email") + ": ";
        int posMail = text.length() + 7;
        text += "dsoftn@gmail.com\n";

        text += OBJECTS.SETTINGS.getl("text_SerbianMobileNumber") + ": ";
        int posSMP = text.length() + 8;
        text += "063/593-728\n";

        text += OBJECTS.SETTINGS.getl("text_SerbianLandlineNumber") + ": ";
        int posSLP = text.length() + 9;
        text += "011/123-4567\n";

        text += OBJECTS.SETTINGS.getl("text_InternationalPhoneNumber") + ": ";
        int posIntPhone = text.length() + 10;
        text += "+385/44/123-4567\n";

        rtwHlSample.setTextPlain(text);
        StyleSheetChar hlStyle = initStyle.duplicate();

        hlStyle.setCss(cssMarkedInteger);
        if (chkHlEnabled.isSelected() && chkHlInt.isSelected()) {
            rtwHlSample.setStyle(posInt, posInt + "123".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedDouble);
        if (chkHlEnabled.isSelected() && chkHlDec.isSelected()) {
            rtwHlSample.setStyle(posDec, posDec + "3.14".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedDate);
        if (chkHlEnabled.isSelected() && chkHlDate.isSelected()) {
            rtwHlSample.setStyle(posDate, posDate + "29.09.1975.".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedTime);
        if (chkHlEnabled.isSelected() && chkHlTime.isSelected()) {
            rtwHlSample.setStyle(posTime, posTime + "12:34:56".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedWebLink);
        if (chkHlEnabled.isSelected() && chkHlWeb.isSelected()) {
            rtwHlSample.setStyle(posWeb, posWeb + "https://www.google.com".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedEmail);
        if (chkHlEnabled.isSelected() && chkHlMail.isSelected()) {
            rtwHlSample.setStyle(posMail, posMail + "dsoftn@gmail.com".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedSerbianMobileNumbers);
        if (chkHlEnabled.isSelected() && chkHlSMP.isSelected()) {
            rtwHlSample.setStyle(posSMP, posSMP + "063/593-728".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedSerbianLandlineNumbers);
        if (chkHlEnabled.isSelected() && chkHlSLP.isSelected()) {
            rtwHlSample.setStyle(posSLP, posSLP + "011/123-4567".length(), hlStyle.getCss());
        }
        hlStyle = initStyle.duplicate();
        hlStyle.setCss(cssMarkedInternationalPhoneNumbers);
        if (chkHlEnabled.isSelected() && chkHlIntPhone.isSelected()) {
            rtwHlSample.setStyle(posIntPhone, posIntPhone + "+385/44/123-4567".length(), hlStyle.getCss());
        }

        StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
        cssParagraph.setAlignment("center");
        rtwHlSample.setParagraphStyle(0, cssParagraph.getCss());
    }
        

    // FXML methods
    @FXML
    private void onBtnCancelAction(ActionEvent event) {
        onExitCallback.accept(false);
        closeMe();
    }

    @FXML
    private void onBtnApplyAction(ActionEvent event) {
        if (saveData()) {
            OBJECTS.SETTINGS.save(false, false, true);
            onExitCallback.accept(true);
            closeMe();
        } else {
            MsgBoxController msgBoxFailed = DIALOGS.getMsgBoxController(stage);
            msgBoxFailed.setTitleText(OBJECTS.SETTINGS.getl("text_Saving"));
            msgBoxFailed.setHeaderText(OBJECTS.SETTINGS.getl("text_Failed_to_Save_Settings"));
            msgBoxFailed.setHeaderIcon(MsgBoxIcon.WARNING);
            msgBoxFailed.setContentText(OBJECTS.SETTINGS.getl("RtSettings_SaveFailed"));
            msgBoxFailed.setContentIcon(MsgBoxIcon.SETTINGS);
            msgBoxFailed.setButtons(MsgBoxButton.OK);
            msgBoxFailed.setDefaultButton(MsgBoxButton.OK);
            msgBoxFailed.startMe();
        }
    }

    @FXML
    private void onBtnCloseAction(ActionEvent event) {
        onExitCallback.accept(false);
        closeMe();
    }

    @FXML
    private void onBtnDefaultAction(ActionEvent event) {
        setupDefaultData(behavior);
    }

    @FXML
    private void OnChkAcEnabledAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlEnabledAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlIntAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlDecAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlDateAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlTimeAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlWebAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlMailAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlSMPAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlSLPAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onChkHlIntPhoneAction(ActionEvent event) {
        updateSamples();
    }

    @FXML
    private void onBtnInitStyleAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar(cssInitTextStyle);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, css, css, this::onInitStyleExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("RtSettings_lblInitText"));
        formatChar.startMe();
    }

    private void onInitStyleExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssInitTextStyle = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnInitParStyleAction(ActionEvent event) {
        StyleSheetParagraph css = new StyleSheetParagraph(cssInitParStyle);

        FormatParagraphController formatParagraph = DIALOGS.getFormatParagraphController(stage, behavior, css, css, new StyleSheetChar(cssInitTextStyle), this::onInitParStyleExitCallback);
        formatParagraph.setTitle(OBJECTS.SETTINGS.getl("RtSettings_lblInitParagraphStyle"));
        formatParagraph.startMe();
    }

    private void onInitParStyleExitCallback(StyleSheetParagraph styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssInitParStyle = styleSheet.getCss();
        updateSamples();
    }


    @FXML
    private void onBtnAcStyleAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssAutoCompleteStyle);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onAcStyleExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_AutoCompleteRecommendations"));
        formatChar.startMe();
    }

    private void onAcStyleExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssAutoCompleteStyle = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlIntAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedInteger);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlIntExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_Integer"));
        formatChar.startMe();
    }

    private void onHlIntExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedInteger = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlDecAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedDouble);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlDecExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_Decimal"));
        formatChar.startMe();
    }

    private void onHlDecExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedDouble = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlDateAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedDate);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlDateExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_Date"));
        formatChar.startMe();
    }

    private void onHlDateExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedDate = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlTimeAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedTime);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlTimeExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_Time"));
        formatChar.startMe();
    }

    private void onHlTimeExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedTime = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlWebAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedWebLink);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlWebExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_WebLink"));
        formatChar.startMe();
    }

    private void onHlWebExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedWebLink = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlMailAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedEmail);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlMailExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_Email"));
        formatChar.startMe();
    }

    private void onHlMailExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedEmail = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlSMPAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedSerbianMobileNumbers);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlSMPExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_SerbianMobileNumber"));
        formatChar.startMe();
    }

    private void onHlSMPExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedSerbianMobileNumbers = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlSLPAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedSerbianLandlineNumbers);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlSLPExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_SerbianLandlineNumber"));
        formatChar.startMe();
    }

    private void onHlSLPExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedSerbianLandlineNumbers = styleSheet.getCss();
        updateSamples();
    }

    @FXML
    private void onBtnHlIntPhoneAction(ActionEvent event) {
        StyleSheetChar css = new StyleSheetChar();
        css.setCss(cssMarkedInternationalPhoneNumbers);

        FormatCharController formatChar = DIALOGS.getFormatCharController(stage, behavior, new StyleSheetChar(cssInitTextStyle), css, this::onHlIntPhoneExitCallback);
        formatChar.setTitle(OBJECTS.SETTINGS.getl("text_InternationalPhoneNumber"));
        formatChar.startMe();
    }

    private void onHlIntPhoneExitCallback(StyleSheetChar styleSheet) {
        if (styleSheet == null) {
            return;
        }
        
        cssMarkedInternationalPhoneNumbers = styleSheet.getCss();
        updateSamples();
    }


}
