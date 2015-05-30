package com.eventshare.eventshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

//import  com.facebook.Session;


public class LoginActivity extends ActionBarActivity {
    private CallbackManager mCallBackManager;
    private AccessTokenTracker maccessTokenTracker;
    private ProfileTracker mProfileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* init facebook */
        FacebookSdk.sdkInitialize(getApplicationContext());

        dbWrapper.initParse(this);

        maccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };
//        if (AccessToken.getCurrentAccessToken() != null) {
//            Toast.makeText(getApplicationContext(),
//                            "inside if", Toast.LENGTH_LONG)
//                            .show();
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//            return;
//
//        }
//        Toast.makeText(getApplicationContext(),
//                "outside if", Toast.LENGTH_LONG)
//                .show();
        mCallBackManager =  CallbackManager.Factory.create();
        setContentView(R.layout.fragment_facebook_login);

        View rootView = (View) findViewById(R.id.container);
        final LoginButton loginButton = (LoginButton)rootView.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(mCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                        signUpOrLogin();
                        mProfileTracker.stopTracking();
                    }
                };
                mProfileTracker.startTracking();


//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {


            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }



        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        mCallBackManager.onActivityResult(0,0, intent );
    }

    private void signUpOrLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Profile facebookProfile = com.facebook.Profile.getCurrentProfile();

        final String username = facebookProfile.getName();
        final String profilePicUri = facebookProfile.getProfilePictureUri(64,64).toString();
        final String password = accessToken.getToken();

        ParseUser.logInInBackground(username, password,
                new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            // If user exist and authenticated, send user to Welcome.class
                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(),
                                    "Successfully Logged in",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {

                            ParseUser puser = new ParseUser();
                            puser.setUsername(username);
                            puser.setPassword(password);
                            puser.put("profilePicUri", profilePicUri);
                            puser.signUpInBackground(new SignUpCallback() {
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // Show a simple Toast message upon successful registration
                                        Toast.makeText(getApplicationContext(),
                                                "Successfully Signed up, please log in.",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Sign up Error", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });

                        }
                    }
                });


    }

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
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
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
            return rootView;
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallBackManager.onActivityResult(requestCode, resultCode, data);
    }
}