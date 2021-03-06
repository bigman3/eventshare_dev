package com.eventshare.eventshare.Activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.eventshare.eventshare.R;


public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventshareApplication.isForeground = true;
        EventshareApplication.currentActivity = this;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
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

    @Override
    public void onResume() {
        super.onResume();
        EventshareApplication.isForeground = true;
        EventshareApplication.currentActivity = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        EventshareApplication.isForeground = false;
        EventshareApplication.currentActivity = null;

    }
/*
    public void onNewPushMessage(Message msg){

    }*/
}
