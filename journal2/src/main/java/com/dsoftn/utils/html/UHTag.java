package com.dsoftn.utils.html;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dsoftn.utils.UString;

public class UHTag {
    // Variables
    private UHDocument document = null;
    private String tagName = null;
    private String tagHeader = null;
    private boolean hasCloseTag = false;
    private int start = -1;
    private int end = -1;
    private int contentStart = -1;
    private int contentEnd = -1;
    private Map<String, String> attributes = null;

    private UHTag parent = null;

    // Constructor
    public UHTag(UHDocument document, String tagName, String tagHeader, int start, int end, int contentStart, int contentEnd, UHTag parent) {
        this.document = document;
        this.tagName = tagName;
        this.tagHeader = tagHeader;
        this.contentStart = contentStart;
        this.contentEnd = contentEnd;
        this.hasCloseTag = !UHParser.HAS_NO_CLOSE_TAG.contains(tagName) && !tagName.startsWith("!");
        this.start = start;
        this.end = end;
        if (parent == null || parent.getTagName().isEmpty()) {
            this.parent = null;
        } else {
            this.parent = parent;
        }
    }

    // Public Methods

    public UHTag getParent() {
        if (parent == null) {
            return UHParser.getParentTag(this);
        }
        return parent;
    }

    public void setParent(UHTag parent) {
        this.parent = parent;
    }

    public List<UHTag> getChildren() {
        List<UHTag> result = UHParser.getTags(this, null);
        if (result == null) return null;

        return result;
    }

    public UHTag getFirstChild() {
        List<UHTag> result = getChildren();
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public List<UHTag> getChildren(String tagName) {
        List<UHTag> result = UHParser.getTags(this, tagName);
        if (result == null) return null;
        
        return result;
    }

    public UHTag getFirstChild(String tagName) {
        List<UHTag> result = getChildren(tagName);
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public List<UHTag> findTags(String tagName) {
        return UHParser.findTags(this, tagName);
    }

    public UHTag findFirstTag(String tagName) {
        List<UHTag> result = findTags(tagName);
        return result == null || result.isEmpty() ? null : result.get(0);
    }


    // Getters

    public UHDocument getDocument() {
        return document;
    }

    public String getTagName() {
        return tagName;
    }

    public String getTagHeader() {
        return tagHeader;
    }

    public String getTagContent() {
        return document.getHtmlCode().substring(contentStart, contentEnd);
    }

    public String getTagCode() {
        return document.getHtmlCode().substring(start, end);
    }

    public boolean isSelfClosing() {
        return !hasCloseTag;
    }

    public boolean hasCloseTag() {
        return hasCloseTag;
    }

    public int getStartPosition() {
        return start;
    }

    public int getEndPosition() {
        return end;
    }

    public int getContentStartPosition() {
        return contentStart;
    }

    public int getContentEndPosition() {
        return contentEnd;
    }

    public Map<String, String> getAttributesMap() {
        if (attributes == null) {
            attributes = UHParser.getAttributes(this);
        }
        return attributes;
    }

    public String getAttribute(String attributeName) {
        if (attributes == null) {
            attributes = UHParser.getAttributes(this);
        }
        
        attributeName = attributeName.toLowerCase();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getKey().toLowerCase().equals(attributeName)) {
                return entry.getValue();
            }
        }
        return "";
    }

    public String getTextRemoveAllTags() {
        String result = getTagContent();
        result = result.replaceAll("<[^>]*>", "\n");
        result = UString.splitAndStrip(result, "\n").stream().map(String::strip).collect(Collectors.joining("\n"));
        result = result.strip();
        
        return result;
    }

    public String getTextRaw() {
        String result = getTagContent();
        result = result.replace("<br>", "\n");
        List<String> lines = UString.splitAndStrip(result, "\n");
        return UString.joinListOfString(lines, "\n");
    }

    public String getTextSingleLine() {
        return getTextRaw().replace("\n", " ");
    }

    // Common attributes

    public String getAttID() {
        return getAttribute("id");
    }

    public String getAttClass() {
        return getAttribute("class");
    }

    public String getAttStyle() {
        return getAttribute("style");
    }

    public String getAttHref() {
        return getAttribute("href");
    }

    public String getAttSrc() {
        return getAttribute("src");
    }

    public String getAttAlt() {
        return getAttribute("alt");
    }

    public String getAttTitle() {
        return getAttribute("title");
    }

    public String getAttLang() {
        return getAttribute("lang");
    }

    public String getAttDir() {
        return getAttribute("dir");
    }

    public String getAttRole() {
        return getAttribute("role");
    }


}
