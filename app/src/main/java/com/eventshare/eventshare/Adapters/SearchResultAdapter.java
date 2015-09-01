package com.eventshare.eventshare.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.Message;
import com.eventshare.eventshare.R;
import com.eventshare.eventshare.Utils;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

public class SearchResultAdapter extends ArrayAdapter<ChatGroups> {
    private static final String TAG = "ES_DEBUG";

    private Context mContext;

    public SearchResultAdapter(Context context, List<ChatGroups> list) {
            super(context, 0, list);
            this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatGroups group = getItem(position);

//        ViewHolder holder;

        if (convertView == null) {
//            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result_item, parent, false);

//            convertView.setTag(holder);
        }

//        holder = (ViewHolder)convertView.getTag();

        TextView groupName = (TextView) convertView.findViewById(R.id.search_result_item_group_name);
        groupName.setText(group.getGroupName());

        TextView groupDate = (TextView) convertView.findViewById(R.id.search_result_item_group_date);
        groupDate.setText(group.getGroupDate());

        ImageView groupPic = (ImageView) convertView.findViewById(R.id.search_result_item_group_image);
        groupPic.setImageBitmap(BitmapFactory.decodeFile(group.getGroupPicLocalPath(ChatGroups.GROUP_SMALL_PIC)));

        return convertView;
    }

//    final class ViewHolder {
//        public TextView body;
//        public ImageView attachedImage;
//        public ImageView ackStatus;
//    }

}
