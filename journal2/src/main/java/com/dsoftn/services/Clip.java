package com.dsoftn.services;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;

import com.dsoftn.OBJECTS;
import com.dsoftn.enums.events.ClipboardActionEnum;
import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.events.ClipboardChangedEvent;
import com.dsoftn.services.timer.ContinuousTimer;
import com.dsoftn.utils.UJavaFX;
import com.dsoftn.utils.URichText;
import com.dsoftn.utils.UString;

import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;


public class Clip {
    // Variables
    private Map<String, List<Integer>> clipMap = null;
    private Clipboard sysClip = Clipboard.getSystemClipboard();

    private String lastClipText = getClipText();
    private Image lastClipImage = getClipImage();
    private List<File> lastClipFiles = getClipFiles();
    private String lastClipHtml = getClipHtml();
    private String lastClipRtf = getClipRtf();
    private String lastClipUrl = getClipUrl();
    ContinuousTimer timer = new ContinuousTimer(200);

    // Constructor

    public Clip() {
        timer.play(this::onClipboardChanged);
    }

    // GET from system clipboard

    public String getClipText() {
        if (sysClip.hasString()) {
            return sysClip.getString().replaceAll("\\R", "\n");
        }
        return null;
    }

    public Image getClipImage() {
        if (sysClip.hasImage()) {
            return sysClip.getImage();
        }
        return null;
    }

    public List<File> getClipFiles() {
        if (sysClip.hasFiles()) {
            return sysClip.getFiles();
        }
        return null;
    }

    public String getClipHtml() {
        if (sysClip.hasHtml()) {
            return sysClip.getHtml();
        }
        return null;
    }

    public String getClipRtf() {
        if (sysClip.hasRtf()) {
            return sysClip.getRtf();
        }
        return null;
    }

    public String getClipUrl() {
        if (sysClip.hasUrl()) {
            return sysClip.getUrl();
        }
        return null;
    }

    // SET to system clipboard

    public void setClipText(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        sysClip.setContent(content);
    }

    public void setClipImage(Image image) {
        ClipboardContent content = new ClipboardContent();
        content.putImage(image);
        sysClip.setContent(content);
    }

    public void setClipFiles(List<File> files) {
        ClipboardContent content = new ClipboardContent();
        content.putFiles(files);
        sysClip.setContent(content);
    }

    public void setClipHtml(String html) {
        ClipboardContent content = new ClipboardContent();
        content.putHtml(html);
        sysClip.setContent(content);
    }

    public void setClipRtf(String rtf) {
        ClipboardContent content = new ClipboardContent();
        content.putRtf(rtf);
        sysClip.setContent(content);
    }

    public String setClipUrl() {
        if (sysClip.hasUrl()) {
            return sysClip.getUrl();
        }
        return null;
    }

    // Check if clipboard is empty

    public boolean hasText() {
        return sysClip.hasString();
    }

    // Clear system clipboard

    public void clearClip() {
        ClipboardContent content = new ClipboardContent();
        sysClip.setContent(content);
    }

    // System clipboard listener

    private void onClipboardChanged() {
        String changed = whatChangedInSystemClipboard();

        if (changed == null) {
            return;
        }

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.SYSTEM_CLIPBOARD_CHANGED, null, changed);
        OBJECTS.EVENT_HANDLER.fireEvent(event);

        // ---
        // System.out.println("\n\n----------------------------------------");
        // if (changed.equals("text")) {
        //     System.out.println("Text: \n" + getClipText());
        // }
        // else if (changed.equals("image")) {
        //     System.out.println("Image: \n" + getClipImage());
        // }
        // else if (changed.equals("files")) {
        //     System.out.println("Files: \n" + getClipFiles());
        // }
        // else if (changed.equals("html")) {
        //     System.out.println("Html: \n" + getClipHtml());
        // }
        // else if (changed.equals("rtf")) {
        //     System.out.println("Rtf: \n" + getClipRtf());
        // }
        // else if (changed.equals("url")) {
        //     System.out.println("Url: \n" + getClipUrl());
        // }
        // ---
    }

    private String whatChangedInSystemClipboard() {
        if (!Objects.equals(lastClipText, getClipText())) {
            lastClipText = getClipText();
            return "text";
        }
        else if (!UJavaFX.isImagesContentEqual(lastClipImage, getClipImage())) {
            lastClipImage = getClipImage();
            return "image";
        }
        else if (!Objects.equals(lastClipFiles, getClipFiles())) {
            lastClipFiles = getClipFiles();
            return "files";
        }
        else if (!Objects.equals(lastClipHtml, getClipHtml())) {
            lastClipHtml = getClipHtml();
            return "html";
        }
        else if (!Objects.equals(URichText.convertRtfToPlainText(lastClipRtf), URichText.convertRtfToPlainText(getClipRtf()))) {
            lastClipRtf = getClipRtf();
            return "rtf";
        }
        else if (!Objects.equals(lastClipUrl, getClipUrl())) {
            lastClipUrl = getClipUrl();
            return "url";
        }
        else {
            return null;
        }
    }


    // Set new IDs

    public void setIDs(String clipModel, List<Integer> ids) {
        if (clipMap == null) {
            clipMap = new HashMap<>();
        }

        clipMap.put(clipModel, ids);

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.COPY, ModelEnum.fromName(clipModel), UString.joinListOfInteger(ids, ","));
        OBJECTS.EVENT_HANDLER.fireEvent(event);
    }

    public void setIDs(String clipModel, Integer id) {
        setIDs(clipModel, new ArrayList<Integer>() { { add(id); } });
    }

    public void setIDs(ModelEnum model, List<Integer> ids) {
        setIDs(model.toString(), ids);
    }

    public void setIDs(ModelEnum model, Integer id) {
        setIDs(model.toString(), new ArrayList<Integer>() { { add(id); } });
    }

    // Add new IDs

    public void addIDs(String clipModel, List<Integer> ids) {
        if (clipMap == null) {
            clipMap = new HashMap<>();
        }

        if (clipMap.containsKey(clipModel)) {
            clipMap.get(clipModel).addAll(ids);
        }
        else {
            clipMap.put(clipModel, ids);
        }
        
        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.ADD, ModelEnum.fromName(clipModel), UString.joinListOfInteger(ids, ","));
        OBJECTS.EVENT_HANDLER.fireEvent(event);
    }

    public void addIDs(String clipModel, Integer id) {
        addIDs(clipModel, new ArrayList<Integer>() { { add(id); } });
    }

    public void addIDs(ModelEnum model, List<Integer> ids) {
        addIDs(model.toString(), ids);
    }

    public void addIDs(ModelEnum model, Integer id) {
        addIDs(model.toString(), new ArrayList<Integer>() { { add(id); } });
    }

    // Get IDs

    public List<Integer> getIDs(String clipModel) {
        if (clipMap == null) {
            return new ArrayList<>();
        }

        if (clipMap.containsKey(clipModel)) {
            return clipMap.get(clipModel);
        }

        return new ArrayList<>();
    }

    public List<Integer> getIDs(ModelEnum model) {
        return getIDs(model.toString());
    }

    // Clear IDs

    public void clearIDs(String clipModel) {
        if (clipMap == null) {
            return;
        }

        if (clipMap.containsKey(clipModel)) {
            clipMap.remove(clipModel);
        }

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.DELETE_MODEL, ModelEnum.fromName(clipModel), null);
        OBJECTS.EVENT_HANDLER.fireEvent(event);
        
    }

    public void clearIDs(ModelEnum model) {
        clearIDs(model.toString());
    }

    public void clearIDs() {
        clipMap = null;

        ClipboardChangedEvent event = new ClipboardChangedEvent(ClipboardActionEnum.DELETE_ALL, null, null);
        OBJECTS.EVENT_HANDLER.fireEvent(event);
    }

    public void clear() {
        clearIDs();
    }

    // Count IDs

    public int countIDs(String clipModel) {
        if (clipMap == null) {
            return 0;
        }

        if (clipMap.containsKey(clipModel)) {
            return clipMap.get(clipModel).size();
        }

        return 0;
    }

    public int countIDs(ModelEnum model) {
        return countIDs(model.toString());
    }


}
