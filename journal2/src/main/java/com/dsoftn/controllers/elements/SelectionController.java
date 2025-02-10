package com.dsoftn.controllers.elements;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.models.Block;
import com.dsoftn.utils.UJavaFX;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SelectionController implements IElementController {
    // Variables
    private String myName = UJavaFX.getUniqueId();    

    private VBox root = null;

    private VBox vLayout = null;

    // FXML widgets
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
            ImageView imgLoadingList;
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
                FlowPane flowLastContent; // This should be populated with items
                    @FXML
                    ImageView imgLoadingLast;
                @FXML
                Label lblLastShowMore;
            @FXML
            VBox vBoxMost; // Items most used
                @FXML
                Label lblMost;
                @FXML
                FlowPane flowMostContent; // This should be populated with items
                    @FXML
                    ImageView imgLoadingMost;
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

    public void initialize() {
        // Loading icons
        Platform.runLater(() -> {
            // Set images
            imgLoadingList.setImage(new Image(getClass().getResourceAsStream("/gifs/loading2.gif")));
            imgLoadingLast.setImage(new Image(getClass().getResourceAsStream("/gifs/loading2.gif")));
            imgLoadingMost.setImage(new Image(getClass().getResourceAsStream("/gifs/loading2.gif")));
        });
    }

    // Implementation of IElementController

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void setRoot(VBox root) {
        this.root = root;
        VBox.setVgrow(root, Priority.ALWAYS);
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

    // Public methods


    



}
