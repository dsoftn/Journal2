package com.dsoftn.utils.html;

import java.util.List;

public class UHDocument {
    // Variables
    private String htmlCode = "";

    // Constructor
    public UHDocument(String htmlCode) {
        if (htmlCode == null) {
            htmlCode = "";
        }
        this.htmlCode = htmlCode;
    }

    // Methods
    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        if (htmlCode == null) {
            htmlCode = "";
        }
        this.htmlCode = htmlCode;
    }

    public List<UHTag> getTags() {
        return UHParser.getTags(new UHTag(this, "", "", 0, htmlCode.length(), 0, htmlCode.length(), null), null);
    }

    public List<UHTag> getTags(String tagName) {
        return UHParser.getTags(new UHTag(this, "", "", 0, htmlCode.length(), 0, htmlCode.length(), null), tagName);
    }

    public UHTag getFirstTag() {
        List<UHTag> result = getTags();
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public UHTag getFirstTag(String tagName) {
        List<UHTag> result = getTags(tagName);
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public List<UHTag> findTags(String tagName) {
        return UHParser.findTags(new UHTag(this, "", "", 0, htmlCode.length(), 0, htmlCode.length(), null), tagName);
    }

    public UHTag findFirstTag(String tagName) {
        List<UHTag> result = findTags(tagName);
        return result == null || result.isEmpty() ? null : result.get(0);
    }

}
