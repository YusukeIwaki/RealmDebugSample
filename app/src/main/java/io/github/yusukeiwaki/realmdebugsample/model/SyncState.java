package io.github.yusukeiwaki.realmdebugsample.model;

public interface SyncState {
    int WAIT_FOR_SYNC = 0;
    int SYNCING = 1;
    int SYNCED = 2;
    int ERROR = 3;
}
