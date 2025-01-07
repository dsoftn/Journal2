package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Block;


public class BlockUpdatedEvent extends Event {

    public static final EventType<BlockUpdatedEvent> BLOCK_UPDATED_EVENT = new EventType<>(Event.ANY, "BLOCK_UPDATED_EVENT");

    private final Block OldBlock;
    private final Block NewBlock;

    // Constructor
    public BlockUpdatedEvent(Block oldBlock, Block newBlock) {
        super(BLOCK_UPDATED_EVENT);
        this.OldBlock = oldBlock;
        this.NewBlock = newBlock;
    }

    // Getters
    
    public Block getOldBlock() { return this.OldBlock; }

    public Block getNewBlock() { return this.NewBlock; }

}
