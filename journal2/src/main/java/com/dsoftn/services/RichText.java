package com.dsoftn.services;

import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;


public class RichText {
    // Variables
    public RichTextRule generalRule = new RichTextRule();
    private List<RichTextRule> rules = new ArrayList<>();

    // Constructors
    
    public RichText() {}

    public RichText(RichTextRule generalRule) {
        this.generalRule = generalRule;
    }

    // Methods
    
    public void addRule(RichTextRule rule) {
        rules.add(rule);
    }

    public List<RichTextRule> getRules() {
        return rules;
    }

    public void setCss(String css) {
        this.generalRule.setCss(css);
    }

    public void addCss(String css) {
        this.generalRule.addCss(css);
    }

    public void clearRules() {
        rules.clear();
    }

    public void setText(String text) {
        this.generalRule.setText(text);
        this.generalRule.setReplacement(null);
    }

    public TextFlow getTextFlow() {
        // Normalize css in rules
        for (int i = 0; i < rules.size(); i++) {
            RichTextRule rule = rules.get(i);
            rule.setCss(generalRule.getCss() + rule.getCss());
        }

        List <RichTextRule> textList = new ArrayList<>();
        String text = this.generalRule.getText();
        
        int pos = 0;
        while (!text.isEmpty()) {
            // Find smallest start index for all rules
            int minIndex = Integer.MAX_VALUE;
            RichTextRule minIndexRule = null;
            
            for (int i = 0; i < rules.size(); i++) {
                RichTextRule rule = rules.get(i);
                int index = text.indexOf(rule.getText(), pos);
                if (index != -1) {
                    if (index < minIndex) {
                        minIndex = index;
                        minIndexRule = rule;
                    }
                }
            }

            // If no rule was found, add the rest of the text to textList
            if (minIndex == Integer.MAX_VALUE) {
                textList.add(new RichTextRule(text.substring(pos), null, generalRule.getCss()));
                break;
            }

            // If text from beginning to minIndex is not empty, add it to textList
            if (minIndex != 0) {
                textList.add(new RichTextRule(text.substring(0, minIndex), null, generalRule.getCss()));
            }

            // Add minIndexRule to textList
            textList.add(minIndexRule);

            // Update text and position
            text = text.substring(minIndex + minIndexRule.getText().length());
        }

        // Create TextFlow
        TextFlow textFlow = new TextFlow();
        for (RichTextRule rule : textList) {
            textFlow.getChildren().add(rule.getTextObject());
        }

        return textFlow;
    }


}
