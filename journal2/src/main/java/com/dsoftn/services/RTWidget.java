package com.dsoftn.services;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.InlineCssTextArea;

import com.dsoftn.models.StyleSheetChar;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class RTWidget extends InlineCssTextArea {
    // Variables
    private StyleSheetChar css = null;
    private Integer maxLines = null;
    private Integer maxChars = null;
    private Integer maxCharsPerLine = null;
    private int minTextWidgetHeight = 24;
    public List<StyleSheetChar> cssStyles = new ArrayList<>();
    private Runnable updateToolbar = null;
    private TextHandler textHandler = null;

    public boolean isOverwriteMode = false;
   
    // Constructors
    public RTWidget(StyleSheetChar cssStyleSheet, Integer maxLines, Integer maxChars, Integer maxCharsPerLine) {
        super();
        this.css = cssStyleSheet;
        this.maxLines = maxLines;
        this.maxChars = maxChars;
        this.maxCharsPerLine = maxCharsPerLine;
    }
    
    public RTWidget(StyleSheetChar cssStyleSheet) {
        super();
        this.css = cssStyleSheet;
    }

    public RTWidget() {
        super();
        this.css = new StyleSheetChar();
    }

    // Public methods

    public void setUpdateToolbar(Runnable updateToolbar) {
        this.updateToolbar = updateToolbar;
    }

    public void setCss(StyleSheetChar css) {
        if (css == null) {
            this.css = new StyleSheetChar();
        } else {
            this.css = css;
        }
    }

    public StyleSheetChar getCss() {
        return this.css;
    }

    public void setMinTextWidgetHeight(int minTextWidgetHeight) {
        this.minTextWidgetHeight = minTextWidgetHeight;
    }

    public void setTextHandler(TextHandler textHandler) {
        this.textHandler = textHandler;
    }

    public void setupWidget() {
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setWrapText(true);
        this.getStyleClass().add("rich-text");

        this.totalHeightEstimateProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.doubleValue() > minTextWidgetHeight) {
                this.setPrefHeight(newVal.doubleValue());
            }
        });
        this.setMinHeight(minTextWidgetHeight);
        this.setPrefHeight(minTextWidgetHeight);

        this.textProperty().addListener((obs, oldText, newText) -> {
            handleTextChange(obs, oldText, newText);
        });

        this.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            handleCaretPositionChange(obs, oldPos, newPos);
        });

        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_INSERT();
            }
        });


        
        // this.appendText("-");
        // this.replaceText("");

        // inlineCssTextArea.getStyleOfChar(0)
        // inlineCssTextArea.getParagraph(0).getText(); // Prvi paragraf
        // inlineCssTextArea.getParagraphs().size();
        // inlineCssTextArea.setParagraphStyle(1, "-fx-font-size: 20px; -fx-text-fill: blue;");

        this.requestFocus();
    }

    // Private methods

    private void sendCssToHandler(Integer caretPosition) {
        if (!this.getSelectedText().isEmpty()) {
            return;
        }

        textHandler.msgFromWidget(css);

        if (caretPosition != null) {
            // get current paragraph
            int paragraphIndex = this.getCurrentParagraph();
            String paragraphStyle = getParagraphStyle(paragraphIndex);

        }
    }

    private void handleKeyPressed_INSERT() {
        isOverwriteMode = !isOverwriteMode;
        if (!isOverwriteMode) {
            if (this.getCaretPosition() < this.cssStyles.size()) {
                // If caret is not at end then there is marked character, demark it
                this.setStyle(this.getCaretPosition(), this.getCaretPosition() + 1, cssStyles.get(this.getCaretPosition()).getCss());
            }
        }
        if (this.cssStyles.size() > 0) {
            if (this.getCaretPosition() < this.cssStyles.size()) {
                handleCaretPositionChange(null, this.getCaretPosition() + 1, this.getCaretPosition());
            }
            else {
                handleCaretPositionChange(null, this.getCaretPosition() - 1, this.getCaretPosition());
            }
        }
    }

    private void handleTextChange(Object obs, String oldText, String newText) {
        // Added new characters
        if (newText.length() > oldText.length()) {
            for (int i = this.getCaretPosition() - (newText.length() - oldText.length()); i < this.getCaretPosition(); i++) {
                this.cssStyles.add(i, css.duplicate());
            }
            this.setStyle(this.getCaretPosition() - (newText.length() - oldText.length()), this.getCaretPosition(), css.getCss());
        }
        // Removed characters
        else if (newText.length() < oldText.length()) {
            for (int i = newText.length(); i < oldText.length(); i++) {
                this.cssStyles.remove(this.getCaretPosition());
            }
        }
        // New and Old text are same length, that occurs usually in OVERWRITE mode
        else {
            this.css = this.cssStyles.get(this.getCaretPosition()).duplicate();
            this.setStyle(this.getCaretPosition(), this.getCaretPosition() + 1, this.css.getCss());
        }
    }

    private void handleCaretPositionChange(Object obs, Integer oldPos, Integer newPos) {
        // When user in overwrite mode change char with same char then "textProperty" does not trigger listener
        // This code will handle that situation and properly change character style
        if (isOverwriteMode && this.cssStyles.size() > 0 && newPos.intValue() > oldPos.intValue() && this.getSelectedText().isEmpty()) {
            if (!this.css.equals(this.cssStyles.get(newPos.intValue() - 1))) {
                this.css = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                this.setStyle(newPos.intValue() - 1, newPos.intValue(), this.css.getCss());
                updateToolbar.run();
            }
        }

        // Case when caret is at the beginning of the text in INSERT mode
        // Global CSS will be as char after caret (Char in pos 0)
        if (newPos.intValue() == 0 && !isOverwriteMode) {
            if (this.cssStyles.size() > 0) {
                if (!css.equals(this.cssStyles.get(0))) {
                    css = this.cssStyles.get(0).duplicate();
                    updateToolbar.run();
                }
            }
        }
        
        // Case when OVERWRITE mode is on or caret is not at the beginning of the text in both modes
        if (newPos.intValue() != 0 || isOverwriteMode) {
            if (isOverwriteMode) {
                // Update global CSS as CSS of char under caret in OVERWRITE mode
                if (newPos.intValue() == this.cssStyles.size()) {
                    // Caret is at the end of the text
                    if (!css.equals(this.cssStyles.get(newPos.intValue() - 1))) {
                        css = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                        updateToolbar.run();
                    }
                }
                else {
                    // Caret is in the middle of the text
                    if (!css.equals(this.cssStyles.get(newPos.intValue()))) {
                        css = this.cssStyles.get(newPos.intValue()).duplicate();
                        updateToolbar.run();
                    }
                }

                // Demark old character and Mark new character
                if (isOverwriteMode) {
                    // If oldPos is not at the end of the text, demark last character
                    if (oldPos.intValue() < this.cssStyles.size()) {
                        this.setStyle(oldPos.intValue(), oldPos.intValue() + 1, this.cssStyles.get(oldPos.intValue()).getCss());
                    }
                    // If there is no selection and caret is not at end, mark new character
                    if (this.getSelectedText().isEmpty() && newPos.intValue() < this.cssStyles.size()) {
                        StyleSheetChar newStylesheet = this.cssStyles.get(newPos.intValue()).duplicate();
                        newStylesheet.setBgColor("#dfdfdf");
                        newStylesheet.setFgColor("#202020");
                        this.setStyle(newPos.intValue(), newPos.intValue() + 1, newStylesheet.getCss());
                        
                        // Update global CSS with char under caret and update toolbar
                        if (!css.equals(this.cssStyles.get(newPos.intValue()))) {
                            css = this.cssStyles.get(newPos.intValue()).duplicate();
                            updateToolbar.run();
                        }
                    }
                }
            }
            else {
                // Case when INSERT mode is on
                if (!css.equals(this.cssStyles.get(newPos.intValue() - 1))) {
                    css = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                    updateToolbar.run();
                }
            }

        }
    }




}
