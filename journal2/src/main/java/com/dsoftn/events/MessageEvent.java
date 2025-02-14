package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

public class MessageEvent extends Event {
    // Variables

    public static final EventType<MessageEvent> RESULT_EVENT = new EventType<>(Event.ANY, "RESULT_EVENT");

    private final String receiverID;
    private final String senderID;
    private final String messageSTRING;

    // Constructor

    public MessageEvent(String receiverID, String senderID, String result) {
        super(RESULT_EVENT);
        this.receiverID = receiverID;
        this.senderID = senderID;
        this.messageSTRING = result;
    }

    // Getters

    public String getReceiverID() { return this.receiverID; }
    public String getSenderID() { return this.senderID; }
    public String getMessageSTRING() { return this.messageSTRING; }

}
