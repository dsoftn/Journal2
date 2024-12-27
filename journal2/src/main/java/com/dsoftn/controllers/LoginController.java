package com.dsoftn.controllers;

import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

import java.util.List;


import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.models.User;
import com.dsoftn.utils.LanguagesEnum;
import com.dsoftn.utils.UError;


public class LoginController implements IBaseController {

    // Variables
    private Stage stage;
    private User selectedUser = null;

    // FXML widgets
    @FXML
    VBox vBoxLogin; // Login window
    @FXML
    VBox vBoxNew; // New user window
    @FXML
    Label lblTitle; // Main title - name of application
    @FXML
    Label lblLoginTitle; // Existing user login title
    @FXML
    ComboBox<String> cmbLoginUser; // Existing users list
    @FXML
    PasswordField pTxtPassword; // Existing user password field
    @FXML
    ComboBox<String> cmbLoginLang; // Existing user language
    @FXML
    Button btnLogin; // Login button
    @FXML
    Button btnCancel; // Cancel button (exit)
    @FXML
    Button btnSwitchNew; // Switch to new user window
    @FXML
    Label lblNewTitle; // New user title
    @FXML
    TextField txtNewUser; // New user name field
    @FXML
    PasswordField pTxtNewPassword; // New user password field
    @FXML
    PasswordField pTxtNewConfirmPassword; // New user password confirmation
    @FXML
    ComboBox<String> cmbNewLang; // New user language
    @FXML
    Button btnCreateNew; // Create new user button
    @FXML
    Button btnSwitchExisting; // Switch to existing user window


    
    // Interface IBaseController methods
    
    @Override
    public void setStage (Stage stage) {
        this.stage = stage;
    }
    
    @Override
    public void startMe () {
        setupWidgetText();
        setupWidgets();

        showExistingUserWindow();
        
        stage.setHeight(470);
        pTxtPassword.requestFocus();
        stage.showAndWait();
    }

    @Override
    public void closeMe () {
        stage.close();
    }

    // Public methods

    public User getAuthenticatedUser() {
        return selectedUser;
    }


    // Private methods

    private void setupWidgetText () {
        lblTitle.setText(CONSTANTS.APPLICATION_NAME);
        lblLoginTitle.setText(OBJECTS.SETTINGS.getl("text_UserLogin"));
        cmbLoginUser.setPromptText(OBJECTS.SETTINGS.getl("text_SelectUsername..."));
        pTxtPassword.setPromptText(OBJECTS.SETTINGS.getl("text_EnterPassword..."));
        btnLogin.setText(OBJECTS.SETTINGS.getl("text_Login"));
        btnCancel.setText(OBJECTS.SETTINGS.getl("text_Cancel"));
        btnSwitchNew.setText(OBJECTS.SETTINGS.getl("text_CreateNewUser"));
        lblNewTitle.setText(OBJECTS.SETTINGS.getl("text_NewUser"));
        txtNewUser.setPromptText(OBJECTS.SETTINGS.getl("text_EnterUsername..."));
        pTxtNewPassword.setPromptText(OBJECTS.SETTINGS.getl("text_EnterPassword..."));
        pTxtNewConfirmPassword.setPromptText(OBJECTS.SETTINGS.getl("text_ConfirmPassword..."));
        btnCreateNew.setText(OBJECTS.SETTINGS.getl("text_CreateNewUser"));
        btnSwitchExisting.setText(OBJECTS.SETTINGS.getl("text_SelectExistingUser"));
    }

    private void setupWidgets () {
        // Populate Existing Users ComboBox
        for (User user : OBJECTS.USERS.getEntityAll()) {
            cmbLoginUser.getItems().add(user.getUsername());
        }
        User lastUser = OBJECTS.USERS.lastActiveUser();
        if (lastUser != null) {
            cmbLoginUser.setValue(lastUser.getUsername());
        }

        // Populate Languages ComboBoxes
        List<String> availableLanguages = OBJECTS.SETTINGS.getAvailableLanguageNames();

        for (String lang : availableLanguages) {
            LanguagesEnum langEnum = LanguagesEnum.fromName(lang);

            if (langEnum != null && langEnum != LanguagesEnum.UNKNOWN) {
                cmbLoginLang.getItems().add(langEnum.getNativeName());
                cmbNewLang.getItems().add(langEnum.getNativeName());
            }
        }
        // Set default values for Language ComboBoxes
        cmbLoginLang.setValue(LanguagesEnum.fromLangCode(CONSTANTS.DEFAULT_SETTINGS_LANGUAGE_CODE).getNativeName());
        cmbNewLang.setValue(LanguagesEnum.fromLangCode(CONSTANTS.DEFAULT_SETTINGS_LANGUAGE_CODE).getNativeName());

        // Set listener for cmbLoginLang ComboBox
        cmbLoginLang.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                OBJECTS.SETTINGS.setActiveLanguage(LanguagesEnum.fromNativeName(newValue).getLangCode());
                setupWidgetText();
            }
        });

        // Set listener for cmbNewLang ComboBox
        cmbNewLang.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                OBJECTS.SETTINGS.setActiveLanguage(LanguagesEnum.fromNativeName(newValue).getLangCode());
                setupWidgetText();
            }
        });

        // Set listener for pTxtPassword, when user press Enter key
        pTxtPassword.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                loginButtonClicked();
            }
        });
    }

    private void showExistingUserWindow () {
        vBoxLogin.setVisible(true);
        vBoxLogin.setManaged(true);

        vBoxNew.setVisible(false);
        vBoxNew.setManaged(false);

        // Set language if cmbLoginLang has selected value
        if (cmbLoginLang.getValue() == null) {
            OBJECTS.SETTINGS.setActiveLanguage(CONSTANTS.DEFAULT_SETTINGS_LANGUAGE_CODE);
        }
        else {
            OBJECTS.SETTINGS.setActiveLanguage(LanguagesEnum.fromNativeName(cmbLoginLang.getValue()).getLangCode());
        }
        setupWidgetText();
    }

    private void showNewUserWindow () {
        vBoxLogin.setVisible(false);
        vBoxLogin.setManaged(false);

        vBoxNew.setVisible(true);
        vBoxNew.setManaged(true);

        // Set language if cmbNewLang has selected value
        if (cmbNewLang.getValue() == null) {
            OBJECTS.SETTINGS.setActiveLanguage(CONSTANTS.DEFAULT_SETTINGS_LANGUAGE_CODE);
        }
        else {
            OBJECTS.SETTINGS.setActiveLanguage(LanguagesEnum.fromNativeName(cmbNewLang.getValue()).getLangCode());
        }
        setupWidgetText();
    }

    private void loginButtonClicked () {
        // Get selected user
        String selectedUserName = cmbLoginUser.getValue();
        if (selectedUserName == null || !OBJECTS.USERS.isExists(selectedUserName)) {
            UError.error("LOGIN: Failed to login.", "Username (" + selectedUserName + ") does not exist");
            return;
        }

        selectedUser = OBJECTS.USERS.getEntity(selectedUserName);
        if (selectedUser == null) {
            UError.error("LOGIN: Failed to login.", "User with username '" + selectedUserName + "' could not be found.");
            return;
        }

        // Check password
        String password = pTxtPassword.getText();
        if (!selectedUser.getPassword().equals(password)) {
            selectedUser = null;
            pTxtPassword.clear();
            pTxtPassword.setPromptText(OBJECTS.SETTINGS.getl("text_InvalidPassword"));
            pTxtPassword.requestFocus();
            return;
        }

        stage.close();
    }
        
    // FXML event handlers

    @FXML
    private void onBtnSwitchNewClick () {
        showNewUserWindow();
    }

    @FXML
    private void onBtnSwitchExistingClick () {
        showExistingUserWindow();
    }

    @FXML
    private void onBtnCancelClick () {
        stage.close();
    }

    @FXML
    private void onBtnLoginClick () {
        loginButtonClicked();
    }



}
