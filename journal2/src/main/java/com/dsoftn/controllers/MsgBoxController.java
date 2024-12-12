package com.dsoftn.controllers;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.Interfaces.IBaseController;

public class MsgBoxController implements IBaseController {

    public enum MsgBoxIcon {
        NONE(""),
        APP("/images/app.png"),
        INFORMATION("/images/information.png"),
        WARNING("/images/warning.png"),
        ERROR("/images/error.png");

        private final String iconPath;

        // Constructor

        MsgBoxIcon(String iconPath) {
            this.iconPath = iconPath;
        }

        // Getters

        public String getIconPath() {
            return iconPath;
        }

    }

    public enum MsgBoxButton {
        CANCEL,
        OK,
        YES,
        NO,
        FIND;
    }

    // Variables
    private Stage stage;
    private List<MsgBoxButton> buttons = new ArrayList<>() { { add(MsgBoxButton.OK); } };


    // Public methods

    public void setButtons (MsgBoxButton... buttons) {
        this.buttons.clear();
        this.buttons.addAll(List.of(buttons));
    }

    // Interface IBaseController methods

    @Override
    public void setStage (Stage stage) {
        this.stage = stage;
    }

    @Override
    public void startMe () {
        stage.showAndWait();
    }

    @Override
    public void closeMe () {
        stage.close();
    }

}
