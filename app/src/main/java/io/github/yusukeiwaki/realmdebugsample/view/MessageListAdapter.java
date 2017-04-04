package io.github.yusukeiwaki.realmdebugsample.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import io.github.yusukeiwaki.realmdebugsample.R;
import io.github.yusukeiwaki.realmdebugsample.model.Message;
import io.github.yusukeiwaki.realmdebugsample.model.SyncState;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

public class MessageListAdapter extends RealmBaseAdapter<Message> {
    private final Context mContext;

    public MessageListAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Message> data) {
        super(data);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_message, parent, false);
        }

        bindView(convertView, getItem(position));
        return convertView;
    }

    private void bindView(View itemView, Message message) {
        int syncstate = message.syncstate;

        TextView username = (TextView) itemView.findViewById(R.id.username);
        username.setText(message.username);

        TextView timestamp = (TextView) itemView.findViewById(R.id.timestamp);
        if (syncstate == SyncState.SYNCED) {
            LocalDateTime dateTime =
                    LocalDateTime.ofEpochSecond(message.timestamp / 1000, 0, ZoneOffset.ofHours(9));
            timestamp.setText(dateTime.format(DateTimeFormatter.ISO_TIME));
        } else if (syncstate == SyncState.ERROR) {
            timestamp.setText("エラー");
        } else {
            timestamp.setText("送信中...");
        }

        TextView body = (TextView) itemView.findViewById(R.id.body);
        body.setText(message.body);

        if (syncstate == SyncState.SYNCED || syncstate == SyncState.ERROR) {
            itemView.setAlpha(1.0f);
        } else {
            itemView.setAlpha(0.6f);
        }
    }
}
