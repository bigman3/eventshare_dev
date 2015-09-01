package com.eventshare.eventshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventshare.eventshare.Activities.AddNewGroupFriendsSelectorActivity;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by tivan on 8/17/2015.
 */
public class ChosenMembersAdapter extends ArrayAdapter<ParseUser> {

    private Context mContext;
    private List<ParseUser> mListChosenMembers;

    public ChosenMembersAdapter(Context context, int resource, List<ParseUser> objects) {
        super(context, resource, objects);
        mContext = context;
        this.mListChosenMembers = objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final ViewHolder holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chosen_friend_list_item, parent, false);
            convertView.setTag(holder);
        }
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        holder.tvFreindName = (TextView) convertView.findViewById(R.id.friend_name);
        holder.ivFriendProfilePic = (ImageView) convertView.findViewById(R.id.friend_pic);
        final ParseUser chosenMember = (ParseUser)getItem(position);
        String name = (String) chosenMember.get("username");
        Log.d("TIVANM", "username: " + name + "position: " + position );
        String fbId = (String) chosenMember.get("fbId");
        holder.tvFreindName.setText(name);
        Picasso.with(mContext).load("https://graph.facebook.com/"+fbId+"/picture?type=small").into(holder.ivFriendProfilePic);
        holder.tvFreindName.setText(name);
        holder.btRemoveFriend = (Button) convertView.findViewById(R.id.remove_friend);
        final int currItem = position;
        holder.btRemoveFriend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ChosenMembersAdapter.this.remove(chosenMember);
                ChosenMembersAdapter.this.notifyDataSetChanged();
                ((AddNewGroupFriendsSelectorActivity)getContext()).updateFriendsListdapter(chosenMember);
              //  AddNewGroupFriendsSelectorActivity.updateFriendsDropDownAfterDelete(chosenMember);
            }
        });
        return convertView;

    }

    final class ViewHolder {
        public TextView tvFreindName;
        public ImageView ivFriendProfilePic;
        public Button btRemoveFriend;
    }

}
