package com.dsoftn.models;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dsoftn.OBJECTS;
import com.dsoftn.utils.UString;

public class StyleSheetChar {
    // Variables
    private String fontName = OBJECTS.SETTINGS.getvSTRING("DefaultTextToolbarFontName"); // -fx-font-family: 'Arial';
    private int fontSize = OBJECTS.SETTINGS.getvINTEGER("DefaultTextToolbarFontSize"); // -fx-font-size: 12px;
    private String fgColor = "#ffff00"; // -fx-fill: #ffff00;
    private String bgColor = "transparent"; // -rtfx-background-color: transparent;
    private boolean bold = false; // -fx-font-weight: bold;
    private boolean italic = false; // -fx-font-style: italic;
    private boolean underline = false; // -fx-underline: true;
    private boolean strike = false; // -fx-strikethrough: true;

    // Constructors

    public StyleSheetChar() {}

    public StyleSheetChar(String css) {
        setCss(css);
    }
    

    // Public methods

    public String getCss() {
        String css = "";

        if (!fontName.isEmpty()) {
            css += "-fx-font-family: '" + fontName + "';";
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

    public String getCssMinimal() {
        String css = "";

        if (!fontName.isEmpty()) {
            css += "-fx-font-family: '" + fontName + "';";
        }

        css += "-fx-font-size: " + fontSize + "px;";

        if (!fgColor.equals("#ffff00")) {
            css += "-fx-fill: " + fgColor + ";";
        }

        if (!bgColor.equals("transparent")) {
            css += "-rtfx-background-color: " + bgColor + ";";
        }

        if (bold) {
            css += "-fx-font-weight: bold;";
        }

        if (italic) {
            css += "-fx-font-style: italic;";
        }

        if (underline) {
            css += "-fx-underline: true;";
        }

        if (strike) {
            css += "-fx-strikethrough: true;";
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
                        setFontName(UString.stripCharacters(cssItemValue, "'"));
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

    public StyleSheetChar duplicate() {
        StyleSheetChar result = new StyleSheetChar();

        result.fontName = this.fontName;
        result.fontSize = this.fontSize;
        result.fgColor = this.fgColor;
        result.bgColor = this.bgColor;
        result.bold = this.bold;
        result.italic = this.italic;
        result.underline = this.underline;
        result.strike = this.strike;

        return result;
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

    // Overrides methods "equals()" and "hashCode()"
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        StyleSheetChar other = (StyleSheetChar) obj;

        return Objects.equals(this.fontName, other.fontName) &&
               this.fontSize == other.fontSize &&
               Objects.equals(this.fgColor, other.fgColor) &&
               Objects.equals(this.bgColor, other.bgColor) &&
               this.bold == other.bold &&
               this.italic == other.italic &&
               this.underline == other.underline &&
               this.strike == other.strike;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontName, fontSize, fgColor, bgColor, bold, italic, underline, strike);
    }

}
