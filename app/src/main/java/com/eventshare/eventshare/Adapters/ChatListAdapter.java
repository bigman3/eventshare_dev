package com.eventshare.eventshare.Adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventshare.eventshare.Activities.EventshareApplication;
import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.Message;
import com.eventshare.eventshare.MyGroupsRecord;
import com.eventshare.eventshare.R;
import com.eventshare.eventshare.Utils;
import com.parse.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends ArrayAdapter<ChatGroups> {

    private HashMap<String, Bitmap> mBitmapCache;

    public ChatListAdapter(Context context, List<ChatGroups> chatGroupsList, HashMap<String, Bitmap> imagesCache) {
            super(context, 0, chatGroupsList);
//        this.chatGroupsList = chatGroupsList;
//            this.lastMessage = lastMessage;
        this.mBitmapCache = imagesCache;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.group_list_item, parent, false);
        }
        final ViewHolder holder = new ViewHolder();
//            convertView.setTag(holder);

        final ChatGroups currentChatGroup = getItem(position);

        TextView groupName = (TextView) convertView.findViewById(R.id.group_name);
        final TextView groupLastMessage = (TextView) convertView.findViewById(R.id.group_last_message);
        final TextView groupLastMessageTime = (TextView) convertView.findViewById(R.id.group_last_message_time);

        ImageView groupPic = (ImageView)convertView.findViewById(R.id.ivSingleGroupPic);
        String photoPath = currentChatGroup.getGroupPicLocalPath(ChatGroups.GROUP_SMALL_PIC);

        File f = null;

        if (photoPath != null) {
            f = new File(photoPath);
        }


        String groupId = currentChatGroup.getObjectId();
        String hashKey = groupId;

        if (f == null || !f.exists() || groupId == null) {
            hashKey = "noGroupImage";

            photoPath = EventshareApplication.tmpPicsDir +
                    File.separator +"no_group_image.png";
        }

        Bitmap bitmap = getBitmapFromCache(hashKey);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(photoPath);
            addBitmapToCache(hashKey, bitmap);
        }

        groupPic.setImageBitmap(bitmap);

        groupName.setText(currentChatGroup.getGroupName());

        String lastMsgText = "";
        String lastMsgTime = "";
        MyGroupsRecord mgr = DbWrapper.getGroupLastRecord(currentChatGroup);

        Message lastMessage = null;
        Date lastMessageDate = null;

        if (mgr != null) {
            lastMessage = (Message) mgr.get("lastMessage");
            lastMessageDate = (Date) mgr.get("lastMessageDate");
        }
        if (DbWrapper.getMyGroupsRecord(currentChatGroup) != null) {
            if (lastMessage != null) {
                try {
                    lastMessage.fetchFromLocalDatastore();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (lastMessage.isDataAvailable()) {
                    lastMsgText = lastMessage.getBody();

                    lastMsgTime = Utils.getFormattedStringDate(lastMessageDate);
                }
            }
        } else {
            if (Utils.isInternetAvailable(getContext())) {
                lastMsgText = "creating event...";
            } else {
                lastMsgText = "no internet connectivity...";
            }
        }
        groupLastMessage.setText(lastMsgText);
        groupLastMessageTime.setText(lastMsgTime);

        return convertView;
    }

    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (mBitmapCache) {
                mBitmapCache.put(url, bitmap);
            }
        }
    }

    private Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap = null;
        synchronized (mBitmapCache) {
            bitmap = mBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                mBitmapCache.remove(url);
                mBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }
        return bitmap;
    }



    final class ViewHolder {
//        public ImageView imageLeft;
//        public ImageView imageRight;
        public TextView text;
    }

}
