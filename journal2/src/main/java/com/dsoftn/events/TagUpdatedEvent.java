package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Tag;


public class TagUpdatedEvent extends Event {
    public static final EventType<TagUpdatedEvent> TAG_UPDATED_EVENT = new EventType<>(Event.ANY, "TAG_UPDATED_EVENT");
    
    private final Tag oldTag;
    private final Tag newTag;

    // Constructor

    public TagUpdatedEvent(Tag oldTag, Tag newTag) {
        super(TAG_UPDATED_EVENT);
        this.oldTag = oldTag;
        this.newTag = newTag;
    }

    // Getters

    public Tag getOldTag() { return this.oldTag; }

    public Tag getNewTag() { return this.newTag; }

}
