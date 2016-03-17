package com.pantheon_inc.odyssey.android;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class GcmService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        System.out.println("GCM From: " + from);
        System.out.println("GCM Message: " + message);
    }
}