package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Attachment;


public class AttachmentAddedEvent extends Event {
    
    public static final EventType<AttachmentAddedEvent> ATTACHMENT_ADDED_EVENT = new EventType<>(Event.ANY, "ATTACHMENT_ADDED_EVENT");
    
    private final Attachment attachment;

    // Constructor

    public AttachmentAddedEvent(Attachment attachment) {
        super(ATTACHMENT_ADDED_EVENT);
        this.attachment = attachment;
    }

    // Getters

    public Attachment getAttachment() { return this.attachment; }

}
