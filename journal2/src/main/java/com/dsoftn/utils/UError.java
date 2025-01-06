package com.dsoftn.utils;

public class UError {

    public static void exception(String messageExplanation, String... messageStrings){
        String header = messageExplanation;
        String content = "";
        for (String messageString : messageStrings) {
            content += messageString + "\n";
        }
        System.out.println(header + "\n" + content);
    }

    public static void exception(String messageExplanation, Exception e, String... messageStrings){
        String header = messageExplanation;
        String content = e.getMessage() + "\n";
        for (String messageString : messageStrings) {
            content += messageString + "\n";
        }
        e.printStackTrace();
        System.out.println(header + "\n" + content);
    }

    public static void error(String messageExplanation, String... messageStrings){
        String header = messageExplanation;
        String content = "";
        for (String messageString : messageStrings) {
            content += messageString + "\n";
        }
        System.out.println(header + "\n" + content);
    }

    public static void warning(String messageExplanation, String... messageStrings){
        String header = messageExplanation;
        String content = "";
        for (String messageString : messageStrings) {
            content += messageString + "\n";
        }
        System.out.println(header + "\n" + content);
    }

    public static void info(String messageExplanation, String... messageStrings){
        String header = messageExplanation;
        String content = "";
        for (String messageString : messageStrings) {
            content += messageString + "\n";
        }
        System.out.println(header + "\n" + content);
    }

}
