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
import com.dsoftn.utils.UString;

import javafx.concurrent.Task;

public class NumberDateTimeMarking {
    // Variables
    private RTWidget rtWidget = null;
    private List<StyleSheetChar> cssChars = new ArrayList<>();
    private List<MarkedItem> markedItems = new ArrayList<>();

    // Constructor
    public NumberDateTimeMarking(RTWidget rtWidget) {
        this.rtWidget = rtWidget;
    }

    // Public methods
    public List<StyleSheetChar> calculate(List<StyleSheetChar> cssChars, List<MarkedItem> markedNumDateTime, Task<Boolean> taskHandler) {
        this.cssChars = cssChars;
        markedItems = new ArrayList<>();

        for (MarkedItem item : markedNumDateTime) {
            if (taskHandler.isCancelled()) {
                return null;
            }

            if (item.markedType == MarkedItem.MarkedType.INTEGER && OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingIntegers")) {
                markedItems.add(item);
            }
            if (item.markedType == MarkedItem.MarkedType.DOUBLE && OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingDoubles")) {
                markedItems.add(item);
            }
            if (item.markedType == MarkedItem.MarkedType.DATE && OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingDates")) {
                markedItems.add(item);
            }
            if (item.markedType == MarkedItem.MarkedType.TIME && OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingTimes")) {
                markedItems.add(item);
            }
        }

        // Serbian mobile numbers
        if (OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingSerbianMobileNumbers")) {
            if (!addSerbianMobileNumbers(rtWidget.getText(), markedItems, taskHandler)) { return null; }
        }

        // Serbian landline numbers
        if (OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingSerbianLandlineNumbers")) {
            if (!addSerbianLandlineNumbers(rtWidget.getText(), markedItems, taskHandler)) { return null; }
        }

        // International phone numbers
        if (OBJECTS.SETTINGS.getvBOOLEAN("AllowMarkingInternationalPhoneNumbers")) {
            if (!addInternationalPhoneNumbers(rtWidget.getText(), markedItems, taskHandler)) { return null; }
        }

        cssChars = Marker.updateCssList(cssChars, markedItems);

        return cssChars;
    }

    public void mark() {
        if (cssChars == null) return;

        int index = 0;
        for (StyleSheetChar item : cssChars) {
            rtWidget.setStyle(index, index + 1, item.getCss());
            index++;
        }
    }

    // Private methods
    private boolean addSerbianMobileNumbers(String text, List<MarkedItem> markedItems, Task<Boolean> taskHandler) {
        Pattern serbianMobilePattern = Pattern.compile(
            "(?:\\+381|00381|\\(?(0))?\\s*\\(?6[0-6]\\)?[\\s/\\-.]*\\d{1,3}[\\s/\\-.]*\\d{2,3}[\\s/\\-.]*\\d{2,3}"
        );
                        
        WordExtractor wordExtractor = new WordExtractor(text, serbianMobilePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar serbianMobileCss = new StyleSheetChar(true);
        serbianMobileCss.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedSerbianMobileNumbers"));

        for (WordExtractor.WordItem word : words) {
            if (taskHandler.isCancelled()) {
                return false;
            }

            markedItems.add(new MarkedItem(word.index(), word.index() + word.word().length(), CONSTANTS.INVALID_ID, serbianMobileCss, MarkedItem.MarkedType.SERBIAN_MOBILE_NUMBER));
        }

        return true;
    }

    private boolean addSerbianLandlineNumbers(String text, List<MarkedItem> markedItems, Task<Boolean> taskHandler) {
        Pattern serbianLandlinePattern = Pattern.compile(
            "(?:\\+381|00381|\\(?(0))?\\s*\\(?[1-3][0-9]\\)?[\\s/\\-.]+\\d{1,4}(?:[\\s/\\-.]?\\d{1,3}){1,3}"
        );
                                
        WordExtractor wordExtractor = new WordExtractor(text, serbianLandlinePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar serbianLandlineCss = new StyleSheetChar(true);
        serbianLandlineCss.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedSerbianLandlineNumbers"));

        boolean hasItem = false;
        for (WordExtractor.WordItem word : words) {
            if (taskHandler.isCancelled()) {
                return false;
            }

            for (MarkedItem item : markedItems) {
                if (word.index() >= item.start && word.index() < item.end && item.markedType == MarkedItem.MarkedType.SERBIAN_MOBILE_NUMBER) {
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
            "(?:\\+|00)\\d{1,3}[\\s/\\-.]*\\(?\\d{1,4}\\)?(?:[\\s/\\-.]?\\d{1,4}){1,6}"
        );
                                        
        WordExtractor wordExtractor = new WordExtractor(text, internationalPhonePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar internationalPhoneCss = new StyleSheetChar(true);
        internationalPhoneCss.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedInternationalPhoneNumbers"));

        boolean hasItem = false;
        for (WordExtractor.WordItem word : words) {
            if (taskHandler.isCancelled()) {
                return false;
            }

            if (word.word().startsWith("+381") || word.word().startsWith("00381")) {
                continue;
            }

            for (MarkedItem item : markedItems) {
                if (word.index() >= item.start && word.index() < item.end && (item.markedType == MarkedItem.MarkedType.SERBIAN_MOBILE_NUMBER || item.markedType == MarkedItem.MarkedType.SERBIAN_LANDLINE_NUMBER)) {
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
    public static List<MarkedItem> getNumbersDatesTimes(String text, Task<Boolean> taskHandler) {
        final String numbers = "0123456789";
        final String allowed = numbers + ".,:";

        StyleSheetChar cssDate = new StyleSheetChar(true);
        cssDate.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedDate"));
        StyleSheetChar cssTime = new StyleSheetChar(true);
        cssTime.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedTime"));
        StyleSheetChar cssInteger = new StyleSheetChar(true);
        cssInteger.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedInteger"));
        StyleSheetChar cssDouble = new StyleSheetChar(true);
        cssDouble.setCss(OBJECTS.SETTINGS.getvSTRING("CssMarkedDouble"));

        List<MarkedItem> result = new ArrayList<>();
        text += " ";
        boolean foundItem = false;
        boolean processItem = false;
        String item = "";
        int start = 0;

        for (int i = 0; i < text.length(); i++) {
            if (taskHandler.isCancelled()) {
                return null;
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
            if (UDate.isStringValidTime(item)) {
                result.add(new MarkedItem(start, start + item.length(), CONSTANTS.INVALID_ID, cssTime, MarkedItem.MarkedType.TIME));
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
