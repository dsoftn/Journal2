package com.dsoftn.utils.html;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UHParser {
    public static final List<String> HAS_NO_CLOSE_TAG = List.of("br", "img", "input", "meta", "link", "hr", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr");

    public static List<UHTag> getTags(UHTag parentTag, String tagName) {
        String htmlCode = parentTag.getDocument().getHtmlCode().substring(parentTag.getContentStartPosition(), parentTag.getContentEndPosition());
        int offset = parentTag.getContentStartPosition();

        List<UHTag> result = new ArrayList<>();

        if (htmlCode == null || htmlCode.isEmpty()) {
            return result;
        }

        if (tagName == null) {
            tagName = "";
        }

        int pos = 0;
        while (pos < htmlCode.length()) {
            pos = htmlCode.indexOf("<", pos);
            if (pos == -1) {
                break;
            }
            int tagNameEnd = validMin(htmlCode.indexOf(" ", pos + 1), htmlCode.indexOf(">", pos + 1));
            if (tagNameEnd == -1) {
                break;
            }

            String curTagName = htmlCode.substring(pos + 1, tagNameEnd);
            
            // Skip tags that are not requested
            if (!curTagName.equals(tagName) && !tagName.isEmpty()) {
                if (HAS_NO_CLOSE_TAG.contains(curTagName) || curTagName.startsWith("!")) {
                    pos = htmlCode.indexOf(">", pos);
                    if (pos == -1) {
                        break;
                    }
                    pos++;
                } else {
                    pos = htmlCode.indexOf("</" + curTagName + ">", pos);
                    if (pos == -1) {
                        break;
                    }
                    pos += curTagName.length() + 3;
                }
                continue;
            }

            // Check if tag has no close tag
            if (HAS_NO_CLOSE_TAG.contains(curTagName) || curTagName.startsWith("!")) {
                int end = htmlCode.indexOf(">", pos);
                if (end == -1) {
                    break;
                }

                result.add(new UHTag(parentTag.getDocument(), curTagName, htmlCode.substring(pos, end + 1), pos + offset, end + offset + 1, end + offset, end + offset, parentTag));
                pos = end + 1;
                continue;
            }

            // Get tag header
            int tagHeaderEnd = htmlCode.indexOf(">", pos);
            if (tagHeaderEnd == -1) {
                break;
            }
            String tagHeader = htmlCode.substring(pos, tagHeaderEnd + 1);

            // Get tag content
            int tagContentStart = tagHeaderEnd + 1;
            int tagContentEnd = htmlCode.indexOf("</" + curTagName + ">", tagContentStart);
            if (tagContentEnd == -1) {
                break;
            }
            int tagEndPosition = tagContentEnd + curTagName.length() + 3;

            // Add tag !
            result.add(new UHTag(parentTag.getDocument(), curTagName, tagHeader, pos + offset, tagEndPosition + offset, tagContentStart + offset, tagContentEnd + offset, parentTag));
            pos = tagContentEnd + curTagName.length() + 3;
        }

        return result;
    }

    public static List<UHTag> findTags(UHTag parentTag, String tagName) {
        String htmlCode = parentTag.getDocument().getHtmlCode().substring(parentTag.getContentStartPosition(), parentTag.getContentEndPosition());
        int offset = parentTag.getContentStartPosition();

        List<UHTag> result = new ArrayList<>();

        if (htmlCode == null || htmlCode.isEmpty()) {
            return result;
        }

        if (tagName == null || tagName.isEmpty()) {
            return result;
        }

        int pos = 0;
        while (pos < htmlCode.length()) {
            pos = htmlCode.indexOf("<" + tagName, pos);
            if (pos == -1) {
                break;
            }
            int tagNameEnd = validMin(htmlCode.indexOf(" ", pos + 1), htmlCode.indexOf(">", pos + 1));
            if (tagNameEnd == -1) {
                break;
            }

            String curTagName = htmlCode.substring(pos + 1, tagNameEnd);
            
            // Skip tags that are not requested
            if (!curTagName.equals(tagName)) {
                pos = htmlCode.indexOf(">", pos);
                if (pos == -1) {
                    break;
                }
                pos++;
                continue;
            }

            // Check if tag has no close tag
            if (HAS_NO_CLOSE_TAG.contains(curTagName) || curTagName.startsWith("!")) {
                int end = htmlCode.indexOf(">", pos);
                if (end == -1) {
                    break;
                }

                result.add(new UHTag(parentTag.getDocument(), curTagName, htmlCode.substring(pos, end + 1), pos + offset, end + offset + 1, end, end, null));
                pos = end + 1;
                continue;
            }

            // Get tag header
            int tagHeaderEnd = htmlCode.indexOf(">", pos);
            if (tagHeaderEnd == -1) {
                break;
            }
            String tagHeader = htmlCode.substring(pos, tagHeaderEnd + 1);

            // Get tag content
            int tagContentStart = tagHeaderEnd + 1;
            int tagContentEnd = htmlCode.indexOf("</" + curTagName + ">", tagContentStart);
            if (tagContentEnd == -1) {
                break;
            }
            int tagEndPosition = tagContentEnd + curTagName.length() + 3;

            // Add tag !
            result.add(new UHTag(parentTag.getDocument(), curTagName, tagHeader, pos + offset, tagEndPosition + offset, tagContentStart + offset, tagContentEnd + offset, null));
            pos = tagContentEnd + curTagName.length() + 3;
        }

        return result;
    }

    public static UHTag getParentTag(UHTag currentTag) {
        String htmlCode = currentTag.getDocument().getHtmlCode();

        if (htmlCode == null || htmlCode.isEmpty() || currentTag == null) {
            currentTag.setParent(null);
            return null;
        }

        int currentTagStartPos = currentTag.getStartPosition();
        int currentTagEndPos = currentTag.getEndPosition();

        if (currentTagStartPos < 0 || currentTagEndPos < 0 || currentTagStartPos > currentTagEndPos || currentTagEndPos >= htmlCode.length()) {
            currentTag.setParent(null);
            return null;
        }

        int pos = currentTagEndPos;
        String closingTagName = "";
        int closingTagStart = -1;
        int closingTagEnd = -1;
        int begTagStart = -1;
        boolean found = false;
        while (true) {
            pos = htmlCode.indexOf("</", pos);
            if (pos == -1) {
                break;
            }

            // Find next closing tag
            int tagNameIdx = validMin(htmlCode.indexOf(" ", pos + 2), htmlCode.indexOf(">", pos + 2));
            if (tagNameIdx == -1) continue;
            closingTagName = htmlCode.substring(pos + 2, tagNameIdx);
            closingTagStart = pos;
            closingTagEnd = htmlCode.indexOf(">", pos);

            begTagStart = htmlCode.indexOf("<" + closingTagName + ">", currentTagEndPos);
            if (begTagStart == -1) {
                  begTagStart = htmlCode.indexOf("<" + closingTagName + " ", currentTagEndPos);
            }

            if (begTagStart != -1) {
                pos = closingTagEnd;
                continue;
            }

            found = true;
            break;
        }

        if (!found) {
            currentTag.setParent(null);
            return null;
        }

        pos = currentTagStartPos;
        while (pos > 0) {
            pos = pos - (closingTagName.length() + 2);
            if (pos < 0) pos = 0;

            int startTagStart = htmlCode.indexOf("<" + closingTagName + ">", pos);
            if (startTagStart == -1) {
                startTagStart = htmlCode.indexOf("<" + closingTagName + " ", pos);
            }
            if (startTagStart == -1) {
                continue;
            }

            if (startTagStart == currentTag.getStartPosition()) {
                continue;
            }

            int startTagEnd = htmlCode.indexOf(">", startTagStart);
            String startTagHeader = htmlCode.substring(startTagStart, startTagEnd + 1);

            UHTag parentTag = new UHTag(currentTag.getDocument(), closingTagName, startTagHeader, startTagStart, closingTagEnd + 1, startTagEnd + 1, closingTagStart, null);
            currentTag.setParent(parentTag);
            return parentTag;
        }

        currentTag.setParent(null);
        return null;
    }

    public static Map<String, String> getAttributes(UHTag tag) {
        String tagHeader = tag.getTagHeader();
        Map<String, String> result = new LinkedHashMap<>();

        if (tagHeader == null || tagHeader.isEmpty()) {
            return result;
        }

        int pos = 0;
        while (pos < tagHeader.length()) {
            pos = tagHeader.indexOf(" ", pos);
            if (pos == -1) {
                break;
            }
            int eqPos = tagHeader.indexOf("=", pos);
            if (eqPos == -1) {
                break;
            }
            
            if (eqPos == tagHeader.length() - 1) {
                break;
            }

            int endPos = -1;
            
            if (tagHeader.charAt(eqPos + 1) == '"') {
                endPos = tagHeader.indexOf("\"", eqPos + 2);
                if (endPos == -1) {
                    break;
                }
                endPos++;
            } else if (tagHeader.charAt(eqPos + 1) == '\'') {
                endPos = tagHeader.indexOf("'", eqPos + 2);
                if (endPos == -1) {
                    break;
                }
                endPos++;
            } else {
                endPos = validMin(tagHeader.indexOf(" ", eqPos), tagHeader.indexOf("/>", eqPos));
                if (endPos == -1) {
                    endPos = tagHeader.indexOf(">", eqPos);
                    if (endPos == -1) {
                        break;
                    }
                }
            }

            String attrName = tagHeader.substring(pos + 1, eqPos).strip();
            String attrValue = tagHeader.substring(eqPos + 1, endPos).strip();

            if (!attrName.isEmpty()) result.put(attrName, attrValue);
            pos = endPos;
        }

        return result;
    }

    private static int validMin(int num1, int num2) {
        if (num1 == -1) return num2;
        if (num2 == -1) return num1;
        return Math.min(num1, num2);
    }


}
