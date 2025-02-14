package com.dsoftn.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.utils.UFile;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.services.RichText;

public class MsgBoxController implements IBaseController {

    public enum MsgBoxIcon {
        NONE(""),
        APP("/images/app.png"),
        INFORMATION("/images/information.png"),
        WARNING("/images/warning.png"),
        ERROR("/images/error.png"),
        FILE_SEARCH("/images/file_search2.png"),
        FILE_ERROR("/images/file_error.png"),
        SETTINGS_FILE("/images/settings_file2.png"),
        COPY("/images/copy.png"),
        USER("/images/user.png"),
        MODEL("/images/model.png"),
        USER_NO_IMAGE("/images/user_no_image.png"),
        DATABASE("/images/database.png");

        private final String iconPath;

        // Constructor

        MsgBoxIcon(String iconPath) {
            this.iconPath = iconPath;
        }

        // Getters

        public String getIconPath() {
            return iconPath;
        }

        public ImageView getImageView(double width, double height) {
            if (iconPath.equals("")) {
                return null;
            }

            Image image = new Image(getClass().getResourceAsStream(iconPath));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            return imageView;
        }

        public Image getImage() {
            if (iconPath.equals("")) {
                return null;
            }
            return new Image(getClass().getResourceAsStream(iconPath));
        }

    }

    public enum MsgBoxButton {
        CANCEL,
        OK,
        YES,
        NO,
        FIND_FILE;
    }

    // Variables
    private String myName = UJavaFX.getUniqueId();

    private Stage stage;
    private List<MsgBoxButton> buttons = new ArrayList<>() { { add(MsgBoxButton.OK); } };
    private MsgBoxButton defaultButton = null;
    private String titleText = CONSTANTS.APPLICATION_NAME;
    private MsgBoxIcon titleIcon = MsgBoxIcon.APP;
    private boolean showHeader = false;
    private MsgBoxIcon headerIcon = MsgBoxIcon.APP;
    private RichText headerText = new RichText();
    private boolean showContent = false;
    private MsgBoxIcon contentIcon = MsgBoxIcon.APP;
    private RichText contentText = new RichText();

    private MsgBoxButton selectedButton = null;
    private String result = "";


    // FXML
    @FXML
    private Label lblIconHeader; // Icon in header
    @FXML
    private Label lblIconContent; // Icon in content
    @FXML
    private VBox vBoxHeader; // Header section
    @FXML
    private HBox hBoxHeaderContent; // This layout contains Image label, text label should be placed here
    @FXML
    private VBox vBoxContent; // Content section
    @FXML
    private HBox hBoxContentContent; // This layout contains Image label, text label should be placed here
    @FXML
    private Button btnFindFile; // Find button
    @FXML
    private Button btnYes; // Yes button
    @FXML
    private Button btnNo; // No button
    @FXML
    private Button btnOk; // Ok button
    @FXML
    private Button btnCancel; // Cancel button


    // Public methods

    public void setTitleText (String titleText) {
        this.titleText = titleText;
    }

    public void setTitleIcon (MsgBoxIcon titleIcon) {
        this.titleIcon = titleIcon;
    }

    public void setShowHeader (boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setHeaderIcon (MsgBoxIcon headerIcon) {
        this.showHeader = true;
        this.headerIcon = headerIcon;
    }

    public void setHeaderText (String headerText) {
        this.showHeader = true;
        this.headerText.setText(headerText);
    }

    public void setHeaderText (RichText headerText) {
        this.showHeader = true;
        this.headerText = headerText;
    }

    public void setShowContent (boolean showContent) {
        this.showContent = showContent;
    }

    public void setContentIcon (MsgBoxIcon contentIcon) {
        this.showContent = true;
        this.contentIcon = contentIcon;
    }

    public void setContentText (String contentText) {
        this.showContent = true;
        this.contentText.setText(contentText);
    }

    public void setContentText (RichText contentText) {
        this.showContent = true;
        this.contentText = contentText;
    }

    public void setButtons (MsgBoxButton... buttons) {
        this.buttons.clear();
        this.buttons.addAll(List.of(buttons));
    }

    public void setDefaultButton (MsgBoxButton defaultButton) {
        this.defaultButton = defaultButton;
    }

    public MsgBoxButton getSelectedButton () {
        return selectedButton;
    }

    public String getResult () {
        return result;
    }

    // Interface IBaseController methods

    @Override
    public String getMyName () {
        return myName;
    }

    @Override
    public void setStage (Stage stage) {
        this.stage = stage;

        stage.setOnCloseRequest(event -> {
            closeMe();
        });

    }

    @Override
    public Stage getStage () {
        return stage;
    }

    @Override
    public void startMe () {
        setupWidgetText();
        setupMsgBox();
        stage.showAndWait();
    }

    @Override
    public void closeMe () {
        stage.close();
    }

    // Private methods

    private void setupWidgetText () {
        if (OBJECTS.SETTINGS.isLanguageKeyExists("text_FindFile")) {
            btnFindFile.setText(OBJECTS.SETTINGS.getl("text_FindFile"));
        }
        if (OBJECTS.SETTINGS.isLanguageKeyExists("text_Yes")) {
            btnYes.setText(OBJECTS.SETTINGS.getl("text_Yes"));
        }
        if (OBJECTS.SETTINGS.isLanguageKeyExists("text_No")) {
            btnNo.setText(OBJECTS.SETTINGS.getl("text_No"));
        }
        if (OBJECTS.SETTINGS.isLanguageKeyExists("text_Ok")) {
            btnOk.setText(OBJECTS.SETTINGS.getl("text_Ok"));
        }
        if (OBJECTS.SETTINGS.isLanguageKeyExists("text_Cancel")) {
            btnCancel.setText(OBJECTS.SETTINGS.getl("text_Cancel"));
        }
    }

    private void setupMsgBox () {
        // Setup title
        stage.setTitle(titleText);
        setTitleImage(titleIcon);

        // Setup header
        if (showHeader) {
            headerText.setCss("-fx-fill: #80ed99; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-style: italic;");
            TextFlow lblHeaderText = headerText.getTextFlow();
            hBoxHeaderContent.getChildren().add(lblHeaderText);
            setImageToLabel(headerIcon, lblIconHeader);
        } else {
            vBoxHeader.setVisible(false);
            vBoxHeader.setManaged(false);
        }

        // Setup content
        if (showContent) {
            contentText.setCss("-fx-fill: #c7f9cc; -fx-font-size: 18px;");
            TextFlow lblContentText = contentText.getTextFlow();
            hBoxContentContent.getChildren().add(lblContentText);
            setImageToLabel(contentIcon, lblIconContent);
        } else {
            vBoxContent.setVisible(false);
            vBoxContent.setManaged(false);
        }

        // Setup buttons
        setupButtons();
    }

    private void setupButtons () {
        btnFindFile.setVisible(false);
        btnFindFile.setManaged(false);
        btnYes.setVisible(false);
        btnYes.setManaged(false);
        btnNo.setVisible(false);
        btnNo.setManaged(false);
        btnOk.setVisible(false);
        btnOk.setManaged(false);
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);

        for (MsgBoxButton button : buttons) {
            switch (button) {
                case FIND_FILE:
                    btnFindFile.setVisible(true);
                    btnFindFile.setManaged(true);
                    if (defaultButton == button) {
                        btnFindFile.requestFocus();
                        btnFindFile.setStyle("-fx-underline: true;");
                    }
                    btnFindFile.setOnAction(event -> {
                        selectFile();
                    });
                    break;
                case YES:
                    btnYes.setVisible(true);
                    btnYes.setManaged(true);
                    if (defaultButton == button) {
                        btnYes.requestFocus();
                        btnYes.setStyle("-fx-underline: true;");
                    }
                    btnYes.setOnAction(event -> {
                        selectedButton = button;
                        closeMe();
                    });
                    break;
                case NO:
                    btnNo.setVisible(true);
                    btnNo.setManaged(true);
                    if (defaultButton == button) {
                        btnNo.requestFocus();
                        btnNo.setStyle("-fx-underline: true;");
                    }
                    btnNo.setOnAction(event -> {
                        selectedButton = button;
                        closeMe();
                    });
                    break;
                case OK:
                    btnOk.setVisible(true);
                    btnOk.setManaged(true);
                    if (defaultButton == button) {
                        btnOk.requestFocus();
                        btnOk.setStyle("-fx-underline: true;");
                    }
                    btnOk.setOnAction(event -> {
                        selectedButton = button;
                        closeMe();
                    });
                    break;
                case CANCEL:
                    btnCancel.setVisible(true);
                    btnCancel.setManaged(true);
                    if (defaultButton == button) {
                        btnCancel.requestFocus();
                        btnCancel.setStyle("-fx-underline: true;");
                    }
                    btnCancel.setOnAction(event -> {
                        selectedButton = button;
                        closeMe();
                    });
                    break;
            }
        }
    }

    private void selectFile () {
        String fileName = UFile.getOpenFileDialog("Select file", null, stage);
        if (fileName != null) {
            result = fileName;
            selectedButton = MsgBoxButton.FIND_FILE;
            closeMe();
        }
    }

    private void setTitleImage (MsgBoxIcon icon) {
        if (icon == null || icon.getImage() == null) {
            return;
        }

        stage.getIcons().add(icon.getImage());
    }

    private void setImageToLabel (MsgBoxIcon icon, Label label) {
        if (icon == null || icon.getImage() == null) {
            return;
        }

        label.setGraphic(icon.getImageView(50, 50));
    }


    


}
