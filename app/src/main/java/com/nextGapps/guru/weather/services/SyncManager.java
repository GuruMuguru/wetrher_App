package com.nextGapps.guru.weather.services;

import android.content.Context;
import android.util.Log;

import java.util.TimerTask;

public class SyncManager extends TimerTask {


    private static final String TAG = "SyncManager";
    Context context;


    public SyncManager(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.e(TAG, "Working...");


    }
}
