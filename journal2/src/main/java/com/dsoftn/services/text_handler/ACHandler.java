package com.dsoftn.services.text_handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fxmisc.richtext.model.TwoDimensional;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ICustomEventListener;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.USettings;
import com.dsoftn.utils.UString;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;


public class ACHandler implements ICustomEventListener {
    // Variables
    private final int BLANKS_TO_FIND = 2;
    RTWidget rtWidget = null;
    List<String> recommendedACList = new ArrayList<>();
    Integer currentRecommendedIndex = null;
    private Integer positionInWidget = null;
    boolean ignoreTextChange = false;
    private int positionInWidgetTMP = -1;
    private String paragraphTextTMP = null;
    private String myName = UJavaFX.getUniqueId();
    private boolean ACisShown = false;
    private Task<Boolean> task = null;
    private TextHandler textHandler = null;

    private boolean canShowAC = false;
    private StyleSheetChar ACStyle = new StyleSheetChar();
    private int maxAutoCompleteRecommendations = 0;
    private int maxAutoCompleteRecommendedWords = 0;
    private int autoCompleteDelay = 0;

    // Constructor
    public ACHandler(RTWidget rtWidget, TextHandler textHandler) {
        this.textHandler = textHandler;
        TextHandler.Behavior behavior = textHandler.getBehavior();

        this.rtWidget = rtWidget;
        ACStyle.setCss(USettings.getAppOrUserSettingsItem("CssAutoCompleteStyle", behavior).getValueSTRING());
        canShowAC = USettings.getAppOrUserSettingsItem("AllowAutoComplete", behavior).getValueBOOLEAN();
        maxAutoCompleteRecommendations = USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendations", behavior).getValueINT();
        maxAutoCompleteRecommendedWords = USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendedWords", behavior).getValueINT();
        autoCompleteDelay = USettings.getAppOrUserSettingsItem("AutoCompleteDelay", behavior).getValueINT();

        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);
    }

    // Implementation of ICustomEventListener
    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof TaskStateEvent) {
            TaskStateEvent taskStateEvent = (TaskStateEvent) event;
            if (taskStateEvent.getID().split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING))[0].equals(this.myName)) {
                if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                    if (hasCurrentAC()) {
                        UError.error("ACHandler.onCustomEvent: AC is already shown", "AC is already shown");
                    }
                    if (hasCurrentAC() || rtWidget.getCaretPosition() != positionInWidgetTMP || !rtWidget.getParagraphText(rtWidget.getCurrentParagraph()).equals(paragraphTextTMP)) {
                        clearAC();
                        if (task != null) {
                            task.cancel();
                            task = null;
                        }
                        if (textHandler != null) {
                            textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + false);
                        }
                        return;
                    }

                    if (recommendedACList == null || recommendedACList.size() == 0) {
                        clearAC();
                        if (task != null) {
                            task.cancel();
                            task = null;
                        }
                        if (textHandler != null) {
                            textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + false);
                        }
                        return;
                    }
                    
                    rtWidget.setBusy(true);
                    rtWidget.insertText(positionInWidgetTMP, getCurrentAC());
                    ACisShown = true;
                    ACStyle = rtWidget.getCssChar().duplicate();
                    ACStyle.setCss(USettings.getAppOrUserSettingsItem("CssAutoCompleteStyle", textHandler.getBehavior()).getValueSTRING());
                    rtWidget.setStyle(positionInWidgetTMP, positionInWidgetTMP + getCurrentAC().length(), ACStyle.getCss());
                    rtWidget.moveTo(positionInWidgetTMP);
                    Platform.runLater(() -> {
                        rtWidget.setBusy(false);

                        if (textHandler != null) {
                            textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + false);
                        }
                    });
                }
                else if (taskStateEvent.getState() == TaskStateEnum.FAILED) {
                    ACisShown = false;
                    if (textHandler != null) {
                        textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + false);
                    }
                }
                else if (taskStateEvent.getState() == TaskStateEnum.CANCELED) {
                    ACisShown = false;
                    if (textHandler != null) {
                        textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + false);
                    }
                }

            }
        }
    }


    // Public methods
    public int getAutoCompleteDelay() {
        return autoCompleteDelay;
    }

    public void updateSettings() {
        this.canShowAC = USettings.getAppOrUserSettingsItem("AllowAutoComplete", textHandler.getBehavior()).getValueBOOLEAN();
        this.maxAutoCompleteRecommendations = USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendations", textHandler.getBehavior()).getValueINT();
        this.maxAutoCompleteRecommendedWords = USettings.getAppOrUserSettingsItem("MaxAutoCompleteRecommendedWords", textHandler.getBehavior()).getValueINT();
        this.autoCompleteDelay = USettings.getAppOrUserSettingsItem("AutoCompleteDelay", textHandler.getBehavior()).getValueINT();
        this.ACStyle.setCss(USettings.getAppOrUserSettingsItem("CssAutoCompleteStyle", textHandler.getBehavior()).getValueSTRING());
    }

    public void enableAutoComplete(boolean enable) {
        this.canShowAC = enable;
    }

    public String getCurrentAC() {
        if (currentRecommendedIndex == null) return null;
        String counter  = CONSTANTS.EMPTY_PARAGRAPH_STRING + " [" + (currentRecommendedIndex + 1) + "/" + recommendedACList.size() + "]";
        return recommendedACList.get(currentRecommendedIndex) + counter;
    }

    public String getCurrentACClean() {
        if (currentRecommendedIndex == null) return null;
        return recommendedACList.get(currentRecommendedIndex);
    }

    public boolean hasCurrentAC() {
        return ACisShown;
        // return currentRecommendedIndex != null;
    }

    /**
     * Returns the current position of the caret
     * <p>If returns null, the autocomplete is not shown</p>
     * @return Start position of autocomplete or null
     */
    public Integer getCurrentWidgetPosition() {
        return positionInWidgetTMP;
    }

    public void removeCurrentAC() {
        if (!hasCurrentAC()) return;

        ACisShown = false;
        rtWidget.deleteText(positionInWidgetTMP, positionInWidgetTMP + getCurrentAC().length());
        recommendedACList.clear();
        currentRecommendedIndex = null;
    }

    public void updateStyleSheet(StyleSheetChar styleSheetChar) {
        this.ACStyle = styleSheetChar.duplicate();
        this.ACStyle.setCss(USettings.getAppOrUserSettingsItem("CssAutoCompleteStyle", textHandler.getBehavior()).getValueSTRING());
    }

    public void nextAC() {
        if (!hasCurrentAC()) return;
        if (recommendedACList.size() == 1) return;

        rtWidget.deleteText(positionInWidget, positionInWidget + getCurrentAC().length());

        currentRecommendedIndex = recommendedACList.size() - 1 == currentRecommendedIndex ? 0 : currentRecommendedIndex + 1;
        ignoreTextChange = true;
        rtWidget.insertText(positionInWidget, getCurrentAC());
        rtWidget.setStyle(positionInWidget, positionInWidget + getCurrentAC().length(), ACStyle.getCss());
        rtWidget.moveTo(positionInWidget);
    }

    public void prevAC() {
        if (!hasCurrentAC()) return;
        if (recommendedACList.size() == 1) return;

        rtWidget.deleteText(positionInWidget, positionInWidget + getCurrentAC().length());
        
        currentRecommendedIndex = currentRecommendedIndex == 0 ? recommendedACList.size() - 1 : currentRecommendedIndex - 1;
        ignoreTextChange = true;
        rtWidget.insertText(positionInWidget, getCurrentAC());
        rtWidget.setStyle(positionInWidget, positionInWidget + getCurrentAC().length(), ACStyle.getCss());
        rtWidget.moveTo(positionInWidget);
    }

    public void showAC(int positionInWidget, String paragraphText) {
        if (!canShowAC || paragraphText == null) {return;}

        if (textHandler != null) {
            textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + true);
        }

        if (task != null) {
            task.cancel();
            task = null;
        }

        if (hasCurrentAC()) {
            removeCurrentAC();
        }

        clearAC();

        int paragraphPosition = rtWidget.offsetToPosition(positionInWidget, TwoDimensional.Bias.Forward).getMinor();
        if (paragraphPosition > 0 && paragraphPosition < paragraphText.length() - 1) {
            if (paragraphText.charAt(paragraphPosition - 1) != ' ' && paragraphText.charAt(paragraphPosition) != ' ' && paragraphText.charAt(paragraphPosition + 1) != ' ') {
                if (textHandler != null) {
                    textHandler.msgForToolbar(TextToolbarActionEnum.AC_WORKING.name() + ":" + false);
                }
                return;
            }
        }

        this.positionInWidgetTMP = positionInWidget;
        this.paragraphTextTMP = paragraphText;
        
        task = UJavaFX.taskStartWithResult(this::showACTask, myName + CONSTANTS.EMPTY_PARAGRAPH_STRING + rtWidget.getTextPlain());
    }
        
    public boolean showACTask() {
        int positionInWidgetNOW = positionInWidgetTMP;
        String paragraphText = paragraphTextTMP;

        return findAC(positionInWidgetNOW, paragraphText);
    }

    // Private methods
    private boolean findAC(int positionInWidget, String paragraphText) {
        List<String> data = getAllACData();
        if (data == null) {
            clearAC();
            return true;
        }

        List<String> filteredData = new ArrayList<>();

        filteredData = getDataByTime(data, paragraphText, positionInWidget);
        if (filteredData == null) {
            clearAC();
            return false;
        }

        if (filteredData.size() == 0) {
            filteredData = getDataByContext(data, paragraphText, positionInWidget);
            if (filteredData == null) {
                clearAC();
                return false;
            }
        }

        if (filteredData.size() > 0) {
            this.recommendedACList = filteredData;
            this.currentRecommendedIndex = 0;
            this.positionInWidget = positionInWidget;
        } else {
            clearAC();
        }

        if (task.isCancelled()) return false;

        return true;
    }

    private List<String> getAllACData() {
        List<String> data = new ArrayList<>();
        // data.add("0" + "|" + "20250420" + CONSTANTS.EMPTY_PARAGRAPH_STRING + "My new autoComplete 123 continue typing.");
        // data.add("0" + "|" + "20250420" + CONSTANTS.EMPTY_PARAGRAPH_STRING + "My only AutoComplete 456 continue typing.");
        // data.add("0" + "|" + "20250420" + CONSTANTS.EMPTY_PARAGRAPH_STRING + "My unique AutoComplete 789 continue typing.");

        // Read file _123456.txt and add data to list
        try (Stream<String> stream = Files.lines(Paths.get(CONSTANTS.FOLDER_DATA_APP_SETTINGS + "/_123456.txt"))) {
            data = stream.collect(Collectors.toList());
        } catch (IOException e) {
            UError.exception("ACHandler.getAllACData: Failed to read file", e);
        }

        for (int i = 0; i < data.size(); i++) {
            data.set(i, data.get(i).replace("\n", ""));
            data.set(i, "0|20250421" + CONSTANTS.EMPTY_PARAGRAPH_STRING + data.get(i));
        }


        return data;
    }

    private List<String> getDataByTime(List<String> data, String paragraphText, int positionInWidget) {
        List<String> filteredData = new ArrayList<>();
        int paragraphPosition = rtWidget.offsetToPosition(positionInWidget, TwoDimensional.Bias.Forward).getMinor();
        String parText = paragraphText.substring(1, paragraphPosition);
        if (parText.isEmpty()) return filteredData;
        
        int counter = 0;
        for (String record : data) {
            if (task.isCancelled()) return null;

            String recCleaned = record.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];
            if (recCleaned.startsWith(parText)) {
                if (recCleaned.length() > parText.length()) {
                    String dataToAdd = recCleaned.substring(parText.length());

                    Integer index = UString.findIndexOfChar(dataToAdd, " ", maxAutoCompleteRecommendedWords);

                    if (index != null) {
                        dataToAdd = dataToAdd.substring(0, index);
                    }

                    if (dataToAdd.endsWith(paragraphText.substring(paragraphPosition).strip())) {
                        dataToAdd = dataToAdd.substring(0, dataToAdd.length() - paragraphText.substring(paragraphPosition).strip().length());
                        if (dataToAdd.endsWith(" ")) dataToAdd = dataToAdd.substring(0, dataToAdd.length() - 1);
                        if (dataToAdd.strip().length() == 0) continue;
                    }
            
                    if (dataToAdd.isEmpty()) continue;

                    filteredData.add(dataToAdd);
                    counter++;
                }
            }
            if (counter == maxAutoCompleteRecommendations) break;
        }

        String commonPrefix = UString.findCommonPrefix(filteredData);
        if (!commonPrefix.isEmpty()) {
            filteredData.add(0, commonPrefix);
            if (filteredData.size() > maxAutoCompleteRecommendations) {
                filteredData.remove(filteredData.size() - 1);
            }
        }
        
        return filteredData;
    }

    private List<String> getDataByContext(List<String> data, String paragraphText, int positionInWidget) {
        List<String> filteredData = new ArrayList<>();
        List<String> filteredDataTMP = new ArrayList<>();
        int paragraphPosition = rtWidget.offsetToPosition(positionInWidget, TwoDimensional.Bias.Forward).getMinor();
        String parText = paragraphText.substring(1, paragraphPosition);
        if (parText.isEmpty()) return filteredData;
        String endParText = "";
        if (paragraphPosition + 1 < paragraphText.length()) {
            endParText = paragraphText.substring(paragraphPosition + 1).strip();
        }

        //  Find start List
        int sPOs = parText.length() - 1;
        int foundBlank = 0;
        List<String> startList = new ArrayList<>();
        while (sPOs >= 0) {
            if (parText.charAt(sPOs) == ' ') {
                foundBlank++;
            }

            if (foundBlank >= BLANKS_TO_FIND && (parText.charAt(sPOs) == ' ' || sPOs == 0)) {
                startList.add(0, parText.substring(sPOs));
            }
            sPOs--;
        }

        //  Find end List
        List<String> endList = new ArrayList<>();
        sPOs = 0;
        while (sPOs < endParText.length()) {
            if (endParText.charAt(sPOs) == ' ') {
                endList.add(0, endParText.substring(0, sPOs));
            }
            sPOs++;
        }

        for (int i = 0; i < data.size(); i++) {
            if (task.isCancelled()) return null;

            String recCleaned = data.get(i).split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];

            boolean isFound = false;
            for (int startSTR = 0; startSTR < startList.size(); startSTR++) {
                if (isFound) { break; }

                String startString = startList.get(startSTR);
                int startIdx = recCleaned.indexOf(startString);
                if (startIdx < 0) { continue; }

                if (endList.isEmpty()) {
                    String dataToAdd = recCleaned.substring(startIdx + startString.length());
                    Integer index = UString.findIndexOfChar(dataToAdd, " ", maxAutoCompleteRecommendedWords);
                    if (index != null) {
                        dataToAdd = dataToAdd.substring(0, index);
                    }

                    String date = data.get(i).split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[0].split(Pattern.quote("|"))[1];
                    String sortData = String.format("%03d", (startSTR)) + "999" + "|" + date + CONSTANTS.EMPTY_PARAGRAPH_STRING + dataToAdd;

                    filteredDataTMP.add(sortData);
                    isFound = true;
                    break;
                }

                for (int endSTR = 0; endSTR < endList.size(); endSTR++) {
                    String endString = endList.get(endSTR);
                    int endIdx = recCleaned.indexOf(endString, startIdx + startString.length() + 2);
                    if (endIdx < 0) { continue; }

                    String dataToAdd = recCleaned.substring(startIdx + startString.length(), endIdx);
                    Integer index = UString.findIndexOfChar(dataToAdd, " ", maxAutoCompleteRecommendedWords);
                    if (index != null) {
                        dataToAdd = dataToAdd.substring(0, index);
                    }

                    String date = data.get(i).split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[0].split(Pattern.quote("|"))[1];
                    String sortData = String.format("%03d", (startSTR)) + String.format("%03d", (endSTR)) + "|" + date + CONSTANTS.EMPTY_PARAGRAPH_STRING + dataToAdd;

                    filteredDataTMP.add(sortData);
                    isFound = true;
                    break;
                }
            }
        }

        Collections.sort(filteredDataTMP);

        int counter = 0;
        for (int i = 0; i < filteredDataTMP.size(); i++) {
            String recCleaned = filteredDataTMP.get(i).split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];
            if (recCleaned.isEmpty()) continue;

            filteredData.add(recCleaned);
            counter++;
            if ((counter) == maxAutoCompleteRecommendations) {
                break;
            }
        }

        String commonPrefix = UString.findCommonPrefix(filteredData);
        if (!commonPrefix.isEmpty()) {
            filteredData.add(0, commonPrefix);
            if (filteredData.size() > maxAutoCompleteRecommendations) {
                filteredData.remove(filteredData.size() - 1);
            }
        }
        
        return filteredData;
    }

    private void clearAC() {
        this.recommendedACList.clear();
        this.currentRecommendedIndex = null;
        this.positionInWidget = null;
    }

}
