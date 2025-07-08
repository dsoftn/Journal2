package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.services.RTWidget;
import com.dsoftn.utils.USettings;

import javafx.application.Platform;
import javafx.concurrent.Task;


public class FindReplace {
    // Variables
    private StyleSheetChar foundStyle = new StyleSheetChar();
    private StyleSheetChar selectedStyle = new StyleSheetChar();
    private Integer selectedItemIndex = null;
    private RTWidget rtwWidget = null;
    private TextHandler textHandler = null;
    private List<StyledString> foundItems = null;
    private boolean isMatchCase = false;
    private boolean isWholeWords = false;
    private String findText = null;
    private String messageSTRING = null;

    // Constructor
    public FindReplace(RTWidget rtwWidget, TextHandler textHandler) {
        this.rtwWidget = rtwWidget;
        this.textHandler = textHandler;
        foundStyle.setCss(OBJECTS.SETTINGS.getvSTRING("CssFindMarked"));
        selectedStyle.setCss(OBJECTS.SETTINGS.getvSTRING("CssFindSelected"));
    }

    // Public methods
    public void updateSettings(TextHandler.Behavior behavior) {
        foundStyle.setCss(USettings.getAppOrUserSettingsItem("CssFindMarked", behavior).getValueSTRING());
        selectedStyle.setCss(USettings.getAppOrUserSettingsItem("CssFindSelected", behavior).getValueSTRING());
    }

    public boolean calculate(String messageString, Task<Boolean> taskHandler) {
        if (messageString == null || messageString.isEmpty() || messageString.startsWith("FIND/REPLACE ACTION:FIND CLOSED")) {
            if (hasSelectedItem()) unMark();
            foundItems = null;
            selectedItemIndex = null;
            this.messageSTRING = null;
            return true;
        }
        if (this.messageSTRING == null || !this.messageSTRING.equals(messageString)) {
            setState(messageString);
        }

        this.messageSTRING = messageString;

        this.selectedItemIndex = null;

        if (!calculateItems(taskHandler)) { return false; }

        return true;
    }

    public boolean mergeWithRTWidgetStyles(List<StyledString> lastRTWidgetStyledStrings, Task<Boolean> taskHandler) {
        foundItems = Marker.mergeStyles(foundItems, lastRTWidgetStyledStrings);
        if (foundItems == null) { return false; }
        if (taskHandler == null || taskHandler.isCancelled()) {
            return false;
        }

        return true;
    }

    public void mark() {
        if (foundItems == null) return;

        for (StyledString item : foundItems) {
            rtwWidget.setStyle(item.getStart(), item.getEnd(), item.getCssCharStyle());
        }

        msgForToolbar("READY");
    }

    public void unMark() {
        if (foundItems == null) return;

        for (StyledString item : foundItems) {
            rtwWidget.setStyle(item.getStart(), item.getEnd(), item.getCss2());
        }
    }

    public boolean changeStyle(StyleSheetChar styleSheet) {
        if (foundItems == null || foundItems.isEmpty()) return false;

        for (StyledString item : foundItems) {
            rtwWidget.setStyle(item.getStart(), item.getEnd(), styleSheet.getCss());
        }
        
        Platform.runLater(() -> {
            rtwWidget.fixHeight();
        });

        return true;
    }

    public boolean selectUP() {
        if (foundItems == null || foundItems.isEmpty()) return false;

        unSelectItem(selectedItemIndex);

        if (selectedItemIndex == null) {
            selectedItemIndex = 0;
        } else if (selectedItemIndex == 0) {
            selectedItemIndex = foundItems.size() - 1;
        } else {
            selectedItemIndex--;
        }

        selectItem(selectedItemIndex);
        msgForToolbar("READY");
        return true;
    }

    public boolean selectDOWN() {
        if (foundItems == null || foundItems.isEmpty()) return false;

        unSelectItem(selectedItemIndex);

        if (selectedItemIndex == null) {
            selectedItemIndex = 0;
        } else if (selectedItemIndex == foundItems.size() - 1) {
            selectedItemIndex = 0;
        } else {
            selectedItemIndex++;
        }

        selectItem(selectedItemIndex);
        msgForToolbar("READY");
        return true;
    }

    public boolean hasSelectedItem() {
        return foundItems != null && !foundItems.isEmpty();
    }

    public void replaceOne(String withString) {
        if (foundItems == null || foundItems.isEmpty()) return;
        if (selectedItemIndex == null) {
            selectDOWN();
            return;
        }

        rtwWidget.replaceFoundItem(foundItems.get(selectedItemIndex).getStart(), foundItems.get(selectedItemIndex).getEnd(), withString);
        int lenDiff = withString.length() - (foundItems.get(selectedItemIndex).getEnd() - foundItems.get(selectedItemIndex).getStart());
        
        // Update found items
        for (int i = selectedItemIndex + 1; i < foundItems.size(); i++) {
            foundItems.get(i).setStart(foundItems.get(i).getStart() + lenDiff);
            foundItems.get(i).setEnd(foundItems.get(i).getEnd() + lenDiff);
        }


        foundItems.remove(foundItems.get(selectedItemIndex));
        if (selectedItemIndex > foundItems.size() - 1) {
            selectedItemIndex = foundItems.size() - 1;
        }
        if (selectedItemIndex < 0) {
            selectedItemIndex = null;
        } else {
            selectItem(selectedItemIndex);
        }

        msgForToolbar("UPDATE LABEL");
    }

    public void replaceAll(String withString) {
        if (foundItems == null || foundItems.isEmpty()) return;
        if (selectedItemIndex == null) {
            selectDOWN();
            return;
        }

        while (foundItems.size() > 0) {
            replaceOne(withString);
        }
    }

    // Private methods
    private void selectItem(Integer index) {
        if (index == null) return;
        if (foundItems == null || index > foundItems.size() - 1) return;
        foundItems.get(index).setCssCharStyleObject(selectedStyle);
        rtwWidget.moveTo(foundItems.get(index).getStart());
    }

    private void unSelectItem(Integer index) {
        if (index == null) return;
        if (foundItems == null || index > foundItems.size() - 1) return;
        foundItems.get(index).setCssCharStyleObject(foundStyle);
        rtwWidget.moveTo(foundItems.get(index).getStart());
    }

    private void msgForToolbar(String action) {
        String result = "ACTION:" + action;
        
        if (foundItems == null) {
            result += "\n?";
        } else {
            result += "\n" + foundItems.size();
        }
        
        if (selectedItemIndex != null) {
            result += "\n" + (selectedItemIndex + 1);
        } else {
            result += "\n?";
        }

        textHandler.msgForToolbar(result);
    }

    private void setState(String messageSTRING) {
        String[] lines = messageSTRING.split("\\R", -1);

        findText = lines[1];
        isMatchCase = Boolean.parseBoolean(lines[3]);
        isWholeWords = Boolean.parseBoolean(lines[4]);
    }

    private boolean calculateItems(Task<Boolean> taskHandler) {
        foundItems.clear();
        selectedItemIndex = null;

        if (findText.isEmpty()) {
            return true;
        }

        String text = rtwWidget.getText();

        if (!isMatchCase) {
            text = text.toLowerCase();
            findText = findText.toLowerCase();
        }

        int pos = 0;
        while (pos < text.length()) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return false;
            }

            int index = text.indexOf(findText, pos);
            if (index == -1) {
                break;
            }

            if (isWholeWords) {
                if (index > 0 && CONSTANTS.WORD_DELIMITERS.indexOf(text.charAt(index - 1)) == -1) {
                    pos = index + 1;
                    continue;
                }
                if (index + findText.length() == text.length() || CONSTANTS.WORD_DELIMITERS.indexOf(text.charAt(index + findText.length())) == -1) {
                    pos = index + 1;
                    continue;
                }
            }

            foundItems.add(new StyledString(index, index + findText.length(), text.substring(index, index + findText.length()), foundStyle, StyledString.StyleType.FIND_REPLACE));
            pos = index + findText.length();
        }

        return true;
    }



}
