package com.icheyy.webrtcdemo;

import android.util.Log;

import org.webrtc.VideoRenderer;

/**
 * @FileName: com.inesadt.webrtc.ProxyRenderer.java
 * @Author: Vita
 * @Date: 2017-09-05 10:47
 * @Usage:
 */
public class ProxyRenderer implements VideoRenderer.Callbacks {
    private static final String TAG = "ProxyRenderer";
    private VideoRenderer.Callbacks target;

    synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
        if (target == null) {
            Log.d(TAG, "Dropping frame in proxy because target is null.");
            VideoRenderer.renderFrameDone(frame);
            return;
        }

        target.renderFrame(frame);
    }

    synchronized public void setTarget(VideoRenderer.Callbacks target) {
        this.target = target;
    }
}
