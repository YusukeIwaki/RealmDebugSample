package io.github.yusukeiwaki.realmdebugsample;

import android.app.Application;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

public class RealmDebugSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);

        Realm.init(this);
        SyncCredentials credentials = SyncCredentials.usernamePassword(
                "yusuke.iwaki@example.com", "hogehoge", false);
        SyncUser.loginAsync(credentials, "http://10.0.3.2:9080/auth", new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                RealmConfiguration config = new SyncConfiguration.Builder(user, "realm://10.0.3.2:9080/~/debug").build();
                Realm.setDefaultConfiguration(config);
            }

            @Override
            public void onError(ObjectServerError error) {
                Log.e("RealmDebugSample", "error", error);
            }
        });
    }
}
