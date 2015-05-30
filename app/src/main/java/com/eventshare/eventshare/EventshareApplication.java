package com.eventshare.eventshare;
//import com.parse.Parse;
import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class EventshareApplication extends Application{

    private final Class<?> postLoginActivity = MainActivity.class;

    @Override
    public void onCreate() {
        super.onCreate();

//        Toast.makeText(getApplicationContext(),
//                "IN EventshareApplication!!!!!",
//                Toast.LENGTH_LONG).show();

        FacebookSdk.sdkInitialize(getApplicationContext());
        DbWrapper.initParse(this);

        //check if we already exist and linked to fb, else login
        if(ParseUser.getCurrentUser() != null &&
                ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())){
            Intent nextIntent = new Intent(this, postLoginActivity);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(nextIntent);

        } else {
            Intent nextIntent = new Intent(this, LoginActivity.class);
            nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(nextIntent);
        }

    }
}
