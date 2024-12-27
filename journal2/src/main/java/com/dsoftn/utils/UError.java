package com.dsoftn.utils;

public class UError {

    public static void exception(String messageExplanation, String messageString){
        System.out.println(messageExplanation + ": " + messageString);
    }

    public static void exception(String messageExplanation, Exception e){
        System.out.println(messageExplanation + ": " + e.getMessage());
    }

    public static void error(String messageExplanation, String messageString){
        System.err.println(messageExplanation + ": " + messageString);
    }

    public static void warning(String messageExplanation, String messageString){
        System.out.println(messageExplanation + ": " + messageString);
    }

    public static void info(String messageExplanation, String messageString){
        System.out.println(messageExplanation + ": " + messageString);
    }

}
