package com.dsoftn.services.text_handler;


import java.util.ArrayList;
import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.USettings;

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
    private NumberDateTimeMarking numberDateTimeMarking = null;
    private WebMark webMark = null;

    //  Constructor
    public Marker(RTWidget rtWidget, TextHandler textHandler) {
        this.rtWidget = rtWidget;
        this.textHandler = textHandler;
        findReplace = new FindReplace(rtWidget, textHandler);
        numberDateTimeMarking = new NumberDateTimeMarking(rtWidget);
        webMark = new WebMark(rtWidget);

        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);

        pause.setOnFinished(event -> {
            if (rtWidget.ac.hasCurrentAC() || rtWidget.busy) {
                pause.playFromStart();
                return;
            }
            mark();
        });

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
                mark(messageSTRING);
            }
            else if (taskEvent.getState() == TaskStateEnum.COMPLETED) {
                textHandler.msgForToolbar(TextToolbarActionEnum.HL_WORKING.name() + ":" + false);
                currentTask = null;
            }
            else if (taskEvent.getState() == TaskStateEnum.FAILED) {
                currentTask = null;
                UError.error("Marker.onCustomEvent: Task failed", "Task failed");
                mark(messageSTRING);
            }
        }

    }

    //  Public methods
    public void onTextChange() {
        if (pause.getStatus() == PauseTransition.Status.RUNNING) {
            pause.stop();
        }
            
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        
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
            // return;
        }

        textHandler.msgForToolbar("ACTION:WORKING");
        textHandler.msgForToolbar(TextToolbarActionEnum.HL_WORKING.name() + ":" + true);

        this.messageSTRING = messageSTRING;

        findReplace.unMark();

        currentTask = UJavaFX.taskStartWithResult(this::taskMark, this.myName);
    }

    public void mark() {
        mark(null);
    }

    public void markRepeat() {
        mark(messageSTRING);
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

    public boolean findReplaceChangeStyle(StyleSheetChar styleSheet) {
        return findReplace.changeStyle(styleSheet);
    }

    public void updateSettings() {
        TextHandler.Behavior behavior = textHandler.getBehavior();

        if (numberDateTimeMarking != null) {
            numberDateTimeMarking.updateSettings(behavior);
        }

        if (webMark != null) {
            webMark.updateSettings(behavior);
        }

        if (findReplace != null) {
            findReplace.updateSettings(behavior);
        }
    }


    //  Private methods
    private boolean taskMark() {
        // This is Task, marking should be done in specific order
        lastCssList = copyCssList(rtWidget.cssStyles);

        List<StyleSheetChar> cssChars = copyCssList(this.lastCssList);
        if (cssChars == null) { return false; }

        // Numbers, dates, times
        List<MarkedItem> markedNumbersDatesTimes = NumberDateTimeMarking.getNumbersDatesTimes(rtWidget.getText(),  currentTask, textHandler.getBehavior());
        if (markedNumbersDatesTimes == null) { return false; }
        List<StyleSheetChar> cssCharsNDT = numberDateTimeMarking.calculate(cssChars, markedNumbersDatesTimes, currentTask);
        if (cssCharsNDT == null) { return false; }
        cssCharsNDT = copyCssList(cssCharsNDT);
        
        // Web links, e-mail
        List<StyleSheetChar> cssCharsWeb = webMark.calculate(cssCharsNDT, currentTask);
        if (cssCharsWeb == null) { return false; }
        cssCharsWeb = copyCssList(cssCharsWeb);

        // Find / Replace
        if (findReplace.calculate(messageSTRING, cssCharsWeb, currentTask) == null) {
            return false;
        }

        Platform.runLater(() -> {
            markText();
        });

        return true;
    }

    private boolean markText() {
        // This is not Task, marking should be done in specific order

        if (!USettings.getAppOrUserSettingsItem("AllowMarking", textHandler.getBehavior()).getValueBOOLEAN()) {
            return true;
        }

        try {
            if (rtWidget.getText().equals(lastTextState) && lastCssList.equals(rtWidget.cssStyles) && !rtWidget.ac.hasCurrentAC() && !rtWidget.busy) {
                numberDateTimeMarking.mark();
            } else { return false; }
            
            if (rtWidget.getText().equals(lastTextState) && lastCssList.equals(rtWidget.cssStyles) && !rtWidget.ac.hasCurrentAC() && !rtWidget.busy) {
                webMark.mark();
            } else { return false; }
            
            if (rtWidget.getText().equals(lastTextState) && lastCssList.equals(rtWidget.cssStyles) && !rtWidget.ac.hasCurrentAC() && !rtWidget.busy) {
                findReplace.mark();
            } else { return false; }

            return true;

        } catch (Exception ex) {
            UError.error("Marker.markText: " + ex.getMessage(), "Error");
            if (currentTask != null) {
                currentTask.cancel();
                currentTask = null;
            }
            // mark();
            return false;
        }
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
