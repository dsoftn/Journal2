package com.dsoftn.services.text_handler;


import java.util.ArrayList;
import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
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
    private String lastRtWidgetText = "";
    private TextHandler textHandler = null;
    private String lastTextState = "";
    private List<StyledString> lastRTWidgetStyledStrings = new ArrayList<>();
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
            if (rtWidget.getAC().hasCurrentAC() || rtWidget.getBusy()) {
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
            lastRTWidgetStyledStrings = copyStyledParagraphs(rtWidget.getStyledParagraphs());
            pause.playFromStart();
        });
    }

    public void mark(String messageSTRING, boolean force) {
        try {
            if (lastRtWidgetText.equals(rtWidget.getText()) && !force) return;
            lastRtWidgetText = rtWidget.getText();

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
            
        } catch (Exception ex) {
            UError.error("Marker.mark: " + ex.getMessage(), "Error");
            if (currentTask != null) {
                currentTask.cancel();
                currentTask = null;
            }
            // mark();
        }
    }

    public void mark(String messageSTRING) {
        mark(messageSTRING, false);
    }

    public void mark() {
        mark(null, false);
    }

    public void markRepeat() {
        mark(messageSTRING, false);
    }

    public void unMarkFindReplace() {
        findReplace.calculate(null, null);
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
            rtWidget.setStateChanged(true);
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
            rtWidget.setStateChanged(true);
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

    public List<StyledString> getLastRTWidgetStyledStrings() {
        return lastRTWidgetStyledStrings;
    }

    public void quickMark() {
        boolean rtWidgetIsBusy = rtWidget.getBusy();
        rtWidget.setBusy(false);
        markText();
        rtWidget.setBusy(rtWidgetIsBusy);
    }

    //  Private methods
    private boolean taskMark() {
        // This is Task, marking should be done in specific order
        lastRTWidgetStyledStrings = copyStyledParagraphs(rtWidget.getStyledParagraphs());

        String textRTWidgetStyledStrings = "";
        for (StyledString styledStr : lastRTWidgetStyledStrings) {
            textRTWidgetStyledStrings += styledStr.getText();
        }
        if (!textRTWidgetStyledStrings.equals(rtWidget.getText())) { return false; }

        // Numbers, dates, times
        boolean isMarkedNDT = numberDateTimeMarking.calculate(currentTask);
        if (!isMarkedNDT) { return false; }
        isMarkedNDT = numberDateTimeMarking.mergeWithRTWidgetStyles(lastRTWidgetStyledStrings, currentTask);
        if (!isMarkedNDT) { return false; }

        // Web links, e-mail
        boolean isMarkedWeb = webMark.calculate(currentTask);
        if (!isMarkedWeb) { return false; }
        isMarkedWeb = webMark.mergeWithRTWidgetStyles(lastRTWidgetStyledStrings, currentTask);
        if (!isMarkedWeb) { return false; }

        // Find / Replace
        boolean isMarkedFR = findReplace.calculate(messageSTRING, currentTask);
        if (!isMarkedFR) { return false; }
        isMarkedFR = findReplace.mergeWithRTWidgetStyles(lastRTWidgetStyledStrings, currentTask);
        if (!isMarkedFR) { return false; }

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
            if (rtWidget.getText().equals(lastTextState) && !rtWidget.getAC().hasCurrentAC() && !rtWidget.getBusy()) {
                numberDateTimeMarking.mark();
            } else { return false; }
            
            if (rtWidget.getText().equals(lastTextState) && !rtWidget.getAC().hasCurrentAC() && !rtWidget.getBusy()) {
                webMark.mark();
            } else { return false; }
            
            if (rtWidget.getText().equals(lastTextState) && !rtWidget.getAC().hasCurrentAC() && !rtWidget.getBusy()) {
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

    private List<StyledString> copyStyledParagraphs(List<StyledParagraph> styledParagraphs) {
        List<StyledString> result = new ArrayList<>();

        int pos = 0;
        for (StyledParagraph styledParagraph : styledParagraphs) {
            for (StyledString styledString : styledParagraph.getStyledStringList()) {
                result.add(new StyledString(pos + styledString.getStart(), pos + styledString.getEnd(), styledString.getText(), styledString.getCssCharStyle()));
            }

            pos += styledParagraph.getPlainText().length();
            result.add(new StyledString(pos, pos + 1, "\n", styledParagraph.getStyledStringList().get(0).getCssCharStyle()));
            pos++;
        }

        result.remove(result.size() - 1);

        return result;
    }

    // Static methods

    public static List<StyledString> mergeStyles(List<StyledString> newStyles, List<StyledString> oldStyles) {
        if (newStyles == null || oldStyles == null) { return new ArrayList<>(); }
        List<StyledString> result = new ArrayList<>();

        int pos = 0;
        for (StyledString newStyle : newStyles) {
            for (int i = pos; i < oldStyles.size(); i++) {
                StyledString oldStyle = oldStyles.get(i);
                if (newStyle.getStart() >= oldStyle.getEnd()) {
                    pos++;
                    continue;
                }
                String newCSS = oldStyle.getCssCharStyleObject().mergeGetNew(newStyle.getCssCharStyle()).getCss();
                if (newStyle.getEnd() <= oldStyle.getEnd()) {
                    result.add(new StyledString(newStyle.getStart(), newStyle.getEnd(), newStyle.getText(), newCSS, newStyle.getStyleTypeEnum(), oldStyle.getCssCharStyle()));
                    break;
                }
                result.add(new StyledString(newStyle.getStart(), oldStyle.getEnd(), newStyle.getText().substring(0, oldStyle.getEnd() - newStyle.getStart()), newCSS, newStyle.getStyleTypeEnum(), oldStyle.getCssCharStyle()));
                newStyle.setStart(oldStyle.getEnd());
                newStyle.setText(newStyle.getText().substring(oldStyle.getEnd() - newStyle.getStart()));
                pos++;
            }
        }
        
        return result;
    }





}
