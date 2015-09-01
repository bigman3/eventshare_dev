package com.eventshare.eventshare.Activities;
//import com.parse.Parse;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.eventshare.eventshare.Activities.BaseActivity;
import com.eventshare.eventshare.DbWrapper;
import com.squareup.picasso.Picasso;

import java.io.File;


public class EventshareApplication extends Application {
    private final static String TAG = "ES_DEBUG";


    public static boolean isForeground = false;
    public static BaseActivity currentActivity = null;
    public static String groupPicsDir;
    public static String profilePicsDir;
    public static String tmpPicsDir;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "EventshareApplication.onCreate()");
        DbWrapper.initParse(this);
        localSetup();
    }


    private void localSetup() {
        String ESdirectoryPath = Environment.getExternalStorageDirectory() + File.separator + "Eventshare";
        File ESdirectory = new File(ESdirectoryPath);

        EventshareApplication.profilePicsDir = ESdirectoryPath + File.separator + "Profile Pictures";
        EventshareApplication.groupPicsDir = ESdirectoryPath + File.separator + "Group Pictures";
        EventshareApplication.tmpPicsDir = ESdirectoryPath + File.separator + "tmp";

        if (!ESdirectory.exists()) {
            ESdirectory.mkdir();
            new File(EventshareApplication.profilePicsDir).mkdir();
            new File(EventshareApplication.groupPicsDir).mkdir();
            new File(EventshareApplication.tmpPicsDir).mkdir();
        }
    }
}
