package com.dsoftn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UString {

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



}
