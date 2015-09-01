package com.eventshare.eventshare;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by tivan on 8/12/2015.
 */
@ParseClassName("MyFriend")
public class MyFriend extends ParseObject {
    public void setFriendName(String friendName) {
        put("friendName", friendName);
    }
    public String getFriendName() {
        return getString("friendName");
    }
    public void setFriendID(String friendID) {
        put("friendID", friendID);
    }
    public String getFriendID() {
        return getString("friendID");
    }
    public void setFriendPic(ParseFile friendPic){
        put("friendPic",friendPic);
    }
    public ParseFile getFriendPic(){
        return getParseFile("friendPic");
    }
}
