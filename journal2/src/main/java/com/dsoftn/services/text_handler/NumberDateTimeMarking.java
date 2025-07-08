package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.dsoftn.OBJECTS;
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
    private List<StyledString> markedItems = new ArrayList<>();

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

    public boolean calculate(Task<Boolean> taskHandler) {
        List<StyledString> markedNumDateTime = getNumbersDatesTimes(rtWidget.getText(), taskHandler, rtWidget.getTextHandler().getBehavior());
        if (markedNumDateTime == null) { return false; }

        markedItems = new ArrayList<>();

        for (StyledString item : markedNumDateTime) {
            if (taskHandler == null || taskHandler.isCancelled()) {
                return false;
            }

            if (item.getStyleTypeEnum() == StyledString.StyleType.INTEGER && allowMarkingIntegers) {
                markedItems.add(item);
            }
            if (item.getStyleTypeEnum() == StyledString.StyleType.DOUBLE && allowMarkingDoubles) {
                markedItems.add(item);
            }
            if (item.getStyleTypeEnum() == StyledString.StyleType.DATE && allowMarkingDates) {
                markedItems.add(item);
            }
            if (item.getStyleTypeEnum() == StyledString.StyleType.TIME && allowMarkingTimes) {
                markedItems.add(item);
            }
        }

        // Serbian mobile numbers
        if (allowMarkingSerbianMobileNumbers) {
            if (!addSerbianMobileNumbers(rtWidget.getText(), markedItems, taskHandler)) { return false; }
        }

        // Serbian landline numbers
        if (allowMarkingSerbianLandlineNumbers) {
            if (!addSerbianLandlineNumbers(rtWidget.getText(), markedItems, taskHandler)) { return false; }
        }

        // International phone numbers
        if (allowMarkingInternationalPhoneNumbers) {
            if (!addInternationalPhoneNumbers(rtWidget.getText(), markedItems, taskHandler)) { return false; }
        }

        return true;
    }

    public boolean mergeWithRTWidgetStyles(List<StyledString> lastRTWidgetStyledStrings, Task<Boolean> taskHandler) {
        markedItems = Marker.mergeStyles(markedItems, lastRTWidgetStyledStrings);
        if (markedItems == null) { return false; }
        if (taskHandler == null || taskHandler.isCancelled()) {
            return false;
        }

        return true;
    }

    public void mark() {
        if (markedItems == null) return;

        for (StyledString item : markedItems) {
            rtWidget.setStyle(item.getStart(), item.getEnd(), item.getCssCharStyle());
        }
    }

    // Private methods
    private boolean addSerbianMobileNumbers(String text, List<StyledString> markedItems, Task<Boolean> taskHandler) {
        Pattern serbianMobilePattern = Pattern.compile(
            "(?:\\+381|00381|\\(0?6[0-6]\\)|0?6[0-6])(?:[\\s/\\-.]*\\d{1,3}){1,4}"
        );
                                
        WordExtractor wordExtractor = new WordExtractor(text, serbianMobilePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar serbianMobileCss = new StyleSheetChar();
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

            for (StyledString item : markedItems) {
                if (word.index() >= item.getStart() && word.index() < item.getEnd() && item.getStyleTypeEnum() == StyledString.StyleType.DATE) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem) {
                hasItem = false;
                continue;
            }

            markedItems.add(new StyledString(word.index(), word.index() + word.word().length(), word.word(), serbianMobileCss, StyledString.StyleType.SERBIAN_MOBILE_NUMBER));
        }

        return true;
    }

    private boolean addSerbianLandlineNumbers(String text, List<StyledString> markedItems, Task<Boolean> taskHandler) {
        Pattern serbianLandlinePattern = Pattern.compile(
            "(?:\\+381|00381|\\(0?[1-3][0-9]\\)|0?[1-3][0-9])(?:[\\s/\\-.]*\\d{1,4}){1,4}"
        );
        
        WordExtractor wordExtractor = new WordExtractor(text, serbianLandlinePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar serbianLandlineCss = new StyleSheetChar();
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

            for (StyledString item : markedItems) {
                if (word.index() >= item.getStart() && word.index() < item.getEnd() && (item.getStyleTypeEnum() == StyledString.StyleType.SERBIAN_MOBILE_NUMBER || item.getStyleTypeEnum() == StyledString.StyleType.DATE)) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem) {
                hasItem = false;
                continue;
            }

            markedItems.add(new StyledString(word.index(), word.index() + word.word().length(), word.word(), serbianLandlineCss, StyledString.StyleType.SERBIAN_LANDLINE_NUMBER));
        }

        return true;
    }

    private boolean addInternationalPhoneNumbers(String text, List<StyledString> markedItems, Task<Boolean> taskHandler) {
        Pattern internationalPhonePattern = Pattern.compile(
            "(?:\\+|00)\\d{1,3}[\\s/\\-.]*\\(?\\d{1,4}\\)?(?:[\\s/\\-.]*\\d{1,4}){1,6}"
        );
                                        
        WordExtractor wordExtractor = new WordExtractor(text, internationalPhonePattern);;
        List<WordExtractor.WordItem> words = wordExtractor.getWordItems();

        StyleSheetChar internationalPhoneCss = new StyleSheetChar();
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

            for (StyledString item : markedItems) {
                if (word.index() >= item.getStart() && word.index() < item.getEnd() && (item.getStyleTypeEnum() == StyledString.StyleType.SERBIAN_MOBILE_NUMBER || item.getStyleTypeEnum() == StyledString.StyleType.SERBIAN_LANDLINE_NUMBER || item.getStyleTypeEnum() == StyledString.StyleType.DATE)) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem) {
                hasItem = false;
                continue;
            }

            markedItems.add(new StyledString(word.index(), word.index() + word.word().length(), word.word(), internationalPhoneCss, StyledString.StyleType.INTERNATIONAL_PHONE_NUMBER));
        }

        return true;
    }

    public List<StyledString> getNumbersDatesTimes(String text, Task<Boolean> taskHandler, TextHandler.Behavior behavior) {
        final String numbers = "0123456789";
        final String allowed = numbers + ".,:";

        StyleSheetChar cssDate = new StyleSheetChar();
        cssDate.setCss(USettings.getAppOrUserSettingsItem("CssMarkedDate", behavior).getValueSTRING());
        StyleSheetChar cssTime = new StyleSheetChar();
        cssTime.setCss(USettings.getAppOrUserSettingsItem("CssMarkedTime", behavior).getValueSTRING());
        StyleSheetChar cssInteger = new StyleSheetChar();
        cssInteger.setCss(USettings.getAppOrUserSettingsItem("CssMarkedInteger", behavior).getValueSTRING());
        StyleSheetChar cssDouble = new StyleSheetChar();
        cssDouble.setCss(USettings.getAppOrUserSettingsItem("CssMarkedDouble", behavior).getValueSTRING());

        List<StyledString> result = new ArrayList<>();
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
                result.add(new StyledString(start, start + item.length(), item, cssDate, StyledString.StyleType.DATE));
                item = "";
                continue;
            }
            // Check is item time
            String timeFixed = UString.stripCharacters(item, ".");
            if (UDate.isStringValidTime(timeFixed)) {
                result.add(new StyledString(start, start + timeFixed.length(), item, cssTime, StyledString.StyleType.TIME));
                item = "";
                continue;
            }
            // Check is item integer
            if (UNumbers.isStringIntegerRemoveComa(item)) {
                result.add(new StyledString(start, start + item.length(), item, cssInteger, StyledString.StyleType.INTEGER));
                item = "";
                continue;
            }
            // Check is item double
            if (UNumbers.isStringDoubleRemoveComa(item)) {
                result.add(new StyledString(start, start + item.length(), item, cssDouble, StyledString.StyleType.DOUBLE));
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
                    result.add(new StyledString(startSub, startSub + subItem.length(), item, cssInteger, StyledString.StyleType.INTEGER));
                    subItem = "";
                }
            }
            item = "";
            foundItem = false;
        }

        return result;
    }


}
