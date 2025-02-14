package com.dsoftn.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UList {

    /**
     * Check if two lists contain the same elements not necessarily in the same order
     * @param list1 - First list
     * @param list2 - Second list
     * @return <b>boolean</b> <i>true</i> if lists contain the same elements, <i>false</i> otherwise
     */
    public static boolean hasSameElements(List<Integer> list1, List<Integer> list2) {
        if (list1.size() != list2.size()) { return false; }

        Map<Integer, Integer> freqMap = new HashMap<>();
        
        for (int num : list1) {
            freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
        }

        for (int num : list2) {
            if (!freqMap.containsKey(num) || freqMap.get(num) == 0) { return false; }
            freqMap.put(num, freqMap.get(num) - 1);
        }

        return true;
    }

    public static List<Integer> deepCopy(List<Integer> list) {
        return new ArrayList<>(list);
    }

    public static List<Integer> listStringToInteger(List<String> list) {
        List<Integer> result = new ArrayList<>();
        for (String str : list) {
            if (!UNumbers.isStringInteger(str)) {
                UError.error("UList.listStringToInteger: String is not a valid integer: " + str, "Application will exit.");
                System.exit(1);
            }
            result.add(Integer.parseInt(str));
        }
        return result;
    }
}
