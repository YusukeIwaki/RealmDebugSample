package io.github.yusukeiwaki.realmdebugsample;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmDebugSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
