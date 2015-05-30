package com.eventshare.eventshare;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Message")
public class Message extends ParseObject{
        public String getUserId() {
	        return getString("userId");
	    }
        public String getgroupId() {
        return getString("groupId");
    }
        public String getBody() {
	        return getString("body");
	    }

        public void setUserId(String userId) {
	        put("userId", userId);  
	    }
        public void setGroupId(String groupId) {
        put("groupId", groupId);
    }
        public void setBody(String body) {
	        put("body", body);
	    }
}
