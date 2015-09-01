package com.eventshare.eventshare;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.eventshare.eventshare.Activities.ChatActivity;
import com.eventshare.eventshare.Activities.EventshareApplication;
import com.eventshare.eventshare.Activities.MainActivity;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.codec.binary.Base64;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MyReceiver extends ParsePushBroadcastReceiver {
    private final String TAG = "ES_DEBUG";


//    @Override
//    public void onPushOpen(Context context, Intent intent) {
//        Log.d(TAG, "onPushOpen triggered!");
//        Intent i = new Intent(context, MainActivity.class);
//        i.putExtras(intent.getExtras());
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);
//    }

    @Override
    public void onPushReceive(Context context, Intent intent) {
        JSONObject pushData = null;
        String pushType = "";

        try {
            pushData = new JSONObject(intent.getStringExtra(MyReceiver.KEY_PUSH_DATA));
            pushType = pushData.getString("tag");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String objectId = Utils.getFieldFromJSON(pushData, "objectId");
        String senderId = Utils.getFieldFromJSON(pushData, "userId");

        Log.d(TAG, "received push notification from type:     "
                + pushType
                + "  (objectId = " + objectId + ")"
                + " (senderId = " + senderId + ")"
        );

        switch (pushType) {
            case "MSG_PUSH" :
                handleMessagePush(pushData, context);
                break;
            case "MSG_ACK_PUSH" :
                handleMessageAckPush(pushData, context);
                break;
            case "SEEN_PUSH" :
                handleSeenPush(pushData, context);
                break;
            case "GROUP_PUSH" :
                handleGroupPush(pushData, context);
                break;
//            case "GROUP_ACK_PUSH" :
//                handleGroupAckPush(pushData, context);
            default:
        }
        Intent cIntent = new Intent(MyReceiver.ACTION_PUSH_OPEN);
        cIntent.putExtras(intent.getExtras());
        cIntent.setPackage(context.getPackageName());

//        Intent i = new Intent(context, NotificationHandler.class);
//        PendingIntent pintent = PendingIntent.getActivity(context, 0, i, 0);

    }

    private void handleMessagePush(JSONObject pushData, final Context context){
        String msgId = Utils.getFieldFromJSON(pushData, "objectId");
        String isPhotoAttached = Utils.getFieldFromJSON(pushData, "photoAttached");

        ParseQuery<Message> msgQuery = ParseQuery.getQuery("Message");
        msgQuery.getInBackground(msgId, new GetCallback<Message>() {
            @Override
            public void done(Message msg, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                ChatGroups group = DbWrapper.getGroupFromDevice(msg.getgroupId());
                group.setLastMessage(msg);

                if (msg.getBoolean("isPhotoAttached")) {
                    String base64imageString = msg.getString("attachedImageSmall");

                    String tempFilePath = EventshareApplication.tmpPicsDir + File.separator + msg.getObjectId() + ".jpeg";
                    boolean writeSuccess = Utils.writeImageStringToFile(base64imageString, tempFilePath);
                    if (writeSuccess == false) {
                        Log.v(TAG, "error in writing small image message to file");
                    }
                    msg.put("largeImageFetched", false);
                    msg.put("localImageUri", tempFilePath);
//                    msg.getParseFile("attachedImageSmall").getDataInBackground(new GetDataCallback() {
//                        @Override
//                        public void done(byte[] bytes, ParseException e) {
//
//                        }
//                    });
                }

                DbWrapper.saveMessageToDevice(msg);
                DbWrapper.setGroupLastMessage(group, msg, msg.getCreatedAt());

                showNotification(context, group.getGroupName(), msg.getBody());

                if(EventshareApplication.currentActivity instanceof ChatActivity &&
                        ((ChatActivity) (EventshareApplication.currentActivity)).getGroupId().equals(msg.getgroupId())) {
                    Log.v(TAG, "current chat room is OPENED");
                    ((ChatActivity) (EventshareApplication.currentActivity)).onNewPushMessage(msg);
                } else {
                    Log.v(TAG, "current chat room is CLOSED");
                }

                //app is off
                if(MainActivity.lvData == null) {
                    return;
                } else {
                    MainActivity.lvData.remove(group);
                    MainActivity.lvData.addFirst(group);
                    MainActivity.lvData.refresh();
                }
            }
        });
//        Message msg = DbWrapper.getMessageFromServer(msgId);

    }

    private void handleMessageAckPush(JSONObject pushData, Context context){
        String msgId = Utils.getFieldFromJSON(pushData, "objectId");

        Message msg = DbWrapper.getMessage(msgId);

        msg.put("ackStatus", "receivedOnServer");

        if(EventshareApplication.currentActivity instanceof ChatActivity &&
                ((ChatActivity) (EventshareApplication.currentActivity)).getGroupId().equals(msg.getgroupId())) {
//            Log.d(TAG, "current chat room is OPENED");
            ((ChatActivity) (EventshareApplication.currentActivity)).onNewPushAck(msg);
        }

        //showNotification(context, "ack notification on message:", msg.getBody());


        //app is off
        if(MainActivity.lvData == null) {
            return;
        } else {
            MainActivity.lvData.refresh();
        }

    }

    private void handleGroupPush(JSONObject pushData, Context context) {
        String groupId = Utils.getFieldFromJSON(pushData, "objectId");
        String groupName = Utils.getFieldFromJSON(pushData, "groupName");
        String changeType = Utils.getFieldFromJSON(pushData, "changeType");

        Log.d(TAG, "got group push from type: " + changeType);

        boolean getPictureToo =
                changeType.equalsIgnoreCase(DbWrapper.GROUP_NEW) ||
                changeType.equals(DbWrapper.GROUP_INFO_AND_PICTURE_UPDATE);

        DbWrapper.getGroupFromServer(groupId, getPictureToo);

        ChatGroups group = (ChatGroups) ParseObject.createWithoutData("ChatGroups", groupId);
        group.setGroupName(groupName);
        DbWrapper.saveGroupToDevice(group);

        if (changeType.equals(DbWrapper.GROUP_NEW) ||
                changeType.equals(DbWrapper.GROUP_NEW_MEMBERS)) {
            DbWrapper.saveGroupMembershipFromServer(groupId);
        }

        if (changeType.equals(DbWrapper.GROUP_NEW)) {
            DbWrapper.addGroupToMyGroupsRecord(group);
        }

        if (MainActivity.lvData != null) {
            if (changeType.equals(DbWrapper.GROUP_NEW)) {
                MainActivity.lvData.remove(group);
                MainActivity.lvData.addFirst(group);
            }
            MainActivity.lvData.refresh();
        }

        if (changeType.equals(DbWrapper.GROUP_NEW)) {
            String notificationTitle = "Your were added to a chat group:";
            String notificationBody = group.getGroupName();
            showNotification(context, notificationTitle, notificationBody);
        }
    }

    private void handleSeenPush(JSONObject pushData, Context context) {
        final String seenById = Utils.getFieldFromJSON(pushData, "seenBy");

        if (seenById.equals(ParseUser.getCurrentUser().getObjectId())) {
            Log.d(TAG, "self push. dropping.");
            return;
        }

        final String seenOnTime = Utils.getFieldFromJSON(pushData, "seenOnTime");
        String groupId = Utils.getFieldFromJSON(pushData, "groupId");

        DbWrapper.updateMessagesStatusFromServer(groupId, seenById, seenOnTime);


        if(EventshareApplication.currentActivity instanceof ChatActivity &&
                ((ChatActivity) (EventshareApplication.currentActivity)).getGroupId().equals(groupId)) {
            Log.v(TAG, "current chat room is OPENED");
            ((ChatActivity) (EventshareApplication.currentActivity)).onNewPushSeen();
        } else {
            Log.v(TAG, "current chat room is CLOSED");
        }

        //app is off
        if(MainActivity.lvData == null) {
            return;
        } else {
            MainActivity.lvData.refresh();
        }
       // showNotification(context, "seen By Notif", groupId + " seenBy " + seenById + " on " + seenOnTime);

    }

    private void showNotification(Context context, String title, String body){
        if(!EventshareApplication.isForeground) {
            Log.d(TAG,"application is in background, showing notification");
        } else {
             Log.d(TAG,"application is in foreground, no notification shown");
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setSmallIcon(R.drawable.messenger_bubble_small_blue);
        builder.setAutoCancel(true);


//        if(!f.exists())
//        {
//            groupPic.setImageDrawable(getContext().getResources().getDrawable(R.drawable.no_group_image));
//        } else {
//            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
//            groupPic.setImageBitmap(bitmap); // TODO: add caching
//        }

        // OPTIONAL create soundUri and set sound:
        // builder.setSound(soundUri);

        notificationManager.notify("MyTag", 0, builder.build());
    }
}
