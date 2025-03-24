package com.dsoftn.models;

import java.util.regex.Pattern;
import javafx.scene.text.Font;

import com.dsoftn.OBJECTS;

public class StyleSheet {
    // Variables
    private String fontName = OBJECTS.SETTINGS.getvSTRING("DefaultTextToolbarFontName");
    private int fontSize = OBJECTS.SETTINGS.getvINTEGER("DefaultTextToolbarFontSize");
    private String fgColor = "#ffff00";
    private String bgColor = "transparent";
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strike = false;


    // Constructors

    public StyleSheet() {}

    public StyleSheet(String css) {
        setCss(css);
    }
    

    // Public methods

    public String getCss() {
        String css = "";

        if (!fontName.isEmpty()) {
            css += "-fx-font-family: " + fontName + ";";
        }

        css += "-fx-font-size: " + fontSize + "px;";

        if (!fgColor.isEmpty()) {
            css += "-fx-fill: " + fgColor + ";";
        }

        if (!bgColor.isEmpty()) {
            css += "-rtfx-background-color: " + bgColor + ";";
        }

        if (bold) {
            css += "-fx-font-weight: bold;";
        } else {
            css += "-fx-font-weight: normal;";
        }

        if (italic) {
            css += "-fx-font-style: italic;";
        } else {
            css += "-fx-font-style: normal;";
        }

        if (underline) {
            css += "-fx-underline: true;";
        } else {
            css += "-fx-underline: false;";
        }

        if (strike) {
            css += "-fx-strikethrough: true;";
        } else {
            css += "-fx-strikethrough: false;";
        }

        return css;
    }

    public void setCss(String css) {
        // Parse css string
        String[] cssArray = css.split(Pattern.quote(";"));
        for (String cssItem : cssArray) {
            String[] cssItemArray = cssItem.split(Pattern.quote(":"));
            if (cssItemArray.length == 2) {
                String cssItemName = cssItemArray[0].strip();
                String cssItemValue = cssItemArray[1].strip();
                switch (cssItemName) {
                    case "-fx-font-family":
                        setFontName(cssItemValue);
                        break;
                    case "-fx-font-size":
                        setFontSize(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));
                        break;
                    case "-fx-fill":
                        setFgColor(cssItemValue);
                        break;
                    case "-rtfx-background-color":
                        setBgColor(cssItemValue);
                        break;
                    case "-fx-font-weight":
                        setBold(cssItemValue.equals("bold"));
                        break;
                    case "-fx-font-style":
                        setItalic(cssItemValue.equals("italic"));
                        break;
                    case "-fx-underline":
                        setUnderline(cssItemValue.equals("true"));
                        break;
                    case "-fx-strikethrough":
                        setStrikethrough(cssItemValue.equals("true"));
                        break;
                }
            }
        }
    }

    // Properties getters and setters

    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { this.fontName = fontName; }
    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) {
        if (fontSize < OBJECTS.SETTINGS.getvINTEGER("MinTextToolbarFontSize")) {
            fontSize = OBJECTS.SETTINGS.getvINTEGER("MinTextToolbarFontSize");
        }
        if (fontSize > OBJECTS.SETTINGS.getvINTEGER("MaxTextToolbarFontSize")) {
            fontSize = OBJECTS.SETTINGS.getvINTEGER("MaxTextToolbarFontSize");
        }
        this.fontSize = fontSize;
    }
    public String getFgColor() { return fgColor; }
    public void setFgColor(String fgColor) { this.fgColor = fgColor; }
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }
    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }
    public boolean isStrikethrough() { return strike; }
    public void setStrikethrough(boolean strike) { this.strike = strike; }



}
