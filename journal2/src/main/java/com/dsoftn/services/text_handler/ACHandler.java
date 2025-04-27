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
import com.dsoftn.controllers.elements.TextInputController;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UString;

import javafx.application.Platform;
import javafx.event.Event;


public class ACHandler implements ICustomEventListener {
    // Variables
    private final int BLANKS_TO_FIND = 2;
    RTWidget rtWidget = null;
    TextInputController.Behavior behavior = null;
    List<String> recommendedACList = new ArrayList<>();
    Integer currentRecommendedIndex = null;
    private Integer positionInWidget = null;
    boolean canShowAC = false;
    StyleSheetChar ACStyle = new StyleSheetChar();
    boolean ignoreTextChange = false;
    private boolean calculating = false;
    private int positionInWidgetTMP = -1;
    private String paragraphTextTMP = null;
    private boolean waitingAC = false;
    private String myName = UJavaFX.getUniqueId();
    private boolean ACisShown = false;

    // Constructor
    public ACHandler(RTWidget rtWidget) {
        this.rtWidget = rtWidget;
        this.behavior = rtWidget.behavior;
        ACStyle.setCss(OBJECTS.SETTINGS.getvSTRING("cssAutoCompleteStyle"));
        if (OBJECTS.SETTINGS.isUserSettingExists("ShowAutoComplete" + this.behavior.name())) {
            canShowAC = OBJECTS.SETTINGS.getvBOOLEAN("ShowAutoComplete" + this.behavior.name());
        }

        OBJECTS.EVENT_HANDLER.register(this, TaskStateEvent.TASK_STATE_EVENT);

        // rtWidget.textProperty().addListener((obs, oldText, newText) -> {
        //     if (ignoreTextChange) {
        //         ignoreTextChange = false;
        //         return;
        //     }

        //     Platform.runLater(() -> {
        //         // if (rtWidget.busy) return;
        //         showACWhenTextChanged(rtWidget.getCaretPosition(), rtWidget.getParagraphTextNoAC(rtWidget.getCurrentParagraph()));
        //     });
        // });
    }

    // Implementation of ICustomEventListener
    @Override
    public void onCustomEvent(Event event) {
        if (event instanceof TaskStateEvent) {
            TaskStateEvent taskStateEvent = (TaskStateEvent) event;
            if (taskStateEvent.getID().split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING))[0].equals(this.myName)) {
                if (taskStateEvent.getState() == TaskStateEnum.COMPLETED) {
                    // Platform.runLater(() -> {
                        String validText = taskStateEvent.getID().split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];
                        if (getCurrentAC() == null || rtWidget.getCaretPosition() != positionInWidgetTMP || !rtWidget.getParagraphTextNoAC(rtWidget.getCurrentParagraph()).equals(paragraphTextTMP)) { //  || !rtWidget.getTextPlain().equals(validText)
                            clearAC();
                            waitingAC = false;
                            calculating = false;
                            return;
                        }
                        
                        rtWidget.busy = true;
                        rtWidget.ignoreTextChangePERMANENT = true;
                        rtWidget.ignoreCaretPositionChange = true;
                        rtWidget.insertText(positionInWidgetTMP, getCurrentAC());
                        ACisShown = true;
                        ACStyle.setFontName(rtWidget.cssChar.getFontName());
                        ACStyle.setFontSize(rtWidget.cssChar.getFontSize());
                        rtWidget.setStyle(positionInWidgetTMP, positionInWidgetTMP + getCurrentAC().length(), ACStyle.getCss());
                        rtWidget.moveTo(positionInWidgetTMP);
                        Platform.runLater(() -> {
                            rtWidget.ignoreTextChangePERMANENT = false;
                            rtWidget.ignoreCaretPositionChange = false;
                            rtWidget.busy = false;
                        });
                    // });
                }
            }
        }
    }


    // Public methods
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
        return currentRecommendedIndex != null;
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
        if (!hasCurrentAC() || !ACisShown) return;

        rtWidget.deleteText(positionInWidgetTMP, positionInWidgetTMP + getCurrentAC().length());
        recommendedACList.clear();
        currentRecommendedIndex = null;
        ACisShown = false;
    }

    public void updateStyleSheet(StyleSheetChar styleSheetChar) {
        this.ACStyle.setFontName(styleSheetChar.getFontName());
        this.ACStyle.setFontSize(styleSheetChar.getFontSize());
    }

    public void nextAC() {
        if (!hasCurrentAC()) return;
        if (recommendedACList.size() == 1) return;

        rtWidget.deleteText(positionInWidget, positionInWidget + getCurrentAC().length());

        currentRecommendedIndex = recommendedACList.size() - 1 == currentRecommendedIndex ? 0 : currentRecommendedIndex + 1;
        ignoreTextChange = true;
        rtWidget.insertText(positionInWidget, getCurrentAC());
        ACStyle.setFontName(rtWidget.cssChar.getFontName());
        ACStyle.setFontSize(rtWidget.cssChar.getFontSize());
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
        ACStyle.setFontName(rtWidget.cssChar.getFontName());
        ACStyle.setFontSize(rtWidget.cssChar.getFontSize());
        rtWidget.setStyle(positionInWidget, positionInWidget + getCurrentAC().length(), ACStyle.getCss());
        rtWidget.moveTo(positionInWidget);
    }

    public void showACWhenTextChanged(int positionInWidget, String paragraphText) {
        // if (!canShowAC) {return;}

        // this.positionInWidgetTMP = positionInWidget;
        // this.paragraphTextTMP = paragraphText;
        
        // if (calculating) {
        //     waitingAC = true;
        //     calculating = false;
        //     return;
        // } else {
        //     waitingAC = false;
        //     calculating = true;
        // }

        // UJavaFX.taskStart(this::showACTask, myName);
    }

    public void showAC(int positionInWidget, String paragraphText) {
        if (!canShowAC || paragraphText == null) {return;}
        clearAC();

        int paragraphPosition = rtWidget.offsetToPosition(positionInWidget, TwoDimensional.Bias.Forward).getMinor();
        if (paragraphPosition > 0 && paragraphPosition < paragraphText.length() - 1) {
            if (paragraphText.charAt(paragraphPosition - 1) != ' ' && paragraphText.charAt(paragraphPosition) != ' ' && paragraphText.charAt(paragraphPosition + 1) != ' ') {return;}
        }

        this.positionInWidgetTMP = positionInWidget;
        this.paragraphTextTMP = paragraphText;
        
        if (calculating) {
            waitingAC = true;
            calculating = false;
            return;
        } else {
            waitingAC = false;
            calculating = true;
        }

        UJavaFX.taskStart(this::showACTask, myName + CONSTANTS.EMPTY_PARAGRAPH_STRING + rtWidget.getTextPlain());
    }
        
    public void showACTask() {
        int positionInWidgetNOW = positionInWidgetTMP;
        String paragraphText = paragraphTextTMP;

        removeCurrentAC();

        boolean success = findAC(positionInWidgetNOW, paragraphText);
        if (!hasCurrentAC()) return;
        if (!success) {
            if (waitingAC) {
                waitingAC = false;
                calculating = true;
                clearAC();
                showACTask();
            } else {
                calculating = false;
                clearAC();
                return;
            }
        }

        calculating = false;
        ignoreTextChange = true;

        
        // rtWidget.insertText(positionInWidgetNOW, getCurrentAC());
        // ACStyle.setFontName(rtWidget.cssChar.getFontName());
        // ACStyle.setFontSize(rtWidget.cssChar.getFontSize());
        // rtWidget.setStyle(positionInWidgetNOW, positionInWidgetNOW + getCurrentAC().length(), ACStyle.getCss());
        // rtWidget.moveTo(positionInWidgetNOW);


        // Platform.runLater(() -> {
        //     rtWidget.insertText(positionInWidgetNOW, getCurrentAC());
        //     ACStyle.setFontName(rtWidget.cssChar.getFontName());
        //     ACStyle.setFontSize(rtWidget.cssChar.getFontSize());
        //     rtWidget.setStyle(positionInWidgetNOW, positionInWidgetNOW + getCurrentAC().length(), ACStyle.getCss());
        //     rtWidget.moveTo(positionInWidgetNOW);
        // });

    }

    // Private methods
    private boolean findAC(int positionInWidget, String paragraphText) {
        List<String> data = getAllACData();
        if (data == null) {
            clearAC();
            return false;
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
            if (!this.calculating) return null;

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
            if (!this.calculating) return null;

            String recCleaned = record.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];
            if (recCleaned.startsWith(parText)) {
                if (recCleaned.length() > parText.length()) {
                    String dataToAdd = recCleaned.substring(parText.length());

                    Integer index = UString.findIndexOfChar(dataToAdd, " ", OBJECTS.SETTINGS.getvINTEGER("MaxAutoCompleteRecommendedWords"));

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
            if (counter == OBJECTS.SETTINGS.getvINTEGER("MaxAutoCompleteRecommendations")) break;
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
            if (!this.calculating) return null;

            String recCleaned = data.get(i).split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];

            boolean isFound = false;
            for (int startSTR = 0; startSTR < startList.size(); startSTR++) {
                if (isFound) { break; }

                String startString = startList.get(startSTR);
                int startIdx = recCleaned.indexOf(startString);
                if (startIdx < 0) { continue; }

                if (endList.isEmpty()) {
                    String dataToAdd = recCleaned.substring(startIdx + startString.length());
                    Integer index = UString.findIndexOfChar(dataToAdd, " ", OBJECTS.SETTINGS.getvINTEGER("MaxAutoCompleteRecommendedWords"));
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
                    Integer index = UString.findIndexOfChar(dataToAdd, " ", OBJECTS.SETTINGS.getvINTEGER("MaxAutoCompleteRecommendedWords"));
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
            if ((counter) == OBJECTS.SETTINGS.getvINTEGER("MaxAutoCompleteRecommendations")) {
                break;
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
