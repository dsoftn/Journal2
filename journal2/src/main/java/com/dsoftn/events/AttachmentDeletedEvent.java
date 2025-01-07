package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Attachment;


public class AttachmentDeletedEvent extends Event {

    public static final EventType<AttachmentDeletedEvent> ATTACHMENT_DELETED_EVENT = new EventType<>(Event.ANY, "ATTACHMENT_DELETED_EVENT");
    
    private final Attachment attachment;

    // Constructor

    public AttachmentDeletedEvent(Attachment attachment) {
        super(ATTACHMENT_DELETED_EVENT);
        this.attachment = attachment;
    }

    // Getters

    public Attachment getAttachment() { return this.attachment; }


}
