package com.eventshare.eventshare.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import
        android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.eventshare.eventshare.DbWrapper;
import com.eventshare.eventshare.R;
import com.eventshare.eventshare.Utils;
import com.facebook.Profile;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class LoginActivity extends Activity {
    private final static String TAG = "ES_DEBUG";


    private final Class<?> postLoginActivity = MainActivity.class;

//    final Intent nextIntent = new Intent(this, postLoginActivity);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Enter login activity");

        if(ParseUser.getCurrentUser() != null &&
                ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {

            Log.d(TAG, "user already logged in. skipping login");

            Intent next = new Intent(LoginActivity.this, postLoginActivity);
            next.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(next);

            if (!Utils.isInternetAvailable(getApplicationContext())) {
                finish();
            }
            updateUserDetails();
        }

        setContentView(R.layout.fragment_login);

        ImageButton loginBtn = (ImageButton) findViewById(R.id.btnFBLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final List<String> fbPermissions = Arrays.asList("public_profile", "email", "user_friends");
                ParseFacebookUtils.logInWithReadPermissionsInBackground((Activity) v.getContext(), fbPermissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.d(TAG, "some exception logging to facebook");
                            e.printStackTrace();
                        } else {
                            if (user == null) {
                                Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                            } else {
                                Log.d(TAG, "login to fb with user input login details");
                                Intent next = new Intent((Activity) v.getContext(), postLoginActivity);
                                next.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(next);
                                updateUserDetails();
                            }
                        }
                    }
                });
            }
        });
    }


    private void updateUserDetails() {
        Log.d(TAG, "calling updateUserDetails()");
        ParseUser user = ParseUser.getCurrentUser();
        Profile facebookProfile = com.facebook.Profile.getCurrentProfile();
        final String profilePicUri = facebookProfile.getProfilePictureUri(256, 256).toString();

        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();

        user.put("profilePicUri", profilePicUri);
        user.put("fullName", facebookProfile.getName());
        user.put("installation", currentInstallation);
        user.put("fbId", facebookProfile.getId());


//        user.saveInBackground();
//        currentInstallation.put("user", user);
        currentInstallation.put("userId", user.getObjectId());
//        currentInstallation.put("fbId", facebookProfile.getId());
//        currentInstallation.saveInBackground();

        List<ParseObject> update = new LinkedList<>();
        update.add(user);
        update.add(currentInstallation);

        ParseObject.saveAllInBackground(update, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    Log.d(TAG, "some error with saving installation to server");
                }
                finish();
            }
        });
    }


//    private void signUpOrLogin() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        Profile facebookProfile = com.facebook.Profile.getCurrentProfile();
//
//        final String username = facebookProfile.getName();
//        final String profilePicUri = facebookProfile.getProfilePictureUri(64,64).toString();
//        final String password = accessToken.getToken();
//
//        ParseUser.logInInBackground(username, password,
//                new LogInCallback() {
//                    public void done(ParseUser user, ParseException e) {
//                        if (user != null) {
//                            // If user exist and authenticated, send user to Welcome.class
//                            Intent intent = new Intent(
//                                    LoginActivity.this,
//                                    MainActivity.class);
//                            startActivity(intent);
//                            Toast.makeText(getApplicationContext(),
//                                    "Successfully Logged in",
//                                    Toast.LENGTH_LONG).show();
//                            finish();
//                        } else {
//
//                            ParseUser puser = new ParseUser();
//                            puser.setUsername(username);
//                            puser.setPassword(password);
//                            puser.put("profilePicUri", profilePicUri);
//                            puser.signUpInBackground(new SignUpCallback() {
//                                public void done(ParseException e) {
//                                    if (e == null) {
//                                        // Show a simple Toast message upon successful registration
//                                        Toast.makeText(getApplicationContext(),
//                                                "Successfully Signed up, please log in.",
//                                                Toast.LENGTH_LONG).show();
//                                    } else {
//                                        Toast.makeText(getApplicationContext(),
//                                                "Sign up Error", Toast.LENGTH_LONG)
//                                                .show();
//                                    }
//                                }
//                            });
//
//                        }
//                    }
//                });
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }

//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
//            final LoginButton loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
//            loginButton.setReadPermissions("user_friends");
//            loginButton.setFragment(this);
//            loginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
//                @Override
//                public void onSuccess(LoginResult loginResult) {
//                    AccessToken accessToken = loginResult.getAccessToken();
//                    Profile Profile = com.facebook.Profile.getCurrentProfile();
////                    Toast.makeText(getApplicationContext(),
////                            "Click ListItem Number " + position, Toast.LENGTH_LONG)
////                            .show();
//                }
//
//                @Override
//                public void onCancel() {
//
//                }
//
//                @Override
//                public void onError(FacebookException e) {
//
//                }
//            });
//            return rootView;
//        }
//    }
    @Override
    public void onResume()
    {
        super.onResume();
//        Profile profile = Profile.getCurrentProfile();
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        mCallBackManager.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

    }
}