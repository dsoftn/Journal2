package com.dsoftn.controllers;

import javafx.stage.Stage;

import com.dsoftn.Interfaces.IBaseController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


public class LoginController implements IBaseController {

    // Variables
    private Stage stage;

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
    ComboBox<String> cmbLoginUsers; // Existing users list
    @FXML
    PasswordField ptxtPassword; // Existing user password field
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
    PasswordField ptxtNewPassword; // New user password field
    @FXML
    PasswordField ptxtNewConfirmPassword; // New user password confirmation
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
        showExistingUserWindow();
        
        stage.setHeight(470);
        stage.showAndWait();
    }

    @Override
    public void closeMe () {
        stage.close();
    }

    // Public methods

    public String getAuthenticatedUser() {
        return "result";
    }


    // Private methods

    private void showExistingUserWindow () {
        vBoxLogin.setVisible(true);
        vBoxLogin.setManaged(true);

        vBoxNew.setVisible(false);
        vBoxNew.setManaged(false);
    }

    private void showNewUserWindow () {
        vBoxLogin.setVisible(false);
        vBoxLogin.setManaged(false);

        vBoxNew.setVisible(true);
        vBoxNew.setManaged(true);
    }



}
