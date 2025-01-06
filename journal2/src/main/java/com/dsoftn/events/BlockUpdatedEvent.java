package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Block;


public class BlockUpdatedEvent extends Event {

    public static final EventType<BlockUpdatedEvent> BLOCK_UPDATED_EVENT = new EventType<>(Event.ANY, "BLOCK_UPDATED_EVENT");

    private final Block block;

    // Constructor
    public BlockUpdatedEvent(Block block) {
        super(BLOCK_UPDATED_EVENT);
        this.block = block;
    }

    // Getters
    public Block getBlock() { return this.block; }

}
