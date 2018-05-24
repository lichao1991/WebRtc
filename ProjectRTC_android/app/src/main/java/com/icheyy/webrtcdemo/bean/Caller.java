package com.icheyy.webrtcdemo.bean;

/**
 * Created by Cheyy on 2017/9/13.
 */

public class Caller {

    private String mName;
    private boolean mStatus;

    public Caller(String name, boolean status) {
        mName = name;
        mStatus = status;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }
}
