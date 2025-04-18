package com.dsoftn.Interfaces;

public interface ITimer {

    public void startMS(long ms);
    
    public void nowMS(long ms);

    public void play(Runnable action);

    public void stop();

    public boolean isActive();

    public void resetInterval();

}
