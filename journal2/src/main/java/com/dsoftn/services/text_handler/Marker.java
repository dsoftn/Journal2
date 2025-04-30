package com.dsoftn.services.text_handler;


import java.util.ArrayList;
import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.util.Duration;

public class Marker implements ICustomEventListener {
    //  Variables
    private String myName = UJavaFX.getUniqueId();
    private RTWidget rtWidget = null;
    private TextHandler textHandler = null;
    private String lastTextState = "";
    private List<StyleSheetChar> lastCssList = new ArrayList<>();
    private PauseTransition pause = new PauseTransition(Duration.millis(300));
    private Task<Boolean> currentTask = null;
    private boolean ignoreTextChangePERMANENT = false;

    private FindReplace findReplace = null;
    private String messageSTRING = null;

    //  Constructor
    public Marker(RTWidget rtWidget, TextHandler textHandler) {
        this.rtWidget = rtWidget;
        this.textHandler = textHandler;
        findReplace = new FindReplace(rtWidget, textHandler);

        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);

        rtWidget.textProperty().addListener((obs, oldText, newText) -> {
            if (!ignoreTextChangePERMANENT) {
                onTextChange();
            }
        });
    }

    // Implement ICustomEventListener interface
    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof TaskStateEvent) {
            TaskStateEvent taskEvent = (TaskStateEvent) event;
            if (!taskEvent.getID().equals(this.myName)) { return; }

            if (taskEvent.getState() == TaskStateEnum.CANCELED) {
                currentTask = null;
                mark();
            }
            else if (taskEvent.getState() == TaskStateEnum.COMPLETED) {
                currentTask = null;
                Platform.runLater(() -> {
                    if (rtWidget.getText().equals(lastTextState) && lastCssList.equals(rtWidget.cssStyles) && !rtWidget.ac.hasCurrentAC() && !rtWidget.busy) {
                        rtWidget.busy = true;
                        rtWidget.ignoreTextChangePERMANENT = true;
                        rtWidget.ignoreCaretPositionChange = true;
                        markText();
                        rtWidget.ignoreTextChangePERMANENT = false;
                        rtWidget.ignoreCaretPositionChange = false;
                        rtWidget.busy = false;
                    } else {
                        lastTextState = rtWidget.getText();
                        lastCssList = copyCssList(rtWidget.cssStyles);
                        mark();
                    }
                });
            }
            else if (taskEvent.getState() == TaskStateEnum.FAILED) {
                currentTask = null;
                UError.error("Marker.onCustomEvent: Task failed", "Task failed");
            }
        }

    }



    //  Public methods
    public void onTextChange() {
        pause.stop();
            
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        
        pause.setOnFinished(event -> {
            if (rtWidget.ac.hasCurrentAC() || rtWidget.busy) {
                pause.playFromStart();
                return;
            }
            mark();
        });
        Platform.runLater(() -> {
            lastTextState = rtWidget.getText();
            lastCssList = copyCssList(rtWidget.cssStyles);
            pause.playFromStart();
        });
    }

    public void mark(String messageSTRING) {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            return;
        }

        textHandler.msgForToolbar("ACTION:WORKING");

        this.messageSTRING = messageSTRING;
        lastCssList = copyCssList(rtWidget.cssStyles);

        findReplace.unMark();

        currentTask = UJavaFX.taskStartWithResult(this::taskMark, this.myName);
    }

    public void mark() {
        mark(null);
    }

    public void unMarkFindReplace() {
        findReplace.calculate(null, null, null);
    }

    public void findReplaceUP() {
        findReplace.selectUP();
    }

    public void findReplaceDOWN() {
        findReplace.selectDOWN();
    }

    public void findReplaceONE(String action) {
        ignoreTextChangePERMANENT = true;
        String[] lines = action.split("\\R", -1);
        
        findReplace.replaceOne(lines[2]);
        Platform.runLater(() -> {
            rtWidget.stateChanged = true;
            textHandler.msgFromWidget("TAKE_SNAPSHOT");
            lastTextState = rtWidget.getText();
            ignoreTextChangePERMANENT = false;
        });
    }

    public void findReplaceALL(String action) {
        ignoreTextChangePERMANENT = true;
        String[] lines = action.split("\\R", -1);

        findReplace.replaceAll(lines[2]);
        Platform.runLater(() -> {
            rtWidget.stateChanged = true;
            textHandler.msgFromWidget("TAKE_SNAPSHOT");
            lastTextState = rtWidget.getText();
            ignoreTextChangePERMANENT = false;
        });
    }

    //  Private methods
    private boolean taskMark() {
        // This is Task, marking should be done in specific order


        List<StyleSheetChar> cssChars = copyCssList(this.lastCssList);

        
        // Find / Replace
        if (cssChars == null) { return false; }
        if (findReplace.calculate(messageSTRING, cssChars, currentTask) == null) {
            return false;
        }

        return true;
    }

    private void markText() {
        // This is not Task, marking should be done in specific order

        findReplace.mark();
    }

    private List<StyleSheetChar> copyCssList(List<StyleSheetChar> cssList) {
        List<StyleSheetChar> result = new ArrayList<>();

        for (StyleSheetChar css : cssList) {
            result.add(css.duplicate());
        }

        return result;
    }

    // Static methods
    public static List<StyleSheetChar> updateCssList(List<StyleSheetChar> cssList, List<MarkedItem> markedItems) {
        for (MarkedItem item : markedItems) {
            for (int i = item.start; i < item.end; i++) {
                cssList.get(i).merge(item.cssStyle);
            }
        }

        return cssList;
    }


}
