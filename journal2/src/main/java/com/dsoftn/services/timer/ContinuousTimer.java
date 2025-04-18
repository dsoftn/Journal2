package com.dsoftn.services.timer;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ITimer;

public class ContinuousTimer implements ITimer {
    // Variables
    private long startTimeMS;
    private long endTimeMS;
    private long intervalMS;
    private long delayMS;
    private Runnable action;
    private boolean active = true;

    // Constructor
    public ContinuousTimer(long intervalMS, long delayMS) {
        this.intervalMS = intervalMS;
        this.delayMS = delayMS;
    }
    
    public ContinuousTimer(long intervalMS) {
        this(intervalMS, 0);
    }

    // Interface ITimer methods

    @Override
    public void play(Runnable action) {
        this.action = action;
        active = true;
        OBJECTS.GLOBAL_TIMER.registerTimer(this);
    }
    
    @Override
    public void stop() {
        active = false;
        OBJECTS.GLOBAL_TIMER.unregisterTimer(this);
    }

    @Override
    public void startMS(long ms) {
        startTimeMS = ms;
        endTimeMS = startTimeMS + delayMS + intervalMS;
    }
    
    @Override
    public void resetInterval() {
        endTimeMS = OBJECTS.GLOBAL_TIMER.getCurrentTimeMS() + intervalMS;
    }

    public void resetInterval(long forDurationMS) {
        endTimeMS = OBJECTS.GLOBAL_TIMER.getCurrentTimeMS() + forDurationMS;
    }

    @Override
    public void nowMS(long ms) {
        if (ms >= endTimeMS && active) {
            action.run();
            endTimeMS = ms + intervalMS;
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

}