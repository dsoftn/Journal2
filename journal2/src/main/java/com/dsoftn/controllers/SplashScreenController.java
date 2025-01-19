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

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.enums.models.ScopeEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.utils.UError;
import com.dsoftn.events.TaskStateEvent;


public class SplashScreenController implements IBaseController, ICustomEventListener {
    // Variables
    private Stage stage;
    private Image imageSelected = new Image(getClass().getResourceAsStream("/images/item_selected.png"));
    private ImageView imgWorking = new ImageView(new Image(getClass().getResourceAsStream("/images/item_selected2.png")));
    private ImageView imgDone = new ImageView(new Image(getClass().getResourceAsStream("/images/ok.png")));
    private ImageView imgError = new ImageView(new Image(getClass().getResourceAsStream("/images/error.png")));

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
    private Label lblLoading;
    @FXML
    private Label lblAppName;
    @FXML
    private ImageView imgLoading;

    public void initialize () {
        imgWorking.setFitHeight(20);
        imgWorking.setPreserveRatio(true);
        imgDone.setFitHeight(20);
        imgDone.setPreserveRatio(true);
        imgError.setFitHeight(20);
        imgError.setPreserveRatio(true);

        // Set image to user
        if (OBJECTS.ACTIVE_USER.getAvatarPath().isEmpty()) {
            imgUser.setImage(new Image(getClass().getResourceAsStream("/images/user_no_image.png")));
        }
        else {
            imgUser.setImage(new Image(OBJECTS.ACTIVE_USER.getAvatarPath()));
        }
        
        // Set loading gif
        imgLoading.setImage(new Image(getClass().getResourceAsStream("/gifs/loading.gif")));

        // Set images for labels
        ImageView imageSelected1 = new ImageView(imageSelected);
        imageSelected1.setFitHeight(20);
        imageSelected1.setPreserveRatio(true);
        lblTaskBlocks.setGraphic(imageSelected1);

        ImageView imageSelected2 = new ImageView(imageSelected);
        imageSelected2.setFitHeight(20);
        imageSelected2.setPreserveRatio(true);
        lblTaskDefinitions.setGraphic(imageSelected2);

        ImageView imageSelected3 = new ImageView(imageSelected);
        imageSelected3.setFitHeight(20);
        imageSelected3.setPreserveRatio(true);
        lblTaskAttachments.setGraphic(imageSelected3);

        ImageView imageSelected4 = new ImageView(imageSelected);
        imageSelected4.setFitHeight(20);
        imageSelected4.setPreserveRatio(true);
        lblTaskCategories.setGraphic(imageSelected4);

        ImageView imageSelected5 = new ImageView(imageSelected);
        imageSelected5.setFitHeight(20);
        imageSelected5.setPreserveRatio(true);
        lblTaskTags.setGraphic(imageSelected5);

        ImageView imageSelected6 = new ImageView(imageSelected);
        imageSelected6.setFitHeight(20);
        imageSelected6.setPreserveRatio(true);
        lblTaskDefVariants.setGraphic(imageSelected6);

        ImageView imageSelected7 = new ImageView(imageSelected);
        imageSelected7.setFitHeight(20);
        imageSelected7.setPreserveRatio(true);
        lblTaskRelations.setGraphic(imageSelected7);

    }

    // Interface ICustomEventListener methods
    @Override
    public void onCustomEvent (Event event) {
        // Check if event belongs to SplashScreenController
        if (!(event instanceof TaskStateEvent)) { return; }
        TaskStateEvent taskStateEvent = (TaskStateEvent) event;
        if (taskStateEvent.getModel() == ScopeEnum.NONE || taskStateEvent.getID() != null) { return; }

        // if event is signaling that task is in progress update widgets.
        ScopeEnum model = taskStateEvent.getModel();
        switch (model) {
            case BLOCK:
                changeTaskWidget(lblTaskBlocks, "text_Blocks", taskStateEvent);
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
            case ALL:
                // Check if event is signaling that task is done
                if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
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
                label.setGraphic(imgDone);
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
                try {
                    Thread.sleep(3000);
                    boolean result = createGlobalDataModels();
                    Thread.sleep(3000);

                    return result;
                } catch (InterruptedException e) {
                    UError.exception("SplashScreenController.startMe -> Thread (createGlobalDataModels) failed", e);
                    return false;
                }
            }
        };

        // When task is finished send event
        task.setOnSucceeded(e -> {
            Boolean result = task.getValue();

            if (result == true) {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        ScopeEnum.ALL,
                        TaskStateEnum.COMPLETED
                    )
                );
            }
            else {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        ScopeEnum.ALL,
                        TaskStateEnum.FAILED
                    )
                );
            }
        });

        task.setOnFailed(e -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ScopeEnum.ALL,
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
        if (!OBJECTS.BLOCKS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.BLOCKS.load() failed");
            return false;
        }
        if (!OBJECTS.DEFINITIONS.load()) {
            UError.error("GuiMain.createGlobalDataModels -> OBJECTS.DEFINITIONS.load() failed");
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
        lblTaskDefinitions.setText(OBJECTS.SETTINGS.getl("text_Definitions"));
        lblTaskAttachments.setText(OBJECTS.SETTINGS.getl("text_Attachments"));
        lblTaskCategories.setText(OBJECTS.SETTINGS.getl("text_Categories"));
        lblTaskTags.setText(OBJECTS.SETTINGS.getl("text_Tags"));
        lblTaskDefVariants.setText(OBJECTS.SETTINGS.getl("text_DefinitionVariants"));
        lblTaskRelations.setText(OBJECTS.SETTINGS.getl("text_Relations"));

        // App Info
        lblAppName.setText(CONSTANTS.APPLICATION_NAME);
        lblLoading.setText(OBJECTS.SETTINGS.getl("text_Loading"));
    }

}
