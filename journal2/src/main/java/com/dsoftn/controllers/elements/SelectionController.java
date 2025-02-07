package com.dsoftn.controllers.elements;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SelectionController {
    // Variables


    // FXML widgets

    // Title
    @FXML
    VBox vBoxTitle; // Title
        @FXML
        Label lblName;
        @FXML
        Button btnClose;
        @FXML
        Region regTitleSpacer;
    @FXML
    VBox vBoxClip; // From Clipboard
        @FXML
        Label lblClipSelect;
        @FXML
        VBox vBoxClipClip; // Data from clipboard
            @FXML
            Button btnClip;
            @FXML
            Label lblClipContent;
        @FXML
        VBox vBoxClipFile; // Clipboard has text that points to a file
            @FXML
            Button btnFile;
            @FXML
            Label lblFileContent;
        @FXML
        VBox vBoxClipWeb; // Clipboard has text that points to a web
            @FXML
            Button btnWeb;
            @FXML
            Label lblWebContent;
    @FXML
    SplitPane splitPane; // SplitPane with List and Recommendations
        @FXML
        AnchorPane ancList; // 1st pane
            @FXML
            TextField txtFind;
            @FXML
            ListView<String> lstItems;
            @FXML
            Label lblCounter;
        @FXML
        AnchorPane ancRecommendations; // 2nd pane
            @FXML
            VBox vBoxLast; // Items last used
                @FXML
                Label lblLast;
                @FXML
                HBox hBoxLastContent; // This should be populated with items
                @FXML
                Label lblLastShowMore;
            @FXML
            VBox vBoxMost; // Items most used
                @FXML
                Label lblMost;
                @FXML
                HBox hBoxMostContent; // This should be populated with items
                @FXML
                Label lblMostShowMore;
            @FXML
            VBox vBoxSelected; // Items that are currently selected
                @FXML
                Label lblSelected;
                @FXML
                VBox vBoxSelectedContent; // This should be populated with items
            @FXML
            Button btnClear; // Clear selected items
            @FXML
            Button btnSelect; // Select items



    



}
