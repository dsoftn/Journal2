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
import com.dsoftn.events.MessageEvent;
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
    private VBox root = null;
    private VBox vLayout = null;
    private SelectionData selectionData = null;
    private boolean ignoreListChanges = false;
    private String expectingResultDialogID = null;
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
    public void calculateData() {
        if (selectionData == null) {
            UError.error("SelectionController.calculateData: SelectionData is null");
            return;
        }

        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);

        setupWidgetsText();
        setupWidgetsAppearance();

        // Calc list items
        UJavaFX.taskStart(this::calcListItems, myName + "LIST");
        // Calc last items
        UJavaFX.taskStart(this::calcLastItems, myName + "LAST");
        // Calc most items
        UJavaFX.taskStart(this::calcMostItems, myName + "MOST");

        return;
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

        txtFind.setDisable(false);
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

        btnElement.setOnMouseClicked(event -> {
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
        return btnElement;
    }

    private void updateSelectedItemAppearance(boolean updateList, boolean updateLast, boolean updateMost) {
        // Update counter
        updateCounter();
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
        
        Region regSpacer = new Region();
        regSpacer.setPrefHeight(HBox.USE_COMPUTED_SIZE);
        regSpacer.setPrefWidth(5);
        
        Button btnRemove = new Button();
        btnRemove.getStyleClass().add("button-icon");
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
    
    public String getExpectingResultDialogID() { return expectingResultDialogID; }

    public void setExpectingResultDialogID(String expectingResultDialogID) { this.expectingResultDialogID = expectingResultDialogID; }

    public void setSelectedItems(List<Integer> itemIds, ModelEnum model) {
        selectionData.setSelectedItems(itemIds, model);
        currentlySelected = new ArrayList<>();
        selectionData.getSelectedItems().forEach(item -> currentlySelected.add(item));
    }

    // Setup widgets

    private void setupWidgetsText() {
        lblClipSelect.setText(OBJECTS.SETTINGS.getl("text_SelectFrom3dots"));
        btnClip.setText(OBJECTS.SETTINGS.getl("text_Clipboard"));
        btnFile.setText(OBJECTS.SETTINGS.getl("text_File"));
        btnWeb.setText(OBJECTS.SETTINGS.getl("text_Web"));

        lblLast.setText(OBJECTS.SETTINGS.getl("text_LastUsed"));
        lblMost.setText(OBJECTS.SETTINGS.getl("text_MostUsed"));
        lblLastShowMore.setText(OBJECTS.SETTINGS.getl("text_ShowMore"));
        lblSelected.setText(OBJECTS.SETTINGS.getl("text_SelectedItems"));
        lblCounter.setText(OBJECTS.SETTINGS.getl("Counter1of1ItemsSelected1"));
        btnClear.setText(OBJECTS.SETTINGS.getl("text_ClearSelection"));
        btnSelect.setText(OBJECTS.SETTINGS.getl("text_SelectItems"));
    }

    private void setupWidgetsAppearance() {
        txtFind.setDisable(true);
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

        OBJECTS.EVENT_HANDLER.fireEvent(new MessageEvent(expectingResultDialogID, myName, result));

        if (getParentController() != null) {
            getParentController().closeMe();
        }
    }
}
