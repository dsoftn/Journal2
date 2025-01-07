package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Attachment;


public class AttachmentUpdatedEvent extends Event {

    public static final EventType<AttachmentUpdatedEvent> ATTACHMENT_UPDATED_EVENT = new EventType<>(Event.ANY, "ATTACHMENT_UPDATED_EVENT");
    
    private final Attachment oldAttachment;
    private final Attachment newAttachment;

    // Constructor

    public AttachmentUpdatedEvent(Attachment oldAttachment, Attachment newAttachment) {
        super(ATTACHMENT_UPDATED_EVENT);
        this.oldAttachment = oldAttachment;
        this.newAttachment = newAttachment;
    }

    // Getters

    public Attachment getOldAttachment() { return this.oldAttachment; }

    public Attachment getNewAttachment() { return this.newAttachment; }

}
