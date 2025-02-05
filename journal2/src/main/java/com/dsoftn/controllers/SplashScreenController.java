package com.dsoftn.controllers;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.Event;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.Map;
import java.util.HashMap;
import java.io.File;

import com.dsoftn.CONSTANTS;
import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.BlockTypeEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.utils.UError;
import com.dsoftn.events.TaskStateEvent;


public class SplashScreenController implements IBaseController, ICustomEventListener {
    // Variables
    private Stage stage;
    private Image imageSelected = new Image(getClass().getResourceAsStream("/images/item_selected.png"));
    private Map<String, ImageView> imgSelected = new HashMap<>();
    private ImageView imgWorking = new ImageView(new Image(getClass().getResourceAsStream("/images/item_selected2.png")));
    private Image imageDone = new Image(getClass().getResourceAsStream("/images/ok.png"));
    private Map<String, ImageView> imgDone = new HashMap<>();
    private ImageView imgError = new ImageView(new Image(getClass().getResourceAsStream("/images/error.png")));
    private int blockTypesCount = BlockTypeEnum.values().length - 1; // -1 to exclude UNDEFINED
    private int blockTypesCurrent = 0;

    // FXML Widgets
    @FXML
    private ImageView imgUser; // Logged in user image
    @FXML
    private Label lblUserName; // Logged in user name
    @FXML
    private Label lblUserEmail; // Logged in user email

    @FXML
    private Label lblTaskBlocks;
    @FXML
    private Label lblTaskBlockTypes; // All block types
    @FXML
    private Label lblTaskDefinitions;
    @FXML
    private Label lblTaskAttachments;
    @FXML
    private Label lblTaskCategories;
    @FXML
    private Label lblTaskTags;
    @FXML
    private Label lblTaskDefVariants;
    @FXML
    private Label lblTaskRelations;
    @FXML
    private Label lblTaskActors;

    @FXML
    private Label lblLoading;
    @FXML
    private Label lblAppName;
    @FXML
    private ImageView imgLoading;

    public void initialize () {
        imgWorking.setFitHeight(20);
        imgWorking.setPreserveRatio(true);
        imgError.setFitHeight(20);
        imgError.setPreserveRatio(true);

        // Add to imgDone map ImageView for all ScopeEnum values
        for (ModelEnum scope : ModelEnum.values()) {
            imgDone.put(scope.toString(), new ImageView(imageDone));
            imgDone.get(scope.toString()).setFitHeight(20);
            imgDone.get(scope.toString()).setPreserveRatio(true);

            imgSelected.put(scope.toString(), new ImageView(imageSelected));
            imgSelected.get(scope.toString()).setFitHeight(20);
            imgSelected.get(scope.toString()).setPreserveRatio(true);
        }

        // Set image to user
        if (OBJECTS.ACTIVE_USER.getAvatarPath().isEmpty()) {
            imgUser.setImage(new Image(getClass().getResourceAsStream("/images/user_no_image.png")));
        }
        else {
            try {
                File file = new File(OBJECTS.ACTIVE_USER.getAvatarPath());
                Image avatarImage = new Image(file.toURI().toString());
                imgUser.setImage(avatarImage);
            }
            catch (Exception e) {
                UError.exception("Failed to load user avatar image: " + OBJECTS.ACTIVE_USER.getAvatarPath(), e);
                // Show message to user
                MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(null);
                msgBoxController.setTitleText("Configuration Error");
                msgBoxController.setHeaderText("Error Loading User Avatar");
                msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxController.setContentText("The user avatar image could not be loaded.\n" + "Please copy the avatar image in '" + OBJECTS.ACTIVE_USER.getPathFolder() + "' as 'avatar.png' or 'avatar.jpg' and try again.\n" + e.getMessage());
                msgBoxController.setContentIcon(MsgBoxIcon.USER_NO_IMAGE);
                msgBoxController.setButtons(MsgBoxButton.OK);
                msgBoxController.setDefaultButton(MsgBoxButton.OK);
                msgBoxController.startMe();

                imgUser.setImage(new Image(getClass().getResourceAsStream("/images/user_no_image.png")));
            }
        }
        
        // Set loading gif
        imgLoading.setImage(new Image(getClass().getResourceAsStream("/gifs/loading.gif")));

        // Set images for labels
        lblTaskBlocks.setGraphic(imgSelected.get(ModelEnum.BLOCK.toString()));
        lblTaskBlockTypes.setGraphic(imgSelected.get(ModelEnum.BLOCK_TYPE.toString()));
        lblTaskDefinitions.setGraphic(imgSelected.get(ModelEnum.DEFINITION.toString()));
        lblTaskAttachments.setGraphic(imgSelected.get(ModelEnum.ATTACHMENT.toString()));
        lblTaskCategories.setGraphic(imgSelected.get(ModelEnum.CATEGORY.toString()));
        lblTaskTags.setGraphic(imgSelected.get(ModelEnum.TAG.toString()));
        lblTaskDefVariants.setGraphic(imgSelected.get(ModelEnum.DEF_VARIANT.toString()));
        lblTaskRelations.setGraphic(imgSelected.get(ModelEnum.RELATION.toString()));
        lblTaskActors.setGraphic(imgSelected.get(ModelEnum.ACTOR.toString()));
    }

    // Interface ICustomEventListener methods
    @Override
    public void onCustomEvent (Event event) {
        // Check if event belongs to SplashScreenController
        if (!(event instanceof TaskStateEvent)) { return; }
        TaskStateEvent taskStateEvent = (TaskStateEvent) event;
        if (taskStateEvent.getModel() == ModelEnum.NONE || taskStateEvent.getID() != null) { return; }

        // if event is signaling that task is in progress update widgets.
        ModelEnum model = taskStateEvent.getModel();
        switch (model) {
            case BLOCK:
                changeTaskWidget(lblTaskBlocks, "text_Blocks", taskStateEvent);
                break;
            case BLOCK_TYPE:
                changeBlockTypeTaskWidget(lblTaskBlockTypes, "text_BlockTypes", taskStateEvent);
                break;
            case DEFINITION:
                changeTaskWidget(lblTaskDefinitions, "text_Definitions", taskStateEvent);
                break;
            case ATTACHMENT:
                changeTaskWidget(lblTaskAttachments, "text_Attachments", taskStateEvent);
                break;
            case CATEGORY:
                changeTaskWidget(lblTaskCategories, "text_Categories", taskStateEvent);
                break;
            case TAG:
                changeTaskWidget(lblTaskTags, "text_Tags", taskStateEvent);
                break;
            case DEF_VARIANT:
                changeTaskWidget(lblTaskDefVariants, "text_DefinitionVariants", taskStateEvent);
                break;
            case RELATION:
                changeTaskWidget(lblTaskRelations, "text_Relations", taskStateEvent);
                break;
            case ACTOR:
                changeTaskWidget(lblTaskActors, "text_Actors", taskStateEvent);
                break;
            case ALL:
                // Check if event is signaling that task is done
                if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                    // Pause for 3 seconds before closing the application
                    // try { Thread.sleep(3000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                    closeMe();
                    return;
                }
                // Check if event is signaling that task is failed
                if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                    closeMe();
                    return;
                }
            default:
                break;
        }
        
    }

    private void changeTaskWidget(Label label, String textSettingsKey, TaskStateEvent taskStateEvent) {
        Platform.runLater(() -> {
            if (taskStateEvent.getState() == TaskStateEnum.STARTED) {
                label.setGraphic(imgWorking);
            }
            else if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                label.setGraphic(imgDone.get(taskStateEvent.getModel().toString()));
                label.setText(OBJECTS.SETTINGS.getl(textSettingsKey));
            }
            else if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                label.setGraphic(imgError);
            }
            else if (taskStateEvent.getState() == TaskStateEnum.EXECUTING && taskStateEvent.getProgressPercent() != null) {
                label.setText(OBJECTS.SETTINGS.getl(textSettingsKey) + ": " + taskStateEvent.getProgressString());
            }
        });

    }

    private void changeBlockTypeTaskWidget(Label label, String textSettingsKey, TaskStateEvent taskStateEvent) {
        Platform.runLater(() -> {
            if (taskStateEvent.getState() == TaskStateEnum.STARTED) {
                blockTypesCurrent++;
                label.setGraphic(imgWorking);
            }
            else if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                if (blockTypesCurrent == blockTypesCount) {
                    label.setGraphic(imgDone.get(taskStateEvent.getModel().toString()));
                    label.setText(OBJECTS.SETTINGS.getl(textSettingsKey));
                }
            }
            else if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                label.setGraphic(imgError);
            }
            else if (taskStateEvent.getState() == TaskStateEnum.EXECUTING && taskStateEvent.getProgressPercent() != null) {
                String text = "(" + blockTypesCurrent + "/" + blockTypesCount + ") " + taskStateEvent.getProgressString();
                label.setText(OBJECTS.SETTINGS.getl(textSettingsKey) + text);
            }
        });

    }

    // Interface IBaseController methods
    @Override
    public void setStage (Stage stage) {
        this.stage = stage;
    }

    @Override
    public void startMe () {
        stage.initStyle(StageStyle.UNDECORATED);
        
        // Register dialog to EventHandler
        OBJECTS.EVENT_HANDLER.register(
            this,
            TaskStateEvent.TASK_STATE_EVENT
        );
        
        // Setup widgets        
        setupWidgetText();

        // Start new task
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return createGlobalDataModels();
            }
        };

        // When task is finished send event
        task.setOnSucceeded(e -> {
            Boolean result = task.getValue();

            if (result == true) {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        ModelEnum.ALL,
                        TaskStateEnum.COMPLETED
                    )
                );
            }
            else {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        ModelEnum.ALL,
                        TaskStateEnum.FAILED
                    )
                );
            }
        });

        task.setOnFailed(e -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ModelEnum.ALL,
                    TaskStateEnum.FAILED
                )
            );
        });

        // Start task
        new Thread(task).start();
        
        stage.showAndWait();
    }

    @Override
    public void closeMe () {
        stage.close();
    }


    // Private methods
    private boolean createGlobalDataModels() {
        if (!OBJECTS.RELATIONS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.RELATIONS.load() failed");
            return false;
        }
        if (!OBJECTS.DEFINITIONS_VARIANTS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.DEFINITIONS_VARIANTS.load() failed");
            return false;
        }
        if (!OBJECTS.TAGS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.TAGS.load() failed");
            return false;
        }
        if (!OBJECTS.CATEGORIES.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.CATEGORIES.load() failed");
            return false;
        }
        if (!OBJECTS.ATTACHMENTS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.ATTACHMENTS.load() failed");
            return false;
        }
        if (!OBJECTS.ACTORS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.ACTORS.load() failed");
            return false;
        }
        if (!OBJECTS.BLOCKS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.BLOCKS.load() failed");
            return false;
        }
        if (!createGlobalBlockTypeModels()) {
            // This will create all BlockType models
            // Each BlockType will handle its own loading and trigger error if failed
            return false;
        }
        if (!OBJECTS.DEFINITIONS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.DEFINITIONS.load() failed");
            return false;
        }

        return true;
    }

    private boolean createGlobalBlockTypeModels() {
        if (!OBJECTS.BLOCKS_DIARY.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.BLOCKS_DIARY.load() failed");
            return false;
        }

        return true;
    }

    private void setupWidgetText () {
        // User Info
        lblUserName.setText(OBJECTS.ACTIVE_USER.getUsername());
        lblUserEmail.setText(OBJECTS.ACTIVE_USER.getEmail());

        // Tasks Info
        lblTaskBlocks.setText(OBJECTS.SETTINGS.getl("text_Blocks"));
        lblTaskBlockTypes.setText(OBJECTS.SETTINGS.getl("text_BlockTypes"));
        lblTaskDefinitions.setText(OBJECTS.SETTINGS.getl("text_Definitions"));
        lblTaskAttachments.setText(OBJECTS.SETTINGS.getl("text_Attachments"));
        lblTaskCategories.setText(OBJECTS.SETTINGS.getl("text_Categories"));
        lblTaskTags.setText(OBJECTS.SETTINGS.getl("text_Tags"));
        lblTaskDefVariants.setText(OBJECTS.SETTINGS.getl("text_DefinitionVariants"));
        lblTaskRelations.setText(OBJECTS.SETTINGS.getl("text_Relations"));
        lblTaskActors.setText(OBJECTS.SETTINGS.getl("text_Actors"));

        // App Info
        lblAppName.setText(CONSTANTS.APPLICATION_NAME);
        lblLoading.setText(OBJECTS.SETTINGS.getl("text_Loading"));
    }

}
