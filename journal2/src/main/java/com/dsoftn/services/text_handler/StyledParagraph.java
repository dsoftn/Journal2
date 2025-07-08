package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.CONSTANTS;
import com.dsoftn.services.RTWText;
import com.dsoftn.utils.UError;


public class StyledParagraph {
    // Variables
    private String cssParagraphStyle = null;
    private List<StyledString> styledStringList = new ArrayList<>();

    // Constructor
    public StyledParagraph(StyleSheetParagraph cssParagraphStyle, StyleSheetChar defaultCharStyle) {
        this.cssParagraphStyle = cssParagraphStyle.getCss();
        this.styledStringList.add(new StyledString(0, 1, CONSTANTS.EMPTY_PARAGRAPH_STRING, defaultCharStyle));
    }

    public StyledParagraph(String cssParagraphStyle, String defaultCharStyle) {
        this.cssParagraphStyle = cssParagraphStyle;
        this.styledStringList.add(new StyledString(0, 1, CONSTANTS.EMPTY_PARAGRAPH_STRING, defaultCharStyle));
    }

    public StyledParagraph(String styledParagraph) {
        if (!styledParagraph.startsWith(CONSTANTS.RTW_TEXT_PARAGRAPH_START)) {
            UError.error("StyledParagraph: Text you are trying to create paragraph from is not styled paragraph text.", "Error");
            return;
        }

        String[] lines = styledParagraph.split("\n");
        cssParagraphStyle = lines[1];
        for (int i = 2; i < lines.length; i += 2) {
            String text = lines[i];
            if (text.startsWith(CONSTANTS.RTW_TEXT_PARAGRAPH_END)) {
                break;
            }
            if (i == 2 && !text.startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                text = CONSTANTS.EMPTY_PARAGRAPH_STRING + text;
            }
            if (text.isEmpty()) {
                continue;
            }
            String css = lines[i + 1];
            styledStringList.add(new StyledString(0, 0, text, css));
        }
        styledStringList = normalize(styledStringList);
    }

    // Public methods

    /**
     * Split paragraph on index
     * <p>Split paragraph on index and return new paragraph with all items after index</p>
     * <p>Old paragraph will contain all items before index</p>
     * @param index - Index to split on
     * @return StyledParagraph - New paragraph with all items after index
     */
    public StyledParagraph splitOnIndex(int index) {
        if (index == 0) {
            index = 1;
        }

        List<StyledString> newStyledStringList = new ArrayList<>();
        List<StyledString> oldStyledStringList = new ArrayList<>();

        // Add all items after index to new list and remove them from old list, if index in in the middle of an item, split it and leave first part in old list and second part in new list
        int currentIndex = 0;
        while (currentIndex < styledStringList.size()) {
            StyledString item = styledStringList.get(currentIndex);
            if (item.getEnd() <= index) {
                oldStyledStringList.add(styledStringList.get(currentIndex));
                currentIndex++;
                continue;
            }

            if (item.getStart() <= index && item.getEnd() > index) {
                String text1 = item.getText().substring(0, index - item.getStart());
                String text2 = item.getText().substring(index - item.getStart());

                item.setText(text1);
                item.setEnd(item.getStart() + text1.length());

                newStyledStringList.add(new StyledString(index, index + text2.length(), text2, item.getCssCharStyleObject().duplicate()));
                
                if (item.getText().isEmpty()) {
                    styledStringList.remove(currentIndex);
                } else {
                    oldStyledStringList.add(styledStringList.get(currentIndex));
                    currentIndex++;
                }
                continue;
            }

            newStyledStringList.add(styledStringList.remove(currentIndex));
        }

        StyledParagraph newStyledParagraph = null;
        if (newStyledStringList.isEmpty()) {
            newStyledParagraph = new StyledParagraph(cssParagraphStyle, "");
            newStyledParagraph.setStyledStringList(newStyledStringList);
        } else {
            newStyledParagraph = new StyledParagraph(cssParagraphStyle, newStyledStringList.get(0).getCssCharStyle());
            newStyledParagraph.insertStyledStrings(0, newStyledStringList);
        }
        

        setStyledStringList(this.normalize(oldStyledStringList));
        newStyledParagraph.setStyledStringList(newStyledParagraph.normalize());

        return newStyledParagraph;
    }

    public String getPlainText() {
        String result = "";
        for (StyledString item : styledStringList) {
            result += item.getText();
        }
        return result;
    }

    public boolean isEmpty() {
        return getPlainText().length() <= 1;
    }

    public StyleSheetChar getPredictedCssChar(int column, boolean isOverwrite) {
        if (column >= getPlainText().length()) {
            column = getPlainText().length() - 1;
        } else {
            if (!isOverwrite) {
                column--;
                if (column < 0) column = 0;
            }
        }

        for (StyledString styledString : styledStringList) {
            if (column >= styledString.getStart() && column < styledString.getEnd()) {
                return styledString.getCssCharStyleObject();
            }
        }

        return new StyleSheetChar();
    }

    public void delete(Integer start, Integer end) {
        if (start == null) start = 1;
        if (end == null) end = getPlainText().length();

        if (start == 0) {
            start = 1;
        }

        if (start == end) {
            return;
        }

        List<StyledString> updatedItems = new ArrayList<>();

        for (StyledString item : styledStringList) {
            if (item.getEnd() <= start) {
                updatedItems.add(item);
                continue;
            }
            if (item.getStart() >= end) {
                updatedItems.add(item);
                continue;
            }

            if (item.getStart() < start && item.getEnd() > end) {
                updatedItems.add(new StyledString(item.getStart(), start, item.getText().substring(0, start - item.getStart()), item.getCssCharStyleObject()));
                updatedItems.add(new StyledString(end, item.getEnd(), item.getText().substring(end - item.getStart()), item.getCssCharStyleObject()));
            } else if (item.getStart() < start && item.getEnd() <= end) {
                updatedItems.add(new StyledString(item.getStart(), start, item.getText().substring(0, start - item.getStart()), item.getCssCharStyleObject()));
            } else if (item.getStart() >= start && item.getEnd() <= end) {
                // Do nothing
            } else if (item.getStart() >= start && item.getEnd() > end) {
                updatedItems.add(new StyledString(end, item.getEnd(), item.getText().substring(end - item.getStart()), item.getCssCharStyleObject()));
            }
        }

        styledStringList = normalize(updatedItems);
    }

    public void insertStyledStrings(int start, List<StyledString> styledStrings) {
        if (styledStrings.isEmpty()) {
            return;
        }

        if (start == 0) {
            start = 1;
        }

        for (StyledString styledString : styledStrings) {
            styledString.setText(styledString.getText().replaceAll(CONSTANTS.EMPTY_PARAGRAPH_STRING, ""));
        }

        boolean isInserted = false;
        for (int i = 0; i < styledStringList.size(); i++) {
            StyledString item = styledStringList.get(i);
            if (start < item.getStart()) {
                for (StyledString styledString : styledStrings) {
                    styledStringList.add(i, styledString);
                    i++;
                }
                isInserted = true;
                break;
            }
            else if (start == item.getStart() && start < item.getEnd()) {
                for (StyledString styledString : styledStrings) {
                    styledStringList.add(i, styledString);
                    i++;
                }
                isInserted = true;
                break;
            } else if (start > item.getStart() && start < item.getEnd()) {
                StyledString prevItem = new StyledString(item.getStart(), start, item.getText().substring(0, start - item.getStart()), item.getCssCharStyleObject());
                StyledString nextItem = new StyledString(start + styledStrings.get(0).getEnd() - start, item.getEnd(), item.getText().substring(start - item.getStart()), item.getCssCharStyleObject());
                styledStringList.set(i, prevItem);
                for (StyledString styledString : styledStrings) {
                    styledStringList.add(i + 1, styledString);
                    i++;
                }
                styledStringList.add(i + 1, nextItem);
                isInserted = true;
                break;
            }
        }
        if (!isInserted) {
            for (StyledString styledString : styledStrings) {
                styledStringList.add(styledString);
            }
        }

        styledStringList = normalize(styledStringList);
    }

    public void insertStyledStrings(int start, String styledText) {
        if (!styledText.startsWith(CONSTANTS.RTW_TEXT_START)) {
            UError.error("StyledParagraph.insertStyledStrings: Text you are trying to insert is not styled text.", "Error");
            return;
        }

        RTWText rtwText = new RTWText(styledText);
        List<StyledParagraph> styledParagraphs = rtwText.getStyledParagraphs();
        if (styledParagraphs.isEmpty()) {
            return;
        } else if (styledParagraphs.size() > 1) {
            UError.error("StyledParagraph.insertStyledStrings: Text you are trying to insert contains more than one paragraph.", "Error");
            return;
        }

        List<StyledString> styledStrings = rtwText.getStyledParagraphs().get(0).getStyledStringList();

        insertStyledStrings(start, styledStrings);
    }

    public void insertPlainText(int start, String text, StyleSheetChar cssCharStyle) {
        if (text.isEmpty()) {
            return;
        }

        if (text.startsWith(CONSTANTS.RTW_TEXT_START)) {
            UError.error("StyledParagraph.insertPlainText: Text you are trying to insert is styled text.", "Error");
            return;
        }

        if (start == 0) {
            start = 1;
        }

        text = text.replaceAll(CONSTANTS.EMPTY_PARAGRAPH_STRING, "");

        boolean isInserted = false;
        for (int i = 0; i < styledStringList.size(); i++) {
            StyledString item = styledStringList.get(i);
            if (start < item.getStart()) {
                styledStringList.add(i, new StyledString(start, start + text.length(), text, cssCharStyle));
                isInserted = true;
                break;
            }
            else if (start == item.getStart() && start < item.getEnd()) {
                styledStringList.add(i, new StyledString(start, start + text.length(), text, cssCharStyle));
                isInserted = true;
                break;
            } else if (start > item.getStart() && start < item.getEnd()) {
                StyledString prevItem = new StyledString(item.getStart(), start, item.getText().substring(0, start - item.getStart()), item.getCssCharStyleObject());
                StyledString nextItem = new StyledString(start + text.length(), item.getEnd(), item.getText().substring(start - item.getStart()), item.getCssCharStyleObject());
                styledStringList.set(i, prevItem);
                styledStringList.add(i + 1, new StyledString(start, start + text.length(), text, cssCharStyle));
                styledStringList.add(i + 2, nextItem);
                isInserted = true;
                break;
            }
        }
        if (!isInserted) {
            styledStringList.add(new StyledString(start, start + text.length(), text, cssCharStyle));
        }

        styledStringList = normalize(styledStringList);
    }

    public boolean updateCss(Integer startParCol, Integer endParCol, String updateWithCss) {
        // if (startParCol == null && endParCol == null) { return false; }
        if (startParCol == null) { startParCol = 0; }
        if (endParCol == null) { endParCol = Integer.MAX_VALUE; }

        List<StyledString> updatedItems = new ArrayList<>();

        for (StyledString item : styledStringList) {
            // Complete overlap
            if (startParCol <= item.getStart() && endParCol >= item.getEnd()) {
                item.setCssCharStyleObject(item.getCssCharStyleObject().mergeGetNew(updateWithCss));
                updatedItems.add(item);
                continue;
            }
            // No overlap
            if (startParCol >= item.getEnd() || endParCol <= item.getStart()) {
                updatedItems.add(item);
                continue;
            }
            // Partial overlap
            int newTextSubstringStart = startParCol - item.getStart();
            int newTextSubstringEnd = endParCol - item.getStart();
            if (newTextSubstringStart < 0) { newTextSubstringStart = 0; }
            if (newTextSubstringEnd > item.getText().length()) { newTextSubstringEnd = item.getText().length(); }
            if (newTextSubstringStart >= newTextSubstringEnd) { continue; }

            StyledString newItem1 = new StyledString(0,0, item.getText().substring(0, newTextSubstringStart), item.getCssCharStyle());
            StyledString newItem2 = new StyledString(0,0, item.getText().substring(newTextSubstringStart, newTextSubstringEnd), item.getCssCharStyleObject().duplicate().mergeGetNew(updateWithCss));
            StyledString newItem3 = new StyledString(0,0, item.getText().substring(newTextSubstringEnd), item.getCssCharStyle());

            if (!newItem1.getText().isEmpty()) {
                updatedItems.add(newItem1);
            }
            if (!newItem2.getText().isEmpty()) {
                updatedItems.add(newItem2);
            }
            if (!newItem3.getText().isEmpty()) {
                updatedItems.add(newItem3);
            }
        }

        styledStringList = normalize(updatedItems);

        return true;
    }

    public List<StyledString> normalize(List<StyledString> styledStringList) {
        List<StyledString> result = new ArrayList<>();

        if (styledStringList.isEmpty()) {
            return result;
        }

        int nextStart = 0;
        StyledString item = null;
        StyledString itemToWrite = null;

        // Set zero string at beginning if not exists
        if (styledStringList.size() > 0) {
            item = styledStringList.get(0);
            if (!item.getText().startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                item.setText(CONSTANTS.EMPTY_PARAGRAPH_STRING + item.getText());
                styledStringList.set(0, item);
            }
        }

        // Join items with same style and set start and end
        for (int i = 0; i < styledStringList.size(); i++) {
            item = styledStringList.get(i);
            item.setStart(nextStart);
            item.setEnd(item.getText().length() + nextStart);

            if (itemToWrite == null) {
                itemToWrite = item.duplicate();
            } else {
                if (itemToWrite.getCssCharStyle().equals(item.getCssCharStyle())) {
                    itemToWrite.setText(itemToWrite.getText() + item.getText());
                    itemToWrite.setEnd(itemToWrite.getEnd() + item.getText().length());
                } else {
                    result.add(itemToWrite);
                    nextStart = itemToWrite.getEnd();
                    itemToWrite = item.duplicate();
                    itemToWrite.setStart(nextStart);
                    itemToWrite.setEnd(itemToWrite.getStart() + itemToWrite.getText().length());
                }
            }

            if (i == styledStringList.size() - 1) {
                result.add(itemToWrite);
                break;
            }
        }

        return result;
    }

    public List<StyledString> normalize() {
        return normalize(styledStringList);
    }

    public StyledParagraph duplicate() {
        StyledParagraph result = new StyledParagraph(cssParagraphStyle, new StyleSheetChar().getCss());

        result.styledStringList = new ArrayList<>();

        for (StyledString item : styledStringList) {
            result.styledStringList.add(item.duplicate());
        }

        return result;
    }

    // Private methods


    // Getters and setters
    public StyleSheetParagraph getCssParagraphStyleObject() { return new StyleSheetParagraph(cssParagraphStyle); }
    public String getCssParagraphStyle() { return cssParagraphStyle; }
    public void setCssParagraphStyleObject(StyleSheetParagraph cssParagraphStyle) { this.cssParagraphStyle = cssParagraphStyle.getCss(); }
    public void setCssParagraphStyle(String cssParagraphStyle) { this.cssParagraphStyle = cssParagraphStyle; }
    public List<StyledString> getStyledStringList() { return styledStringList; }
    public void setStyledStringList(List<StyledString> styledStringList) { this.styledStringList = styledStringList; }
    public StyleSheetChar getCssCharStyleObject(Integer column) {
        if (column == null) {
            return new StyleSheetChar();
        }
        for (StyledString item : styledStringList) {
            if (column >= item.getStart() && column < item.getEnd()) {
                return item.getCssCharStyleObject();
            }
        }
        return new StyleSheetChar();
    }
    public String getCssCharStyle(Integer column) {
        return getCssCharStyleObject(column).getCss();
    }

}
