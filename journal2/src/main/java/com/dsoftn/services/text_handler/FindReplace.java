package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.dsoftn.CONSTANTS;
import com.dsoftn.services.RTWidget;

public class FindReplace {
    private record FoundItem(int start, int end, List<String> oldStyles) {}

    // Variables
    private final String FIND_STYLE = "-rtfx-background-color:#beac5a;";
    private RTWidget rtwWidget = null;
    private List<FoundItem> foundItems = new ArrayList<>();

    // Constructor
    public FindReplace() {}

    // Public methods
    public void setRTWidget(RTWidget rtwWidget) {
        this.rtwWidget = rtwWidget;
    }

    public void find(String findText) {
        removeAllMarks();
        if (findText == null) {
            foundItems = new ArrayList<>();
            return;
        }

        if (findText.contains(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
            findText = findText.split(Pattern.quote(CONSTANTS.EMPTY_PARAGRAPH_STRING), -1)[1];
        }

        if (findText.isEmpty()) {
            removeAllMarks();
            return;
        }

        calculate(findText);
        markFoundItems();
    }

    public void markFoundItems() {
        rtwWidget.ac.removeCurrentAC();

        for (FoundItem foundItem : foundItems) {
            rtwWidget.setStyle(foundItem.start, foundItem.end, FIND_STYLE);
        }
    }

    public void removeAllMarks() {
        for (FoundItem foundItem : foundItems) {
            for (int i = foundItem.start; i < foundItem.end; i++) {
                rtwWidget.setStyle(i, i + 1, foundItem.oldStyles.get(i - foundItem.start));
            }
        }

        foundItems = new ArrayList<>();
    }


    // Private methods
    private void calculate(String findText) {
        foundItems.clear();

        int pos = 0;
        String text = this.rtwWidget.getTextNoAC();
        
        while (true) {
            pos = text.indexOf(findText, pos);
            if (pos == -1) {
                break;
            }

            List<String> oldStyles = new ArrayList<>();
            for (int i = pos; i < pos + findText.length(); i++) {
                String oldStyle = this.rtwWidget.getStyleAtPosition(i);
                int bgPos = oldStyle.indexOf("-rtfx-background-color:");
                int bgPosEnd = -1;

                if (bgPos == -1) {
                    oldStyle = oldStyle + "-rtfx-background-color: transparent;";
                } else {
                    bgPosEnd = oldStyle.indexOf(";", bgPos + 1);
                    if (bgPosEnd == -1) {
                        oldStyle = oldStyle + "-rtfx-background-color: transparent;";
                    }
                }

                oldStyles.add(oldStyle);
            }

            foundItems.add(new FoundItem(pos, pos + findText.length(), oldStyles));
            pos++;
        }
    }



}
