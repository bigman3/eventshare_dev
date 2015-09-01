package com.eventshare.eventshare.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eventshare.eventshare.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends BaseActivity {
    private LatLng mLocation;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng mStartPoint;
    static public OnCompleteListener onCompleteListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mStartPoint = (LatLng)(getIntent().getExtras()).get("chosenLocation");
        mLocation = mStartPoint;
        setUpMapIfNeeded();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

//    public void onMapLongClick(LatLng point) {
//        Log.d("TIVAN", "tapped, point=" + point);
//        Intent intent = new Intent(MapsActivity.this,AddNewGroupActivity.class);
//        intent.putExtra("point", point);
//        startActivity(intent);
//    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude));
                       // .title("New Marker");
                mMap.addMarker(marker);
                System.out.println(latLng.latitude + "---" + latLng.longitude);
                mLocation = latLng;
                Log.d("TIVAN","lon: " + latLng.longitude+"lan "+latLng.latitude);

            }

        });
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mStartPoint));
        mMap.addMarker(new MarkerOptions().position(new LatLng(mStartPoint.latitude, mStartPoint.longitude)).title("Marker").snippet("Snippet"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }

    public static interface OnCompleteListener {
        public abstract void onComplete(LatLng point);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_add_location:
                onCompleteListener.onComplete(mLocation);
                MapsActivity.this.finish();
                break;
        }
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
