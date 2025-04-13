package com.dsoftn;

import java.time.format.DateTimeFormatter;

import com.dsoftn.utils.UFile;

public class CONSTANTS {

    // Global
    public static final String APPLICATION_NAME = "My Journal 2";
    public static final String FOLDER_DATA = "data";
    public static final String FOLDER_DATA_APP = UFile.concatPaths("data", "app");
    public static final String FOLDER_DATA_APP_SETTINGS = UFile.concatPaths("data", "app", "settings");
    public static final String SETTINGS_FILE_PATH = UFile.concatPaths("data", "app", "settings", "settings.json");
    public static final String LANGUAGES_FILE_PATH = UFile.concatPaths("data", "app", "settings", "languages.json");
    public static final String FOLDER_DATA_USERS = UFile.concatPaths("data", "users");
    public static final String USER_ATTACHMENT_FOLDER_NAME = "attachments";
    public static final String USER_AVATAR_FILE_NAME_WITHOUT_EXTENSION = "avatar";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_FOR_JSON = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER_FOR_JSON = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static final int INVALID_ID = -1;
    public static final int INVALID_SIZE = -1;
    public static final String INVALID_DATETIME_STRING = "?";
    public static final String DEFAULT_SETTINGS_LANGUAGE_CODE = "en";
    public static final String DATA_STRUCTURE_CORRUPTED_MESSAGE_STRING = "Solution: NONE";
    public static final int DIALOG_MARGIN = 5;
    public static final String EMPTY_PARAGRAPH_STRING = "\u200B"; // Zero-width space at beginning of each paragraph
    public static final char EMPTY_PARAGRAPH_CHAR = '\u200B'; // Zero-width space at beginning of each paragraph
    public static final String RTW_TEXT_HEADER = EMPTY_PARAGRAPH_STRING.repeat(3) + "RTW"; // RTW = Rich Text Widget header
    public static final String RTW_TEXT_CHAR_STYLE_START = EMPTY_PARAGRAPH_STRING.repeat(3) + "C"; // RTW = Rich Text Widget characters stylesheets - section start
    public static final String RTW_TEXT_CHAR_STYLE_END = EMPTY_PARAGRAPH_STRING.repeat(3) + "-C"; // RTW = Rich Text Widget characters stylesheets - section end
    public static final String RTW_TEXT_PARAGRAPH_STYLE_START = EMPTY_PARAGRAPH_STRING.repeat(3) + "P"; // RTW = Rich Text Widget paragraphs stylesheets - section start
    public static final String RTW_TEXT_PARAGRAPH_STYLE_END = EMPTY_PARAGRAPH_STRING.repeat(3) + "-P"; // RTW = Rich Text Widget paragraphs stylesheets - section end
    public static final String RTW_TEXT_CONTENT = EMPTY_PARAGRAPH_STRING.repeat(3) + "T"; // RTW = Rich Text Widget content - section start, this is last section and it does not have end section
}
