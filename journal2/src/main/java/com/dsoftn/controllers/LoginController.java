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
import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.models.User;
import com.dsoftn.utils.LanguagesEnum;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;


public class LoginController implements IBaseController {

    // Variables

    private String myName = UJavaFX.getUniqueId();

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
    public String getMyName () {
        return myName;
    }

    @Override
    public void setStage (Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage () {
        return stage;
    }
    
    @Override
    public void startMe () {
        setupWidgetText();
        setupWidgets();

        showExistingUserWindow();
        
        stage.setHeight(480);
        pTxtPassword.requestFocus();
        stage.showAndWait();
    }

    @Override
    public void closeMe () {
        stage.close();
    }

    // Public methods

    public User getAuthenticatedUser() {
        if (selectedUser != null) {
            selectedUser.setLanguage(LanguagesEnum.fromLangCode(OBJECTS.SETTINGS.getActiveLanguage()));
        }
        
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

        // Populate Existing Users ComboBox
        for (User user : OBJECTS.USERS.getEntityAll()) {
            cmbLoginUser.getItems().add(user.getUsername());
        }

        // Set listener for cmbLoginUser ComboBox
        cmbLoginUser.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cmbLoginLang.setValue(OBJECTS.USERS.getEntity(newValue).getLanguage().getNativeName());
                pTxtPassword.requestFocus();
            }
        });

        // Set last active user
        User lastUser = OBJECTS.USERS.lastActiveUser();
        if (lastUser != null) {
            cmbLoginUser.setValue(lastUser.getUsername());
        }
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
        pTxtPassword.requestFocus();
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
        txtNewUser.requestFocus();
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

    private User getNewUser () {
        String userName = txtNewUser.getText();
        String password = pTxtNewPassword.getText();
        String passwordConfirm = pTxtNewConfirmPassword.getText();

        User newUser = new User();

        String checkString = checkUser(userName, password, passwordConfirm);
        if (!checkString.isEmpty()) {
            MsgBoxController msgBox = DIALOGS.getMsgBoxController(stage);
            msgBox.setHeaderIcon(MsgBoxIcon.USER);
            msgBox.setHeaderText(OBJECTS.SETTINGS.getl("text_CreateNewUser"));
            msgBox.setContentIcon(MsgBoxIcon.WARNING);
            msgBox.setContentText(checkString);
            msgBox.setButtons(MsgBoxButton.OK);
            msgBox.startMe();
            return null;
        }

        newUser.setUsername(userName);
        newUser.setPassword(password);
        newUser.setLanguage(LanguagesEnum.fromNativeName(cmbNewLang.getValue()));

        return newUser;
    }

    private String checkUser (String userName, String password, String passwordConfirm) {
        String result = "";
        User userObject = new User();

        if (OBJECTS.USERS.isExists(userName)) {
            result += OBJECTS.SETTINGS.getl("username_exist")
                .replace("#1", userName);
            result += "\n\n";
        }

        if (userName.isEmpty()) {
            result += OBJECTS.SETTINGS.getl("username_empty");
            result += "\n\n";
        }

        if (!userObject.isUserNameValid(userName)) {
            result += OBJECTS.SETTINGS.getl("username_invalid")
                .replace("#1", String.valueOf(userObject.getUserNameMaxLength()))
                .replace("#2", userObject.getUserNameAllowedChars());
            result += "\n\n";
        }

        if (!userObject.isPasswordValid(password)) {
            result += OBJECTS.SETTINGS.getl("password_invalid")
                .replace("#1", String.valueOf(userObject.getPasswordMaxLength()))
                .replace("#2", userObject.getPasswordAllowedChars());
            result += "\n\n";
        }

        if (!password.equals(passwordConfirm)) {
            result += OBJECTS.SETTINGS.getl("password_confirmation");
            result += "\n\n";
        }

        return result;
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

    @FXML
    private void onBtnCreateNewClick () {
        User newUser = getNewUser();
        if (newUser == null) {
            return;
        }

        boolean added = newUser.add();
        if (!added) {
            UError.error("LOGIN: Failed to create new user.", "User could not be added to database.");
            return;
        }

        // Populate Existing Users ComboBox
        cmbLoginUser.getItems().clear();
        for (User user : OBJECTS.USERS.getEntityAll()) {
            cmbLoginUser.getItems().add(user.getUsername());
        }
        cmbLoginUser.setValue(newUser.getUsername());

        showExistingUserWindow();
    }


}
