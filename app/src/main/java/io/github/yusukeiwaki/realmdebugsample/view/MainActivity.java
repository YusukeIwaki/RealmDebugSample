package io.github.yusukeiwaki.realmdebugsample.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.github.yusukeiwaki.realmdebugsample.R;
import io.github.yusukeiwaki.realmdebugsample.api.DummyAPISetting;
import io.github.yusukeiwaki.realmdebugsample.model.Message;
import io.github.yusukeiwaki.realmdebugsample.model.SyncState;
import io.github.yusukeiwaki.realmdebugsample.service.BackgroundSyncService;
import io.realm.Realm;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();

        setContentView(R.layout.activity_main);

        setupListView();

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView editor = (TextView) findViewById(R.id.editor_message);
                sendMessage(editor.getText().toString());
            }
        });

        setupDebug();
    }

    private void setupListView() {
        final MessageListAdapter adapter = new MessageListAdapter(this,
                mRealm.where(Message.class).findAllSorted("timestamp", Sort.ASCENDING));
        ListView messageListView = (ListView) findViewById(R.id.listview);
        messageListView.setAdapter(adapter);
        messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final Message message = adapter.getItem(position);
                final String messageId = message.id;
                if (message.syncstate == SyncState.ERROR) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("再送しますか？")
                            .setPositiveButton("再送", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mRealm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            try {
                                                realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                                                        .put("id", messageId)
                                                        .put("syncstate", SyncState.WAIT_FOR_SYNC));
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    });
                                }
                            })
                            .setNeutralButton(android.R.string.cancel, null)
                            .setNeutralButton("削除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mRealm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.where(Message.class).equalTo("id", messageId).findAll().deleteAllFromRealm();
                                        }
                                    });
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void sendMessage(final String body) {
        final String newId = UUID.randomUUID().toString();
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                            .put("id", newId)
                            .put("syncstate", SyncState.WAIT_FOR_SYNC)
                            .put("username", "YusukeIwaki")
                            .put("body", body)
                            .put("timestamp", System.currentTimeMillis()));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                TextView editor = (TextView) findViewById(R.id.editor_message);
                editor.setText("");
                BackgroundSyncService.keepAlive(MainActivity.this);
            }
        });
    }

    private void setupDebug() {
        CompoundButton checkbox = (CompoundButton) findViewById(R.id.checkbox_debug_error_send_message);
        setDebug(checkbox.isChecked());
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setDebug(isChecked);
            }
        });
    }

    private void setDebug(boolean debug) {
        DummyAPISetting.get(this).edit()
                .putBoolean(DummyAPISetting.KEY_SEND_MESSAGE_ERROR, debug)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackgroundSyncService.keepAlive(this);
    }

    @Override
    protected void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }
}
