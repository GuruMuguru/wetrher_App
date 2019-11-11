package com.nextGapps.guru.weather.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;

public class SyncService extends Service {

    String TAG = "SyncService";
    Timer syncTimer;

    SyncManager syncManager;

    final int TIME_INERVAL_IN_SEC = 120;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        syncTimer = new Timer();
        Log.e(TAG, "Service Created");
        syncTimer = new Timer();
        syncManager = new SyncManager(this);
        syncTimer.schedule(syncManager, 1000, (TIME_INERVAL_IN_SEC * 10000));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Service Started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "Service Destroyed");

        if (syncManager != null)
            syncManager.cancel();

        if (syncTimer != null)
            syncTimer.cancel();
    }
}
