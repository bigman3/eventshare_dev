package com.eventshare.eventshare.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.eventshare.eventshare.Adapters.ChatListAdapter;
import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.DeviceCallback;
import com.eventshare.eventshare.LvData;
import com.eventshare.eventshare.Message;
import com.eventshare.eventshare.R;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends BaseActivity {
    private final String TAG = "ES_DEBUG";

    //    static ListView listViewChats;
//    static List<ChatGroups> chatGroupsList;
//    static ChatListAdapter chatGroupsAdapter;
    public static LvData<ChatGroups, ChatListAdapter> lvData;
    private final HashMap<String, Bitmap> mBitmapCache =
            new LinkedHashMap<>(10 / 2, 0.75f, true);
    // final int ADD_GROUP = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//		 mContext = getApplicationContext(); //TODO

//        LinkedList<String> members = new LinkedList<>();
//        members.add("abcd");
//        members.add("efgh");
//        Toast.makeText(getApplicationContext(),
//                new JSONArray(Arrays.asList(members.toArray())).toString(),
//                Toast.LENGTH_LONG).show();

//        Toast.makeText(getApplicationContext(),
//                DateFormat.getDateTimeInstance().format(new Date()),
//                Toast.LENGTH_LONG).show();

      /*  List<ParseObject> membershipList = DbWrapper.getAllUserMemberships(ParseUser.getCurrentUser());
        ListIterator<ParseObject> itr = membershipList.listIterator();

        while(itr.hasNext()) {
            ParsePush.subscribeInBackground(itr.next().getString("groupId"));
        }
        */
        filesPreparations();
        final ListView lv = (ListView) findViewById(R.id.listViewChats);
        List<ChatGroups> l = new LinkedList<ChatGroups>();
        lvData = new LvData<>(lv, l, getApplicationContext());
        ChatListAdapter ad = new ChatListAdapter(getApplicationContext(), l, mBitmapCache);
        lvData.setAdapter(ad);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ChatGroups group = (ChatGroups) lv.getItemAtPosition(position);
                List<ParseUser> users = DbWrapper.getGroupUsers(group);

                Log.d(TAG, "#users =   " + users.size());

                for (ParseUser user : users) {
                    Log.d(TAG, "group user  " + user.get("fullName"));
                }

                return false;
            }
        });

//        Bundle b = getIntent().getExtras();
//        if(b != null) {
//            Log.d("pusher", b.toString());
//        }

//        testGroupAdd();

        List<ChatGroups> myChatGroups = DbWrapper.getUserChatGroups();


        for (ListIterator<ChatGroups> i = myChatGroups.listIterator(); i.hasNext(); ) {
            ChatGroups g = i.next();
            //chatGroupsList.add(g);
            lvData.add(g);
        }

        lvData.refresh();
        //refreshGroupsList();

        //listViewChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

//                view.findViewById(R.layout.)
                // ChatGroups cGroup = (ChatGroups) listViewChats.getItemAtPosition(position);
                ChatGroups cGroup = (ChatGroups) lvData.getItemAtPosition(position);
                if (DbWrapper.getMyGroupsRecord(cGroup) == null) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("groupId", cGroup.getObjectId());
                startActivity(intent);

            }
        });

        registerForContextMenu(lvData.getListView());

    }

    private void filesPreparations() {
        String noGroupImagesPath = EventshareApplication.tmpPicsDir +
                File.separator +"no_group_image.png";

        File f = new File(noGroupImagesPath);
        if (!f.exists()){
            try {
                InputStream is = getAssets().open("no_group_image.png");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(buffer);
                fos.close();

                Bitmap bitmap = BitmapFactory.decodeFile(noGroupImagesPath);

                synchronized (mBitmapCache) {
                    mBitmapCache.put("noGroupImage", bitmap);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        //refreshGroupsList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_group: {
                Intent intent = new Intent(MainActivity.this, AddNewGroupActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_search: {
                Intent intent = new Intent(MainActivity.this, SearchEventActivity.class);
                startActivity(intent);
                break;
            }
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Event Info");
        menu.add(0, v.getId(), 0, "Leave Event Chat");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        //  info.position will give the index of selected item
        int position = info.position;
        ChatGroups group = (ChatGroups) lvData.getItemAtPosition(position);

        if (item.getTitle().equals("Event Info")) {
            Intent intent = new Intent(MainActivity.this, GroupInfoActivity.class);
            intent.putExtra("groupId", group.getObjectId());
            startActivity(intent);
        } else if (item.getTitle().equals("Leave Event Chat")) {

        }

        return true;
    }
}