package com.eventshare.eventshare;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.eventshare.eventshare.Activities.ChatActivity;
import com.eventshare.eventshare.Activities.EventshareApplication;
import com.eventshare.eventshare.Activities.MainActivity;
import com.eventshare.eventshare.Activities.ShowSearchEventResultsActivity;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DbWrapper {
    private final static String TAG = "ES_DEBUG";
    public static final String GROUP_NEW = "GROUP_NEW";
    public static final String GROUP_INFO_AND_PICTURE_UPDATE = "GROUP_INFO_AND_PICTURE_UPDATE";
    public static final String GROUP_INFO_UPDATE = "GROUP_INFO_UPDATE";
    public static final String GROUP_NEW_MEMBERS = "GROUP_NEW_MEMBERS";
    public static final String GROUP_REMOVE_MEMBER = "GROUP_REMOVE_MEMBER";

    private static Context mContext;
//    public static MyGroupsRecord mgr;

    public static final String APPLICATION_ID = "fiIq9aboE53Q72ZkwZRgBAgqOx01QOH4LJ00hYKF";
    public static final String CLIENT_KEY = "zidsRUHNMhS9xlC58LqVanljqQdMdZ7V4TTwEZu0";

    static public void initParse(android.content.Context context) {
        mContext = context;

        ParseObject.registerSubclass(ChatGroups.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(MessagesStatus.class);
        ParseObject.registerSubclass(GroupMembership.class);
        ParseObject.registerSubclass(MyGroupsRecord.class);
		ParseObject.registerSubclass(MyFriend.class);
		
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, APPLICATION_ID, CLIENT_KEY);
        ParseFacebookUtils.initialize(context);
        ParseUser.enableRevocableSessionInBackground();

        //already done in update user details
        // ParseInstallation.getCurrentInstallation().saveInBackground();

        ParseQuery<MyGroupsRecord> groupsRecordQuery = ParseQuery.getQuery("MyGroupsRecord");
        groupsRecordQuery.fromLocalDatastore();
    }

    static public void setGroupMyLastActivity(ChatGroups group){
        ParseQuery<MyGroupsRecord> updateQuery = ParseQuery.getQuery("MyGroupsRecord");
        updateQuery.fromLocalDatastore();
        updateQuery.whereEqualTo("group", group);


        try {
            MyGroupsRecord myGroupsRecord = updateQuery.getFirst();
            myGroupsRecord.put("lastActivity", Utils.getCurrentDate());

            myGroupsRecord.pinInBackground();
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }
    static public void setGroupLastMessage(ChatGroups group, final Message msg, Date date){
        ParseQuery<MyGroupsRecord> updateQuery = ParseQuery.getQuery("MyGroupsRecord");
        updateQuery.fromLocalDatastore();
        updateQuery.whereEqualTo("group", group);


        try {
            MyGroupsRecord myGroupsRecord = updateQuery.getFirst();
            myGroupsRecord.put("lastMessage", msg);
            myGroupsRecord.put("lastUpdate", Utils.getCurrentDate());
            myGroupsRecord.put("lastMessageDate", date);

            myGroupsRecord.pinInBackground();
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }

    static public MyGroupsRecord getGroupLastRecord(ChatGroups group) {
        ParseQuery<MyGroupsRecord> updateQuery = ParseQuery.getQuery("MyGroupsRecord");
        updateQuery.fromLocalDatastore();
        updateQuery.whereEqualTo("group", group);

        try {
            return (MyGroupsRecord) updateQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    static public MyGroupsRecord getMyGroupsRecord(ChatGroups group) {
        ParseQuery<MyGroupsRecord> updateQuery = ParseQuery.getQuery("MyGroupsRecord");
        updateQuery.fromLocalDatastore();
        updateQuery.whereEqualTo("group", group);

        try {
            return updateQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    static public List<ParseUser> getGroupUsers(ChatGroups group) {
        ParseQuery<GroupMembership> usersQuery = ParseQuery.getQuery("GroupMembership");
        usersQuery.fromLocalDatastore();
        usersQuery.whereEqualTo("groupId", group.getObjectId());

        List<GroupMembership> memberships = null;
        try {
            memberships =  usersQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<ParseUser> users = new ArrayList<>(memberships.size());

        for (GroupMembership gm : memberships) {
            users.add((ParseUser)gm.get("user"));
        }

        return users;
    }


    static public List<ParseUser> getGroupUsersFromServer(ChatGroups group) {
        ParseQuery<GroupMembership> usersQuery = ParseQuery.getQuery("GroupMembership");
        usersQuery.whereEqualTo("groupId", group.getObjectId());

        List<GroupMembership> memberships = null;
        try {
            memberships =  usersQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<ParseUser> users = new ArrayList<>(memberships.size());

        for (GroupMembership gm : memberships) {
            users.add((ParseUser)gm.get("user"));
        }

        return users;
    }


    static private void saveGroupMembershipToDevice(List<ParseObject> memberships){

        ParseObject.pinAllInBackground(memberships, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.v(TAG, "pinned memberships to device");
                } else {
                    e.printStackTrace();
                }
            }
        });

        List<ParseUser> users = new ArrayList<>(memberships.size());

        for (ParseObject gm : memberships) {
            ParseUser u = (ParseUser) gm.get("user");

            if (u.hasSameId(ParseUser.getCurrentUser())) {
                continue;
            }

            if (u == null) {
                Log.w(TAG, "saveGroupMembershipToDevice(): null user from group membership. this shouldn't happen!!");
                continue;
            }

            String localPath = EventshareApplication.profilePicsDir + File.separator + u.getObjectId().toString()+".png";
            u.put("localImagePath",localPath);
            users.add(u);

//            PraseUserImageDownloader pid = new PraseUserImageDownloader();
//            new PraseUserImageDownloader(mContext).execute(u);
            downloadProfilePic(u);
        }

        ParseObject.pinAllInBackground(users, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.v(TAG, "pinned users to device");
                } else {
                    e.printStackTrace();
                }
            }
        });

    }


    /************* TO/FROM SERVER ROUTINES ***************/


    static public void searchForEventByQueries(List<ParseQuery<ChatGroups>> queries,
                                               final ShowSearchEventResultsActivity.PostSearch postSearchCallback) {

        ParseQuery<ChatGroups> query = ParseQuery.or(queries);

        query.findInBackground(new FindCallback<ChatGroups>() {
            @Override
            public void done(List<ChatGroups> groups, ParseException e) {

                ParseObject.fetchAllIfNeededInBackground(groups, new FindCallback<ChatGroups>() {

                    @Override
                    public void done(List<ChatGroups> chatGroupses, ParseException e) {
                        if (e == null) {
                            postSearchCallback.execute(chatGroupses);
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    static public ParseQuery<ChatGroups> getSearchByNearGeoPointQuery(ParseGeoPoint center, double searchRadius) {
        ParseQuery<ChatGroups> groupQuery = ParseQuery.getQuery("ChatGroups");
        groupQuery.whereWithinKilometers("groupLocation", center, searchRadius);
        groupQuery.whereGreaterThanOrEqualTo("takingPlaceOn", Utils.toISO8061Date2(new Date()));

        return groupQuery;
    }


    static public List<ChatGroups> searchForRelevantEvents() { //TODO
//        ParseQuery<ChatGroups> groupQuery = ParseQuery.getQuery("ChatGroups");
//
//        groupQuery.whereContainedIn("keywords", keywords);

        List<ChatGroups> result = null;
//        try {
//            result = groupQuery.find();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        return result;
    }

    static public ParseQuery<ChatGroups> getSearchByKeywordsQuery(List<String> keywords) {
        ParseQuery<ChatGroups> groupQuery = ParseQuery.getQuery("ChatGroups");
        groupQuery.whereContainedIn("keywords", keywords);
        groupQuery.whereGreaterThanOrEqualTo("takingPlaceOn", Utils.toISO8061Date2(new Date()));

        return groupQuery;
    }



    static public ParseQuery<ChatGroups> getSearchByTimeRangeQuery(Date start, Date end) {
        ParseQuery<ChatGroups> groupQuery = ParseQuery.getQuery("ChatGroups");

        groupQuery.whereGreaterThanOrEqualTo("takingPlaceOn", Utils.toISO8061Date2(start));
        groupQuery.whereLessThanOrEqualTo("takingPlaceOn", Utils.toISO8061Date2(end));

        return groupQuery;
    }


    static public Message getMessageFromServer(String msgId) {
        ParseQuery<Message> msgQuery = ParseQuery.getQuery("Message");
        Message msg = null;
        try {
            msg = msgQuery.get(msgId);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return msg;
    }

    static public ChatGroups getGroupFromServer(final String groupId, boolean getPictureToo) {

        try {
            ParseQuery<ChatGroups> query = ParseQuery.getQuery("ChatGroups");
            final ChatGroups group = query.get(groupId); //from server!

            if (getPictureToo) {

                //download small image anyway

                String base64imageString = group.getString("groupPicSmall");
                String filePath = EventshareApplication.groupPicsDir + File.separator + groupId.toString() + "_small.png";
                boolean writeSuccess = Utils.writeImageStringToFile(base64imageString, filePath);

                if (writeSuccess == false) {
                    Log.v(TAG, "error in writing small image message to file");
                } else {
                    group.setGroupPicPath(filePath, ChatGroups.GROUP_SMALL_PIC);
                }

                //download large image in background
                ParseFile groupImage = group.getParseFile("groupPic");
                groupImage.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        String localPath = EventshareApplication.groupPicsDir + File.separator + groupId.toString() + ".png";
                        File targetFile = new File(localPath);
                        BufferedOutputStream bos = null;
                        try {
                            bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            bos.write(bytes);
                            bos.flush();
                            bos.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        Log.d(TAG, "new group: fetched image and saved to device");
                        group.setGroupPicPath(localPath, ChatGroups.GROUP_LARGE_PIC);

                        if (MainActivity.lvData != null) {
                            MainActivity.lvData.refresh();
                        }
                    }
                });
            }
            return group;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    static void saveGroupMembershipFromServer(String groupId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMembership");
        query.whereEqualTo("groupId", groupId);
        query.include("user");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> memberships, ParseException e) {
                if (e == null) {
                    saveGroupMembershipToDevice(memberships);
                } else {
                    Log.v(TAG, "error getting group members from server");
                    e.printStackTrace();
                }
            }
        });
    }

//
//    static public List<ParseObject> getGroupMembershipFromServer(String groupId){
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMembership");
//        query.whereEqualTo("groupId", groupId);
//        query.include("user");
//        List<ParseObject> membershipList = null;
//        try {
//            membershipList = query.find();
//            Log.d(TAG, "retrieved " + membershipList.size() + " members in group");
//        } catch (ParseException e) {
//            Log.v(TAG, "error getting group members from server");
//            e.printStackTrace();
//        }
//
//        return membershipList;
//    }

    static public void updateMessagesStatusFromServer(final String groupId, String seenById, String seenOnTime){
        Date seenOnTimeDateObj = Utils.getISO8061Date(seenOnTime);

        ParseQuery<MessagesStatus> query = ParseQuery.getQuery("MessagesStatus");
        query.whereEqualTo("groupId", groupId);
        query.whereEqualTo("targetId", seenById);
        query.whereEqualTo("msgAuthor", ParseUser.getCurrentUser().getObjectId());
        query.whereLessThanOrEqualTo("sentOn", seenOnTimeDateObj);
        query.whereEqualTo("fetched", false);
       // query.include("message");

        query.findInBackground(new FindCallback<MessagesStatus>() {
            @Override
            public void done(final List<MessagesStatus> msList, ParseException e) {
                if(e == null) {
                    Log.d(TAG, "query result length = " + msList.size());
                    if(msList.isEmpty()){
                        return;
                    }

                    LinkedList<String> messagesIds = new LinkedList<>();

                    for(MessagesStatus ms : msList) {
                        ms.put("fetched", true);

                        String mId = (String) ms.get("messageId");
                        messagesIds.add(mId);
                    }

                    ParseQuery<Message> msgsQuery = ParseQuery.getQuery("Message");
                    msgsQuery.whereContainedIn("objectId", messagesIds);

                    msgsQuery.findInBackground(new FindCallback<Message>() {
                        @Override
                        public void done(final List<Message> msgList, ParseException e) {
                            if (e == null) {
                                for (Message m : msgList) {
                                    if (m.getInt("unseenCount") == -1) {
                                        Log.v(TAG, "msg " + m.getObjectId() + " is seen by all now!");
                                        m.put("isSeenByAll", true);
//                                        m.setBody(m.getBody() + " |v");
                                        m.put("ackStatus", "seenByAll");
                                    }
                                    m.increment("unseenCount", 1);
                                }

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

                                ParseObject.pinAllInBackground(msgList, new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Log.d(TAG, "pinned all seen by updates(" + msgList.size() + ") to msgs.");
                                        } else {
                                            Log.d(TAG, "error pinning seen by updates(" + msgList.size() + ") to msgs.");
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });




                    ParseObject.pinAllInBackground(msList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "pinned all seen by updates(" + msList.size() + ").");
                            } else {
                                Log.d(TAG, "error pinning seen by updates(" + msList.size() + ").");

                                e.printStackTrace();
                            }
                        }
                    });

                    ParseObject.saveAllInBackground(msList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "marked fetched on  the seen by updates(" + msList.size() + ") on server.");
                            } else {
                                Log.d(TAG, "error marking fetched seen by updates(" + msList.size() + ") on server.");
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "error querying seen by updates");
                    e.printStackTrace();
                }
            }
        });

    }

    static public void createMessageStatusOnServerAndDevice(Message msg){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMembership");
        query.fromLocalDatastore();
        query.whereEqualTo("groupId", msg.getgroupId());
        query.whereNotEqualTo("userId", ParseUser.getCurrentUser().getObjectId()); //TODO:change to pointers??

        List<ParseObject> gs = null;
        try {
            gs = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final LinkedList<MessagesStatus> status = new LinkedList<>();
        for (ParseObject membership : gs) {
            MessagesStatus ms = new MessagesStatus();

            // THIS POINTER PROBLEMS!!! ON EVERY SAVE OF ms IS SAVES THE MESSAGE AS WELL
            ms.put("messageId", msg.getObjectId());
            ms.put("msgAuthor", ParseUser.getCurrentUser().getObjectId());
            ms.put("targetId", ((ParseUser) membership.get("user")).getObjectId());
            ms.put("sentOn", msg.getCreatedAt());
            ms.put("groupId", msg.getgroupId());
            ms.put("fetched", false);
            status.add(ms);
        }

        ParseObject.pinAllInBackground(status, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    Log.v(TAG, "pinned all msg targets(" + status.size() + ").");
                } else {
                    Log.d(TAG, "error in pinning msg targets(" + status.size() + ").");
                    e.printStackTrace();
                }
            }
        });

        ParseObject.saveAllInBackground(status, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    Log.v(TAG, "SAVED STATUSES(" + status.size() + ").");
                } else {
                    Log.v(TAG, "saving STATUSES failed(" + status.size() + ").");
                    e.printStackTrace();
                }
            }
        });
    }


    /************* TO/FROM DEVICE ROUTINES ***************/

    static public Message getMessage(String msgId) {
        Message msg = null;
        ParseQuery<Message> query = ParseQuery.getQuery("Message");
        query.fromLocalDatastore();

       // Log.i(TAG, "getting message " + msgId + " from device");

        try {
            // msg.fetchIfNeeded();
            msg = query.get(msgId);

        } catch (ParseException e) {
            Log.i(TAG, "msg not found on device. fetching from server");
            msg = getMessageFromServer(msgId);
        }
        return msg;
    }

    static public ChatGroups getGroupFromDevice(String groupId) {
        ChatGroups group = null;
        ParseQuery<ChatGroups> query = ParseQuery.getQuery("ChatGroups");
        query.fromLocalDatastore();

        try {
            group = query.get(groupId);
        } catch (ParseException e) {
            Log.i(TAG, "group not found on device. now FUCK YOU");
            e.printStackTrace();
        }

        return group;
    }

    static public List<Message> getGroupMessages(ChatGroups group) {
        List<Message> messages = null;
        ParseQuery<Message> query = ParseQuery.getQuery("Message");
        query.fromLocalDatastore();
        query.whereEqualTo("groupId", group.getObjectId());
        // Configure limit and sort order
        //query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        query.orderByAscending("createdAt");

        try {
            messages = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return messages;
    }

    static public boolean isInGroup(ChatGroups group) {
        ParseQuery<MyGroupsRecord> myGroupsQuery = ParseQuery.getQuery("MyGroupsRecord");
        myGroupsQuery.fromLocalDatastore();

        myGroupsQuery.whereEqualTo("group", group);

        boolean result = false;
        try {
            MyGroupsRecord gr = myGroupsQuery.getFirst();
            result = (gr != null) ? true : false;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    static public ParseUser getParseUserFromDevice(String userId) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();

        try {
            return userQuery.get(userId);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
    static public List<ChatGroups> getUserChatGroups() {
//        ParseUser user = ParseUser.getCurrentUser();
//        List<ParseObject> membershipList = getAllUserMemberships(user);
//        List<String> groupIds = new LinkedList<>();
//        ListIterator<ParseObject> itr = membershipList.listIterator();
//
//        while(itr.hasNext()) {
//            groupIds.add((String) itr.next().get("groupId"));
//        }
//
        ParseQuery<MyGroupsRecord> myGroupsQuery = ParseQuery.getQuery("MyGroupsRecord");
        myGroupsQuery.fromLocalDatastore();
        myGroupsQuery.orderByDescending("lastUpdate");

        List<ChatGroups> userChatGroups = new LinkedList<>();

        List<MyGroupsRecord> mgr = null;
        try {
            mgr = myGroupsQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (MyGroupsRecord gr : mgr){
            ChatGroups group = (ChatGroups)gr.get("group");
            try {
                group.fetchFromLocalDatastore();
                userChatGroups.add(group);
                if (!group.isDataAvailable()){
                    Log.d(TAG, "groups data not availble");
                }
            } catch (ParseException e) {
//                group.unpinInBackground();
//                gr.unpinInBackground();
                e.printStackTrace();
            }

        }

        Log.d(TAG, "getUserChatGroups() returning  " + userChatGroups.size() + " groups");


//        myGroupsQuery.findInBackground(new FindCallback<MyGroupsRecord>() {
//            @Override
//            public void done(List<MyGroupsRecord> myGroupsRecords, ParseException e) {
//                if(e == null) {
//                    Log.d(TAG, "found " + myGroupsRecords.size() + " on device");
//
//                    for(MyGroupsRecord mgr : myGroupsRecords) {
//                        Log.d(TAG, ((ChatGroups)mgr.get("group")).getObjectId() + " on device");
//                    }
//
//                } else {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//
//
//        ParseQuery<ChatGroups> query = ParseQuery.getQuery("ChatGroups");
//        query.fromLocalDatastore();
//        query.orderByDescending("updatedAt");
////        query.whereContainedIn("objectId", groupIds);
//        query.whereMatchesKeyInQuery("objectId", "group", myGroupsQuery);
//
//        try {
//            userChatGroups =  query.find();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        return userChatGroups;










//        ParseQuery innerQuery = new ParseQuery("ChatGroups");
//        innerQuery.fromLocalDatastore();
//        innerQuery.orderByDescending("updatedAt");
//
//        ParseQuery query = new ParseQuery("MyGroupsRecord");
//        query.fromLocalDatastore();
//
//        query.whereMatchesQuery("group", innerQuery);
//        query.findInBackground(new FindCallback() {
//            public void done(List<ChatGroups> groups, ParseException e) {
//
//            }
//        });
    }


    static public List<ParseObject> getGroupMembershipFromDevice(String groupId){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMembership");
        query.whereEqualTo("groupId", groupId);
        query.fromLocalDatastore();
        List<ParseObject> membershipList = null;
        try {
            membershipList = query.find();
            Log.d("Eventshare", "retrieved " + membershipList.size() + " members in group");
        } catch (ParseException e) {
            Log.d("Eventshare1", "error getting group members from server");
            e.printStackTrace();
        }

        return membershipList;
    }

    static public void saveMessageToDevice(final Message msg){
        msg.pinInBackground(msg.getgroupId(), new SaveCallback() {
            @Override
            public void done(ParseException e) {
            if (e == null) {
                Log.d(TAG, "saved msg to device");

            } else {
                Log.d(TAG, "error saving message to device");
                e.printStackTrace();
            }
            }
        });
    }
//
//    static public void saveGroupToDevice(final ChatGroups group, String author) {
//        if (!author.equals(ParseUser.getCurrentUser().getObjectId())) {
//            saveGroupToDevice(group);
//            //app is off
//            if (MainActivity.lvData == null) {
//                return;
//            }
//
//            MainActivity.lvData.remove(group);
//            MainActivity.lvData.addFirst(group);
//            MainActivity.lvData.refresh();
//
//        }
//    }


    static public void saveGroupToDevice(final ChatGroups group) {

        group.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v(TAG, "saving group to device done: " + group.getGroupName());

                } else {
                    Log.d(TAG, "error saving group to device");
                    e.printStackTrace();
                }
            }

//        group.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    Log.d(TAG, "saving group to device done: " + group.getGroupName());
//
//                } else {
//                    Log.d(TAG, "error saving group to device");
//                    e.printStackTrace();
//                }
//            }
        });

    }


    static public void addGroupToMyGroupsRecord(ChatGroups group) {
        MyGroupsRecord gr = new MyGroupsRecord();
        gr.put("lastUpdate", Utils.getCurrentDate());

        gr.put("group", group);
//        gr.put("lastMessage", 0);
        gr.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.d(TAG, "pinned new group record");
                } else {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        });

    }

    /************* REST CALLBACKS ROUTINES ***************/

    static public void addNewGroupToServer(final ChatGroups group, List<String> members,
                                           final DeviceCallback<ChatGroups> deviceCallback) {

        //        members.add("129946754013097");
        addOrUpdateGroupOnServer(group, members, deviceCallback, GROUP_NEW);
    }

    static public void updateGroupInfoOnServer(final ChatGroups group,
                                               final DeviceCallback<ChatGroups> deviceCallback) {
        String updateType;
        if (group.isDirty("groupPic")) {
            updateType = GROUP_INFO_AND_PICTURE_UPDATE;
        } else {
            updateType = GROUP_INFO_UPDATE;
        }
        addOrUpdateGroupOnServer(group, new ArrayList<String>(), deviceCallback,  updateType);
    }

    static public void removeUserFromGroupOnServer(final ChatGroups group, List<String> members,
                                                  final DeviceCallback<ChatGroups> deviceCallback) {

//        Log.d(TAG, "trying to add user   " + members.get(0));
        addOrUpdateGroupOnServer(group, members, deviceCallback, GROUP_REMOVE_MEMBER);
    }

    static public void addMemebersToGroupOnServer(final ChatGroups group, List<String> members,
                                           final DeviceCallback<ChatGroups> deviceCallback) {

//        Log.d(TAG, "trying to add user   " + members.get(0));
        addOrUpdateGroupOnServer(group, members, deviceCallback, GROUP_NEW_MEMBERS);
    }

    static public void addOrUpdateGroupOnServer(final ChatGroups group, List<String> members,
                                    final DeviceCallback<ChatGroups> deviceCallback,
                                    final String objectStatus){

        final ParseFile groupPicFile =  (group.getParseFile("groupPic"));
        group.put("objectStatus", objectStatus);

        if (objectStatus.equals(GROUP_NEW) || objectStatus.equals(GROUP_NEW_MEMBERS)) {
            List<String> old = group.getList("members"); //if no members - will be NULL
//            Log.d(TAG, "old memebres list size : " + old.size());
//            group.remove("members");
            group.addAllUnique("members", members);
        }

        SaveCallback groupSave = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (deviceCallback != null) {
                        deviceCallback.saveFailed();
                    }
                    e.printStackTrace();
                    return;
                }
                group.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null) {

                            if (objectStatus.equals(GROUP_NEW) ||
                                objectStatus.equals(GROUP_INFO_AND_PICTURE_UPDATE) ||
                                objectStatus.equals(GROUP_NEW_MEMBERS)){

                                String localPath =
                                        EventshareApplication.groupPicsDir +
                                        File.separator +
                                        group.getObjectId().toString();

                                String oldName = group.getGroupPicLocalPath(ChatGroups.GROUP_LARGE_PIC);
                                Utils.renameFile(oldName, localPath + ".png");
                                group.setGroupPicPath(localPath + ".png", ChatGroups.GROUP_LARGE_PIC);

                                Utils.writeImageStringToFile(group.getString("groupPicSmall"), localPath + "_small.png");
                                group.setGroupPicPath(localPath + "_small.png", ChatGroups.GROUP_SMALL_PIC);
                            }

                            if (objectStatus.equals(GROUP_NEW) ||
                                    objectStatus.equals(GROUP_NEW_MEMBERS)){
                                addGroupToMyGroupsRecord(group);
                                saveGroupToDevice(group);
                            }

                            if (    objectStatus.equals(GROUP_NEW) ||
                                    objectStatus.equals(GROUP_NEW_MEMBERS) ||
                                    objectStatus.equals(GROUP_NEW_MEMBERS)){
                                saveGroupMembershipFromServer(group.getObjectId());
                            }

                            if (deviceCallback != null) {
                                deviceCallback.setGroupAvailable(group);
                            }
                        } else {
                            if (deviceCallback != null) {
                                deviceCallback.saveFailed();
                            }
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        if (    objectStatus.equals(GROUP_NEW) ||
                objectStatus.equals(GROUP_INFO_AND_PICTURE_UPDATE) ||
                objectStatus.equals(GROUP_NEW_MEMBERS)) {
            groupPicFile.saveInBackground(groupSave);
        } else {
            groupSave.done(null);
        }

        if(deviceCallback != null) {
            deviceCallback.onNewEventReceived(group);
        }

    }

    static public void postMessageToServer(final Message msg, final DeviceCallback<Message> deviceCallback){
        final ParseFile attachedImage =  (msg.getBoolean("isPhotoAttached"))
                ? (ParseFile) msg.get("attachedImage") : null;

        SaveCallback msgSaveCallback = new SaveCallback(){
            @Override
            public void done(ParseException e1){
                if(e1 == null) {

                    msg.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e2) {
                            if (e2 == null) {
                                DbWrapper.createMessageStatusOnServerAndDevice(msg);

                                if (deviceCallback != null) {
                                    deviceCallback.onAckOnPostEvent(msg);
                                }

                                msg.pinInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Log.v(TAG, "I created a msg locally and pinned it");
                                        } else {
                                            Log.d(TAG, "I created a msg locally BUT pinning FAILED");

                                        }
                                    }
                                });
                            } else {
                                e2.printStackTrace();
                            }
                        }
                    });

                    if (deviceCallback != null) {
                        deviceCallback.onNewEventReceived(msg);
                    }
                } else {
                    e1.printStackTrace();
                }
            }
        };

        ProgressCallback msgProgressCallback = new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                Log.d(TAG, "progress is " + integer + "%");
                if (msg.canceledProgress) {
                    assert attachedImage != null;
                    attachedImage.cancel();
                }
                if(deviceCallback != null) {
                    deviceCallback.onProgressBarUpdate(msg, integer);
                }
            }
        };

        if (msg.getBoolean("isPhotoAttached")){
            attachedImage.saveInBackground(msgSaveCallback, msgProgressCallback);
        } else {
            msgSaveCallback.done(null);
        }

    }

    static public void startImageDownload(final Message msg, ChatActivity context) {
        msg.put("newMessage", true);
        final DeviceCallback<Message> deviceCallback = context.messageDeviceCallback;

        final ParseFile attachedImage = msg.getParseFile("attachedImage");

        ProgressCallback msgProgressCallback = new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                Log.d(TAG, "progress is " + integer + "%");
                if (msg.canceledProgress) {
                    assert attachedImage != null;
                    attachedImage.cancel();
                }
                if(deviceCallback != null) {
                    deviceCallback.onProgressBarUpdate(msg, integer);
                }
            }
        };

        GetDataCallback downloadCompleteCallback = new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {

                String filePath = EventshareApplication.groupPicsDir + File.separator + msg.getObjectId() + ".jpeg";
                Utils.writeImageStringToFile(bytes, filePath);
                msg.put("newMessage", false);
                msg.put("largeImageFetched", true);
                msg.put("localImageUri", filePath);

                deviceCallback.onNewEventReceived(msg);
            }
        };

        attachedImage.getDataInBackground(downloadCompleteCallback, msgProgressCallback);
    }

    //sends if necessary
    static public void sendSeenAllMessagesUpToNow(final ChatGroups group) {

        MyGroupsRecord mgr = getMyGroupsRecord(group);

        if (mgr == null){
            return;
        }

        Date groupLastUpdate = mgr.getDate("lastUpdate");
        Date myLastActivityTime = mgr.getDate("lastActivity");

        if (myLastActivityTime == null ||
                groupLastUpdate == null ||
                myLastActivityTime.after(groupLastUpdate)){
            Log.d(TAG ,"markSeenAllMessagesUpToNow:  NO NEED");
            return;
        }

        final HashMap<String, Object> params = new HashMap<>();

        params.put("groupId", group.getObjectId());
        params.put("seenBy", ParseUser.getCurrentUser().getObjectId());
        params.put("seenOnTime", Utils.toISO8061Date(new Date()));
//        params.put("lastMsgId", DbWrapper.getGroupLastMessage(group).getObjectId());

        ParseCloud.callFunctionInBackground("markSeenAllMessagesUpToNow", params, new FunctionCallback<String>() {
            @Override
            public void done(String sts, ParseException e) {
                if (e == null) {
                    Log.d(TAG ,"markSeenAllMessagesUpToNow successful");

                    /*if (lastMs != null) {
                       group.put("lastMessageSeen", lastMs.get("messageId"));
                       group.pinInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null) {
                                    Log.d(TAG, "save group last message ack to device success");
                                } else {
                                    Log.d(TAG, "save group last message ack to device failed");

                                }
                            }
                        });
                    }*/
                } else {
                    Log.d(TAG, "markSeenAllMessagesUpToNow failed");
                    e.printStackTrace();
                }
            }
        });
    }


    public static void downloadProfilePic(final ParseUser user) {
        new AsyncTask<ParseUser, Void, Void> (){

            @Override
            protected Void doInBackground(ParseUser... params) {
//                ParseUser user = params[0];
                String url = user.getString("profilePicUri");
                String localPath = user.getString("localImagePath");

                // initilize the default HTTP client object
                final DefaultHttpClient client = new DefaultHttpClient();

                //forming a HttoGet request
                final HttpGet getRequest = new HttpGet(url);
                try {

                    HttpResponse response = client.execute(getRequest);

                    //check 200 OK for success
                    final int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != HttpStatus.SC_OK) {
                        Log.w("ImageDownloader", "Error " + statusCode +
                                " while retrieving bitmap from " + url);
                    }

                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        OutputStream outputStream = null;
                        try {
                            try {
                                outputStream = new FileOutputStream(localPath);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            try {
                                entity.writeTo(outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            entity.consumeContent();
                        }

                        Log.d(TAG, "finnished downloading user pic");
                    }
                } catch (Exception e) {
                    // You Could provide a more explicit error message for IOException
                    getRequest.abort();
                    Log.e("ImageDownloader", "Something went wrong while" +
                            " retrieving bitmap from " + url + e.toString());
                }
                return null;
            }
        }.execute();
    }


}