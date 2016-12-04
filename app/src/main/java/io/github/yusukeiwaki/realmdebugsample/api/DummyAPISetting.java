package io.github.yusukeiwaki.realmdebugsample.api;

import android.content.Context;
import android.content.SharedPreferences;

public class DummyAPISetting {
    public static final String KEY_SEND_MESSAGE_ERROR = "send_message_error";

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("dummy_api", Context.MODE_PRIVATE);
    }
}
