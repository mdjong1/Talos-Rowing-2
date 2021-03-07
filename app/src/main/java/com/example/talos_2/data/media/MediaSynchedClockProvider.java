package com.example.talos_2.data.media;

import com.example.talos_2.data.ClockProvider;

public class MediaSynchedClockProvider implements ClockProvider {

    private final ExternalMedia media;
    
 
    public MediaSynchedClockProvider(ExternalMedia media) {
        this.media = media;
    }

    @Override
    public long getTime() {
        return media.getTime();
    }

    @Override
    public void run() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset(long initialTime) {
    }
}
