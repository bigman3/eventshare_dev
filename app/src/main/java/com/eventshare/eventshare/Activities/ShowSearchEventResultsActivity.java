package com.eventshare.eventshare.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.eventshare.eventshare.Adapters.SearchResultAdapter;
import com.eventshare.eventshare.ChatGroups;
import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.R;
import com.eventshare.eventshare.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class ShowSearchEventResultsActivity extends BaseActivity {
    private static final String TAG = "ES_DEBUG";
    public static final int REQUEST_INFO_FROM_SEARCH_RESULTS = 5;
    public static List<ParseQuery<ChatGroups>> queries;

    private ListView lvResult;
    private RelativeLayout rlNoEventsFoundDialog;
    private RelativeLayout rlResultsFoundDialog;

    private ImageView btSearchAgain;
    private ImageView btCancleSearchAgain;
    private GoogleMap mapGroupsLocation;


    private SearchResultAdapter mAdapter;
    private List<ChatGroups> mSearchResultList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_search_event_results);

        setupLayout();
        setupLayoutCallbacks();
        setupListView();

        if (queries == null) {
            finish();
        }

        PostSearch postSearchCallback = new PostSearch();
        DbWrapper.searchForEventByQueries(queries, postSearchCallback);


    }

    private void setupLayoutCallbacks() {
        btSearchAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btCancleSearchAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowSearchEventResultsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatGroups group = (ChatGroups) lvResult.getItemAtPosition(position);
//                DbWrapper.getGroupFromServer(group.getObjectId(), true);
                Intent intent = new Intent(ShowSearchEventResultsActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", group.getObjectId());
                intent.putExtra("joinedStatus", GroupInfoActivity.NOT_JOIND_YET);
                startActivityForResult(intent, REQUEST_INFO_FROM_SEARCH_RESULTS);
            }
        });

//        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ChatGroups group = (ChatGroups) lvResult.getItemAtPosition(position);
//                DbWrapper.getGroupFromServer(group.getObjectId(), true);
//                Intent intent = new Intent(ShowSearchEventResultsActivity.this, GrouoInfoActivity.class);
//                intent.putExtra("groupId", group.getObjectId());
//                startActivity(intent);
//                Log.d(TAG, "trying to add myself to group");
//                List<String> me = new ArrayList<String>(1);
//                me.add(ParseUser.getCurrentUser().getString("fbId"));
//                DbWrapper.addMemebersToGroupOnServer(group, me, null);
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, requestCode, intent);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case REQUEST_INFO_FROM_SEARCH_RESULTS:
                    setResult(RESULT_OK);
                    finish();
            }
        }
    }

    private void setupListView() {
        lvResult = (ListView) findViewById(R.id.search_result_lv);
        mSearchResultList = new ArrayList<>();
        mAdapter = new SearchResultAdapter(ShowSearchEventResultsActivity.this, mSearchResultList);
        lvResult.setAdapter(mAdapter);
    }

    public void refreshListView(){
        mAdapter.notifyDataSetChanged(); // update adapter
        lvResult.invalidate(); // redraw listview
//        lvResult.setSelection(mSearchResultList.size() - 1);
    }
//
    private void setupLayout() {
        lvResult = (ListView) findViewById(R.id.search_result_lv);
        rlNoEventsFoundDialog = (RelativeLayout) findViewById(R.id.search_result_no_reults_dialog);
        rlResultsFoundDialog = (RelativeLayout) findViewById(R.id.search_results_results_dialog);
        btSearchAgain = (ImageView) findViewById(R.id.search_result_search_again);
        btCancleSearchAgain = (ImageView) findViewById(R.id.search_result_cancel_search_again);
        mapGroupsLocation = ((SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.search_result_ivLocation)).getMap();
        mapGroupsLocation.clear();
    }


    public class PostSearch extends AsyncTask<List<ChatGroups>, Boolean, Void> {

        private List<ChatGroups> searchResult;
        private CameraUpdate cu;

        @Override
        protected Void doInBackground(List<ChatGroups> ... params) {
            searchResult = params[0];
            Boolean haveResults;

            if (searchResult.size() == 0) {
                haveResults  = new Boolean(false);
            } else {
                haveResults  = new Boolean(true);
            }

            if (!haveResults) {
                publishProgress(haveResults);
                return null;
            }

            ParseObject.pinAllInBackground(searchResult);

            Log.d(TAG, "found " + searchResult.size() + "  groups ");

//            semaphore = new Semaphore(1 - searchResult.size());

            LatLngBounds.Builder b = new LatLngBounds.Builder();
            for(ChatGroups g : searchResult){
                DbWrapper.getGroupFromServer(g.getObjectId(), true); //refactor - put out the get image, and call seperatly
                List<ParseUser> gUsers = DbWrapper.getGroupUsersFromServer(g);
                ParseObject.pinAllInBackground(gUsers);
//                Log.d(TAG, "group id " + g.getObjectId());
                new ShowGroupOnMapTask().execute(g);
                b.include(new LatLng(g.getGroupLocation().getLatitude(), g.getGroupLocation().getLongitude()));
            }

            LatLngBounds bounds = b.build();
            cu = CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    Utils.dpToPx(getApplicationContext(), 250),
                    Utils.dpToPx(getApplicationContext(), 250),
                    50);

            publishProgress(haveResults);
            return null;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);

            Boolean haveResults = values[0];

            if (!haveResults) {
                rlResultsFoundDialog.setVisibility(View.INVISIBLE);
                rlNoEventsFoundDialog.setVisibility(View.VISIBLE);
                return;
            } else {
                mSearchResultList.clear();
                mSearchResultList.addAll(searchResult);
                refreshListView();

//                try {
//                    semaphore.acquire();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                mapGroupsLocation.moveCamera(cu);
            }
        }
    }

    class ShowGroupOnMapTask extends AsyncTask<ChatGroups, String, Void> {

        MarkerOptions mMarkerOptions;
        CameraUpdate mCameraUpdate;

        @Override
        protected Void doInBackground(ChatGroups... groups) {
            Log.d(TAG, "enetred ShowGroupOnMapTask.doInBackground()");

            ChatGroups group = groups[0];
            LatLng groupLocation = new LatLng(group.getGroupLocation().getLatitude(), group.getGroupLocation().getLongitude());

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(groupLocation.latitude, groupLocation.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String street = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getAddressLine(1);
//            String countryName = addresses.get(0).getAddressLine(2);
            final String address = street + ", " + city;

            mMarkerOptions = new MarkerOptions()
                            .position(groupLocation)
                            .title(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));


            mCameraUpdate = CameraUpdateFactory.newLatLng(
                    new LatLng(
                            group.getGroupLocation().getLatitude(),
                            group.getGroupLocation().getLongitude() + 0.005)

            );
//            CameraUpdate scrollUp = CameraUpdateFactory.scrollBy(0, 350);
//            CameraUpdate zoom = CameraUpdateFactory.zoomBy(1.05f);
//            mapGroupLocation.moveCamera(scrollUp);
//            mapGroupLocation.moveCamera(zoom);
            //Log.d(TAG, "pre publishProgress");

//            Void[] v = new Void[1];
//            publishProgress(v);


            ShowSearchEventResultsActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    // add your marker here
                    Marker m = mapGroupsLocation.addMarker(mMarkerOptions);
//                    haspMap.put(m, company.getId());
//                    Log.d("marker", markerOptions.getTitle());
                   // Log.d(TAG, "1 marker done");

//                    semaphore.release();


                }
            });

            return null;
        }

//        @Override
//        protected void onProgressUpdate(Void... values) {
//            mapGroupsLocation.addMarker(mMarkerOptions);
//            Log.d(TAG, "1 marker done");
//            semaphore.release();
////            mapGroupsLocation.moveCamera(mCameraUpdate);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_search_event_results, menu);
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
