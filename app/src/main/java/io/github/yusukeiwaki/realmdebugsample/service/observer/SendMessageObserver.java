package io.github.yusukeiwaki.realmdebugsample.service.observer;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import io.github.yusukeiwaki.realmdebugsample.api.DummyAPI;
import io.github.yusukeiwaki.realmdebugsample.model.Message;
import io.github.yusukeiwaki.realmdebugsample.model.SyncState;
import io.github.yusukeiwaki.realmdebugsample.service.Registerable;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class SendMessageObserver implements Registerable, RealmChangeListener<RealmResults<Message>> {
    private final Context mContext;
    private RealmResults<Message> mRealmResults;
    private final DummyAPI mAPI;
    private Realm mRealm;

    public SendMessageObserver(Context context) {
        mContext = context;
        mAPI = new DummyAPI(context);
    }

    @Override
    public void register() {
        mRealm = Realm.getDefaultInstance();
        resetPendingMessagesIfNeeded();
        mRealmResults = mRealm.where(Message.class)
                .equalTo("syncstate", SyncState.WAIT_FOR_SYNC).findAll();
        onChange(mRealmResults);
        mRealmResults.addChangeListener(this);
    }

    @Override
    public void unregister() {
        if (mRealmResults.isValid()) {
            mRealmResults.removeChangeListener(this);
        }
        mRealm.close();
    }

    private void resetPendingMessagesIfNeeded() {
        if (mRealm.where(Message.class).equalTo("syncstate", SyncState.SYNCING).count() > 0) {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<Message> pendingMessages = realm.where(Message.class)
                            .equalTo("syncstate", SyncState.SYNCING)
                            .findAll();
                    for (Message m  : pendingMessages) {
                        m.setSyncstate(SyncState.WAIT_FOR_SYNC);
                    }
                }
            });
        }
    }


    @Override
    public void onChange(RealmResults<Message> results) {
        if (results.isEmpty()) return;

        Message targetMessage = results.get(0);

        final String messageId = targetMessage.getId();
        final String username = targetMessage.getUsername();
        final String body = targetMessage.getBody();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                            .put("id", messageId)
                            .put("syncstate", SyncState.SYNCING));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        mAPI.sendMessage(messageId, username, body)
                .onSuccess(new Continuation<Message, Object>() {
                    @Override
                    public Object then(Task<Message> task) throws Exception {
                        Message newMessage = task.getResult();

                        final String id = newMessage.getId();
                        final long timestamp = newMessage.getTimestamp();

                        try (Realm realm = Realm.getDefaultInstance()){
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                                                .put("id", id)
                                                .put("syncstate", SyncState.SYNCED)
                                                .put("timestamp", timestamp));
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                        return null;
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            try (Realm realm = Realm.getDefaultInstance()) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        try {
                                            realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                                                    .put("id", messageId)
                                                    .put("syncstate", SyncState.ERROR));
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                            }
                        }
                        return null;
                    }
                });
    }
}
