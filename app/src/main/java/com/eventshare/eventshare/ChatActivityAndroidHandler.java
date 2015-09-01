package com.eventshare.eventshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.eventshare.eventshare.Activities.MainActivity;

public class ChatActivityAndroidHandler implements DeviceCallback<ChatGroups> {

    private static final String TAG = "ES_DEBUG";
    private Activity mActivity;

    public ChatActivityAndroidHandler(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onNewEventReceived(ChatGroups group) {
        Log.d(TAG, "performing groupAddCallback");

        synchronized (group) {
            if (MainActivity.lvData != null) {
                MainActivity.lvData.addFirst(group);
                MainActivity.lvData.refresh();
            }

        }
        Intent intent = new Intent("bla");
        mActivity.setResult(mActivity.RESULT_OK, intent);
        mActivity.finish();
    }

    @Override
    public void onAckOnPostEvent(ChatGroups chatGroups) {

    }

    @Override
    public void onMessageSeen(ChatGroups chatGroups) {

    }

    @Override
    public void setGroupAvailable(ChatGroups group) {
        //                group.setAvailable();
        MainActivity.lvData.refresh();

    }

    @Override
    public void onProgressBarUpdate(ChatGroups chatGroups, int percent) {

    }

    @Override
    public void saveFailed() {
        Toast.makeText(mActivity,
                "Saving event failed for some reason. Check internet connectivity",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewMember(ChatGroups chatGroups) {

    }
}

