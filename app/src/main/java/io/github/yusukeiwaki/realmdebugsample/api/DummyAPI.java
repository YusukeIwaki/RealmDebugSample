package io.github.yusukeiwaki.realmdebugsample.api;

import android.content.Context;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.github.yusukeiwaki.realmdebugsample.model.Message;

public class DummyAPI {
    private final Context mContext;

    public DummyAPI(Context context) {
        mContext = context;
    }

    public static class DummyException extends Exception {
        public DummyException(String message) {
            super(message);
        }
    }

    public Task<Message> sendMessage(final String id, final String username, final String body) {
        final TaskCompletionSource<Message> task = new TaskCompletionSource<>();

        new Thread() {
            @Override
            public void run() {
                boolean isError = DummyAPISetting.get(mContext)
                        .getBoolean(DummyAPISetting.KEY_SEND_MESSAGE_ERROR, false);

                if (!isError) {
                    try {
                        Thread.sleep(1600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Message newMessage = new Message();
                    newMessage.setId(id);
                    newMessage.setUsername(username);
                    newMessage.setBody(body);
                    newMessage.setTimestamp(System.currentTimeMillis());

                    task.setResult(newMessage);
                } else {
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    task.setError(new DummyException("dummy error"));
                }
            }
        }.start();

        return task.getTask();
    }
}
