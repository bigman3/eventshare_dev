package com.eventshare.eventshare.Activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.eventshare.eventshare.ChatActivityAndroidHandler;
import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.ChosenMembersAdapter;
import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.DeviceCallback;
import com.eventshare.eventshare.FriendsListAdapter;
import com.eventshare.eventshare.R;
import com.eventshare.eventshare.Utils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


public class AddNewGroupFriendsSelectorActivity extends BaseActivity {
    private static final String TAG = "ES_DEBUG";
    private final int DROP_DOWN_HEIGHT = 5;
    static public ChatGroups mChatGroup;
    private ListView lvFriendsList;
    private AutoCompleteTextView etGroupMembers;
    private List<String> mFriendsIds;
    private ArrayAdapter<ParseUser> mFriendsListadapter;
    private List<ParseUser> chosenFriendsList;
    private ChosenMembersAdapter chosenMembersAdapter;
//    private static List<ParseUser> listHolder;

    private  List<ParseUser> listAllFbFriends;

    private DeviceCallback<ChatGroups> groupAddCallback =
            new ChatActivityAndroidHandler(this);

    private boolean created = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_group_friends_selector);

        initLayoutAndPrivateParams();
        initFBfriendsDB();
        initSelectGroupMembersOnClick();

    }

    public void updateFriendsListdapter(ParseUser user) {
        listAllFbFriends.add(user);
        mFriendsListadapter.notifyDataSetChanged();
        etGroupMembers.invalidate();

        mFriendsIds.remove(user.getString("fbId"));
    }

    private void initLayoutAndPrivateParams() {
        etGroupMembers = (AutoCompleteTextView) findViewById(R.id.etSearchMembers);
        lvFriendsList = (ListView) findViewById(R.id.friends_list);
        mFriendsIds = new LinkedList<String>();

//        listHolder = new LinkedList<ParseUser>();
        //all friends
        listAllFbFriends = new LinkedList<ParseUser>();
        mFriendsListadapter = new FriendsListAdapter(AddNewGroupFriendsSelectorActivity.this, 0, listAllFbFriends);
        etGroupMembers.setAdapter(mFriendsListadapter);

        //chosen friends
        chosenFriendsList = new LinkedList<ParseUser>();
        chosenMembersAdapter = new ChosenMembersAdapter(AddNewGroupFriendsSelectorActivity.this, 0, chosenFriendsList);
        lvFriendsList.setAdapter(chosenMembersAdapter);
    }

    private void initSelectGroupMembersOnClick() {
        etGroupMembers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                etGroupMembers.showDropDown();

                etGroupMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ParseUser listItem = (ParseUser) parent.getItemAtPosition(position);
                        etGroupMembers.setText("");
                        listAllFbFriends.remove(listItem);
                        mFriendsListadapter.notifyDataSetChanged();
                        etGroupMembers.invalidate();

                        mFriendsIds.add(listItem.getString("fbId"));
//                        List<ParseUser> chosenFriendsDb = new LinkedList<ParseUser>();
//                        chosenFriendsDb.addAll(chosenFriendsList);
                        chosenFriendsList.add(listItem);
                        chosenMembersAdapter.notifyDataSetChanged();
                        lvFriendsList.invalidate();
//                        updatChosenFbFriends(chosenFriendsDb);
//                        chosenMembersAdapter.notifyDataSetChanged();

                    }

                });
            }

        });
    }

//        etGroupMembers.setOnClickListener(new View.OnClickListener() {
//            etGroupMembers.showDropDown();
//                @Override
//                ParseUser listItem = (ParseUser) parent.getItemAtPosition(position);
//            etGroupMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                List<ParseUser> allFbFriendsDb = new LinkedList<ParseUser>();
//                allFbFriendsDb.addAll(listAllFbFriends);
//                allFbFriendsDb.remove(listItem);
//                updateAllFbFriends(allFbFriendsDb);
//                List<ParseUser> chosenFbFriendsDb = new LinkedList<ParseUser>();
//                chosenFbFriendsDb.addAll(chosenFriendsList);
//                chosenFbFriendsDb.add(listItem);
//                updatChosenFbFriends(chosenFbFriendsDb);
//
//                mFriendsIds.add((String) listItem.get("fbId"));
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            }
//                etGroupMembers.setText("");
//
//        });
//            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//                Log.d("TIVAN", "OnTextChanged");
////                mFriendsListadapter = new FriendsListAdapter(AddNewGroupFriendsSelectorActivity.this, 0, listAllFbFriends);
////                resetAllFbFriendsAdapter();
////                etGroupMembers.setAdapter(mFriendsListadapter);
//                etGroupMembers.setThreshold(1);
//                etGroupMembers.showDropDown();
//                etGroupMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        ParseUser listItem = (ParseUser) parent.getItemAtPosition(position);
//                        /**
//                         * cloths.setThreshold(1);
//                         cloths.setAdapter(My_arr_adapter);
//                         cloths.setOnItemClickListener(new OnItemClickListener() {
//                         public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
//                         */
//
//                        etGroupMembers.setText("");
//                        List<ParseUser> allFbFriendsDb = new LinkedList<ParseUser>();
//                        allFbFriendsDb.addAll(listAllFbFriends);
//                        allFbFriendsDb.remove(listItem);
//                        updateAllFbFriends(allFbFriendsDb);
////                        listAllFbFriends.remove(listItem);
////                        mFriendsListadapter.notifyDataSetChanged();
//                        List<ParseUser> chosenFbFriendsDb = new LinkedList<ParseUser>();
//                        chosenFbFriendsDb.addAll(chosenFriendsList);
//                        chosenFbFriendsDb.add(listItem);
//                        updatChosenFbFriends(chosenFbFriendsDb);
//
//                        mFriendsIds.add((String) listItem.get("fbId"));
//
//
//                    }
//
//                });
//            }
//        });



//    public static void updateFriendsDropDownAfterDelete(ParseUser user)
//    {
//        listAllFbFriends.add(user);
//        mFriendsListadapter.notifyDataSetChanged();
//        chosenFriendsList.remove(user);
//        chosenMembersAdapter.notifyDataSetChanged();
//        mFriendsIds.remove((String) user.get("fbId"));
//    }

//    private static void updateChosenFriendsList(List<ParseUser> listHolder)
//    {
//        chosenFriendsList.clear();
//        chosenFriendsList.addAll(listHolder);
//        Log.d("TIVAN", "chosenFriendsList size: " + chosenFriendsList.size());
//        chosenMembersAdapter.notifyDataSetChanged();
//
//    }

    private void initFBfriendsDB() {
        GraphRequest myFriensRequest = GraphRequest.newMyFriendsRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                        List<ParseUser> allFriendsDb = new LinkedList<ParseUser>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject curr = null;
                            try {
                                curr = jsonArray.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (curr != null) {
                                final ParseUser fbFriend = new ParseUser();

                                String id = Utils.getFieldFromJSON(curr, "id");
                                String userName = Utils.getFieldFromJSON(curr, "name");
                                fbFriend.put("username", userName);
                                fbFriend.put("fbId", id);

                                allFriendsDb.add(fbFriend);

                            }
                        }
                        updateAllFbFriends(allFriendsDb);
                    }

                });
        myFriensRequest.executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_group_friends_selector, menu);
        return true;
    }

    private void updateAllFbFriends(List<ParseUser> allFbFriendsDb) {
        listAllFbFriends.clear();
        listAllFbFriends.addAll(allFbFriendsDb);
        mFriendsListadapter.notifyDataSetChanged();
        etGroupMembers.invalidate();
    }
    private void updatChosenFbFriends(List<ParseUser> chosenFbFriendsDb)
    {
        chosenFriendsList.clear();
        chosenFriendsList.addAll(chosenFbFriendsDb);
        chosenMembersAdapter.notifyDataSetChanged();
        lvFriendsList.invalidate();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_confirm_friends: {
                if (created) {
                    return false;
                }
                DbWrapper.addNewGroupToServer(mChatGroup, mFriendsIds, groupAddCallback);
                created = true;
            }
            break;
        }

        return super.onOptionsItemSelected(item);

    }

}