package com.eventshare.eventshare;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ChatGroups")
public class ChatGroups extends ParseObject{

    public String getGroupName() {
        return getString("groupName");
    }
    public String getAdmin() {
        return getString("adminId");
    }


    public void setGroupName(String groupName) {
        put("groupName", groupName);
    }
    public void setAdmin(String adminId) {
        put("adminId", adminId);
    }
}
