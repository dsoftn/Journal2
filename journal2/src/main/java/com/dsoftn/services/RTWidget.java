package com.dsoftn.services;

import org.fxmisc.richtext.InlineCssTextArea;

import com.dsoftn.models.StyleSheet;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class RTWidget extends InlineCssTextArea {
    // Variables
    private StyleSheet css = null;
    private Integer maxLines = null;
    private Integer maxChars = null;
    private Integer maxCharsPerLine = null;
    private int minTextWidgetHeight = 24;
   
    // Constructors
    public RTWidget(StyleSheet cssStyleSheet, Integer maxLines, Integer maxChars, Integer maxCharsPerLine) {
        super();
        setCss(cssStyleSheet);
        this.maxLines = maxLines;
        this.maxChars = maxChars;
        this.maxCharsPerLine = maxCharsPerLine;
    }
    public RTWidget(StyleSheet cssStyleSheet) {
        super();
        setCss(cssStyleSheet);
    }


    // Public methods

    public void setCss(StyleSheet css) {
        if (css == null) {
            this.css = new StyleSheet();
        } else {
            this.css = css;
        }
    }

    public void setMinTextWidgetHeight(int minTextWidgetHeight) {
        this.minTextWidgetHeight = minTextWidgetHeight;
    }

    public void setupWidget() {
        HBox.setHgrow(this, Priority.ALWAYS);
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
            if (newText.length() > oldText.length()) {
                System.out.println(this.getCaretPosition() - (newText.length() - oldText.length()) + " --- " + this.getCaretPosition());
                this.setStyle(this.getCaretPosition() - (newText.length() - oldText.length()), this.getCaretPosition(), css.getCss());
            }
        });

        this.appendText("-");
        this.replaceText("");

        this.requestFocus();
    }


}
