package com.dsoftn.events;

import com.dsoftn.enums.events.ClipboardActionEnum;
import com.dsoftn.enums.models.ModelEnum;

import javafx.event.Event;
import javafx.event.EventType;

public class ClipboardChangedEvent extends Event {
    // Variables

    public static final EventType<ClipboardChangedEvent> CLIPBOARD_CHANGED_EVENT = new EventType<>(Event.ANY, "CLIPBOARD_CHANGED_EVENT");

    private final ClipboardActionEnum clipActionTaken;
    private final ModelEnum clipModel;
    private final String clipValue;

    // Constructor

    public ClipboardChangedEvent(ClipboardActionEnum clipActionTaken, ModelEnum clipModel, String clipValue) {
        super(CLIPBOARD_CHANGED_EVENT);

        this.clipActionTaken = clipActionTaken;
        this.clipModel = clipModel;
        this.clipValue = clipValue;
    }

    public ClipboardChangedEvent(ClipboardActionEnum clipActionTaken) {
        super(CLIPBOARD_CHANGED_EVENT);

        this.clipActionTaken = clipActionTaken;
        this.clipModel = null;
        this.clipValue = null;
    }

    // Getters

    public ClipboardActionEnum getClipActionTaken() { return clipActionTaken; }
    public ModelEnum getClipModel() { return clipModel; }
    public String getClipValue() { return clipValue; }

}
