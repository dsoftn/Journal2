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

}
