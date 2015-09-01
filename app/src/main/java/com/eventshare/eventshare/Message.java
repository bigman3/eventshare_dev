package com.eventshare.eventshare;

import android.util.Pair;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.HashMap;
import java.util.List;

@ParseClassName("Message")
public class Message extends ParseObject{
        public int imageProgress = 0;
        public boolean canceledProgress = false;

        public String getUserId() {
	        return getString("userId");
	    }
        public String getgroupId() { return getString("groupId");}
        public String getBody() { return getString("body"); }

        public void setUserId(String userId) {
	        put("userId", userId);  
	    }
        public void setGroupId(String groupId) {
        put("groupId", groupId);
    }
        public void setBody(String body) {
	        put("body", body);
	    }

//        public HashMap<String, Object> getMapObject() {
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("userId", getString("userId"));
//            map.put("groupId", getString("groupId"));
//            map.put("body", getString("body"));
//
//            return map;
//        }
}
