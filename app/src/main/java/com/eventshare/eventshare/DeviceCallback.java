package com.eventshare.eventshare;

public interface DeviceCallback<T> {
    public void onNewEventReceived(T t);
    public void onAckOnPostEvent(T t);
    public void onMessageSeen(T t);

    public void setGroupAvailable(T t);
    public void onProgressBarUpdate(T t, int percent);

    public void saveFailed();

    public void onNewMember(T t);
}
