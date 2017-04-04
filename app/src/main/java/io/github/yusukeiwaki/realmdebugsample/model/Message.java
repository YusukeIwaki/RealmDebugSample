package io.github.yusukeiwaki.realmdebugsample.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Message extends RealmObject {
    @PrimaryKey
    public String id;
    public int syncstate;
    public String username;
    public String body;
    public long timestamp;
}
