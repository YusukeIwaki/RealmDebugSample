package io.github.yusukeiwaki.realmdebugsample.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import io.github.yusukeiwaki.realmdebugsample.service.observer.SendMessageObserver;

public class BackgroundSyncService extends Service {

    private ArrayList<Registerable> mListeners;

    public static final void keepAlive(Context context) {
        Intent intent = new Intent(context, BackgroundSyncService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupListeners();
        registerListeners();
    }

    @Override
    public void onDestroy() {
        unregisterListeners();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupListeners() {
        mListeners = new ArrayList<>();
        mListeners.add(new SendMessageObserver(getBaseContext()));
    }

    private void registerListeners() {
        for (Registerable registerable : mListeners) {
            registerable.register();
        }
    }

    private void unregisterListeners() {
        for (Registerable registerable : mListeners) {
            registerable.unregister();
        }
    }
}
