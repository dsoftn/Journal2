package com.dsoftn.controllers.elements;

import java.util.function.Consumer;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.models.StyleSheet;
import com.dsoftn.services.TextHandler;
import com.dsoftn.utils.ColorPopup;
import com.dsoftn.utils.UJavaFX;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TextEditToolbarController implements IElementController {
    // Variables
    private Stage stage = null;
    private String myName = UJavaFX.getUniqueId();
    private String mySettingsName = "TextEditToolbar";
    private VBox root = null;
    private VBox vLayout = null;
    private String receiverID = null;
    private IBaseController parentController = null;
    private TextHandler textHandler = null;
    // Properties
    private StyleSheet curStyleSheet = new StyleSheet();
    private boolean matchCase = false;
    private boolean wholeWords = false;


    // FXML variables

    @FXML
    private Button btnBackground;

    @FXML
    private Button btnBold;

    @FXML
    private Button btnCopy;

    @FXML
    private Button btnCut;

    @FXML
    private Button btnFind;

    @FXML
    private Button btnFindClose;

    @FXML
    private Button btnFindDown;

    @FXML
    private Button btnFindUp;

    @FXML
    private Button btnForeground;

    @FXML
    private Button btnInsertData;

    @FXML
    private Button btnInsertImage;

    @FXML
    private Button btnInsertSmiley;

    @FXML
    private Button btnItalic;

    @FXML
    private Button btnMatchCase;

    @FXML
    private Button btnPaste;

    @FXML
    private Button btnRedo;

    @FXML
    private Button btnReplace;

    @FXML
    private Button btnReplaceOne;

    @FXML
    private Button btnReplaceAll;

    @FXML
    private Button btnStrike;

    @FXML
    private Button btnUnderline;

    @FXML
    private Button btnUndo;

    @FXML
    private Button btnWholeWords;

    @FXML
    private ComboBox<String> cmbFont;

    @FXML
    private HBox hBoxFind;

    @FXML
    private HBox hBoxReplace;

    @FXML
    private HBox hBoxTextEdit;

    @FXML
    private Label lblFindResult;

    @FXML
    private Spinner<Integer> spnFontSize;

    @FXML
    private TextField txtFind;

    @FXML
    private TextField txtReplace;

    @FXML
    private VBox vBoxFindReplace;

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
    public VBox getRoot() {
        return root;
    }

    @Override
    public void setParentController(IBaseController parentController) {
        this.parentController = parentController;
    }

    @Override
    public IBaseController getParentController() {
        return parentController;
    }

    @Override
    public void setRoot(VBox root) {
        this.root = root;
    }

    @Override
    public void addToLayout(VBox layout) {
        layout.getChildren().add(root);
        vLayout = layout;
    }

    @Override
    public void addToLayout(VBox layout, int insertIntoIndex) {
        layout.getChildren().add(insertIntoIndex, root);
        vLayout = layout;
    }

    @Override
    public void removeFromLayout() {
        removeFromLayout(this.vLayout);
    }

    @Override
    public void removeFromLayout(VBox layout) {
        if (vLayout != null) {
            vLayout.getChildren().remove(root);
        }
    }

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public void calculateData() {
        setupWidgetsText();
        setupWidgetsAppearance();
        updateStylesheet(curStyleSheet);
    }

    @Override
    public void closeMe() {
    }

    // Public methods

    public void messageReceived(String messageSTRING) {
        // TODO Auto-generated method stub
    }

    public void messageReceived(StyleSheet styleSheet) {
        this.curStyleSheet = styleSheet;
        updateStylesheet(styleSheet);
    }

    public String getReceiverID() { return receiverID; }

    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public void setTextHandler(TextHandler textHandler) { this.textHandler = textHandler; }

    public void showFindSection() {
        vBoxFindReplace.setVisible(true);
        vBoxFindReplace.setManaged(true);
        hBoxFind.setVisible(true);
        hBoxFind.setManaged(true);
        hBoxReplace.setVisible(false);
        hBoxReplace.setManaged(false);
    }

    public void hideFindSection() {
        vBoxFindReplace.setVisible(false);
        vBoxFindReplace.setManaged(false);
        hBoxFind.setVisible(false);
        hBoxFind.setManaged(false);
        hBoxReplace.setVisible(false);
        hBoxReplace.setManaged(false);
    }

    public void showReplaceSection() {
        vBoxFindReplace.setVisible(true);
        vBoxFindReplace.setManaged(true);
        hBoxFind.setVisible(true);
        hBoxFind.setManaged(true);
        hBoxReplace.setVisible(true);
        hBoxReplace.setManaged(true);
    }

    public void hideReplaceSection() {
        vBoxFindReplace.setVisible(true);
        vBoxFindReplace.setManaged(true);
        hBoxFind.setVisible(true);
        hBoxFind.setManaged(true);
        hBoxReplace.setVisible(false);
        hBoxReplace.setManaged(false);
    }

    public void updateStylesheet(StyleSheet styleSheet) {
        this.curStyleSheet = styleSheet;
        
        cmbFont.setValue(styleSheet.getFontName());
        spnFontSize.getValueFactory().setValue(styleSheet.getFontSize());
        btnForeground.setStyle("-fx-text-fill: " + styleSheet.getFgColor() + ";");
        btnBackground.setStyle("-fx-background-color: " + styleSheet.getBgColor() + ";");
        setButtonSelectedSmall(btnBold, styleSheet.isBold());
        setButtonSelectedSmall(btnItalic, styleSheet.isItalic());
        setButtonSelectedSmall(btnUnderline, styleSheet.isUnderline());
        setButtonSelectedSmall(btnStrike, styleSheet.isStrikethrough());


    }

    // Private methods

    private void messageSent(String messageSTRING) {
        // Send message to TextHandler
        textHandler.messageReceived(messageSTRING);
    }

    private void messageSent(StyleSheet styleSheet) {
        // Send message to TextHandler
        textHandler.messageReceived(styleSheet);
    }
    
    private void setupWidgetsText() {
        UJavaFX.setTooltip(btnUndo, OBJECTS.SETTINGS.getl("text_Undo"));
        UJavaFX.setTooltip(btnRedo, OBJECTS.SETTINGS.getl("text_Redo"));
        UJavaFX.setTooltip(btnCut, OBJECTS.SETTINGS.getl("text_Cut"));
        UJavaFX.setTooltip(btnCopy, OBJECTS.SETTINGS.getl("text_Copy"));
        UJavaFX.setTooltip(btnPaste, OBJECTS.SETTINGS.getl("text_Paste"));
        UJavaFX.setTooltip(btnInsertImage, OBJECTS.SETTINGS.getl("text_InsertImage"));
        UJavaFX.setTooltip(btnInsertSmiley, OBJECTS.SETTINGS.getl("text_InsertSmiley"));
        UJavaFX.setTooltip(btnInsertData, OBJECTS.SETTINGS.getl("text_InsertData"));
        UJavaFX.setTooltip(btnFind, OBJECTS.SETTINGS.getl("text_Find"));
        UJavaFX.setTooltip(btnReplace, OBJECTS.SETTINGS.getl("text_Replace"));
        UJavaFX.setTooltip(btnBackground, OBJECTS.SETTINGS.getl("text_BackgroundColor"));
        UJavaFX.setTooltip(btnBold, OBJECTS.SETTINGS.getl("text_Bold"));
        UJavaFX.setTooltip(btnItalic, OBJECTS.SETTINGS.getl("text_Italic"));
        UJavaFX.setTooltip(btnUnderline, OBJECTS.SETTINGS.getl("text_Underline"));
        UJavaFX.setTooltip(btnStrike, OBJECTS.SETTINGS.getl("text_Strikethrough"));
        UJavaFX.setTooltip(btnMatchCase, OBJECTS.SETTINGS.getl("text_MatchCase"));
        UJavaFX.setTooltip(btnWholeWords, OBJECTS.SETTINGS.getl("text_WholeWords"));
        UJavaFX.setTooltip(btnFindUp, OBJECTS.SETTINGS.getl("text_FindUp"));
        UJavaFX.setTooltip(btnFindDown, OBJECTS.SETTINGS.getl("text_FindDown"));
        UJavaFX.setTooltip(btnFindClose, OBJECTS.SETTINGS.getl("text_Close"));
        UJavaFX.setTooltip(btnReplaceOne, OBJECTS.SETTINGS.getl("text_ReplaceNextItem"));
        UJavaFX.setTooltip(btnReplaceAll, OBJECTS.SETTINGS.getl("text_ReplaceAllItems"));
    }

    private void setupWidgetsAppearance() {
        hideFindSection();

        btnUndo.setDisable(true);
        btnRedo.setDisable(true);


        // Initialize font size spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                OBJECTS.SETTINGS.getvINTEGER("MinTextToolbarFontSize"),
                OBJECTS.SETTINGS.getvINTEGER("MaxTextToolbarFontSize"),
                OBJECTS.SETTINGS.getvINTEGER("DefaultTextToolbarFontSize")
                );
        spnFontSize.setValueFactory(valueFactory);

        // Populate font ComboBox
        cmbFont.setCellFactory(cb -> new ListCell<>() {
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
        cmbFont.setItems(FXCollections.observableArrayList(Font.getFamilies()));

        // Link Spinner value to StyleSheet
        spnFontSize.valueProperty().addListener((obs, oldValue, newValue) -> {
            curStyleSheet.setFontSize(newValue);
            updateStylesheet(curStyleSheet);
        });

        // Link ComboBox value to StyleSheet
        cmbFont.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                curStyleSheet.setFontName(newValue);
                updateStylesheet(curStyleSheet);
            }
        });

        setImageToButton(btnUndo, "/images/undo.png");
        setImageToButton(btnRedo, "/images/redo.png");
        setImageToButton(btnCut, "/images/cut.png");
        setImageToButton(btnCopy, "/images/copy.png");
        setImageToButton(btnPaste, "/images/paste.png");
        setImageToButton(btnInsertImage, "/images/image.png");
        setImageToButton(btnInsertSmiley, "/images/smiley.png");
        setImageToButton(btnInsertData, "/images/data.png");
        setImageToButton(btnFind, "/images/find.png");
        setImageToButton(btnReplace, "/images/replace.png");
        setImageToButton(btnBackground, "/images/background.png");
        setImageToButtonSmall(btnBold, "/images/bold.png");
        setImageToButtonSmall(btnItalic, "/images/italic.png");
        setImageToButtonSmall(btnUnderline, "/images/underline.png");
        setImageToButtonSmall(btnStrike, "/images/strikethrough.png");
        setImageToButton(btnMatchCase, "/images/match_case.png");
        setImageToButton(btnWholeWords, "/images/whole_words.png");
        setImageToButton(btnFindUp, "/images/find_up.png");
        setImageToButton(btnFindDown, "/images/find_down.png");
        setImageToButtonSmall(btnFindClose, "/images/find_close.png");
        setImageToButton(btnReplaceOne, "/images/replace_one.png");
        setImageToButton(btnReplaceAll, "/images/replace_all.png");
    }

    private void setImageToButton(Button button, String imageResourcePath) {
        Image image = new Image(getClass().getResourceAsStream(imageResourcePath));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(28);
        button.setGraphic(imageView);
    }

    private void setImageToButtonSmall(Button button, String imageResourcePath) {
        Image image = new Image(getClass().getResourceAsStream(imageResourcePath));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(18);
        button.setGraphic(imageView);
    }

    private void setButtonSelected(Button button, boolean isSelected) {
        button.getStyleClass().remove("button-icon");
        button.getStyleClass().remove("button-icon-selected");

        if (isSelected) {
            button.getStyleClass().add("button-icon-selected");
        }
        else {
            button.getStyleClass().add("button-icon");
        }
    }

    private void setButtonSelectedSmall(Button button, boolean isSelected) {
        button.getStyleClass().remove("button-icon-small");
        button.getStyleClass().remove("button-icon-small-selected");

        if (isSelected) {
            button.getStyleClass().add("button-icon-small-selected");
        }
        else {
            button.getStyleClass().add("button-icon-small");
        }
    }


    // FXML methods

    @FXML
    public void onBtnForegroundAction() {
        ColorPopup colorPopup = new ColorPopup(color -> {
            curStyleSheet.setFgColor(color);
            updateStylesheet(curStyleSheet);

            messageSent(curStyleSheet);
            messageSent(TextToolbarActionEnum.FOCUS_TO_TEXT + "\n1");
        });

        colorPopup.startMe(root.getScene().getWindow());
    }

    @FXML
    public void onBtnBackgroundAction() {
        ColorPopup colorPopup = new ColorPopup(color -> {
            curStyleSheet.setBgColor(color);
            updateStylesheet(curStyleSheet);

            messageSent(curStyleSheet);
            messageSent(TextToolbarActionEnum.FOCUS_TO_TEXT + "\n1");
        });

        colorPopup.startMe(root.getScene().getWindow());
    }

    @FXML
    public void onBtnBoldAction() {
        curStyleSheet.setBold(!curStyleSheet.isBold());
        updateStylesheet(curStyleSheet);
    }

    @FXML
    public void onBtnItalicAction() {
        curStyleSheet.setItalic(!curStyleSheet.isItalic());
        updateStylesheet(curStyleSheet);
    }

    @FXML
    public void onBtnUnderlineAction() {
        curStyleSheet.setUnderline(!curStyleSheet.isUnderline());
        updateStylesheet(curStyleSheet);
    }

    @FXML
    public void onBtnStrikeAction() {
        curStyleSheet.setStrikethrough(!curStyleSheet.isStrikethrough());
        updateStylesheet(curStyleSheet);
    }

    @FXML
    public void onBtnFindAction() {
        showFindSection();
    }

    @FXML
    public void onBtnReplaceAction() {
        showReplaceSection();
    }

    @FXML
    public void onBtnFindCloseAction() {
        hideFindSection();
    }

    @FXML
    public void onBtnMatchCaseAction() {
        matchCase = !matchCase;
        setButtonSelected(btnMatchCase, matchCase);
    }

    @FXML
    public void onBtnWholeWordsAction() {
        wholeWords = !wholeWords;
        setButtonSelected(btnWholeWords, wholeWords);
    }

}
