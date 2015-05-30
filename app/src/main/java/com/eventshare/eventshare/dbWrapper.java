package com.eventshare.eventshare;


import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class dbWrapper {
    public static final String APPLICATION_ID = "fiIq9aboE53Q72ZkwZRgBAgqOx01QOH4LJ00hYKF";
    public static final String CLIENT_KEY = "zidsRUHNMhS9xlC58LqVanljqQdMdZ7V4TTwEZu0";

    static void initParse(android.content.Context context) {
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        Parse.enableLocalDatastore(context);
        Parse.initialize(context, APPLICATION_ID, CLIENT_KEY);
        //  ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    static void createNewChatGroup(){
        ParseObject newGroup = new ParseObject("ChatGroups");
        newGroup.put("adminId", ParseUser.getCurrentUser().getObjectId());
        newGroup.saveInBackground();

        ParseQuery<ParseObject> lastQuery = ParseQuery.getQuery("ChatGroups");
        lastQuery.orderByDescending("createdAt");

        lastQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                ParseObject newMembership = new ParseObject("GroupMembership");
                newMembership.put("groupId", parseObject.getObjectId());
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
}
