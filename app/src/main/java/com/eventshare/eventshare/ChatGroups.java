package com.eventshare.eventshare;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@ParseClassName("ChatGroups")
public class ChatGroups extends ParseObject {
    public static int GROUP_LARGE_PIC = 0;
    public static int GROUP_SMALL_PIC = 1;

//    boolean available = false;

    public String getGroupName() {
        return getString("groupName");
    }
    public String getGroupDate() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(getDate("takingPlaceOn"));
    }

    public String getAdmin() {
        return getString("adminId");
    }

//    public String getLastMessageId() { return getString("lastMessage");}
    public Object getLastMessage() { return get("lastMessage");}

    public void setGroupName(String groupName) {
        put("groupName", groupName);
    }
    public void setAdmin(String adminId) {
        put("adminId", adminId);
    }
//    public void setLastMessageId(String messageId) {
//        put("lastMessage", messageId);
//    }
    public void setGroupPicPath(String groupPicPath, int size) {

        if (size == GROUP_SMALL_PIC)
            put("localImagePathSmall", groupPicPath);

        if (size == GROUP_LARGE_PIC)
            put("localImagePathLarge", groupPicPath);

}


    public String getGroupPicLocalPath(int size) {
        if (size == GROUP_SMALL_PIC)
            return getString("localImagePathSmall");

        if (size == GROUP_LARGE_PIC)
            return getString("localImagePathLarge");
        return "";
    }


    public  void setGroupLocation(ParseGeoPoint groupLocation){put("groupLocation",groupLocation);}
    public ParseGeoPoint getGroupLocation(){return getParseGeoPoint("groupLocation");}

    public void setLastMessage(Message message) {
        put("lastMessage", message);
    }

    public HashMap<String, Object> getMapObject() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("groupName", getString("groupName"));
        map.put("adminId", getString("adminId"));

        return map;
    }

    @Override
    public String toString() {
      return "ObjectId: " + getObjectId() +
              ", GroupName: " + getGroupName() +
              ", AdminId: " + getAdmin();
    }


}
