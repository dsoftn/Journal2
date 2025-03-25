package com.dsoftn.controllers.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dsoftn.DIALOGS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.controllers.MsgBoxController;
import com.dsoftn.controllers.MsgBoxController.MsgBoxButton;
import com.dsoftn.controllers.MsgBoxController.MsgBoxIcon;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.events.ActorAddedEvent;
import com.dsoftn.events.ActorDeletedEvent;
import com.dsoftn.events.ActorUpdatedEvent;
import com.dsoftn.events.AttachmentAddedEvent;
import com.dsoftn.events.AttachmentDeletedEvent;
import com.dsoftn.events.AttachmentUpdatedEvent;
import com.dsoftn.events.BlockAddedEvent;
import com.dsoftn.events.BlockDeletedEvent;
import com.dsoftn.events.BlockUpdatedEvent;
import com.dsoftn.events.CategoryAddedEvent;
import com.dsoftn.events.CategoryDeletedEvent;
import com.dsoftn.events.CategoryUpdatedEvent;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.events.DefinitionAddedEvent;
import com.dsoftn.events.DefinitionDeletedEvent;
import com.dsoftn.events.DefinitionUpdatedEvent;
import com.dsoftn.events.MessageEvent;
import com.dsoftn.events.TagAddedEvent;
import com.dsoftn.events.TagDeletedEvent;
import com.dsoftn.events.TagUpdatedEvent;
import com.dsoftn.services.SelectionData;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SelectionController implements IElementController, ICustomEventListener {
    public enum Section {
        CLIPBOARD,
        LIST,
        RECOMMENDATIONS,
        LAST_USED,
        MOST_USED,
        SELECTED;
    }

    // Variables
    private Stage stage = null;
    private String myName = UJavaFX.getUniqueId();
    private String mySettingsName = "Selection";
    private int listFontSize = 12;
    private VBox root = null;
    private VBox vLayout = null;
    private SelectionData selectionData = null;
    private boolean ignoreListChanges = false;
    private String receiverID = null;
    private IBaseController parentController = null;

    private List<SelectionData.Item> dataAll = new ArrayList<>();
    private List<SelectionData.Item> dataFiltered = null;
    private List<SelectionData.Item> dataLast = new ArrayList<>();
    private List<SelectionData.Item> dataMost = new ArrayList<>();

    private List<SelectionData.Item> currentlySelected = new ArrayList<>();

    // FXML widgets
    @FXML
    VBox vBoxClip; // From Clipboard
        @FXML
        Label lblClipboard;
        @FXML
        VBox vBoxClipControls; // Clipboard controls like select all, select none...
            @FXML
            Label lblClipCounter;
            @FXML
            Button btnClipSelectAll;
            @FXML
            Button btnClipSelectNone;
        @FXML
        FlowPane flowClipContent; // This should be populated with items

    @FXML
    SplitPane splitPane; // SplitPane with List and Recommendations
        @FXML
        AnchorPane ancList; // 1st pane
            @FXML
            ImageView imgLoadingList;
            @FXML
            TextField txtFind;
            @FXML
            ListView<SelectionData.Item> lstItems;
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

    // Implementation of ICustomEventListener

    @Override
    public void onCustomEvent(Event event) {
        // Handle events for all Models
        modelEventsHandler(event);

        // Handle events for Clipboard
        if (event instanceof ClipboardChangedEvent) {
            ClipboardChangedEvent clipboardChangedEvent = (ClipboardChangedEvent) event;
            if (clipboardChangedEvent.getClipModel() == selectionData.getRelatedModel()) {
                updateClipboardAppearance();
            }
        }

        if (!(event instanceof TaskStateEvent)) { return; }

        TaskStateEvent taskStateEvent = (TaskStateEvent) event;

        // List
        if (taskStateEvent.getID().equals(myName + "LIST")) {
            if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                populateList();
            }
            if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                UError.error("GuiMain.onCustomEvent: Error in loading list: ", "ID: " + myName);
                MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
                msgBoxController.setTitleText("Loading Error");
                msgBoxController.setHeaderText("Loading List Failed");
                msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxController.setContentText("Failed to load list data.\nList cannot be loaded and populated with data.");
                msgBoxController.setContentIcon(MsgBoxIcon.WARNING);
                msgBoxController.setButtons(MsgBoxButton.OK);
                msgBoxController.setDefaultButton(MsgBoxButton.OK);
                msgBoxController.startMe();
                this.dataAll = new ArrayList<>();
            }
        }

        // Last items
        if (taskStateEvent.getID().equals(myName + "LAST")) {
            if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                populateLast(dataLast);
            }
            if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                UError.error("GuiMain.onCustomEvent: Error in loading last used items: ", "ID: " + myName);
                MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
                msgBoxController.setTitleText("Loading Error");
                msgBoxController.setHeaderText("Loading last items Failed");
                msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxController.setContentText("Failed to load last used items data.\nSection cannot be loaded and populated with data.");
                msgBoxController.setContentIcon(MsgBoxIcon.WARNING);
                msgBoxController.setButtons(MsgBoxButton.OK);
                msgBoxController.setDefaultButton(MsgBoxButton.OK);
                msgBoxController.startMe();
                this.dataLast = new ArrayList<>();
            }
        }

        // Most items
        if (taskStateEvent.getID().equals(myName + "MOST")) {
            if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                populateMost(dataMost);
            }
            if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                UError.error("GuiMain.onCustomEvent: Error in loading most used items: ", "ID: " + myName);
                MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
                msgBoxController.setTitleText("Loading Error");
                msgBoxController.setHeaderText("Loading most items Failed");
                msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxController.setContentText("Failed to load most used items data.\nSection cannot be loaded and populated with data.");
                msgBoxController.setContentIcon(MsgBoxIcon.WARNING);
                msgBoxController.setButtons(MsgBoxButton.OK);
                msgBoxController.setDefaultButton(MsgBoxButton.OK);
                msgBoxController.startMe();
                this.dataMost = new ArrayList<>();
            }
        }

        // Filtering text
        if (taskStateEvent.getID().equals(myName + "FILTER")) {
            if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                Platform.runLater(() -> {
                    imgLoadingList.setVisible(false);

                    if (!txtFind.getText().equals("@LAST_USED") && !txtFind.getText().equals("@MOST_USED")) {
                        txtFind.getStyleClass().remove("text-field-special");
                        txtFind.getStyleClass().remove("text-field-default");
                        txtFind.getStyleClass().add("text-field-default");
            
                        lstItems.getStyleClass().remove("list-default");
                        lstItems.getStyleClass().remove("list-special");
                        lstItems.getStyleClass().add("list-default");
                    }
                });
            }
            if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                UError.error("GuiMain.onCustomEvent: Error in filtering list: ", "ID: " + myName);
                MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(stage);
                msgBoxController.setTitleText("Filter Error");
                msgBoxController.setHeaderText("Filtering List Failed");
                msgBoxController.setHeaderIcon(MsgBoxIcon.ERROR);
                msgBoxController.setContentText("Failed to filter list data.\nList cannot be filtered and populated with data.");
                msgBoxController.setContentIcon(MsgBoxIcon.WARNING);
                msgBoxController.setButtons(MsgBoxButton.OK);
                msgBoxController.setDefaultButton(MsgBoxButton.OK);
                msgBoxController.startMe();
                this.dataFiltered = new ArrayList<>(); // Clear filtered list
            }
        }
    }

    private void modelEventsHandler(Event event) {
        if (selectionData.getRelatedModel() == null) return;

        SelectionData.Item addedModel = null;
        SelectionData.Item updatedModelOld = null;
        SelectionData.Item updatedModelNew = null;
        SelectionData.Item deletedModel = null;

        switch (selectionData.getRelatedModel()) {
            case ACTOR: {
                if (event instanceof ActorAddedEvent) {
                    addedModel = selectionData.getItemEntity(((ActorAddedEvent) event).getActor());
                }
                if (event instanceof ActorUpdatedEvent) {
                    updatedModelOld = selectionData.getItemEntity(((ActorUpdatedEvent) event).getOldBActor());
                    updatedModelNew = selectionData.getItemEntity(((ActorUpdatedEvent) event).getNewActor());
                }
                if (event instanceof ActorDeletedEvent) {
                    deletedModel = selectionData.getItemEntity(((ActorDeletedEvent) event).getActor());
                }
                break;
            }
            case ATTACHMENT: {
                if (event instanceof AttachmentAddedEvent) {
                    addedModel = selectionData.getItemEntity(((AttachmentAddedEvent) event).getAttachment());
                }
                if (event instanceof AttachmentUpdatedEvent) {
                    updatedModelOld = selectionData.getItemEntity(((AttachmentUpdatedEvent) event).getOldAttachment());
                    updatedModelNew = selectionData.getItemEntity(((AttachmentUpdatedEvent) event).getNewAttachment());
                }
                if (event instanceof AttachmentDeletedEvent) {
                    deletedModel = selectionData.getItemEntity(((AttachmentDeletedEvent) event).getAttachment());
                }
                break;
            }
            case BLOCK: {
                if (event instanceof BlockAddedEvent) {
                    addedModel = selectionData.getItemEntity(((BlockAddedEvent) event).getBlock());
                }
                if (event instanceof BlockUpdatedEvent) {
                    updatedModelOld = selectionData.getItemEntity(((BlockUpdatedEvent) event).getOldBlock());
                    updatedModelNew = selectionData.getItemEntity(((BlockUpdatedEvent) event).getNewBlock());
                }
                if (event instanceof BlockDeletedEvent) {
                    deletedModel = selectionData.getItemEntity(((BlockDeletedEvent) event).getBlock());
                }
                break;
            }
            case CATEGORY: {
                if (event instanceof CategoryAddedEvent) {
                    addedModel = selectionData.getItemEntity(((CategoryAddedEvent) event).getCategory());
                }
                if (event instanceof CategoryUpdatedEvent) {
                    updatedModelOld = selectionData.getItemEntity(((CategoryUpdatedEvent) event).getOldCategory());
                    updatedModelNew = selectionData.getItemEntity(((CategoryUpdatedEvent) event).getNewCategory());
                }
                if (event instanceof CategoryDeletedEvent) {
                    deletedModel = selectionData.getItemEntity(((CategoryDeletedEvent) event).getCategory());
                }
                break;
            }
            case DEFINITION: {
                if (event instanceof DefinitionAddedEvent) {
                    addedModel = selectionData.getItemEntity(((DefinitionAddedEvent) event).getDefinition());
                }
                if (event instanceof DefinitionUpdatedEvent) {
                    updatedModelOld = selectionData.getItemEntity(((DefinitionUpdatedEvent) event).getOldDefinition());
                    updatedModelNew = selectionData.getItemEntity(((DefinitionUpdatedEvent) event).getNewDefinition());
                }
                if (event instanceof DefinitionDeletedEvent) {
                    deletedModel = selectionData.getItemEntity(((DefinitionDeletedEvent) event).getDefinition());
                }
                break;
            }
            case TAG: {
                if (event instanceof TagAddedEvent) {
                    addedModel = selectionData.getItemEntity(((TagAddedEvent) event).getTag());
                }
                if (event instanceof TagUpdatedEvent) {
                    updatedModelOld = selectionData.getItemEntity(((TagUpdatedEvent) event).getOldTag());
                    updatedModelNew = selectionData.getItemEntity(((TagUpdatedEvent) event).getNewTag());
                }
                if (event instanceof TagDeletedEvent) {
                    deletedModel = selectionData.getItemEntity(((TagDeletedEvent) event).getTag());
                }
                break;
            }
            default: {
                return;
            }
        }

        if (addedModel != null) {
            dataAll.add(addedModel);
        }
        if (updatedModelOld != null && updatedModelNew != null) {
            int index = dataAll.indexOf(updatedModelOld);
            if (index != -1) dataAll.set(index, updatedModelNew);

            index = dataLast.indexOf(updatedModelOld);
            if (index != -1) dataLast.set(index, updatedModelNew);

            index = dataMost.indexOf(updatedModelOld);
            if (index != -1) dataMost.set(index, updatedModelNew);

            index = currentlySelected.indexOf(updatedModelOld);
            if (index != -1) currentlySelected.set(index, updatedModelNew);
        }
        if (deletedModel != null) {
            int index = dataAll.indexOf(deletedModel);
            if (index != -1) dataAll.remove(index);

            index = dataLast.indexOf(deletedModel);
            if (index != -1) dataLast.remove(index);

            index = dataMost.indexOf(deletedModel);
            if (index != -1) dataMost.remove(index);

            index = currentlySelected.indexOf(deletedModel);
            if (index != -1) currentlySelected.remove(index);
        }

        updateSelectedItemAppearance(true, true, true);
    }

    // Implementation of IElementController

    @Override
    public void setStage(Stage stage) { this.stage = stage; }

    @Override
    public Stage getStage() { return this.stage; }

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
    public IBaseController getParentController() { return parentController; }

    @Override
    public void setParentController(IBaseController parentController) { this.parentController = parentController; }

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

    @Override
    public void closeMe() {
        saveSettings();
    }

    @Override
    public void beforeShowing() {
        txtFind.requestFocus();
    }

    @Override
    public void calculateData() {
        if (selectionData == null) {
            UError.error("SelectionController.calculateData: SelectionData is null");
            return;
        }

        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);

        setupWidgetsText();
        setupWidgetsAppearance();
        loadSettings();

        // Calc list items
        UJavaFX.taskStart(this::calcListItems, myName + "LIST");
        // Calc last items
        UJavaFX.taskStart(this::calcLastItems, myName + "LAST");
        // Calc most items
        UJavaFX.taskStart(this::calcMostItems, myName + "MOST");

        return;
    }

    private void loadSettings() {
        // Define element name for settings
        if (selectionData.getBaseModel() != null) {
            mySettingsName += "_" + selectionData.getBaseModel().toString();
        }
        if (selectionData.getRelatedModel() != null) {
            mySettingsName += "_" + selectionData.getRelatedModel().toString();
        }

        // List Font Size
        if (OBJECTS.SETTINGS.isAppSettingExists(mySettingsName + "_ListFontSize")) {
            listFontSize = OBJECTS.SETTINGS.getAppINTEGER(mySettingsName + "_ListFontSize");
            lstItems.setStyle("-fx-font-size: " + listFontSize + "px;");
        }
        // SplitPane divider position
        if (OBJECTS.SETTINGS.isAppSettingExists(mySettingsName + "_SplitPaneDividerPosition")) {
            splitPane.setDividerPositions(OBJECTS.SETTINGS.getAppDOUBLE(mySettingsName + "_SplitPaneDividerPosition"));
        }

    }

    private void saveSettings() {
        if (!OBJECTS.SETTINGS.isAppSettingExists(mySettingsName + "_ListFontSize")) {
            OBJECTS.SETTINGS.addAppSettings(mySettingsName + "_ListFontSize", listFontSize, true);
        } else {
            OBJECTS.SETTINGS.setApp(mySettingsName + "_ListFontSize", listFontSize);
        }

        if (!OBJECTS.SETTINGS.isAppSettingExists(mySettingsName + "_SplitPaneDividerPosition")) {
            OBJECTS.SETTINGS.addAppSettings(mySettingsName + "_SplitPaneDividerPosition", splitPane.getDividerPositions()[0], true);
        } else {
            OBJECTS.SETTINGS.setApp(mySettingsName + "_SplitPaneDividerPosition", splitPane.getDividerPositions()[0]);
        }
    }

    private void calcListItems() {
        dataAll = selectionData.getAllItems();
    }

    private void populateList() {
        List<SelectionData.Item> items = null;
        if (dataFiltered == null) {
            items = dataAll;
        }
        else {
            items = dataFiltered;
        }

        imgLoadingList.setVisible(false);

        lstItems.getItems().clear();

        
        lstItems.getItems().addAll(items);

        updateSelectedItemAppearance(true, false, false);
    }

    private void calcLastItems() {
        dataLast = selectionData.getLastItems();
    }

    private void populateLast(List<SelectionData.Item> items) {
        flowLastContent.getChildren().clear();

        for (SelectionData.Item item : items) {
            Button btnElement = getElementForLastAndMost(item);
            flowLastContent.getChildren().add(btnElement);
        }

        updateSelectedItemAppearance(false, true, false);
    }

    private void calcMostItems() {
        dataMost = selectionData.getMostItems();
    }

    private void populateMost(List<SelectionData.Item> items) {
        flowMostContent.getChildren().clear();

        for (SelectionData.Item item : items) {
            Button btnElement = getElementForLastAndMost(item);
            flowMostContent.getChildren().add(btnElement);
        }

        updateSelectedItemAppearance(false, false, true);
    }

    private Button getElementForLastAndMost(SelectionData.Item item) {
        Button btnElement = new Button(item.getShortName(OBJECTS.SETTINGS.getvINTEGER("SelectionLastMost_MaxChars")));
        btnElement.getStyleClass().add("button-item");
        btnElement.setId(String.valueOf(item.hashCode()));
        Image img = item.getImage();
        if (img != null) {
            ImageView imgV = new ImageView(img);
            imgV.setFitHeight(OBJECTS.SETTINGS.getvINTEGER("Selection_ImageHeight"));
            imgV.setFitWidth(OBJECTS.SETTINGS.getvINTEGER("Selection_ImageWidth"));
            imgV.preserveRatioProperty();
            btnElement.setGraphic(imgV);
        }

        UJavaFX.setTooltip(
            btnElement,
            item.getTooltip(),
            item.getName(),
            item.getImage(),
            null,
            null
        );

        btnElement.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY || event.getButton() == MouseButton.SECONDARY) {
                if (event.isControlDown() || event.getButton() == MouseButton.SECONDARY) {
                    if (currentlySelected.contains(item)) {
                        currentlySelected.remove(item);
                    }
                    else {
                        currentlySelected.add(item);
                    }
                    Platform.runLater(() -> updateSelectedItemAppearance(true, true, true));
                }
                else {
                    currentlySelected.clear();
                    currentlySelected.add(item);
                    Platform.runLater(() -> updateSelectedItemAppearance(true, true, true));
                }
            }
        });
        return btnElement;
    }

    private Label getElementForClipboard(SelectionData.Item item) {
        Label lblElement = new Label(item.getShortName(OBJECTS.SETTINGS.getvINTEGER("SelectionLastMost_MaxChars")));
        lblElement.getStyleClass().remove("label-clipboard");
        lblElement.getStyleClass().remove("label-clipboard-selected");

        if (item.getImage() != null) {
            ImageView imgV = new ImageView(item.getImage());
            imgV.setFitHeight(25);
            imgV.setFitWidth(25);
            imgV.preserveRatioProperty();
            lblElement.setGraphic(imgV);
        }

        if (currentlySelected.contains(item)) {
            lblElement.getStyleClass().add("label-clipboard-selected");
        }
        else {
            lblElement.getStyleClass().add("label-clipboard");
        }

        UJavaFX.setTooltip(lblElement, item.getTooltip(), item.getName(), item.getImage(), null, null);

        lblElement.onMouseClickedProperty().set(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.isControlDown()) {
                    if (currentlySelected.contains(item)) {
                        currentlySelected.remove(item);
                    }
                    else {
                        currentlySelected.add(item);
                    }
                    Platform.runLater(() -> updateSelectedItemAppearance(true, true, true));
                }
                else {
                    currentlySelected.clear();
                    currentlySelected.add(item);
                    Platform.runLater(() -> updateSelectedItemAppearance(true, true, true));
                }
            }
        });
        
        return lblElement;
    }

    private Label getElementForClipboardThatNotShown(int moreItems) {
        Label lblElement = new Label(OBJECTS.SETTINGS.getl("AndSomeMoreItems").replace("#1", String.valueOf(moreItems)));
        lblElement.getStyleClass().remove("label-clipboard");
        lblElement.setStyle("-fx-text-fill: white;");

        Image imgMore = new Image(getClass().getResourceAsStream("/images/more.png"));
        ImageView imgV = new ImageView(imgMore);
        imgV.setFitHeight(25);
        imgV.setFitWidth(25);
        imgV.preserveRatioProperty();
        lblElement.setGraphic(imgV);

        return lblElement;
    }

    private void updateSelectedItemAppearance(boolean updateList, boolean updateLast, boolean updateMost) {
        // Update counter
        updateCounter();
        // Update Clipboard
        updateClipboardAppearance();
        // List
        List<Integer> indices = new ArrayList<>();
        for (SelectionData.Item item : currentlySelected) {
            int index = lstItems.getItems().indexOf(item);
            if (index != -1) {
                indices.add(index);
            }
        }
        if (indices.size() > 0) {
            Platform.runLater(() -> {
                ignoreListChanges = true;
                lstItems.getSelectionModel().clearSelection();
                lstItems.getSelectionModel().selectIndices(-1, indices.stream().mapToInt(i -> i).toArray());
                ignoreListChanges = false;
            });
        }
        else {
            Platform.runLater(() -> {
                ignoreListChanges = true;
                lstItems.getSelectionModel().clearSelection();
                ignoreListChanges = false;
            });
        }
        
        // Last used
        if (updateLast) {
            for (Node item : flowLastContent.getChildren()) {
                if (item instanceof Button) {
                    Button btn = (Button) item;
                    if (currentlySelected.stream().anyMatch(i -> String.valueOf(i.hashCode()).equals(btn.getId()))) {
                        btn.getStyleClass().remove("button-item");
                        btn.getStyleClass().remove("button-item-selected");
                        btn.getStyleClass().add("button-item-selected");
                    }
                    else {
                        btn.getStyleClass().remove("button-item-selected");
                        btn.getStyleClass().remove("button-item");
                        btn.getStyleClass().add("button-item");
                    }
                    
                }
            }
        }
        // Most used
        if (updateMost) {
            for (Node item : flowMostContent.getChildren()) {
                if (item instanceof Button) {
                    Button btn = (Button) item;
                    if (currentlySelected.stream().anyMatch(i -> String.valueOf(i.hashCode()).equals(btn.getId()))) {
                        btn.getStyleClass().remove("button-item");
                        btn.getStyleClass().remove("button-item-selected");
                        btn.getStyleClass().add("button-item-selected");
                    }
                    else {
                        btn.getStyleClass().remove("button-item-selected");
                        btn.getStyleClass().remove("button-item");
                        btn.getStyleClass().add("button-item");
                    }
                }
            }
        }

        showSelectedItemsWidgets();
    }

    private void updateClipboardAppearance() {
        if (selectionData.getClipItems().size() == 0) {
            disableSections(Section.CLIPBOARD);
            return;
        }
        enableSections(Section.CLIPBOARD);
        lblClipCounter.setText(OBJECTS.SETTINGS.getl("ClipboardItemsCounter").replace("#1", String.valueOf(selectionData.getClipItems().size())));
        flowClipContent.getChildren().clear();
        int counter = 0;
        int totalClipItems = selectionData.getClipItems().size();
        for (SelectionData.Item item : selectionData.getClipItems()) {
            Label lblElement = getElementForClipboard(item);
            flowClipContent.getChildren().add(lblElement);

            counter++;
            if (counter >= OBJECTS.SETTINGS.getvINTEGER("MaxShownItemsInClipboard") && counter < totalClipItems) {
                Label lblElement2 = getElementForClipboardThatNotShown(totalClipItems - counter);
                flowClipContent.getChildren().add(lblElement2);
                break;
            }
        }
    }

    private void showSelectedItemsWidgets() {
        vBoxSelectedContent.getChildren().clear();

        for (SelectionData.Item item : currentlySelected) {
            vBoxSelectedContent.getChildren().add(selectedItemWidget(item));
        }
    }

    private HBox selectedItemWidget(SelectionData.Item item) {
        HBox hBox = new HBox();
        hBox.setPrefHeight(HBox.USE_COMPUTED_SIZE);
        hBox.setPrefWidth(HBox.USE_COMPUTED_SIZE);
        
        Label lblName = new Label(item.getName());
        lblName.getStyleClass().add("selected-item");
        UJavaFX.setTooltip(
            lblName,
            item.getTooltip(),
            item.getName(),
            item.getImage(),
            null,
            null
        );
        
        Region regSpacer = new Region();
        regSpacer.setPrefHeight(HBox.USE_COMPUTED_SIZE);
        regSpacer.setPrefWidth(5);
        
        Button btnRemove = new Button();
        btnRemove.getStyleClass().add("button-icon");
        UJavaFX.setTooltip(btnRemove, OBJECTS.SETTINGS.getl("text_Remove"));
        ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/images/close.png")));
        img.setFitWidth(20);
        img.setFitHeight(20);
        img.setPreserveRatio(true);
        btnRemove.setGraphic(img);
        btnRemove.setOnMouseClicked(event -> {
            currentlySelected.remove(item);
            Platform.runLater(() -> {
                updateSelectedItemAppearance(true, true, true);
                vBoxSelectedContent.getChildren().remove(hBox);
            });
        });
        
        hBox.getChildren().addAll(lblName, regSpacer, btnRemove);

        return hBox;
    }

    private void updateCounter() {
        int size = 0;
        if (dataFiltered == null) {
            size = dataAll.size();
        }
        else {
            size = dataFiltered.size();
        }

        lblCounter.setText(OBJECTS.SETTINGS.getl("Counter1of1ItemsSelected1")
                .replace("#1", String.valueOf(lstItems.getItems().size()))
                .replace("#2", String.valueOf(size))
                .replace("#3", String.valueOf(currentlySelected.size())));
    }

    // Public methods

    public void setData(SelectionData data) {
        this.selectionData = data;
    }
    
    public String getReceiverID() { return receiverID; }

    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public void setSelectedItems(List<Integer> itemIds, ModelEnum model) {
        selectionData.setSelectedItems(itemIds, model);
        currentlySelected = new ArrayList<>();
        selectionData.getSelectedItems().forEach(item -> currentlySelected.add(item));
    }

    // Setup widgets

    private void setupWidgetsText() {
        lblClipboard.setText(OBJECTS.SETTINGS.getl("text_ClipboardContent"));
        lblClipCounter.setText(OBJECTS.SETTINGS.getl("ClipboardItemsCounter").replace("#1", String.valueOf(selectionData.getClipItems().size())));
        btnClipSelectAll.setText(OBJECTS.SETTINGS.getl("text_SelectAll"));
        btnClipSelectNone.setText(OBJECTS.SETTINGS.getl("text_SelectNone"));

        lblLast.setText(OBJECTS.SETTINGS.getl("text_LastUsed"));
        UJavaFX.setTooltip(lblLast, OBJECTS.SETTINGS.getl("tt_Selection_lblLast"));
        lblMost.setText(OBJECTS.SETTINGS.getl("text_MostUsed"));
        UJavaFX.setTooltip(lblMost, OBJECTS.SETTINGS.getl("tt_Selection_lblMost"));
        lblLastShowMore.setText(OBJECTS.SETTINGS.getl("text_ShowMore"));
        UJavaFX.setTooltip(lblLastShowMore, OBJECTS.SETTINGS.getl("tt_Selection_lblLastShowMore"));
        lblMostShowMore.setText(OBJECTS.SETTINGS.getl("text_ShowMore"));
        UJavaFX.setTooltip(lblMostShowMore, OBJECTS.SETTINGS.getl("tt_Selection_lblMostShowMore"));
        lblSelected.setText(OBJECTS.SETTINGS.getl("text_SelectedItems"));
        UJavaFX.setTooltip(lblSelected, OBJECTS.SETTINGS.getl("tt_Selection_lblSelected"));
        lblCounter.setText(OBJECTS.SETTINGS.getl("Counter1of1ItemsSelected1"));
        btnClear.setText(OBJECTS.SETTINGS.getl("text_ClearSelection"));
        UJavaFX.setTooltip(btnClear, OBJECTS.SETTINGS.getl("tt_Selection_btnClear"));
        btnSelect.setText(OBJECTS.SETTINGS.getl("text_SelectItems"));
        UJavaFX.setTooltip(btnSelect, null, OBJECTS.SETTINGS.getl("tt_Selection_btnSelect"), new Image(getClass().getResourceAsStream("/images/ok.png")), 30, 30);
    }

    private void setupWidgetsAppearance() {
        txtFind.requestFocus();

        lstItems.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        lstItems.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SelectionData.Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getName());
                    Image image = item.getImage();
                    if (image != null) {
                        ImageView imgV = new ImageView(image);
                        imgV.setFitHeight(OBJECTS.SETTINGS.getvINTEGER("Selection_ImageHeight"));
                        imgV.setFitWidth(OBJECTS.SETTINGS.getvINTEGER("Selection_ImageWidth"));
                        imgV.preserveRatioProperty();
                        setGraphic(new ImageView(image));
                    }
                }
                else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });

        lstItems.getSelectionModel().getSelectedItems().addListener((ListChangeListener<SelectionData.Item>) c -> {
            listSelectionChanged();
        });

        lstItems.setOnScroll(event -> {
            if (event.isControlDown()) {
                double deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    if (OBJECTS.SETTINGS.getvINTEGER("MaxListFontSize") > listFontSize) {
                        listFontSize++;
                        lstItems.setStyle("-fx-font-size: " + listFontSize + "px;");
                    }
                } else {
                    if (OBJECTS.SETTINGS.getvINTEGER("MinListFontSize") < listFontSize) {
                        listFontSize--;
                        lstItems.setStyle("-fx-font-size: " + listFontSize + "px;");
                    }
                }
                event.consume();
            }
        });
        
        txtFind.textProperty().addListener((observable, oldValue, newValue) -> onFilterTextChanged(newValue));
    }

    private void toggleSection(Section section, boolean isDisabled) {
        switch (section) {
            case CLIPBOARD:
                vBoxClip.setDisable(isDisabled);
                vBoxClip.setManaged(!isDisabled);
                break;
            case LIST:
                ancList.setDisable(isDisabled);
                ancList.setManaged(!isDisabled);
                break;
            case RECOMMENDATIONS:
                ancRecommendations.setDisable(isDisabled);
                ancRecommendations.setManaged(!isDisabled);
                break;
            case LAST_USED:
                vBoxLast.setDisable(isDisabled);
                vBoxLast.setManaged(!isDisabled);
                break;
            case MOST_USED:
                vBoxMost.setDisable(isDisabled);
                vBoxMost.setManaged(!isDisabled);
                break;
            case SELECTED:
                vBoxSelected.setDisable(isDisabled);
                vBoxSelected.setManaged(!isDisabled);
                break;
            default:
                break;
        }
    }

    public void enableSections(Section... sections) {
        for (Section s : sections) {
            toggleSection(s, false);
        }
    }

    public void disableSections(Section... sections) {
        for (Section s : sections) {
            toggleSection(s, true);
        }
    }

    public void enableAllSections() {
        for (Section section : Section.values()) {
            toggleSection(section, false);
        }
    }

    public void disableAllSections() {
        for (Section section : Section.values()) {
            toggleSection(section, true);
        }
    }

    public void setVisibleSections(Section... sections) {
        disableAllSections();
        for (Section s : sections) {
            toggleSection(s, false);
        }
    }

    public void onLstItemsMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            boolean isCtrlPressed = event.isControlDown();
            
            SelectionData.Item clickedItem = lstItems.getSelectionModel().getSelectedItem();
    
            if (isCtrlPressed) {
                Platform.runLater(() -> {
                    currentlySelected.clear();
                    for (int idx = 0; idx < lstItems.getItems().size(); idx++) {
                        boolean isSelected = lstItems.getSelectionModel().isSelected(idx);
                        if (isSelected) {
                            currentlySelected.add(lstItems.getItems().get(idx));
                        }
                        else {
                            currentlySelected.remove(lstItems.getItems().get(idx));
                        }
                    }
                    updateSelectedItemAppearance(true, true, true);
                    return;
                });
            }

            if (clickedItem != null) {
               if (!isCtrlPressed)
                    currentlySelected.clear();
                    currentlySelected.add(clickedItem);
                }
    
            updateSelectedItemAppearance(true, true, true);

        }
    }

    private void listSelectionChanged() {
        if (ignoreListChanges) { return; }

        currentlySelected = new ArrayList<>(lstItems.getSelectionModel().getSelectedItems());
        updateSelectedItemAppearance(false, true, true);
        
    }

    private void onFilterTextChanged(String filterText) {
        imgLoadingList.setVisible(true);
        UJavaFX.taskStart(this::filterData, myName + "FILTER");
    }

    private void filterData() {
        Platform.runLater(() -> {
            txtFind.getStyleClass().remove("text-field-special");
            txtFind.getStyleClass().remove("text-field-default");
            txtFind.getStyleClass().add("text-field-default");

            lstItems.getStyleClass().remove("list-default");
            lstItems.getStyleClass().remove("list-special");
            lstItems.getStyleClass().add("list-default");
        });

        String filterText = txtFind.getText().trim();
        if (filterText.isEmpty()) {
            dataFiltered = null;
            Platform.runLater(() -> {
                populateList();
                updateSelectedItemAppearance(true, true, true);
            });
            return;
        }

        if (filterText.equals("@LAST_USED")) {
            dataFiltered = dataLast;
            Platform.runLater(() -> {
                txtFind.getStyleClass().remove("text-field-default");
                txtFind.getStyleClass().remove("text-field-special");
                txtFind.getStyleClass().add("text-field-special");

                lstItems.getStyleClass().remove("list-default");
                lstItems.getStyleClass().remove("list-special");
                lstItems.getStyleClass().add("list-special");

                populateList();
                updateSelectedItemAppearance(true, true, true);
            });
            return;
        }

        if (filterText.equals("@MOST_USED")) {
            dataFiltered = dataMost;
            Platform.runLater(() -> {
                txtFind.getStyleClass().remove("text-field-default");
                txtFind.getStyleClass().remove("text-field-special");
                txtFind.getStyleClass().add("text-field-special");

                lstItems.getStyleClass().remove("list-default");
                lstItems.getStyleClass().remove("list-special");
                lstItems.getStyleClass().add("list-special");

                populateList();
                updateSelectedItemAppearance(true, true, true);
            });
            return;
        }
        
        dataFiltered = new ArrayList<>();

        OBJECTS.TEXT_FILTER.setFilterText(filterText);
        for (SelectionData.Item item : dataAll) {
            if (OBJECTS.TEXT_FILTER.isValid(item.getName())) {
                dataFiltered.add(item);
            }
        }

        Platform.runLater(() -> {
            populateList();
            updateSelectedItemAppearance(true, true, true);
        });

        return;
    }

    // FXMl

    @FXML
    public void onBtnClearAction() {
        currentlySelected.clear();
        txtFind.setText("");
        updateSelectedItemAppearance(true, true, true);
    }

    @FXML
    public void onLblLastShowMoreClick() {
        txtFind.setText("@LAST_USED");
    }

    @FXML
    public void onLblMostShowMoreClick() {
        txtFind.setText("@MOST_USED");
    }

    @FXML
    public void onBtnSelectAction() {
        String result = "";
        if (currentlySelected != null && !currentlySelected.isEmpty()) {
            result += currentlySelected.stream().map(item -> String.valueOf(item.getId())).collect(Collectors.joining(","));
        }

        OBJECTS.EVENT_HANDLER.fireEvent(new MessageEvent(receiverID, myName, result));

        if (getParentController() != null) {
            getParentController().closeMe();
        }
    }

    @FXML
    public void onBtnClipSelectAllAction() {
        for (int idx = 0; idx < flowClipContent.getChildren().size(); idx++) {
            if (flowClipContent.getChildren().get(idx) instanceof Label) {
                Label lbl = (Label) flowClipContent.getChildren().get(idx);
                lbl.getStyleClass().remove("label-clipboard");
                lbl.getStyleClass().remove("label-clipboard-selected");

                lbl.getStyleClass().add("label-clipboard-selected");
            }
        }

        for (SelectionData.Item item : selectionData.getClipItems()) {
            if (!currentlySelected.contains(item)) {
                currentlySelected.add(item);
            }
        }

        updateSelectedItemAppearance(true, true, true);
    }

    @FXML
    public void onBtnClipSelectNoneAction() {
        for (int idx = 0; idx < flowClipContent.getChildren().size(); idx++) {
            if (flowClipContent.getChildren().get(idx) instanceof Label) {
                Label lbl = (Label) flowClipContent.getChildren().get(idx);
                lbl.getStyleClass().remove("label-clipboard");
                lbl.getStyleClass().remove("label-clipboard-selected");

                lbl.getStyleClass().add("label-clipboard");
            }
        }

        for (SelectionData.Item item : selectionData.getClipItems()) {
            currentlySelected.remove(item);
        }

        updateSelectedItemAppearance(true, true, true);
    }


}
