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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eventshare.eventshare.*;
import com.eventshare.eventshare.Activities.ChatActivity;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageListAdapter extends ArrayAdapter<Message> {
    private static final String TAG = "ES_DEBUG";

    private static final int TYPE_MSG_RIGHT = 0;
    private static final int TYPE_IMAGE_MSG_RIGHT = 1;
    private static final int TYPE_MSG_LEFT= 2;
    private static final int TYPE_IMAGE_MSG_LEFT = 3;
    private static final int TYPE_MAX_COUNT = 4;

    private String mUserId;
    private Context mContext;
    private HashMap<String, Bitmap> mBitmapCache;

    public MessageListAdapter(Context context, String userId, List<Message> messages, HashMap<String, Bitmap> imagesCache) {
            super(context, 0, messages);
            this.mUserId = userId;
            this.mContext = context;
            this.mBitmapCache = imagesCache;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Message message = getItem(position);
        final boolean hasImage = message.getBoolean("isPhotoAttached");

        if (convertView == null) {
            int layoutType = getItemViewType(position);

            final ViewHolder holder = new ViewHolder();
            switch (layoutType) {
                case TYPE_MSG_RIGHT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item_right, parent, false);
                    break;
                case TYPE_IMAGE_MSG_RIGHT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_image_item_right, parent, false);
                    break;
                case TYPE_MSG_LEFT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_item_left, parent, false);
                    break;
                case TYPE_IMAGE_MSG_LEFT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_image_item_left, parent, false);
                    break;
            }
            convertView.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder)convertView.getTag();
        holder.ackStatus = (ImageView)convertView.findViewById(R.id.msgStatus);
        holder.body = (TextView)convertView.findViewById(R.id.tvBody);
        holder.body.setText(message.getBody());
        holder.userName = (TextView) convertView.findViewById(R.id.userName);
        holder.date = (TextView) convertView.findViewById(R.id.msgDate);

        Date dd ;
        if (message.getCreatedAt() != null)  {
            dd = message.getCreatedAt();
        } else {
            dd = new Date();
        }
        holder.date.setText(Utils.getFormattedStringDate(dd));

        try {
            holder.userName.setText(ParseUser.getQuery().fromLocalDatastore().get(message.getUserId()).getString("fullName"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (message.getUserId().equals(mUserId)) {
            if (message.get("ackStatus").equals("receivedOnServer")) {
                holder.ackStatus.setImageDrawable(getContext().getResources().getDrawable(R.drawable.msg_check_0));
                holder.ackStatus.setVisibility(View.VISIBLE);
            } else if (message.get("ackStatus").equals("seenByAll")) {
                holder.ackStatus.setImageDrawable(getContext().getResources().getDrawable(R.drawable.msg_check_1));
                holder.ackStatus.setVisibility(View.VISIBLE);
            } else if (message.get("ackStatus").equals("sent")) {
                holder.ackStatus.setVisibility(View.INVISIBLE);
            }
        }

        if (hasImage) {
//            RelativeLayout rl = (RelativeLayout) convertView.findViewById(R.id.popupLayout);

            final String imagePath = (String) message.get("localImageUri");


            if (imagePath != null) {
                holder.attachedImage = (ImageView) convertView.findViewById(R.id.attachedImage);

                String msgId = message.getObjectId();
                String hashKey = msgId;

                //this happens when trying to upload an image.
                // Message gets its objectId only after upload success
                if (msgId == null) {
                    hashKey = imagePath;
                }
                Bitmap bitmap = getBitmapFromCache(hashKey);
                if (bitmap == null) {
                    Log.v(TAG, "cache MISS");

                    bitmap = BitmapFactory.decodeFile(imagePath);

                    int boundWidth = Utils.dpToPx(getContext(), 270);
//                    int boundHeight = Utils.dpToPx(getContext(), 270);

                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

                    float ratio = (float)height / width;

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, boundWidth, (int)(boundWidth*ratio), false);
                    if (scaledBitmap.getHeight() > boundWidth) {
                        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, boundWidth, boundWidth);
                    } else if (scaledBitmap.getHeight() < boundWidth) {
                        holder.attachedImage.getLayoutParams().height = scaledBitmap.getHeight();
                        holder.attachedImage.requestLayout();
                    }
                    addBitmapToCache(hashKey, scaledBitmap);
                } else {
                    Log.v(TAG, "cache HIT");
                }

                holder.attachedImage.setImageBitmap(bitmap);

            }

            final DonutProgress progressBar = (DonutProgress) convertView.findViewById(R.id.donut_progress);
            final ImageView cancelSign = (ImageView) convertView.findViewById(R.id.cancel_x);

            cancelSign.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            if (message.get("newMessage") == true) {
                final View finalConvertView = convertView;
                View.OnClickListener cancelProgressCallback = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        message.canceledProgress = true;
                        ImageView tryAgainView = (ImageView) finalConvertView.findViewById(R.id.try_again);
                        cancelSign.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        tryAgainView.setVisibility(View.INVISIBLE);

                    }
                };
                cancelSign.setOnClickListener(cancelProgressCallback);
                progressBar.setOnClickListener(cancelProgressCallback);

                cancelSign.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(message.imageProgress);
                if (message.imageProgress == 100){
                    cancelSign.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            } else {

                    holder.attachedImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (message.getBoolean("largeImageFetched") == false) {
                               DbWrapper.startImageDownload(message, (ChatActivity)getContext());
                            } else {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + imagePath), "image/*");
                                getContext().startActivity(intent);
                            }
                            Log.d(TAG, "clicked image");
                        }
                    });
                }
            }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        final Message message = getItem(position);
        final boolean hasImage = message.getBoolean("isPhotoAttached");
        final boolean isMe = message.getUserId().equals(mUserId);
        int layoutType;

        if (isMe) {
            layoutType = TYPE_MSG_RIGHT;
            if (hasImage) {
                layoutType = TYPE_IMAGE_MSG_RIGHT;
            }
        } else {
            layoutType = TYPE_MSG_LEFT;
            if (hasImage) {
                layoutType = TYPE_IMAGE_MSG_LEFT;
            }
        }

        return layoutType;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
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

    public void invalidateCache() {
        synchronized (mBitmapCache) {
            mBitmapCache.clear();
        }
    }


    final class ViewHolder {
        public TextView userName;
        public TextView body;
        public ImageView attachedImage;
        public ImageView ackStatus;
        public  TextView date;
    }

}
