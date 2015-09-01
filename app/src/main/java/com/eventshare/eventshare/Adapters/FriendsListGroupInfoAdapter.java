package com.eventshare.eventshare.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventshare.eventshare.Activities.AddNewGroupFriendsSelectorActivity;
import com.eventshare.eventshare.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by tivan on 8/23/2015.
 */
public class FriendsListGroupInfoAdapter extends ArrayAdapter<ParseUser> {
        private TextView tvFreindName;
        private ImageView ivFriendProfilePic;
//        private Button btRemoveFriend;
        private Context mContext;
        private List<ParseUser> mListChosenMembers;

        public FriendsListGroupInfoAdapter(Context context, int resource, List<ParseUser> objects) {
            super(context, resource, objects);
            mContext = context;
            this.mListChosenMembers = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {

                convertView = LayoutInflater.from(mContext).inflate(R.layout.friends_list_item, parent, false);
            }
            tvFreindName = (TextView) convertView.findViewById(R.id.friend_name);
            ivFriendProfilePic = (ImageView) convertView.findViewById(R.id.friend_pic);
            final ParseUser chosenMember = (ParseUser)getItem(position);
            try {
                chosenMember.fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String name = (String) chosenMember.get("fullName");

//            Bitmap bitmap = BitmapFactory.decodeFile(chosenMember.getString("profilePicLocalPath"));
//            ivFriendProfilePic.setImageBitmap(bitmap);
            Picasso.with(mContext).load( chosenMember.getString("profilePicUri")).into(ivFriendProfilePic);
            tvFreindName.setText(name);

            //TODO - for Tivan implement it if you have time
//            btRemoveFriend = (Button) convertView.findViewById(R.id.remove_friend);
//            if (ParseUser.getCurrentUser().getObjectId() == )
//            btRemoveFriend.setVisibility(View.INVISIBLE);
//            final int currItem = position;
//            btRemoveFriend.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    FriendsListGroupInfoAdapter.this.remove(chosenMember);
//                    FriendsListGroupInfoAdapter.this.notifyDataSetChanged();
//                    AddNewGroupFriendsSelectorActivity.updateFriendsDropDownAfterDelete(chosenMember);
//                }
//            });
            return convertView;

        }

    }

