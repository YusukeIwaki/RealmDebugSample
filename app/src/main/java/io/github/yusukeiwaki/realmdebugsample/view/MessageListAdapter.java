package io.github.yusukeiwaki.realmdebugsample.view;

import android.content.Context;
import android.databinding.BindingAdapter;
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
import io.github.yusukeiwaki.realmdebugsample.databinding.ListitemMessageBinding;
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
            ListitemMessageBinding binding = ListitemMessageBinding.inflate(LayoutInflater.from(mContext), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(R.id.dataBinding, binding);
        }

        bindView(convertView, getItem(position));
        return convertView;
    }

    private void bindView(View itemView, Message message) {
        ListitemMessageBinding binding = (ListitemMessageBinding) itemView.getTag(R.id.dataBinding);
        binding.setMessage(message);
    }

    @BindingAdapter("timestamp")
    public static void bindTimeStamp(TextView timestamp, Message message) {
        int syncstate = message.syncstate;
        if (syncstate == SyncState.SYNCED) {
            LocalDateTime dateTime =
                    LocalDateTime.ofEpochSecond(message.timestamp / 1000, 0, ZoneOffset.ofHours(9));
            timestamp.setText(dateTime.format(DateTimeFormatter.ISO_TIME));
        } else if (syncstate == SyncState.ERROR) {
            timestamp.setText("エラー");
        } else {
            timestamp.setText("送信中...");
        }
    }
}
