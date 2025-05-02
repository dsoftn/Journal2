package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;

import javafx.concurrent.Task;


public class FindReplace {
    // Variables
    private StyleSheetChar foundStyle = new StyleSheetChar(true);
    private StyleSheetChar selectedStyle = new StyleSheetChar(true);
    private Integer selectedItemIndex = null;
    private RTWidget rtwWidget = null;
    private TextHandler textHandler = null;
    private List<MarkedItem> foundItems = new ArrayList<>();
    private List<StyleSheetChar> cssChars = new ArrayList<>();
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

    public List<StyleSheetChar> calculate(String messageString, List<StyleSheetChar> cssChars, Task<Boolean> taskHandler) {
        if (messageString == null || messageString.isEmpty() || messageString.startsWith("FIND/REPLACE ACTION:FIND CLOSED")) {
            unMark();
            foundItems = new ArrayList<>();
            selectedItemIndex = null;
            this.messageSTRING = null;
            this.cssChars = new ArrayList<>();
            return cssChars;
        }
        if (this.messageSTRING == null || !this.messageSTRING.equals(messageString)) {
            setState(messageString);
        }
        if (this.cssChars == null || !this.cssChars.equals(cssChars)) {
            this.cssChars = cssChars;
        }

        this.messageSTRING = messageString;

        this.selectedItemIndex = null;

        if (!calculateItems(taskHandler)) { return null; }

        return cssChars;
    }

    public void mark() {
        if (cssChars == null) return;

        int index = 0;
        for (StyleSheetChar item : cssChars) {
            rtwWidget.setStyle(index, index + 1, item.getCss());
            index++;
        }
        msgForToolbar("READY");
    }

    public void unMark() {
        if (cssChars == null) return;

        int index = 0;
        for (StyleSheetChar item : cssChars) {
            if (item.getStyleBeforeMerge().isEmpty()) {
                rtwWidget.setStyle(index, index + 1, item.getCss());
            } else {
                rtwWidget.setStyle(index, index + 1, item.getStyleBeforeMerge());
            }
            
            index++;
        }
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

    public void replaceOne(String withString) {
        if (foundItems == null || foundItems.isEmpty()) return;
        if (selectedItemIndex == null) {
            selectDOWN();
            return;
        }

        rtwWidget.replaceText(foundItems.get(selectedItemIndex).start, foundItems.get(selectedItemIndex).end, withString);
        
        if (cssChars.get(foundItems.get(selectedItemIndex).start).getStyleBeforeMerge().isEmpty()) {
            rtwWidget.setStyle(foundItems.get(selectedItemIndex).start, foundItems.get(selectedItemIndex).end, cssChars.get(foundItems.get(selectedItemIndex).start).getCss());
        } else {
            rtwWidget.setStyle(foundItems.get(selectedItemIndex).start, foundItems.get(selectedItemIndex).end, cssChars.get(foundItems.get(selectedItemIndex).start).getStyleBeforeMerge());
        }

        StyleSheetChar css = rtwWidget.getCssChar();

        int start = foundItems.get(selectedItemIndex).start;

        // Remove from widget css list
        for (int i = foundItems.get(selectedItemIndex).start; i < foundItems.get(selectedItemIndex).end; i++) {
            rtwWidget.cssStyles.remove(start);
        }

        // Remove from css list
        for (int i = foundItems.get(selectedItemIndex).start; i < foundItems.get(selectedItemIndex).end; i++) {
            cssChars.remove(start);
        }

        // Add to widget css list
        for (int i = 0; i < withString.length(); i++) {
            rtwWidget.cssStyles.add(start, css.duplicate());
        }

        // Add to css list
        for (int i = 0; i < withString.length(); i++) {
            cssChars.add(start, css.duplicate());
        }

        // Update found items
        for (int i = selectedItemIndex + 1; i < foundItems.size(); i++) {
            foundItems.get(i).start += withString.length() - (foundItems.get(selectedItemIndex).end - foundItems.get(selectedItemIndex).start);
            foundItems.get(i).end += withString.length() - (foundItems.get(selectedItemIndex).end - foundItems.get(selectedItemIndex).start);
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
        for (int i = foundItems.get(index).start; i < foundItems.get(index).end; i++) {
            cssChars.get(i).mergeNoSave(selectedStyle);
            rtwWidget.setStyle(i, i + 1, cssChars.get(i).getCss());
        }
        rtwWidget.moveTo(foundItems.get(index).start);
    }

    private void unSelectItem(Integer index) {
        if (index == null) return;
        if (foundItems == null || index > foundItems.size() - 1) return;
        for (int i = foundItems.get(index).start; i < foundItems.get(index).end; i++) {
            cssChars.get(i).mergeNoSave(foundStyle);
            rtwWidget.setStyle(i, i + 1, cssChars.get(i).getCss());
        }
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
            if (taskHandler.isCancelled()) {
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

            foundItems.add(new MarkedItem(index, index + findText.length(), CONSTANTS.INVALID_ID, foundStyle, MarkedItem.MarkedType.FIND_REPLACE));
            pos = index + findText.length();
        }

        cssChars = Marker.updateCssList(cssChars, foundItems);

        return true;

    }



}
