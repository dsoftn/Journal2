package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Tag;


public class TagAddedEvent extends Event {
    public static final EventType<TagAddedEvent> TAG_ADDED_EVENT = new EventType<>(Event.ANY, "TAG_ADDED_EVENT");
    
    private final Tag tag;

    // Constructor

    public TagAddedEvent(Tag tag) {
        super(TAG_ADDED_EVENT);
        this.tag = tag;
    }

    // Getters

    public Tag getTag() { return this.tag; }

}
