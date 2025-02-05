package com.dsoftn.controllers.elements;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.models.Actor;
import com.dsoftn.models.Block;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.InlineCssTextArea;


public class BlockGeneralController implements IBaseController, IElementController {

    // Variables
    private Stage stage = null;
    private AnchorPane root = null;
    private Block block = null;

    // FXML variables

    @FXML
    private HBox hBoxTitle;
        @FXML
        private Label lblID;
        @FXML
        private Button btnActors;
        @FXML
        private Button btnType;
        @FXML
        private Button btnDate;
        @FXML
        private Button btnName;
        @FXML
        private HBox hBoxMessages; // Messages goes here
        @FXML
        private Button btnOptions;
        @FXML
        private Button btnMinimize;
        @FXML
        private Button btnRestore;
        @FXML
        private Button btnClose;

    @FXML
    private HBox hBoxHeader;
        @FXML
        private Button btnAddTag;
        @FXML
        private Button btnCategory;

    @FXML
    private HBox hBoxName;
        @FXML
        private Label lblName; // Block name in center

    @FXML
    private HBox hBoxContent; // Here goes block content

    @FXML
    private HBox hBoxAttachments;
        @FXML
        private HBox hBoxAttachmentsList; // Put here attachments, make sure that last entry is vBoxAttachmentsControl
            @FXML
            private VBox vBoxAttachmentsControl;
                @FXML
                private Button btnAddAttachment;
    
    @FXML
    private HBox hBoxBlocks;
        @FXML
        private HBox hBoxBlocksList; // Put here blocks, make sure that last entry is vBoxBlocksControl
            @FXML
            private VBox vBoxBlockControl;
                @FXML
                private Button btnAddBlock;

    @FXML
    private HBox hBoxFooter;
        @FXML
        private Button btnSave; // Save as completed block
        @FXML
        private Button btnSaveAsDraft; // Save as draft block
        @FXML
        private Button btnDelete;
        @FXML
        private Button btnRelateAttachment;
        @FXML
        private Button btnRelateBlock;
        @FXML
        private Button btnAddNewBlock;
    

    // Constructor
    public BlockGeneralController() {
        setBlock(null);
    }

    public BlockGeneralController(Block block) {
        setBlock(block);
    }

    public void initialize() {
        // Setup icons
        int iconSize = 20;

        ImageView imgOptions = new ImageView(new Image(getClass().getResourceAsStream("/images/options_bw.png")));
        imgOptions.setPreserveRatio(true);
        imgOptions.setFitHeight(iconSize);
        ImageView imgMinimize = new ImageView(new Image(getClass().getResourceAsStream("/images/minimize_bw.png")));
        imgMinimize.setPreserveRatio(true);
        imgMinimize.setFitHeight(iconSize);
        ImageView imgRestore = new ImageView(new Image(getClass().getResourceAsStream("/images/restore_bw.png")));
        imgRestore.setPreserveRatio(true);
        imgRestore.setFitHeight(iconSize);
        ImageView imgClose = new ImageView(new Image(getClass().getResourceAsStream("/images/close.png")));
        imgClose.setPreserveRatio(true);
        imgClose.setFitHeight(iconSize);

        btnOptions.setGraphic(imgOptions);
        btnMinimize.setGraphic(imgMinimize);
        btnRestore.setGraphic(imgRestore);
        btnClose.setGraphic(imgClose);

    }

    // Interface IBaseController methods

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void startMe() {
        if (stage == null) {
            return;
        }

        stage.show();
    }

    @Override
    public void closeMe() {
        if (stage == null) {
            return;
        }

        stage.close();
    }

    // Interface IElementController methods

    @Override
    public AnchorPane getRoot() {
        return root;
    }

    @Override
    public void setRoot(AnchorPane root) {
        this.root = root;
    }

    // Public methods

    public void setBlock(Block block) {
        this.block = block;

        refresh();
    }

    public void refresh(Block block) {
        if (block == null) {
            return;
        }

        // ID
        setLblID(block);
        // Actors
        setBtnActors(block);
        // Block Type
        setBtnType(block);
        // Date
        btnDate.setText(block.getDateSTR());
        // Name
        setBtnName(block);
        


    }

    public void refresh() {
        refresh(this.block);
    }

    private void setLblID(Block block) {
        if (block.getID() != CONSTANTS.INVALID_ID) {
            lblID.setText("ID: " + block.getID());
        }
        else {
            lblID.setText("ID: ?");
        }
    }

    private void setBtnActors(Block block) {
        String actors = "";
        actors = block.getRelatedActors().stream().map(Actor::getName).reduce(actors, (a, b) -> a + ", " + b);

        if (actors.length() == 0) {
            Image image = new Image(getClass().getResourceAsStream("/images/actor_none.png"));
            btnActors.setText(OBJECTS.SETTINGS.getl("text_Actors"));
            btnActors.setGraphic(new ImageView(image));
            return;
        }

        if (block.getRelatedActorsIDs().size() == 1) {
            Actor actor = block.getRelatedActors().get(0);

            btnActors.setText(actor.getName());

            if (actor.getImagePath() != null && !actor.getImagePath().isEmpty()) {
                Image image = new Image(actor.getImagePath());
                btnActors.setGraphic(new ImageView(image));
            }
            else {
                btnActors.setGraphic(null);
            }
            
            return;
        }

        btnActors.setText(actors);
        btnActors.setGraphic(null);
    }

    private void setBtnType(Block block) {
        btnType.setText(block.getBlockType().toString());
        Image image = new Image(block.getBlockType().getImagePath());
        btnType.setGraphic(new ImageView(image));
    }

    private void setBtnName(Block block) {
        if (block.getName() == null && block.getName().isEmpty()) {
            btnName.setText(OBJECTS.SETTINGS.getl("text_SetName"));
            return;
        }

        btnName.setText(OBJECTS.SETTINGS.getl("text_ChangeName"));
    }



}
