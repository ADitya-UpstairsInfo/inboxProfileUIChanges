package com.yearlater.inboxmessenger.events;

/**
 * Created by yearlater on 04/01/2018.
 */

public class UpdateNetworkProgress {
    private String id;
    private int progress;

    public UpdateNetworkProgress(String id, int progress) {
        this.id = id;
        this.progress = progress;
    }

    public String getId() {
        return id;
    }

    public int getProgress() {
        return progress;
    }
}
