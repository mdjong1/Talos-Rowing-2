package com.example.talos_2.data;

public interface ClockProvider {
    public long getTime();
    public void run();
    public void stop();
    public void reset(long initialTime);
}