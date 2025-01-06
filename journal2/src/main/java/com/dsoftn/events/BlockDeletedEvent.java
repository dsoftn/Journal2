package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Block;


public class BlockDeletedEvent extends Event {

    public static final EventType<BlockDeletedEvent> BLOCK_DELETED_EVENT = new EventType<>(Event.ANY, "BLOCK_DELETED_EVENT");

    private final Block block;

    // Constructor
    public BlockDeletedEvent(Block block) {
        super(BLOCK_DELETED_EVENT);
        this.block = block;
    }

    // Getters
    public Block getBlock() { return this.block; }

}
