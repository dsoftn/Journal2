package com.dsoftn.services.text_handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.text.Style;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.elements.TextInputController;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.WordExtractor;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.UString;

import javafx.application.Platform;
import javafx.scene.input.KeyEvent;

public class ACHandler {
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

    // Constructor
    public ACHandler(RTWidget rtWidget) {
        this.rtWidget = rtWidget;
        this.behavior = rtWidget.behavior;
        ACStyle.setCss(OBJECTS.SETTINGS.getvSTRING("cssAutoCompleteStyle"));
        if (OBJECTS.SETTINGS.isUserSettingExists("ShowAutoComplete" + this.behavior.name())) {
            canShowAC = OBJECTS.SETTINGS.getvBOOLEAN("ShowAutoComplete" + this.behavior.name());
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
        return positionInWidget;
    }

    public void removeCurrentAC() {
        if (!hasCurrentAC()) return;
        rtWidget.deleteText(positionInWidget, positionInWidget + getCurrentAC().length());
        recommendedACList.clear();
        currentRecommendedIndex = null;
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

    public void showAC(int positionInWidget, String paragraphText) {
        removeCurrentAC();
        if (!canShowAC) return;
        findAC(positionInWidget, paragraphText);
        if (!hasCurrentAC()) return;

        ignoreTextChange = true;
        rtWidget.insertText(positionInWidget, getCurrentAC());
        ACStyle.setFontName(rtWidget.cssChar.getFontName());
        ACStyle.setFontSize(rtWidget.cssChar.getFontSize());
        rtWidget.setStyle(positionInWidget, positionInWidget + getCurrentAC().length(), ACStyle.getCss());
        rtWidget.moveTo(positionInWidget);

    }

    // Private methods
    private void findAC(int positionInWidget, String paragraphText) {
        List<String> data = getAllACData();
        List<String> filteredData = new ArrayList<>();

        filteredData = getDataByTime(data, paragraphText, positionInWidget);

        if (filteredData.size() == 0) {
            filteredData = getDataByContext(data, paragraphText, positionInWidget);
            System.out.println("Filtered by context: " + filteredData.size());
        }

        if (filteredData.size() > 0) {
            this.recommendedACList = filteredData;
            this.currentRecommendedIndex = 0;
            this.positionInWidget = positionInWidget;
        } else {
            this.recommendedACList.clear();
            this.currentRecommendedIndex = null;
            this.positionInWidget = null;
        }

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
        String parText = paragraphText.substring(1, positionInWidget);
        if (parText.isEmpty()) return filteredData;
        
        int counter = 0;
        for (String record : data) {
            String recCleaned = record.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];
            if (recCleaned.startsWith(parText)) {
                if (recCleaned.length() > parText.length()) {
                    String dataToAdd = recCleaned.substring(parText.length());

                    Integer index = UString.findIndexOfChar(dataToAdd, " ", OBJECTS.SETTINGS.getvINTEGER("MaxAutoCompleteRecommendedWords"));

                    if (index != null) {
                        dataToAdd = dataToAdd.substring(0, index);
                    }

                    if (dataToAdd.endsWith(paragraphText.substring(positionInWidget).strip())) {
                        dataToAdd = dataToAdd.substring(0, dataToAdd.length() - paragraphText.substring(positionInWidget).strip().length());
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
        String parText = paragraphText.substring(1, positionInWidget);
        if (parText.isEmpty()) return filteredData;
        String endParText = "";
        if (positionInWidget + 1 < paragraphText.length()) {
            endParText = paragraphText.substring(positionInWidget + 1).strip();
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



}
