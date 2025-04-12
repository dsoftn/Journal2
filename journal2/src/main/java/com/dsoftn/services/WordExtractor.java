package com.dsoftn.services;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dsoftn.CONSTANTS;

public class WordExtractor {

    public record WordItem(String word, int index) {}

    // Variables

    private String text = "";
    private Pattern pattern = null;

    // Constructors

    public WordExtractor(String text) {
        this.pattern = Pattern.compile("[^\\s~!@#$%^&*()_+=`{}|\\[\\]\\\\:\";'<>?,./-" + CONSTANTS.EMPTY_PARAGRAPH_STRING + "]+");
        this.text = text;
    }

    public WordExtractor() {
        this.pattern = Pattern.compile("[^\\s~!@#$%^&*()_+=`{}|\\[\\]\\\\:\";'<>?,./-" + CONSTANTS.EMPTY_PARAGRAPH_STRING + "]+");
    }

    // Getters and setters

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    // Public Methods

    public List<WordItem> getWords(String text) {
        Matcher matcher = pattern.matcher(text);

        List<WordItem> words = new ArrayList<>();
        
        while (matcher.find()) {
            words.add(new WordItem(matcher.group(), matcher.start()));
        }

        return words;
    }

    public List<WordItem> getWords() { return getWords(this.text); }

    public int countWords(String text) { return getWords().size(); }

    public int countWords() { return countWords(this.text); }


}
