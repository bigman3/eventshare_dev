package com.eventshare.eventshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class ChatListAdapter extends ArrayAdapter<ChatGroups> {

//	private Message lastMessage;
private List<ChatGroups> chatGroupsList;

    public ChatListAdapter(Context context, List<ChatGroups> chatGroupsList) {
            super(context, 0, chatGroupsList);
        this.chatGroupsList = chatGroupsList;
//            this.lastMessage = lastMessage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.group_list_item, parent, false);
        }
            final ViewHolder holder = new ViewHolder();
//            convertView.setTag(holder);

            final ChatGroups currentChatGroup = (ChatGroups)getItem(position);

            TextView groupName = (TextView) convertView.findViewById(R.id.group_name);
            final TextView groupLastMessage = (TextView) convertView.findViewById(R.id.group_last_message);


        groupName.setText(currentChatGroup.getGroupName());

            ParseQuery<Message> lastMessageQuery = ParseQuery.getQuery(Message.class);

            if (currentChatGroup.getLastMessageId() != null) {
                lastMessageQuery.getInBackground(currentChatGroup.getLastMessageId(), new GetCallback<Message>() {
                    @Override
                    public void done(Message message, ParseException e) {
                        groupLastMessage.setText(message.getBody());

                        Log.d("ARIEH3", "set body of: " + currentChatGroup.getGroupName() + " to: " + message.getBody());

                    }
                });
            }




        return convertView;
    }

//    @Override
//    public String toString() {
//        return "aaaa";
//    }

    final class ViewHolder {
//        public ImageView imageLeft;
//        public ImageView imageRight;
        public TextView text;
    }

}
