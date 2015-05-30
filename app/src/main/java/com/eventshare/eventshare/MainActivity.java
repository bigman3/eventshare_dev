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
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    ListView listViewChats;
    List<String> chatGroupsList;
    ArrayAdapter<String> chatGroupsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**/


        //dbWrapper.createNewChatGroup();
        dbWrapper.addMemberToGroup("abcde", "zqjevQHHiU");
        /**/
        chatGroupsList = new LinkedList<String>();
        chatGroupsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chatGroupsList);

        listViewChats = (ListView) findViewById(R.id.listViewChats);
        listViewChats.setAdapter(chatGroupsAdapter);

        ParseObject.registerSubclass(Message.class);



        //TODO: fill listview with chats from data base

//
//        dummy.add("a1");  dummy.add("a2");  dummy.add("a3");  dummy.add("a4");
//        dummy.add("a11");  dummy.add("a22");  dummy.add("a33");  dummy.add("a44");
//        dummy.add("a21");  dummy.add("a32");  dummy.add("a43");  dummy.add("a54");

        ParseQuery<ParseUser> query = ParseUser.getQuery();

      //  query.selectKeys(Arrays.asList("username"));
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    // chatGroupsList.addAll(users);

                    for(Iterator<ParseUser> i = users.iterator(); i.hasNext(); ) {
                        chatGroupsList.add(i.next().getUsername());
                    }
                    chatGroupsAdapter.notifyDataSetChanged(); // update adapter
                    listViewChats.invalidate(); // redraw listview
                    listViewChats.setSelection(1);
                } else {
                    Log.d("user retrieve error", "Error: " + e.getMessage());
                }
            }
        });



        listViewChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view,
                                    int position, long id) {

                String itemValue = (String) listViewChats.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);

                Bundle msgInfo = new Bundle();
                msgInfo.putString("name", itemValue);
                intent.putExtras(msgInfo);
                startActivity(intent);


               /* Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();*/
            }
        });

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
