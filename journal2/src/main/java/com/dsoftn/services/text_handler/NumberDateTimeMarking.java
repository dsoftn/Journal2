package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.services.RTWidget;
import com.dsoftn.services.WordExtractor;
import com.dsoftn.utils.UDate;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.utils.USettings;
import com.dsoftn.utils.UString;

import javafx.concurrent.Task;

public class NumberDateTimeMarking {
    // Variables
    private RTWidget rtWidget = null;
    private List<StyleSheetChar> cssChars = new ArrayList<>();
    private List<MarkedItem> markedItems = new ArrayList<>();

    private boolean allowMarkingIntegers = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingIntegers");
    private boolean allowMarkingDoubles = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingDoubles");
    private boolean allowMarkingDates = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingDates");
    private boolean allowMarkingTimes = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingTimes");
    private boolean allowMarkingSerbianMobileNumbers = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingSerbianMobileNumbers");
    private String cssMarkedSerbianMobileNumbers = OBJECTS.SETTINGS.getvSTRING("CssMarkedSerbianMobileNumbers");
    private boolean allowMarkingSerbianLandlineNumbers = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingSerbianLandlineNumbers");
    private String cssMarkedSerbianLandlineNumbers = OBJECTS.SETTINGS.getvSTRING("CssMarkedSerbianLandlineNumbers");
    private boolean allowMarkingInternationalPhoneNumbers = OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingInternationalPhoneNumbers");
    private String cssMarkedInternationalPhoneNumbers = OBJECTS.SETTINGS.getvSTRING("CssMarkedInternationalPhoneNumbers");

    // Constructor
    public NumberDateTimeMarking(RTWidget rtWidget) {
        this.rtWidget = rtWidget;
    }

    // Public methods
    public void updateSettings(TextHandler.Behavior behavior) {
        allowMarkingIntegers = USettings.getAppOrUserSettingsItem("AllowMarkingIntegers", behavior).getValueBOOLEAN();
        allowMarkingDoubles = USettings.getAppOrUserSettingsItem("AllowMarkingDoubles", behavior).getValueBOOLEAN();
        allowMarkingDates = USettings.getAppOrUserSettingsItem("AllowMarkingDates", behavior).getValueBOOLEAN();
        allowMarkingTimes = USettings.getAppOrUserSettingsItem("AllowMarkingTimes", behavior).getValueBOOLEAN();
        allowMarkingSerbianMobileNumbers = USettings.getAppOrUserSettingsItem("AllowMarkingSerbianMobileNumbers", behavior).getValueBOOLEAN();
        cssMarkedSerbianMobileNumbers = USettings.getAppOrUserSettingsItem("CssMarkedSerbianMobileNumbers", behavior).getValueSTRING();
        allowMarkingSerbianLandlineNumbers = USettings.getAppOrUserSettingsItem("AllowMarkingSerbianLandlineNumbers", behavior).getValueBOOLEAN();
        cssMarkedSerbianLandlineNumbers = USettings.getAppOrUserSettingsItem("CssMarkedSerbianLandlineNumbers", behavior).getValueSTRING();
        allowMarkingInternationalPhoneNumbers = USettings.getAppOrUserSettingsItem("AllowMarkingInternationalPhoneNumbers", behavior).getValueBOOLEAN();
        cssMarkedInternationalPhoneNumbers = USettings.getAppOrUserSettingsItem("CssMarkedInternationalPhoneNumbers", behavior).getValueSTRING();
    }

    public List<StyleSheetChar> calculate(List<StyleSheetChar> cssChars, List<MarkedItem> markedNumDateTime, Task<Boolean> taskHandler) {
        this.cssChars = cssChars;
        markedItems = new ArrayList<>();

        for (MarkedItem item : markedNumDateTime) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return null;
            }

            if (item.markedType == MarkedItem.MarkedType.INTEGER && allowMarkingIntegers) {
                markedItems.add(item);
            }
            if (item.markedType == MarkedItem.MarkedType.DOUBLE && allowMarkingDoubles) {
                markedItems.add(item);
            }
            if (item.markedType == MarkedItem.MarkedType.DATE && allowMarkingDates) {
                markedItems.add(item);
            }
            if (item.markedType == MarkedItem.MarkedType.TIME && allowMarkingTimes) {
                markedItems.add(item);
            }
        }

        // Serbian mobile numbers
        if (allowMarkingSerbianMobileNumbers) {
            if (!addSerbianMobileNumbers(rtWidget.getText(), markedItems, taskHandler)) { return null; }
        }

        // Serbian landline numbers
        if (allowMarkingSerbianLandlineNumbers) {
            if (!addSerbianLandlineNumbers(rtWidget.getText(), markedItems, taskHandler)) { return null; }
        }

        // International phone numbers
        if (allowMarkingInternationalPhoneNumbers) {
            if (!addInternationalPhoneNumbers(rtWidget.getText(), markedItems, taskHandler)) { return null; }
        }

        cssChars = Marker.updateCssList(cssChars, markedItems);

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
    private boolean addSerbianMobileNumbers(String text, List<MarkedItem> markedItems, Task<Boolean> taskHandler) {
        Pattern serbianMobilePattern = Pattern.compile(
            "(?:\\+381|00381|\\(0?6[0-6]\\)|0?6[0-6])(?:[\\s/\\-.]*\\d{1,3}){1,4}"
        );
                                
        WordExtractor wordExtractor = new WordExtractor(text, serbianMobilePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar serbianMobileCss = new StyleSheetChar(true);
        serbianMobileCss.setCss(cssMarkedSerbianMobileNumbers);

        boolean hasItem = false;
        for (WordExtractor.WordItem word : words) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return false;
            }

            if (word.word().length() < 10 || !(word.word().startsWith("0") || word.word().startsWith("+")) || word.word().startsWith("000")) {
                continue;
            }

            boolean hasSeparator = false;
            for (String sep : List.of(" ", "-", "/", ".")) {
                if (word.word().indexOf(sep) > 0 && word.word().indexOf(sep) < word.word().length() - 1) {
                    hasSeparator = true;
                    break;
                }
            }
            if (!hasSeparator) {
                continue;
            }

            for (MarkedItem item : markedItems) {
                if (word.index() >= item.start && word.index() < item.end && item.markedType == MarkedItem.MarkedType.DATE) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem) {
                hasItem = false;
                continue;
            }

            markedItems.add(new MarkedItem(word.index(), word.index() + word.word().length(), CONSTANTS.INVALID_ID, serbianMobileCss, MarkedItem.MarkedType.SERBIAN_MOBILE_NUMBER));
        }

        return true;
    }

    private boolean addSerbianLandlineNumbers(String text, List<MarkedItem> markedItems, Task<Boolean> taskHandler) {
        Pattern serbianLandlinePattern = Pattern.compile(
            "(?:\\+381|00381|\\(0?[1-3][0-9]\\)|0?[1-3][0-9])(?:[\\s/\\-.]*\\d{1,4}){1,4}"
        );
        
        WordExtractor wordExtractor = new WordExtractor(text, serbianLandlinePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar serbianLandlineCss = new StyleSheetChar(true);
        serbianLandlineCss.setCss(cssMarkedSerbianLandlineNumbers);

        boolean hasItem = false;
        for (WordExtractor.WordItem word : words) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return false;
            }

            if (word.word().length() < 10 || !(word.word().startsWith("0") || word.word().startsWith("+")) || word.word().startsWith("000")) {
                continue;
            }

            boolean hasSeparator = false;
            for (String sep : List.of(" ", "-", "/", ".")) {
                if (word.word().indexOf(sep) > 0 && word.word().indexOf(sep) < word.word().length() - 1) {
                    hasSeparator = true;
                    break;
                }
            }
            if (!hasSeparator) {
                continue;
            }

            for (MarkedItem item : markedItems) {
                if (word.index() >= item.start && word.index() < item.end && (item.markedType == MarkedItem.MarkedType.SERBIAN_MOBILE_NUMBER || item.markedType == MarkedItem.MarkedType.DATE)) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem) {
                hasItem = false;
                continue;
            }

            markedItems.add(new MarkedItem(word.index(), word.index() + word.word().length(), CONSTANTS.INVALID_ID, serbianLandlineCss, MarkedItem.MarkedType.SERBIAN_LANDLINE_NUMBER));
        }

        return true;
    }

    private boolean addInternationalPhoneNumbers(String text, List<MarkedItem> markedItems, Task<Boolean> taskHandler) {
        Pattern internationalPhonePattern = Pattern.compile(
            "(?:\\+|00)\\d{1,3}[\\s/\\-.]*\\(?\\d{1,4}\\)?(?:[\\s/\\-.]*\\d{1,4}){1,6}"
        );
                                        
        WordExtractor wordExtractor = new WordExtractor(text, internationalPhonePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar internationalPhoneCss = new StyleSheetChar(true);
        internationalPhoneCss.setCss(cssMarkedInternationalPhoneNumbers);

        boolean hasItem = false;
        for (WordExtractor.WordItem word : words) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return false;
            }

            if (word.word().length() < 10 || !(word.word().startsWith("0") || word.word().startsWith("+")) || word.word().startsWith("000")) {
                continue;
            }

            boolean hasSeparator = false;
            for (String sep : List.of(" ", "-", "/", ".")) {
                if (word.word().indexOf(sep) > 0 && word.word().indexOf(sep) < word.word().length() - 1) {
                    hasSeparator = true;
                    break;
                }
            }
            if (!hasSeparator) {
                continue;
            }

            if (word.word().startsWith("+381") || word.word().startsWith("00381")) {
                continue;
            }

            for (MarkedItem item : markedItems) {
                if (word.index() >= item.start && word.index() < item.end && (item.markedType == MarkedItem.MarkedType.SERBIAN_MOBILE_NUMBER || item.markedType == MarkedItem.MarkedType.SERBIAN_LANDLINE_NUMBER || item.markedType == MarkedItem.MarkedType.DATE)) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem) {
                hasItem = false;
                continue;
            }

            markedItems.add(new MarkedItem(word.index(), word.index() + word.word().length(), CONSTANTS.INVALID_ID, internationalPhoneCss, MarkedItem.MarkedType.INTERNATIONAL_PHONE_NUMBER));
        }

        return true;
    }

    // Static methods
    public static List<MarkedItem> getNumbersDatesTimes(String text, Task<Boolean> taskHandler, TextHandler.Behavior behavior) {
        final String numbers = "0123456789";
        final String allowed = numbers + ".,:";

        StyleSheetChar cssDate = new StyleSheetChar(true);
        cssDate.setCss(USettings.getAppOrUserSettingsItem("CssMarkedDate", behavior).getValueSTRING());
        StyleSheetChar cssTime = new StyleSheetChar(true);
        cssTime.setCss(USettings.getAppOrUserSettingsItem("CssMarkedTime", behavior).getValueSTRING());
        StyleSheetChar cssInteger = new StyleSheetChar(true);
        cssInteger.setCss(USettings.getAppOrUserSettingsItem("CssMarkedInteger", behavior).getValueSTRING());
        StyleSheetChar cssDouble = new StyleSheetChar(true);
        cssDouble.setCss(USettings.getAppOrUserSettingsItem("CssMarkedDouble", behavior).getValueSTRING());

        List<MarkedItem> result = new ArrayList<>();
        text += " ";
        boolean foundItem = false;
        boolean processItem = false;
        String item = "";
        int start = 0;

        for (int i = 0; i < text.length(); i++) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return null;
            }

            if (text.charAt(i) == '-' && i < (text.length() - 1) && numbers.indexOf(text.charAt(i + 1)) != -1) {
                item += text.charAt(i);
                if (!foundItem) {
                    start = i;
                    foundItem = true;
                }
                continue;
            }

            if (numbers.indexOf(text.charAt(i)) != -1) {
                item += text.charAt(i);
                if (!foundItem) {
                    start = i;
                    foundItem = true;
                }
                continue;
            }
            if (allowed.indexOf(text.charAt(i)) != -1) {
                if (!foundItem) {
                    continue;
                }
                item += text.charAt(i);
                continue;
            }

            if (foundItem) {
                processItem = true;
            }

            if (!foundItem || !processItem) {
                continue;
            }

            // At this point we have item that needs to be processed
            
            foundItem = false;
            processItem = false;
            item = UString.stripCharacters(item, ":,");

            // Check is item date
            if (UDate.isStringValidDate(item)) {
                result.add(new MarkedItem(start, start + item.length(), CONSTANTS.INVALID_ID, cssDate, MarkedItem.MarkedType.DATE));
                item = "";
                continue;
            }
            // Check is item time
            String timeFixed = UString.stripCharacters(item, ".");
            if (UDate.isStringValidTime(timeFixed)) {
                result.add(new MarkedItem(start, start + timeFixed.length(), CONSTANTS.INVALID_ID, cssTime, MarkedItem.MarkedType.TIME));
                item = "";
                continue;
            }
            // Check is item integer
            if (UNumbers.isStringIntegerRemoveComa(item)) {
                result.add(new MarkedItem(start, start + item.length(), CONSTANTS.INVALID_ID, cssInteger, MarkedItem.MarkedType.INTEGER));
                item = "";
                continue;
            }
            // Check is item double
            if (UNumbers.isStringDoubleRemoveComa(item)) {
                result.add(new MarkedItem(start, start + item.length(), CONSTANTS.INVALID_ID, cssDouble, MarkedItem.MarkedType.DOUBLE));
                item = "";
                continue;
            }

            item += " ";
            String subItem = "";
            int startSub = start;
            for (int j = 0; j < item.length(); j++) {
                if (numbers.indexOf(item.charAt(j)) != -1) {
                    if (!foundItem) {
                        startSub = start + j;
                        foundItem = true;
                    }
                    subItem += item.charAt(j);
                    continue;
                }

                if (foundItem) {
                    foundItem = false;
                    result.add(new MarkedItem(startSub, startSub + subItem.length(), CONSTANTS.INVALID_ID, cssInteger, MarkedItem.MarkedType.INTEGER));
                    subItem = "";
                }
            }
            item = "";
            foundItem = false;
        }

        return result;
    }


}
