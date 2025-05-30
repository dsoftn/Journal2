package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.WordExtractor;
import com.dsoftn.utils.USettings;

import javafx.concurrent.Task;

public class WebMark {
    // Variables
    private RTWidget rtWidget = null;
    private List<StyleSheetChar> cssChars = new ArrayList<>();
    private List<MarkedItem> foundItems = new ArrayList<>();
    private boolean allowMarkingWebLinks = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingWebLinks");
    private boolean allowMarkingEmails = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingEmails");
    private String cssMarkedWebLink = OBJECTS.SETTINGS.getvSTRING("CssMarkedWebLink");
    private String cssMarkedEmail = OBJECTS.SETTINGS.getvSTRING("CssMarkedEmail");

    // Constructor
    public WebMark(RTWidget rtWidget) {
        this.rtWidget = rtWidget;
    }

    // Public methods
    public void updateSettings(TextHandler.Behavior behavior) {
        allowMarkingWebLinks = USettings.getAppOrUserSettingsItem("AllowMarkingWebLinks", behavior).getValueBOOLEAN();
        allowMarkingEmails = USettings.getAppOrUserSettingsItem("AllowMarkingEmails", behavior).getValueBOOLEAN();
        cssMarkedWebLink = USettings.getAppOrUserSettingsItem("CssMarkedWebLink", behavior).getValueSTRING();
        cssMarkedEmail = USettings.getAppOrUserSettingsItem("CssMarkedEmail", behavior).getValueSTRING();
    }


    public List<StyleSheetChar> calculate(List<StyleSheetChar> cssChars, Task<Boolean> taskHandler) {
        this.cssChars = cssChars;
        foundItems = new ArrayList<>();

        String text = rtWidget.getText() + " ";

        if (allowMarkingWebLinks) {
            if (!addWebLinks(text, taskHandler)) { return null; }
        }
        
        if (allowMarkingEmails) {
            if (!addEmails(text, taskHandler)) { return null; }
        }

        cssChars = Marker.updateCssList(cssChars, foundItems);

        return cssChars;
    }

    public void mark() {
        if (cssChars == null || cssChars.size() != rtWidget.getText().length()) return;

        int index = 0;
        for (StyleSheetChar item : cssChars) {
            rtWidget.setStyle(index, index + 1, item.getCss());
            index++;
        }
    }

    // Private methods
    private boolean addWebLinks(String text, Task<Boolean> taskHandler) {
        Pattern linkPattern = Pattern.compile(
            "(?<!@)\\b(" +
                // With http/https or www
                "((https?://|www\\.)[\\w\\-]+(\\.[\\w\\-]+)+(:\\d+)?(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?)" +
                "|" +
                // Without http/https or www
                "([\\w\\-]+\\.[a-z]{2,6}(/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?)" +
            ")\\b",
            Pattern.CASE_INSENSITIVE
        );
                
        WordExtractor wordExtractor = new WordExtractor(text, linkPattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar webLinkCss = new StyleSheetChar(true);
        webLinkCss.setCss(cssMarkedWebLink);

        for (WordExtractor.WordItem word : words) {
            if (taskHandler.isCancelled()) {
                return false;
            }

            foundItems.add(new MarkedItem(word.index(), word.index() + word.word().length(), CONSTANTS.INVALID_ID, webLinkCss, MarkedItem.MarkedType.WEB_LINK));
        }

        return true;
    }

    private boolean addEmails(String text, Task<Boolean> taskHandler) {
        Pattern emailPattern = Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b");
        WordExtractor wordExtractor = new WordExtractor(text, emailPattern);

        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar eMailCss = new StyleSheetChar(true);
        eMailCss.setCss(cssMarkedEmail);

        for (WordExtractor.WordItem word : words) {
            if (taskHandler.isCancelled()) {
                return false;
            }

            foundItems.add(new MarkedItem(word.index(), word.index() + word.word().length(), CONSTANTS.INVALID_ID, eMailCss, MarkedItem.MarkedType.EMAIL));
        }

        return true;
    }


}
