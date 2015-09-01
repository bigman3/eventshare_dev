package com.eventshare.eventshare.Activities;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eventshare.eventshare.*;
import com.eventshare.eventshare.Adapters.MessageListAdapter;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class ChatActivity extends BaseActivity {
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    private static final String TAG = "ES_DEBUG";
    private String mUserId;
    private String mImagePath;
    private EditText etMessage;
    private Button btSend;

    private ListView lvChat;
    private ArrayList<Message> mMessages;
    private MessageListAdapter mAdapter;
    private final HashMap<String, Bitmap> mBitmapCache =
            new LinkedHashMap<>(10 / 2, 0.75f, true);

    private ChatGroups mChatGroup;
    private boolean isPhotoAttached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Bundle b = getIntent().getExtras();

        mUserId = ParseUser.getCurrentUser().getObjectId();
        mChatGroup = DbWrapper.getGroupFromDevice((String) b.get("groupId"));

        updateMenuTitleToGroupName();
        setupListView();
        setupMessagePosting();
        loadGroupMessages();
//        refreshListView();

        lvChat.setLongClickable(true);
        lvChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Message msg = (Message)lvChat.getItemAtPosition(position);

                // add check if its my message!!
                ParseQuery<MessagesStatus> query = ParseQuery.getQuery("MessagesStatus");
                query.fromLocalDatastore();
                query.whereEqualTo("messageId", msg.getObjectId());
                query.whereExists("seenOnTime");


                try {
                    List<MessagesStatus> messagesStatuses = query.find();

                    Log.d(TAG, "message seen by query successful with " + messagesStatuses.size() + " records");

                    if(messagesStatuses.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "no one seen this yet.  unseenCount= " +
                                msg.get("unseenCount"), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    //TODO: for each - not only THE FIRST ONE!!!
                    MessagesStatus first = messagesStatuses.get(0);
                    Toast.makeText(getApplicationContext(), first.get("targetId") + " on " + first.get("seenOnTime") +
                            " unseenCount=" + msg.get("unseenCount") + " seenByAll = " +  msg.getBoolean("isSeenByAll")
                            , Toast.LENGTH_SHORT).show();

                } catch (ParseException e) {
                    Log.d(TAG, "error in message seen by query.");
                    e.printStackTrace();
                }

                return false;
            }
        });

        lvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void setupListView() {
        lvChat = (ListView) findViewById(R.id.lvChatMessages);
        mMessages = new ArrayList<>();
        mAdapter = new MessageListAdapter(ChatActivity.this, mUserId, mMessages, mBitmapCache);
        lvChat.setAdapter(mAdapter);
    }

    public void refreshListView(){
        mAdapter.notifyDataSetChanged(); // update adapter
        lvChat.invalidate(); // redraw listview
        lvChat.setSelection(mMessages.size() - 1);
    }

    public void onNewPushMessage(Message msg){
        Log.d(TAG, "windows is open, sending ack on message!!!");
        mMessages.add(msg);
        messageDeviceCallback.onNewEventReceived(msg);
        DbWrapper.sendSeenAllMessagesUpToNow(mChatGroup);

    }

    public void onNewPushAck(Message msg){
        refreshListView();
    }

    public void onNewPushSeen(){
        refreshListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);

        return true;
    }


    private void updateMenuTitleToGroupName() {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(mChatGroup.getGroupName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id)
        {
            case R.id.action_add_photo:
                selectImage();
                break;

            case R.id.action_event_info:
                Intent intent = new Intent(ChatActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", mChatGroup.getObjectId());
                startActivity(intent);

            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {

        final CharSequence[] options = { "Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera")) {
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);

                } else if (options[item].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)  {
        super.onActivityResult(requestCode, requestCode, intent);
        Uri selectedImage;
        if (resultCode == RESULT_OK) {
            selectedImage = intent.getData();
//            switch (requestCode) {
//                case 1:
//                    selectedImage = intent.getData();
//                    break;
//                case 2:
//                    selectedImage = intent.getData();
//                    break;
//            }
            isPhotoAttached = true;
            mImagePath = Utils.getRealPathFromURI(this, selectedImage);
            Log.d(TAG, "mImagePath uri: " + mImagePath);

            onSendClicked.onClick(null);

        }
    }
	
	
    @Override
    public void onPause() {
        super.onPause();
        //refreshListView();

        DbWrapper.setGroupMyLastActivity(mChatGroup);
    }

    @Override
    public void onResume() {
        super.onResume();
        DbWrapper.sendSeenAllMessagesUpToNow(mChatGroup);
        DbWrapper.setGroupMyLastActivity(mChatGroup);
//        refreshListView();
    }



    // Setup message field and posting
    private void setupMessagePosting() {
        etMessage = (EditText) findViewById(R.id.typedMessage);
        btSend = (Button) findViewById(R.id.buttonSend);

        btSend.setOnClickListener(onSendClicked);
    }

    View.OnClickListener onSendClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String body = etMessage.getText().toString();
            if(!isPhotoAttached && body.isEmpty()){
                return;
            }

            final Message message = new Message();
            message.setUserId(mUserId);
            message.setGroupId(mChatGroup.getObjectId());
            message.setBody(body);
            message.put("unseenCount", 1 - DbWrapper.getGroupMembershipFromDevice(mChatGroup.getObjectId()).size());
            message.put("newMessage", true);
            message.put("ackStatus", "sent");
            message.put("isPhotoAttached", isPhotoAttached);
            if (isPhotoAttached) {
                Bitmap bitmap = BitmapFactory.decodeFile(mImagePath);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                byte[] bytes = bos.toByteArray();

                ParseFile photoFile = new ParseFile(bytes);
                message.put("attachedImage", photoFile);
                message.put("localImageUri", mImagePath);
            }
            mMessages.add(message);

//                mChatGroup.setLastMessage(message);
//                DbWrapper.saveGroupToDevice(mChatGroup);
            DbWrapper.setGroupLastMessage(mChatGroup, message, Utils.toISO8061Date2(new Date()));
            refreshListView();

            synchronized (this) {
                MainActivity.lvData.remove(mChatGroup);
                MainActivity.lvData.addFirst(mChatGroup);
                MainActivity.lvData.refresh();
            }
            DbWrapper.postMessageToServer(message, messageDeviceCallback);

            etMessage.setText("");

            RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.chat_actv_rlayout);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);

        }
    };

    public DeviceCallback<Message> messageDeviceCallback = new DeviceCallback<Message>() {
        @Override
        public void onNewEventReceived(Message msg) {
            mAdapter.invalidateCache();
            refreshListView();
        }

        @Override
        public void onAckOnPostEvent(Message message) {
            message.put("ackStatus", "receivedOnServer");
            refreshListView();
        }

        @Override
        public void onMessageSeen(Message message) {
            message.put("ackStatus", "seenByAll");
            refreshListView();
        }

        @Override
        public void setGroupAvailable(Message message) {

        }

        @Override
        public void onProgressBarUpdate(Message msg, int percent) {
            msg.imageProgress = percent;
            refreshListView();

        }

        @Override
        public void saveFailed() {
            Toast.makeText(getApplicationContext(),
                    "Sending message failed for some reason. Try again",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNewMember(Message message) {

        }
    };


    // Query messages from Parse so we can load them into the chat adapter
    private void loadGroupMessages() {
        List<Message> messages = DbWrapper.getGroupMessages(mChatGroup);
        mMessages.clear();
        mMessages.addAll(messages);
    }


    public String getGroupId(){
        return mChatGroup.getObjectId();
    }

}
