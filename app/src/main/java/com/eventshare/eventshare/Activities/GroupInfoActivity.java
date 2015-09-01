package com.eventshare.eventshare.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eventshare.eventshare.Adapters.FriendsListGroupInfoAdapter;
import com.eventshare.eventshare.ChatActivityAndroidHandler;
import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupInfoActivity extends ActionBarActivity {
    private static final String TAG = "ES_DEBUG";
    public static final int ALREADY_JOIND = 1;
    public static final int NOT_JOIND_YET = 2;

    private ChatGroups mChatGroup;
    private ImageView ivGroupPic;
    private TextView tvGroupName;
//    private TextView tvLocationName;
    private TextView tvAdminName;
    private GoogleMap mapGroupLocation;
    private ListView lvGroupMembers;

    private List<ParseUser> mGroupMembers;
    private TextView tvOccursOn;
    private TextView tvParticipants;

//    private int joinedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        Bundle b = getIntent().getExtras();
        mChatGroup = DbWrapper.getGroupFromDevice((String) b.get("groupId"));
//        mEventMembers = DbWrapper.getGroupUsers(mChatGroup); TODO remove remark

        setupLayout();
        updateFields();

    }


    private void setupLayout()
    {
        tvGroupName = (TextView) findViewById(R.id.info_tvGroupName);
        tvAdminName = (TextView) findViewById(R.id.info_tvAdminName);
        ivGroupPic = (ImageView) findViewById(R.id.info_ivGroupPhoto);
        mapGroupLocation = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.search_result_ivLocation)).getMap();
        tvOccursOn = (TextView) findViewById(R.id.info_tvOccursOn);
        lvGroupMembers = (ListView)findViewById(R.id.info_lvMembers);
        tvParticipants = (TextView) findViewById(R.id.info_tvParticipants);


//        mEventMembersAdapter = new ChosenMembersAdapter(EventInfoActivity.this,0,mEventMembers); TODO remove remark
//        lvEventMembers.setAdapter(mEventMembersAdapter); TODO remove remark
    }

    private void updateFields() {
        tvGroupName.setText(mChatGroup.getGroupName());
        tvAdminName.setText(DbWrapper.getParseUserFromDevice(mChatGroup.getAdmin()).getString("fullName"));

        if (mChatGroup.getGroupLocation() != null) {
            new MapLoadTask().doInBackground();
        }
        groupMembersLoad();

        String photoPath = mChatGroup.getGroupPicLocalPath(ChatGroups.GROUP_SMALL_PIC);
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        ivGroupPic.setImageBitmap(bitmap);
        ivGroupPic.setEnabled(false);
        tvParticipants.setText(DbWrapper.getGroupUsers(mChatGroup).size() + " participants:");
        tvOccursOn.setText(mChatGroup.getGroupDate());
    }

    private void groupMembersLoad()
    {
        List<ParseUser> membersParseUserList = DbWrapper.getGroupUsersFromServer(mChatGroup);//mChatGroup.getList("GroupMembers");
        FriendsListGroupInfoAdapter chosenMembersAdapter = new FriendsListGroupInfoAdapter(GroupInfoActivity.this,0,membersParseUserList);
        lvGroupMembers.setAdapter(chosenMembersAdapter);

    }

    private class MapLoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            LatLng groupLocation = new LatLng(mChatGroup.getGroupLocation().getLatitude(), mChatGroup.getGroupLocation().getLongitude());

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(groupLocation.latitude, groupLocation.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String city = "";
            String street = "";

            try {
                street = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getAddressLine(1);
            } catch (Exception e) {

            }
//            String countryName = addresses.get(0).getAddressLine(2);
            final String address = street + ", " + city;


            mapGroupLocation.clear();
            Marker marker = mapGroupLocation.addMarker(new MarkerOptions()
                            .position(groupLocation)
                            .title(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            );

            marker.showInfoWindow();

            CameraUpdate center = CameraUpdateFactory.newLatLng(
                    new LatLng(
                            mChatGroup.getGroupLocation().getLatitude(),
                            mChatGroup.getGroupLocation().getLongitude()+0.005)

            );
//            CameraUpdate scrollUp = CameraUpdateFactory.scrollBy(0, 350);
//            CameraUpdate zoom = CameraUpdateFactory.zoomBy(1.05f);

            mapGroupLocation.moveCamera(center);
//            mapGroupLocation.moveCamera(scrollUp);

//            mapGroupLocation.moveCamera(zoom);

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_info_joined, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_join_group) {

            Log.d(TAG, "trying to add myself to group");
            List<String> me = new ArrayList<String>(1);
            me.add(ParseUser.getCurrentUser().getString("fbId"));
            DbWrapper.addMemebersToGroupOnServer(mChatGroup, me, new ChatActivityAndroidHandler(this));

            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.clear();

        boolean isInGroup = DbWrapper.isInGroup(mChatGroup);

        if (!isInGroup) {
            getMenuInflater().inflate(R.menu.menu_event_info_not_joined, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_event_info_joined, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
