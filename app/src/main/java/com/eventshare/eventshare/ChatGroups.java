package com.eventshare.eventshare;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;

@ParseClassName("ChatGroups")
public class ChatGroups extends ParseObject/* implements Parcelable*/ {
//    private static final long serialVersionUID = 1L;

//    public ChatGroups() {}

//    public ChatGroups(Parcel in) {
//        readFromParcel(in);
//    }

    public String getGroupName() {
        return getString("groupName");
    }
    public String getAdmin() {
        return getString("adminId");
    }

    public String getLastMessageId() { return getString("lastMessage");}

    public void setGroupName(String groupName) {
        put("groupName", groupName);
    }
    public void setAdmin(String adminId) {
        put("adminId", adminId);
    }
    public void setLastMessageId(String messageId) {
        put("lastMessage", messageId);
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(this.getObjectId());
//        dest.writeString(this.getGroupName());
//        dest.writeString(this.getAdmin());
//    }

    @Override
    public String toString() {
      return "ObjectId: " + getObjectId() +
              ", GroupName: " + getGroupName() +
              ", AdminId: " + getAdmin();
    }
/*
    private void readFromParcel(Parcel in) {
       this.setObjectId(in.readString());
      this.setGroupName(in.readString());
      this.setAdmin(in.readString());
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ChatGroups createFromParcel(Parcel in) {
            return new ChatGroups(in);
        }

        public ChatGroups[] newArray(int size) {
            return new ChatGroups[size];
        }
    };
*/
}
