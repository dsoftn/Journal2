package com.dsoftn.utils;

public class UError {

    public static void exception(String messageExplanation, String messageString){
        System.out.println(messageExplanation + ": " + messageString);
    }

    public static void exception(String messageExplanation, Exception e){
        System.out.println(messageExplanation + ": " + e.getMessage());
    }

}
