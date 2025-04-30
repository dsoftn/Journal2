package com.dsoftn.controllers.elements;

import java.util.regex.Pattern;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.utils.ColorPopup;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TextEditToolbarController implements IElementController, ICustomEventListener {
    public enum AlignmentEnum {
        LEFT,
        CENTER,
        RIGHT,
        JUSTIFY
    }

    public enum ToolbarSectionsEnum {
        CLIPBOARD,
        INSERT,
        FIND_REPLACE,
        BOLD_ITALIC_UNDERLINE_STRIKETHROUGH,
        ALIGNMENT
    }

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
    private StyleSheetChar curCharStyle = new StyleSheetChar();
    private StyleSheetParagraph curParagraphStyle = new StyleSheetParagraph();
    private boolean matchCase = false;
    private boolean wholeWords = false;
    private AlignmentEnum alignment = AlignmentEnum.LEFT;


    // FXML variables

    // HBox with all commands
    @FXML
    private HBox hBoxTextEdit;

    // Section Undo, Redo
    @FXML
    private Button btnUndo;
    @FXML
    private Button btnRedo;
    // Section Cut, Copy, Paste
    @FXML
    private Separator sepClip;
    @FXML
    private Button btnCut;
    @FXML
    private Button btnCopy;
    @FXML
    private Button btnPaste;
    // Section Insert Image, Smiley, Data
    @FXML
    private Separator sepInsert;
    @FXML
    private Button btnInsertImage;
    @FXML
    private Button btnInsertSmiley;
    @FXML
    private Button btnInsertData;
    // Section Find, Replace
    @FXML
    private Separator sepFind;
    @FXML
    private Button btnFind;
    @FXML
    private Button btnReplace;
    // Section Font Name, Font Size, Foreground, Background
    @FXML
    private ComboBox<String> cmbFont;
    @FXML
    private Spinner<Integer> spnFontSize;
    @FXML
    private Button btnForeground;
    @FXML
    private Button btnBackground;
    // Section Bold, Italic, Underline, Strikethrough
    @FXML
    private Separator sepBold;
    @FXML
    private Button btnBold;
    @FXML
    private Button btnItalic;
    @FXML
    private Button btnUnderline;
    @FXML
    private Button btnStrike;
    // Section Alignment
    @FXML
    private Separator sepAlign;
    @FXML
    private Button btnAlignLeft;
    @FXML
    private Button btnAlignCenter;
    @FXML
    private Button btnAlignRight;
    @FXML
    private Button btnAlignJustify;

    // Find / Replace
    @FXML
    private VBox vBoxFindReplace;
        @FXML
        private HBox hBoxFind;
            @FXML
            private TextField txtFind;
            @FXML
            private Button btnMatchCase;
            @FXML
            private Button btnWholeWords;
            @FXML
            private Label lblFindResult;
            @FXML
            private ImageView imgFindWorking;
            @FXML
            private Button btnFindUp;
            @FXML
            private Button btnFindDown;
            @FXML
            private Button btnFindClose;
        @FXML
        private HBox hBoxReplace;
            @FXML
            private TextField txtReplace;
            @FXML
            private Button btnReplaceOne;
            @FXML
            private Button btnReplaceAll;


    // Interface ICustomEventListener

    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof ClipboardChangedEvent) {
            btnPaste.setDisable(OBJECTS.CLIP.getClipText() == null);
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
        updateCharStyle(curCharStyle);
        OBJECTS.EVENT_HANDLER.register(this, ClipboardChangedEvent.CLIPBOARD_CHANGED_EVENT);
    }

    @Override
    public void closeMe() {
    }

    // Public methods

    public void msgFromHandler(String messageSTRING) {
        // Process message from handler
        if (messageSTRING.equals("UNDO:" + true)) {
            btnUndo.setDisable(false);
        } else if (messageSTRING.equals("UNDO:" + false)) {
            btnUndo.setDisable(true);
        } else if (messageSTRING.equals("REDO:" + true)) {
            btnRedo.setDisable(false);
        } else if (messageSTRING.equals("REDO:" + false)) {
            btnRedo.setDisable(true);
        } else if (messageSTRING.equals("SELECTED: False")) {
            btnCut.setDisable(true);
            btnCopy.setDisable(true);
        } else if (messageSTRING.equals("SELECTED: True")) {
            btnCut.setDisable(false);
            btnCopy.setDisable(false);
        } else if (messageSTRING.startsWith(TextToolbarActionEnum.FIND_SHOW.name())) {
            if (messageSTRING.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1).length > 1) {
                txtFind.setText(messageSTRING.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1]);
            }
            showFindSection();
        } else if (messageSTRING.startsWith(TextToolbarActionEnum.REPLACE_SHOW.name())) {
            if (messageSTRING.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1).length > 1) {
                txtFind.setText(messageSTRING.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1]);
            }
            showReplaceSection();
        } else if (messageSTRING.equals(TextToolbarActionEnum.FIND_CLOSE.name())) {
            hideFindSection();
        } else if (messageSTRING.startsWith("ACTION:READY")) {
            setFindWorking(false);
            String[] lines = messageSTRING.split("\\R", -1);
            lblFindResult.setText(OBJECTS.SETTINGS.getl("FindResult").replace("#1", lines[2]).replace("#2", lines[1]));
        } else if (messageSTRING.startsWith("ACTION:UPDATE LABEL")) {
            String[] lines = messageSTRING.split("\\R", -1);
            lblFindResult.setText(OBJECTS.SETTINGS.getl("FindResult").replace("#1", lines[2]).replace("#2", lines[1]));
        } else if (messageSTRING.startsWith("ACTION:WORKING")) {
            setFindWorking(true);
        }

    }

    public void msgFromHandler(StyleSheetChar styleSheet) {
        this.curCharStyle = styleSheet;
        updateCharStyle(styleSheet);
    }

    public void msgFromHandler(StyleSheetParagraph styleSheet) {
        this.curParagraphStyle = styleSheet;
        updateParagraphStyle(styleSheet);
    }

    public String getReceiverID() { return receiverID; }

    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public void setTextHandler(TextHandler textHandler) { this.textHandler = textHandler; }

    public void showFindSection() {
        boolean sendMsgToHandler = !hBoxFind.isVisible();

        vBoxFindReplace.setVisible(true);
        vBoxFindReplace.setManaged(true);
        hBoxFind.setVisible(true);
        hBoxFind.setManaged(true);
        hBoxReplace.setVisible(false);
        hBoxReplace.setManaged(false);
        txtFind.requestFocus();
        
        if (sendMsgToHandler) {
            msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.FIND_ALL.name(), null));
        }
    }

    public void hideFindSection() {
        vBoxFindReplace.setVisible(false);
        vBoxFindReplace.setManaged(false);
        hBoxFind.setVisible(false);
        hBoxFind.setManaged(false);
        hBoxReplace.setVisible(false);
        hBoxReplace.setManaged(false);
        msgForHandler(findReplaceActionForHandler("FIND CLOSED", null));
        msgForHandler(TextToolbarActionEnum.FOCUS_TO_TEXT.name());
    }

    public void showReplaceSection() {
        boolean sendMsgToHandler = !hBoxFind.isVisible();

        vBoxFindReplace.setVisible(true);
        vBoxFindReplace.setManaged(true);
        hBoxFind.setVisible(true);
        hBoxFind.setManaged(true);
        hBoxReplace.setVisible(true);
        hBoxReplace.setManaged(true);
        txtReplace.requestFocus();

        if (sendMsgToHandler) {
            msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.FIND_ALL.name(), null));
        }
    }

    public void hideReplaceSection() {
        vBoxFindReplace.setVisible(true);
        vBoxFindReplace.setManaged(true);
        hBoxFind.setVisible(true);
        hBoxFind.setManaged(true);
        hBoxReplace.setVisible(false);
        hBoxReplace.setManaged(false);
        txtFind.requestFocus();
    }

    public void updateCharStyle(StyleSheetChar styleSheet) {
        this.curCharStyle = styleSheet;
        
        cmbFont.setValue(styleSheet.getFontName());
        spnFontSize.getValueFactory().setValue(styleSheet.getFontSize());
        btnForeground.setStyle("-fx-text-fill: " + styleSheet.getFgColor() + ";");
        btnBackground.setStyle("-fx-background-color: " + styleSheet.getBgColor() + ";");
        setButtonSelectedSmall(btnBold, styleSheet.isBold());
        setButtonSelectedSmall(btnItalic, styleSheet.isItalic());
        setButtonSelectedSmall(btnUnderline, styleSheet.isUnderline());
        setButtonSelectedSmall(btnStrike, styleSheet.isStrikethrough());
    }

    public void updateParagraphStyle(StyleSheetParagraph styleSheet) {
        this.curParagraphStyle = styleSheet;
        setAlignment(styleSheet.getAlignmentEnum());
    }

    public void setAlignment(AlignmentEnum alignment) {
        this.alignment = alignment;
        setButtonSelected(btnAlignLeft, alignment == AlignmentEnum.LEFT);
        setButtonSelected(btnAlignCenter, alignment == AlignmentEnum.CENTER);
        setButtonSelected(btnAlignRight, alignment == AlignmentEnum.RIGHT);
        setButtonSelected(btnAlignJustify, alignment == AlignmentEnum.JUSTIFY);
    }

    public boolean canBeClosed() {
        if (vBoxFindReplace.isVisible()) {
            hideFindSection();
            return false;
        }
        return true;
    }

    public void setToolbarSectionsVisibility() {
        for (ToolbarSectionsEnum section : ToolbarSectionsEnum.values()) {
            String settingKey = "ToolbarSectionVisible_" + section.toString();
            setToolbarSectionVisible(section, OBJECTS.SETTINGS.getvBOOLEAN(settingKey));
        }
    }

    // Private methods

    private String findReplaceActionForHandler(String action, String findText) {
        /*
            STRUCTURE:
                FIND/REPLACE ACTION:action
                findText
                replaceText
                matchCase
                wholeWords
         */

        String result = "";

        result += "FIND/REPLACE ACTION:" + action + "\n";

        if (findText != null) {
            result += findText + "\n";
        } else {
            result += txtFind.getText() + "\n";
        }

        result += txtReplace.getText() + "\n";

        result += matchCase + "\n";
        result += wholeWords;

        return result;
    }

    private void setToolbarSectionVisible(ToolbarSectionsEnum section, boolean visible) {
        switch (section) {
            case CLIPBOARD:
                setToolbarSectionVisibleCLIPBOARD(visible);
                break;
            case INSERT:
                setToolbarSectionVisibleINSERT(visible);
                break;
            case FIND_REPLACE:
                setToolbarSectionVisibleFIND_REPLACE(visible);
                break;
            case BOLD_ITALIC_UNDERLINE_STRIKETHROUGH:
                setToolbarSectionVisibleBOLD_ITALIC_UNDERLINE_STRIKETHROUGH(visible);
                break;
            case ALIGNMENT:
                setToolbarSectionVisibleALIGNMENT(visible);
                break;
            default:
                UError.error("TextEditToolbarController.setToolbarSectionVisible", "Unknown section: " + section.toString());
                break;
        }
    }

    private void setToolbarSectionVisibleCLIPBOARD(boolean visible) {
        sepClip.setVisible(visible);
        sepClip.setManaged(visible);
        btnCut.setVisible(visible);
        btnCut.setManaged(visible);
        btnCopy.setVisible(visible);
        btnCopy.setManaged(visible);
        btnPaste.setVisible(visible);
        btnPaste.setManaged(visible);
    }

    private void setToolbarSectionVisibleINSERT(boolean visible) {
        sepInsert.setVisible(visible);
        sepInsert.setManaged(visible);
        btnInsertImage.setVisible(visible);
        btnInsertImage.setManaged(visible);
        btnInsertSmiley.setVisible(visible);
        btnInsertSmiley.setManaged(visible);
        btnInsertData.setVisible(visible);
        btnInsertData.setManaged(visible);
    }

    private void setToolbarSectionVisibleFIND_REPLACE(boolean visible) {
        sepFind.setVisible(visible);
        sepFind.setManaged(visible);
        btnFind.setVisible(visible);
        btnFind.setManaged(visible);
        btnReplace.setVisible(visible);
        btnReplace.setManaged(visible);
    }

    private void setToolbarSectionVisibleBOLD_ITALIC_UNDERLINE_STRIKETHROUGH(boolean visible) {
        sepBold.setVisible(visible);
        sepBold.setManaged(visible);
        btnBold.setVisible(visible);
        btnBold.setManaged(visible);
        btnItalic.setVisible(visible);
        btnItalic.setManaged(visible);
        btnUnderline.setVisible(visible);
        btnUnderline.setManaged(visible);
        btnStrike.setVisible(visible);
        btnStrike.setManaged(visible);
    }

    private void setToolbarSectionVisibleALIGNMENT(boolean visible) {
        sepAlign.setVisible(visible);
        sepAlign.setManaged(visible);
        btnAlignLeft.setVisible(visible);
        btnAlignLeft.setManaged(visible);
        btnAlignCenter.setVisible(visible);
        btnAlignCenter.setManaged(visible);
        btnAlignRight.setVisible(visible);
        btnAlignRight.setManaged(visible);
        btnAlignJustify.setVisible(visible);
        btnAlignJustify.setManaged(visible);
    }

    private void msgForHandler(String messageSTRING) {
        if (textHandler == null) {
            return;
        }
        // Send message to TextHandler
        textHandler.msgFromToolbar(messageSTRING);
    }

    private void msgForHandler(StyleSheetChar styleSheet) {
        if (textHandler == null) {
            return;
        }
        // Send message to TextHandler
        textHandler.msgFromToolbar(styleSheet.duplicate());
    }

    private void msgForHandler(StyleSheetParagraph styleSheet) {
        if (textHandler == null) {
            return;
        }
        // Send message to TextHandler
        textHandler.msgFromToolbar(styleSheet.duplicate());
    }
    
    private void setupWidgetsText() {
        lblFindResult.setText(OBJECTS.SETTINGS.getl("FindResult").replace("#1", "?").replace("#2", "?"));
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
        UJavaFX.setTooltip(btnForeground, OBJECTS.SETTINGS.getl("text_ForegroundColor"));
        UJavaFX.setTooltip(btnAlignLeft, OBJECTS.SETTINGS.getl("text_AlignLeft"));
        UJavaFX.setTooltip(btnAlignCenter, OBJECTS.SETTINGS.getl("text_AlignCenter"));
        UJavaFX.setTooltip(btnAlignRight, OBJECTS.SETTINGS.getl("text_AlignRight"));
        UJavaFX.setTooltip(btnAlignJustify, OBJECTS.SETTINGS.getl("text_AlignJustify"));
    }

    private void setFindWorking(boolean working) {
        if (working) {
            lblFindResult.setVisible(false);
            lblFindResult.setManaged(false);
            imgFindWorking.setVisible(true);
            imgFindWorking.setManaged(true);
        } else {
            lblFindResult.setVisible(true);
            lblFindResult.setManaged(true);
            imgFindWorking.setVisible(false);
            imgFindWorking.setManaged(false);
        }
    }

    private void setupWidgetsAppearance() {
        setFindWorking(false);   
        imgFindWorking.setImage(new Image(getClass().getResourceAsStream("/gifs/loading.gif")));
        hideFindSection();

        btnUndo.setDisable(true);
        btnRedo.setDisable(true);
        btnCut.setDisable(true);
        btnCopy.setDisable(true);
        btnPaste.setDisable(OBJECTS.CLIP.getClipText() == null);


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
        cmbFont.setItems(FXCollections.observableArrayList(Font.getFontNames()));

        // Link Spinner value to StyleSheet
        spnFontSize.valueProperty().addListener((obs, oldValue, newValue) -> {
            curCharStyle.setFontSize(newValue);
            updateCharStyle(curCharStyle);
            msgForHandler(curCharStyle);
        });

        // Link ComboBox value to StyleSheet
        cmbFont.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                curCharStyle.setFontName(newValue);
                updateCharStyle(curCharStyle);
                msgForHandler(curCharStyle);
            }
        });

        // Find text field listener
        txtFind.textProperty().addListener((obs, oldValue, newValue) -> {
            msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.FIND_ALL.name(), newValue));
        });

        // TAB switches between Find and Replace sections
        txtFind.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                if (!hBoxReplace.isVisible()) {
                    showReplaceSection();
                } else {
                    txtReplace.requestFocus();
                }
            }
        });
        txtReplace.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                txtFind.requestFocus();
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
        setImageToButton(btnAlignLeft, "/images/align_left.png");
        setImageToButton(btnAlignCenter, "/images/align_center.png");
        setImageToButton(btnAlignRight, "/images/align_right.png");
        setImageToButton(btnAlignJustify, "/images/align_justify.png");

        setAlignment(this.alignment);
        setToolbarSectionsVisibility();

        // Show/Hide sections
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
    public void onBtnCutAction() {
        msgForHandler("CUT");
    }

    @FXML
    public void onBtnCopyAction() {
        msgForHandler("COPY");
    }

    @FXML
    public void onBtnPasteAction() {
        msgForHandler("PASTE");
    }

    @FXML
    public void onBtnForegroundAction() {
        UJavaFX.getColorPickerPopUp(root.getScene().getWindow(), this::onFGColorSelectedCallback);
    }

    private void onFGColorSelectedCallback(String color) {
        curCharStyle.setFgColor(color);
        updateCharStyle(curCharStyle);
        msgForHandler(curCharStyle);
    }

    @FXML
    public void onBtnBackgroundAction() {
        UJavaFX.getColorPickerPopUp(root.getScene().getWindow(), this::onBGColorSelectedCallback);
    }

    private void onBGColorSelectedCallback(String color) {
        curCharStyle.setBgColor(color);
        updateCharStyle(curCharStyle);
        msgForHandler(curCharStyle);
    }

    @FXML
    public void onBtnBoldAction() {
        curCharStyle.setBold(!curCharStyle.isBold());
        updateCharStyle(curCharStyle);
        msgForHandler(curCharStyle);
    }

    @FXML
    public void onBtnItalicAction() {
        curCharStyle.setItalic(!curCharStyle.isItalic());
        updateCharStyle(curCharStyle);
        msgForHandler(curCharStyle);
    }

    @FXML
    public void onBtnUnderlineAction() {
        curCharStyle.setUnderline(!curCharStyle.isUnderline());
        updateCharStyle(curCharStyle);
        msgForHandler(curCharStyle);
    }

    @FXML
    public void onBtnStrikeAction() {
        curCharStyle.setStrikethrough(!curCharStyle.isStrikethrough());
        updateCharStyle(curCharStyle);
        msgForHandler(curCharStyle);
    }

    @FXML
    public void onBtnFindAction() {
        if (vBoxFindReplace.isVisible()) {
            hideFindSection();
            return;
            }
        showFindSection();
    }

    @FXML
    public void onBtnReplaceAction() {
        if (vBoxFindReplace.isVisible()) {
            if (hBoxReplace.isVisible()) {
                hideReplaceSection();
                return;
            }
        }
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
        msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.FIND_MATCH_CASE.name(), null));
    }

    @FXML
    public void onBtnWholeWordsAction() {
        wholeWords = !wholeWords;
        setButtonSelected(btnWholeWords, wholeWords);
        msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.FIND_WHOLE_WORDS.name(), null));
    }

    @FXML
    public void onAlignLeftAction() {
        setAlignment(AlignmentEnum.LEFT);
        curParagraphStyle.setAlignmentEnum(AlignmentEnum.LEFT);
        msgForHandler(curParagraphStyle);
    }

    @FXML
    public void onBtnAlignCenterAction() {
        setAlignment(AlignmentEnum.CENTER);
        curParagraphStyle.setAlignmentEnum(AlignmentEnum.CENTER);
        msgForHandler(curParagraphStyle);
    }

    @FXML
    public void onBtnAlignRightAction() {
        setAlignment(AlignmentEnum.RIGHT);
        curParagraphStyle.setAlignmentEnum(AlignmentEnum.RIGHT);
        msgForHandler(curParagraphStyle);
    }

    @FXML
    public void onBtnAlignJustifyAction() {
        setAlignment(AlignmentEnum.JUSTIFY);
        curParagraphStyle.setAlignmentEnum(AlignmentEnum.JUSTIFY);
        msgForHandler(curParagraphStyle);
    }

    @FXML
    public void onBtnUndoAction() {
        msgForHandler(TextToolbarActionEnum.UNDO.name());
    }

    @FXML
    public void onBtnRedoAction() {
        msgForHandler(TextToolbarActionEnum.REDO.name());
    }

    @FXML
    public void onBtnFindUp() {
        msgForHandler(TextToolbarActionEnum.FIND_UP.name());
    }

    @FXML
    public void onBtnFindDown() {
        msgForHandler(TextToolbarActionEnum.FIND_DOWN.name());
    }

    @FXML
    public void onBtnReplaceOne() {
        msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.REPLACE_ONE.name(), null));
    }

    @FXML
    public void onBtnReplaceAll() {
        msgForHandler(findReplaceActionForHandler(TextToolbarActionEnum.REPLACE_ALL.name(), null));
    }

}
