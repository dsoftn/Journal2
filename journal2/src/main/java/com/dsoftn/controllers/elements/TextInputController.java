package com.dsoftn.controllers.elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dsoftn.CONSTANTS;
import com.dsoftn.ELEMENTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IBaseController;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.Interfaces.IElementController;
import com.dsoftn.controllers.elements.TextEditToolbarController.AlignmentEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.services.text_handler.TextHandler.Behavior;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.html.UHDocument;
import com.dsoftn.utils.html.UHTag;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class TextInputController implements IElementController, ICustomEventListener {
    // Variables
    private Behavior behavior = null;
    private Stage stage = null;
    private String myName = UJavaFX.getUniqueId();
    private String mySettingsName = "TextInput";
    private VBox root = null;
    private VBox vLayout = null;
    private String receiverID = null;
    private IBaseController parentController = null;
    private TextEditToolbarController toolbarController = null;
    private TextHandler textHandler = null;

    // FXML variables
    @FXML
    private HBox hBoxRichText; // Here goes rich text widget
        private RTWidget rTxtRichText = new RTWidget(); // Rich text widget

    @FXML
    private HBox hBoxControls;
        @FXML
        private Button btnOk;


    // Interface ICustomEventListener methods
    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof TaskStateEvent) {
            TaskStateEvent taskEvent = (TaskStateEvent) event;
            if (!taskEvent.getID().equals(this.myName)) { return; }

            if (taskEvent.getState() == TaskStateEnum.COMPLETED) {
                if (taskEvent.getMessage().equals("SAVE: Ctrl+S")) {
                    // Save text
                    if (behavior == Behavior.BLOCK_NAME) {
                        saveAndClose(taskEvent);
                    }
                }
                else if (taskEvent.getMessage().equals("SAVE: Ctrl+Shift+S")) {
                    // Save text as draft
                    if (behavior == Behavior.BLOCK_NAME) {
                        saveAndClose(taskEvent);
                    }
                }
                else if (taskEvent.getMessage().equals("SAVE: Alt+ENTER")) {
                    // Save text as draft
                    if (behavior == Behavior.BLOCK_NAME) {
                        saveAndClose(taskEvent);
                    }
                }
                else if (taskEvent.getMessage().equals("SET TEXT")) {
                    if (taskEvent.getRTWText().getPlainText().isEmpty()) {
                        return;
                    }
                    rTxtRichText.setRTWTextObject(taskEvent.getRTWText());
                }
            }
        }
    }


    // Interface IElementController methods

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void setParentController(IBaseController parentController) {
        this.parentController = parentController;
    }

    @Override
    public IBaseController getParentController() {
        return parentController;
    }

    @Override
    public void setRoot(VBox root) {
        this.root = root;
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

    @Override
    public String getMyName() {
        return myName;
    }

    @Override
    public void beforeShowing() {
        rTxtRichText.requestFocus();
    }

    @Override
    public boolean canBeClosed() {
        if (toolbarController != null) {
            if (!toolbarController.canBeClosed()) {
                return false;
            }
        }

        if (rTxtRichText != null) {
            if (!rTxtRichText.canBeClosed()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void calculateData() {
        // Register for event
        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);

        // Add Rich text
        hBoxRichText.getChildren().add(rTxtRichText);
        HBox.setHgrow(rTxtRichText, Priority.ALWAYS);

        // Create toolbar
        toolbarController = ELEMENTS.getTextEditToolbarController(stage);
        toolbarController.setReceiverID(myName);

        // Add Toolbar
        this.root.getChildren().add(0, toolbarController.getRoot());

        // Create TextHandler
        this.textHandler = new TextHandler(rTxtRichText, toolbarController);
        this.textHandler.setReceiverID(myName);
        // this.textHandler.enableAutoComplete(false);
        // this.textHandler.enableMarkingIntegers(false);
        // this.textHandler.enableMarkingDoubles(false);
        // this.textHandler.enableMarkingDates(false);
        // this.textHandler.enableMarkingTimes(false);
        // this.textHandler.enableMarkingSerbianMobileNumbers(false);
        // this.textHandler.enableMarkingSerbianLandlineNumbers(false);
        // this.textHandler.enableMarkingInternationalPhoneNumbers(false);
        test();


        setupWidgetsText();
        setupWidgetsAppearance();
    }

    @Override
    public void closeMe() {
    }

    private void test() {
        // Load file (CONSTANTS.FOLDER_DATA_APP_SETTINGS + "/_000000.txt")
        String content = "";
        try (Stream<String> stream = Files.lines(Paths.get(CONSTANTS.FOLDER_DATA_APP_SETTINGS + "/_000000.txt"))) {
            content = stream.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            UError.exception("TextInputController.test: Failed to read file", e);
        }

        UHDocument doc = new UHDocument(content);

        List<UHTag> roots = doc.getTags();

        List<UHTag> bodies = roots.get(1).findTags("a");
        System.out.println("Attributes: " + bodies.get(0).getAttributesMap());
        System.out.println(bodies.get(0).getTagCode());

        UHTag parent = bodies.get(0).getParent();
        System.out.println("Attributes: " + parent.getAttributesMap());
        System.out.println(parent.getTagCode());
        
        UHTag parent1 = parent.getParent();
        System.out.println("Attributes: " + parent1.getAttributesMap());
        System.out.println(parent1.getTagCode());

        UHTag parent2 = parent1.getParent();
        System.out.println("Attributes: " + parent2.getAttributesMap());
        System.out.println(parent2.getTagCode());

        UHTag parent3 = parent2.getParent();
        System.out.println("Attributes: " + parent3.getAttributesMap());
        System.out.println(parent3.getTagCode());

        UHTag parent4 = parent3.getParent();
        System.out.println(parent4);


    }

    // Public methods

    public String getReceiverID() { return receiverID; }

    public void setReceiverID(String receiverID) { this.receiverID = receiverID; }

    public void setBehavior(Behavior behavior) {
        switch (behavior) {
            case BLOCK_NAME:
                this.behavior = behavior;
                rTxtRichText.setBehavior(Behavior.BLOCK_NAME);
                StyleSheetChar css = new StyleSheetChar();
                css.setCss(OBJECTS.SETTINGS.getvSTRING("CssBlockName"));
                rTxtRichText.setCssChar(css);
                rTxtRichText.setStyle(css.getCss());
                StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
                cssParagraph.setAlignmentEnum(AlignmentEnum.CENTER);
                // rTxtRichText.setParagraphCss(cssParagraph);
                rTxtRichText.setMinTextWidgetHeight(OBJECTS.SETTINGS.getvINTEGER("BlockNameMinTextWidgetHeight"));
                rTxtRichText.setMinHeight(OBJECTS.SETTINGS.getvINTEGER("BlockName_MinRichTextHeight"));
                rTxtRichText.setPrefHeight(OBJECTS.SETTINGS.getvINTEGER("BlockName_MinRichTextHeight"));
                // rTxtRichText.setMaxNumberOfParagraphs(2);
                break;
        }
    }

    // Private methods

    private void setupWidgetsText() {
        btnOk.setText(OBJECTS.SETTINGS.getl("text_Ok"));
    }

    private void setupWidgetsAppearance() {
    }

    // FXML methods
    @FXML
    private void onBtnOkAction() {
        if (behavior == Behavior.BLOCK_NAME) {
            saveAndClose(new TaskStateEvent(myName, rTxtRichText.getRTWTextObject(), "SAVE: OK_BUTTON"));
        }
    }

    private void saveAndClose(TaskStateEvent taskEvent) {
        OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(receiverID, taskEvent.getRTWText(), taskEvent.getMessage()));
        if (parentController != null) {
            parentController.closeMe();
        } else {
            UError.error("TextInputController.onBtnOkAction: Parent controller is null", "Unable to close dialog.");
        }
    }


}
