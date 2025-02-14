package com.dsoftn.services;

public class TextFilter {
    // Variables
    private String text = "";
    private String filterText = "";
    private boolean matchCase = false;
    private boolean ignoreSerbianCharacters = false;

    // Constructor
    public TextFilter() {}

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getFilterText() { return filterText; }
    public void setFilterText(String filterText) { this.filterText = filterText; }

    public boolean isMatchCase() { return matchCase; }
    public void setMatchCase(boolean matchCase) { this.matchCase = matchCase; }

    public boolean isIgnoreSerbianCharacters() { return ignoreSerbianCharacters; }
    public void setIgnoreSerbianCharacters(boolean ignoreSerbianCharacters) { this.ignoreSerbianCharacters = ignoreSerbianCharacters; }

    // Public Methods
    public Boolean isValid(String text, String filterText, boolean matchCase, boolean ignoreSerbianCharacters) {
        return getFilterResult(text, filterText, matchCase, ignoreSerbianCharacters);
    }

    public Boolean isValid(String text, String filterText) {
        return isValid(text, filterText, matchCase, ignoreSerbianCharacters);
    }

    public Boolean isValid(String text) {
        return isValid(text, filterText, matchCase, ignoreSerbianCharacters);
    }

    public Boolean isValid() { return isValid(text, filterText, matchCase, ignoreSerbianCharacters); }

    // Private Methods
    private Boolean getFilterResult(String text, String filterText, boolean matchCase, boolean ignoreSerbianCharacters) {
        // TODO apply filter
        return text.contains(filterText);
    }






}
