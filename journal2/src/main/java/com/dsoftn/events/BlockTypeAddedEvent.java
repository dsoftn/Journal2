package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.block_types.BlockDiary;


public class BlockTypeAddedEvent extends Event {
    
    public static final EventType<BlockTypeAddedEvent> BLOCK_TYPE_ADDED = new EventType<>(Event.ANY, "BLOCK_TYPE_ADDED");

    private final BlockDiary blockDiary;

    // Constructor
    public BlockTypeAddedEvent(BlockDiary blockDiary) {
        super(BLOCK_TYPE_ADDED);
        this.blockDiary = blockDiary;
    }

    // Getters
    
    public BlockDiary getBlockDiary() {
        return blockDiary;
    }

}
