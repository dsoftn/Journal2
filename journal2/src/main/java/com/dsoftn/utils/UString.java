package com.dsoftn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UString {

    /**
     * Count the number of occurrences of a substring in a string
     */
    public static int Count(String text, String searchString) {
        int count = 0;
        int pos = 0;
        while (true) {
            pos = text.indexOf(searchString, pos);
            if (pos >= 0) {
                count++;
                pos += searchString.length();
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Split a string by a delimiter and remove empty strings
     */
    public static List<String> splitAndStrip(String text, String delimiter) {
        String[] segments = text.split(Pattern.quote(delimiter), -1);
        List<String> parts = new ArrayList<>();
        for (String segment : segments) {
            if (segment.strip().isEmpty()) {
                continue;
            }
            parts.add(segment.strip());
        }
        return parts;
    }

    /**
     * Join a list of strings with a delimiter
     */
    public static String joinListOfString(List<String> list, String delimiter) {
        return String.join(delimiter, list.stream().map(String::strip).toList());
    }

    /**
     * Join a list of integers with a delimiter
     */
    public static String joinListOfInteger(List<Integer> list, String delimiter) {
        return String.join(delimiter, list.stream().map(String::valueOf).toList());
    }

    /**
     * Removes specified characters from the beginning and end of a string
     * @param text The text to process
     * @param characters String containing characters to remove
     * @return Processed string with specified characters removed from start and end
     */
    public static String stripCharacters(String text, String characters) {
        if (text == null || text.isEmpty() || characters == null || characters.isEmpty()) {
            return text;
        }
        
        int start = 0;
        int end = text.length() - 1;
        
        while (start <= end && characters.indexOf(text.charAt(start)) >= 0) {
            start++;
        }
        
        while (end >= start && characters.indexOf(text.charAt(end)) >= 0) {
            end--;
        }
        
        return text.substring(start, end + 1);
    }

    /**
     * Find the index of a character in a string
     * @param text The text to search
     * @param searchString The character to find
     * @param indexNumber The number of times to find the character
     * @return The index of the character if found, null if not found
     */
    public static Integer findIndexOfChar(String text, String searchString, int indexNumber) {
        int counter = 0;
        int fromIndex = 0;
        while (counter <= indexNumber) {
            counter++;
            int found = text.indexOf(searchString, fromIndex);
            if (found == -1) {
                return null;
            }
            fromIndex = found + 1;
        }
        return fromIndex;
    }

    /**
     * Find the common prefix of a list of strings
     * @param strings List of strings
     * @return Common prefix of the list of strings or empty string if there is no common prefix
     */
    public static String findCommonPrefix(List<String> strings) {
        if (strings == null || strings.isEmpty()) return "";
        if (strings.size() == 1) return strings.get(0);
    
        String prefix = strings.get(0);
        for (int i = 1; i < strings.size(); i++) {
            prefix = commonPrefix(prefix, strings.get(i));
            if (prefix.isEmpty()) break;
        }
    
        return !prefix.strip().isEmpty() ? prefix.strip() : "";
    }
    
    private static String commonPrefix(String s1, String s2) {
        int minLen = Math.min(s1.length(), s2.length());
        int i = 0;
        while (i < minLen && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }
        return s1.substring(0, i);
    }

}
