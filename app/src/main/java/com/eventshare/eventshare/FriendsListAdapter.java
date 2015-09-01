package com.eventshare.eventshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tivan on 8/15/2015.
 */
public class FriendsListAdapter extends ArrayAdapter<ParseUser> {
    private  List<ParseUser> fullList;
    private TextView mFreindName;
    private ImageView mFriendProfilePic;
    private Context mContext;

    final class ViewHolder {
        public TextView name;
        public ImageView pic;
    }

    public FriendsListAdapter(Context context, int resource, List<ParseUser> objects) {

        super(context, resource, objects);
        this.fullList = objects;
//        if (objects != null) {
//            fullList = new ArrayList<ParseUser>(objects);
//        } else {
//            fullList = new ArrayList<ParseUser>();
//
//        }
        mContext = context;

    }

    @Override
    public int getCount() {
        return fullList.size();
    }

    @Override
    public ParseUser getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.friends_list_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            convertView.setTag(holder);
        }
        mFreindName = (TextView) convertView.findViewById(R.id.friend_name);
        mFriendProfilePic = (ImageView) convertView.findViewById(R.id.friend_pic);
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.name = (TextView) convertView.findViewById(R.id.friend_name);
        holder.pic = (ImageView) convertView.findViewById(R.id.friend_pic);
        final ParseUser fbFriend = (ParseUser)getItem(position);
        String name = (String)( (ParseUser) fbFriend).get("username");
        String fbId = (String) ((ParseUser) fbFriend).get("fbId");
        Log.d("TIVAN", "getview: username:" + (String) ((ParseUser) fbFriend).get("username"));
        holder.name.setText(name);
        Picasso.with(mContext).load("https://graph.facebook.com/" + fbId + "/picture?type=small").into(holder.pic);
        return convertView;

    }
}

