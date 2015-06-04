package com.eventshare.eventshare;


import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class DbWrapper {
    public static final String APPLICATION_ID = "fiIq9aboE53Q72ZkwZRgBAgqOx01QOH4LJ00hYKF";
    public static final String CLIENT_KEY = "zidsRUHNMhS9xlC58LqVanljqQdMdZ7V4TTwEZu0";

    static void initParse(android.content.Context context) {

        ParseObject.registerSubclass(ChatGroups.class);
        ParseObject.registerSubclass(Message.class);

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, APPLICATION_ID, CLIENT_KEY);
        ParseFacebookUtils.initialize(context);

        //  ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    static void createNewChatGroup(final ChatGroups newGroup) {
        newGroup.saveInBackground(new SaveCallback() {


//        ParseQuery<ChatGroups> lastQuery = ParseQuery.getQuery("ChatGroups");
//        lastQuery.orderByDescending("createdAt");


//        try {
//            newGroup = lastQuery.getFirst();
//            ParseObject newMembership = new ParseObject("GroupMembership");
//            newMembership.put("groupId", newGroup.getObjectId());
//            newMembership.put("userId", ParseUser.getCurrentUser().getObjectId());
//            newMembership.save();
//        } catch (ParseException e) {
//            Log.d("Eventshare", e.toString());
//            e.printStackTrace();
//            return;
//        }

            @Override
            public void done(ParseException e) {
                ParseObject newMembership = new ParseObject("GroupMembership");
                newMembership.put("groupId", newGroup.getObjectId());
                newMembership.put("userId", ParseUser.getCurrentUser().getObjectId());
                newMembership.saveInBackground();
            }
        });
    }

    static void addMemberToGroup(String userId, String groupId){
        ParseObject newMembership = new ParseObject("GroupMembership");
        newMembership.put("groupId", groupId);
        newMembership.put("userId", userId);
        newMembership.saveInBackground();
    }

    static List<ChatGroups> getUserChatGroups(ParseUser user){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupMembership");
        query.whereEqualTo("userId", user.getObjectId());
        query.orderByDescending("createdAt");

        List<ParseObject> membershipList = null;
        try {
            membershipList = query.find();
        } catch (ParseException e) {
            Log.d("Eventshare1", e.toString());
            e.printStackTrace();
        }

        List<String> groupIds = new LinkedList<>();
        ListIterator<ParseObject> itr = membershipList.listIterator();

        while(itr.hasNext()) {
            groupIds.add((String) itr.next().get("groupId"));
        }

        List<ChatGroups> userChatGroups = null;
        ParseQuery<ChatGroups> query2 = ParseQuery.getQuery("ChatGroups");
        query2.orderByDescending("updatedAt");
        query2.whereContainedIn("objectId", groupIds);
//        ListIterator<ParseObject> itr = membershipList.listIterator();
//        while(itr.hasNext()){
//            query2.whereEqualTo("objectId", itr.next().get("groupId"));
            try {
                userChatGroups =  query2.find();
                //query2.findInBackground(new FindCallback<ChatGroups>() {
//                    @Override
//                    public void done(List<ChatGroups> chatGroupses, ParseException e) {
//                        userChatGroups = chatGroupses;
                        Log.d("Eventshare", "retrieved " + userChatGroups.size() + " groups");
//                    }
//                });
//                userChatGroups.add(query2.getFirst()); //optimize, all in once..
            } catch (ParseException e) {
            Log.d("Eventshare2", e.toString());
                e.printStackTrace();
        }
//        }
        return userChatGroups;
    }
}
