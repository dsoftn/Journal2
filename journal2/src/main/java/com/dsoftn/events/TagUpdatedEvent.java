package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Tag;


public class TagUpdatedEvent extends Event {
    public static final EventType<TagUpdatedEvent> TAG_UPDATED_EVENT = new EventType<>(Event.ANY, "TAG_UPDATED_EVENT");
    
    private final Tag tag;

    // Constructor

    public TagUpdatedEvent(Tag tag) {
        super(TAG_UPDATED_EVENT);
        this.tag = tag;
    }

    // Getters

    public Tag getTag() { return this.tag; }

}
