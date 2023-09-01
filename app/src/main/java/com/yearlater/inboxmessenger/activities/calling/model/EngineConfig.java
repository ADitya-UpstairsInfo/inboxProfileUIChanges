/*
 * Created by yearlater on 2020
 */

package com.yearlater.inboxmessenger.activities.calling.model;

public class EngineConfig {
    public int mUid;

    public String mChannel;

    public void reset() {
        mChannel = null;
    }

    public EngineConfig() {
    }
}
