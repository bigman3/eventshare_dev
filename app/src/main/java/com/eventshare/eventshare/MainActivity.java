package com.eventshare.eventshare;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends BaseActivity {
    ListView listViewChats;
    List<ChatGroups> chatGroupsList;
    ArrayAdapter<ChatGroups> chatGroupsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testGroupAdd();

        List<ChatGroups> myChatGroups = DbWrapper.getUserChatGroups(ParseUser.getCurrentUser());
        /**/
        chatGroupsList = new LinkedList<ChatGroups>();
        chatGroupsAdapter = new ArrayAdapter<ChatGroups>(this, android.R.layout.simple_list_item_1, chatGroupsList);

        listViewChats = (ListView) findViewById(R.id.listViewChats);
        listViewChats.setAdapter(chatGroupsAdapter);

//



        //TODO: fill listview with chats from data base

//
//        dummy.add("a1");  dummy.add("a2");  dummy.add("a3");  dummy.add("a4");
//        dummy.add("a11");  dummy.add("a22");  dummy.add("a33");  dummy.add("a44");
//        dummy.add("a21");  dummy.add("a32");  dummy.add("a43");  dummy.add("a54");

//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//
//      //  query.selectKeys(Arrays.asList("username"));
//        query.findInBackground(new FindCallback<ParseUser>() {
//            public void done(List<ParseUser> users, ParseException e) {
//                if (e == null) {
//                    // chatGroupsList.addAll(users);

                    for(ListIterator<ChatGroups> i = myChatGroups.listIterator(); i.hasNext(); ) {
                        chatGroupsList.add(i.next());
                    }
                    chatGroupsAdapter.notifyDataSetChanged(); // update adapter
                    listViewChats.invalidate(); // redraw listview
                    listViewChats.setSelection(1);
//                } else {
//                    Log.d("user retrieve error", "Error: " + e.getMessage());
//                }
//            }
//        });



        listViewChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view,
                                    int position, long id) {

                ChatGroups cGroup = (ChatGroups) listViewChats.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);

                Bundle msgInfo = new Bundle();
//                msgInfo.putString("groupId", cGroup.getObjectId());
                msgInfo.putString("groupName", cGroup.getGroupName());
                intent.putExtras(msgInfo);
                startActivity(intent);


               /* Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();*/
            }
        });

    }

    private void testGroupAdd() {
        Button buttonGroupCreate = (Button) findViewById(R.id.button_create_group);
        String newGroupName = ((EditText) findViewById(R.id.group_name)).getText().toString();

        final ChatGroups newGroup = new ChatGroups();
        newGroup.setAdmin(ParseUser.getCurrentUser().getObjectId());
        newGroup.setGroupName(newGroupName);

        buttonGroupCreate.setOnClickListener(new View.OnClickListener() {

             @Override
             public void onClick(View v) {
                 DbWrapper.createNewChatGroup(newGroup);
             }
        });

    }


    @Override
    public void onResume()
    {
        super.onResume();

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
