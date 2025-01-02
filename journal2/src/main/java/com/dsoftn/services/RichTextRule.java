package com.dsoftn.services;

import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;


public class RichTextRule {
    // Variables
    private String text = "";
    private String replacement;
    private String css = "";

    // Constructors

    public RichTextRule() {}

    public RichTextRule(String text) {
        this.text = text;
    }

    public RichTextRule(String text, String replacement) {
        this.text = text;
        this.replacement = replacement;
    }

    public RichTextRule(String text, String replacement, String css) {
        this.text = text;
        this.replacement = replacement;
        this.css = css;
    }

    // Methods

    public String getText() {
        return text;
    }

    public String getReplacement() {
        if (replacement == null) {
            return this.text;
        }
        return replacement;
    }

    public String getCss() {
        return this.css;
    }

    public void setCss(String css) {
        this.css = css.strip();
        if (!this.css.endsWith(";")) {
            this.css += ";";
        }
    }

    public void addCss(String css) {
        this.css += "\n" + css.strip();
        if (!this.css.endsWith(";")) {
            this.css += ";";
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public void setFontBold() {
        addCss("-fx-font-weight: bold;");
    }

    public void setFontItalic() {
        addCss("-fx-font-style: italic;");
    }

    public void setFontUnderline() {
        addCss("-fx-font-style: underline;");
    }

    public void setFontColor(String color) {
        addCss("-fx-text-fill: " + color + ";");
    }

    public void setFontSize(int size) {
        addCss("-fx-font-size: " + size + "px;");
    }

    public void setFontFamily(String family) {
        addCss("-fx-font-family: " + family + ";");
    }

    public Text getTextObject() {
        String textToPrint = this.text;
        if (this.replacement != null) {
            textToPrint = this.replacement;
        }
        Text textObj = new Text(textToPrint);
        
        textObj.setStyle(this.css);

        return textObj;
    }

}
