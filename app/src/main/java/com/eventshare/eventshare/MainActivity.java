package com.eventshare.eventshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.ParseUser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends ActionBarActivity {
//    static ListView listViewChats;
//    static List<ChatGroups> chatGroupsList;
//    static ChatListAdapter chatGroupsAdapter;

    static LvData<ChatGroups, ChatListAdapter> lvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lv = (ListView) findViewById(R.id.listViewChats);
        List<ChatGroups> l = new LinkedList<ChatGroups>();
        lvData = new LvData<>(lv, l, getApplicationContext());
        ChatListAdapter ad = new ChatListAdapter(getApplicationContext(), l);
        lvData.setAdapter(ad);


        testGroupAdd();

        List<ChatGroups> myChatGroups = DbWrapper.getUserChatGroups(ParseUser.getCurrentUser());

//        chatGroupsList = new LinkedList<ChatGroups>();
//        chatGroupsAdapter = new ChatListAdapter(getApplicationContext(), chatGroupsList);

//
//
//        listViewChats = (ListView) findViewById(R.id.listViewChats);
//        listViewChats.setAdapter(chatGroupsAdapter);


        for(ListIterator<ChatGroups> i = myChatGroups.listIterator(); i.hasNext(); ) {
            ChatGroups g = i.next();
            //chatGroupsList.add(g);
            lvData.add(g);
        }

        lvData.refresh();
        //refreshGroupsList();

        //listViewChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view,
                                    int position, long id) {

               // ChatGroups cGroup = (ChatGroups) listViewChats.getItemAtPosition(position);
                ChatGroups cGroup = (ChatGroups) lvData.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("groupId", cGroup.getObjectId());
                startActivity(intent);

            }
        });

    }

//    public static void refreshGroupsList() {
//        chatGroupsAdapter.notifyDataSetChanged(); // update adapter
//        listViewChats.invalidate(); // redraw listview
//        listViewChats.setSelection(1);
//    }

    private void testGroupAdd() {
        Button buttonGroupCreate = (Button) findViewById(R.id.button_create_group);
        buttonGroupCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newGroupName = ((EditText) findViewById(R.id.new_group_name)).getText().toString();
                ChatGroups newGroup = new ChatGroups();
                newGroup.setAdmin(ParseUser.getCurrentUser().getObjectId());
                newGroup.setGroupName(newGroupName);

                lvData.addFirst(newGroup);

                DbWrapper.createNewChatGroup(newGroup);

                lvData.refresh();
                //chatGroupsList.add(0, newGroup);
                //refreshGroupsList();


            }
        });

    }


    @Override
    public void onResume()
    {
        super.onResume();

        //refreshGroupsList();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
