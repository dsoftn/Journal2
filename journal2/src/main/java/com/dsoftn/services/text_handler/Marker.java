package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.services.RTWidget;

public class Marker {
    public enum MarkerType {
        ALL,
        FIND;
    }

    //  Variables
    private RTWidget rtWidget = null;
    private List<MarkedItem> markedItemsFIND = new ArrayList<>();

    //  Constructor
    public Marker(RTWidget rtWidget) {
        this.rtWidget = rtWidget;
    }

    //  Public methods
    

    //  Private methods
}
