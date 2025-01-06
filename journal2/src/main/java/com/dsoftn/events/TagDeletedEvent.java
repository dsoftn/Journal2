package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Tag;


public class TagDeletedEvent extends Event {
    public static final EventType<TagDeletedEvent> TAG_DELETED_EVENT = new EventType<>(Event.ANY, "TAG_DELETED_EVENT");

    private final Tag tag;
    
    // Constructor

    public TagDeletedEvent(Tag tag) {
        super(TAG_DELETED_EVENT);
        this.tag = tag;
    }
    
    // Getters
    
    public Tag getTag() {
        return tag;
    }

}
