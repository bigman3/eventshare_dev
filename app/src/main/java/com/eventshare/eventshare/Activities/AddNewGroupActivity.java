package com.eventshare.eventshare.Activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.eventshare.eventshare.*;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseACL;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AddNewGroupActivity
        extends ActionBarActivity
        implements MapsActivity.OnCompleteListener {

    private static final String TAG = "ES_DEBUG";
    public static final int REQUEST_CODE_GET_FB_IDS = 100;
    public static final int CODE_IMAGE_FROM_CAMERA = 1;
    public static final int CODE_IMAGE_FROM_GALLERY = 2;

    private Uri imageUri;

    private ImageView ivGroupPic;
    private EditText etGroupName;
    private EditText etLocation;
    private EditText mKeywords;
    private ChatGroups mChatGroup;
    private Calendar mEventCalander;

    EditText mChooseLoc;
    private LatLng mChosenLocation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_group);
        initLayoutItemsAndMemberFields();
        /*if (savedInstanceState != null)
        {
            recreateActivity(savedInstanceState);
        }*/

//        mKeywords.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(!TextUtils.isEmpty(mKeywords.getText().toString())){
//                    mKeywords.setHint("");
//                }
////                else{
////                    mKeywords.setHint(R.string.);
////                }
//
////                mKeywords.setBackgroundColor(Color.WHITE);
////                mKeywords.setAlpha(0.65f);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        setupView();
        initLocation();
        initLocationOnClick();
        initSelectImageOnClick();


    }

    private void initLocation()
    {
        double lon,lat;

        LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);// Context.LOCATION_SERVICE);
        LocationListener locationListener = new MapManager();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) { //for emulator testing
            lon = 32.0868965;
            lat = 34.7903255;
        } else {
            lon = location.getLongitude();
            lat = location.getLatitude();
        }
          mChosenLocation = new LatLng(lat,lon);
    }
    private void initLayoutItemsAndMemberFields()
    {
        mEventCalander = Calendar.getInstance();
        etGroupName = (EditText) findViewById(R.id.etGroupName);
        etLocation = (EditText) findViewById(R.id.tvGroupLocation);
        ivGroupPic = (ImageView) findViewById(R.id.ivAddPhoto);
        mChatGroup = new ChatGroups();

        mChooseLoc = (EditText)findViewById(R.id.tvGroupLocation);
        mKeywords = (EditText) findViewById(R.id.event_keywords);

//        fManager = getSupportFragmentManager();

    }
    private void initLocationOnClick()
    {

         /*Get current location */

//        final String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mLatitude, mLongitude);
        mChooseLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivity.onCompleteListener = AddNewGroupActivity.this;
                Intent intent = new Intent(AddNewGroupActivity.this,MapsActivity.class);
                intent.putExtra("chosenLocation", mChosenLocation);
                startActivity(intent);
            }
        });
    }
    private void initSelectImageOnClick()
    {
        ivGroupPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void setupView() {
        final EditText date = (EditText) findViewById(R.id.event_date);
        EditText time = (EditText) findViewById(R.id.event_time);


        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                mEventCalander.set(Calendar.DAY_OF_MONTH, day);
                                mEventCalander.set(Calendar.MONTH, month);
                                mEventCalander.set(Calendar.YEAR, year);

                                EditText date = (EditText) findViewById(R.id.event_date);

                                String myFormat = "dd/MM/yyyy";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                                date.setText(sdf.format(mEventCalander.getTime()));
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMinDate(Calendar.getInstance());
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });


        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minutes) {
                                mEventCalander.set(Calendar.HOUR_OF_DAY, hour);
                                mEventCalander.set(Calendar.MINUTE, minutes);

                                EditText time = (EditText) findViewById(R.id.event_time);

                                String myFormat = "HH:mm";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                                time.setText(sdf.format(mEventCalander.getTime()));
                            }
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });

    }

    private void selectImage() {

                final CharSequence[] options = {"Camera", "Gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(AddNewGroupActivity.this);
                builder.setTitle("Add Photo");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Camera")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photo));
                            imageUri = Uri.fromFile(photo);
                            startActivityForResult(intent, 1);

                        } else if (options[item].equals("Gallery")) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, 2);

//                        } else if (options[item].equals("Cancel")) {
//                            dialog.dismiss();
                        }
                    }
                });

                builder.show();
            }

            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
                super.onActivityResult(requestCode, requestCode, intent);
                Uri selectedImage;

                if (resultCode == RESULT_OK) {

                    switch (requestCode) {
                        case CODE_IMAGE_FROM_CAMERA:
                            selectedImage = imageUri;

                            getContentResolver().notifyChange(selectedImage, null);
                            ContentResolver cr = getContentResolver();
                            Bitmap bitmap;
                            try {
                                bitmap = MediaStore.Images.Media
                                        .getBitmap(cr, selectedImage);
//                                roundedImage = new RoundImageView(bitmap, ivGroupPic.getHeight(), ivGroupPic.getWidth());
//                                ivGroupPic.setBackgroundColor(Color.TRANSPARENT);
//                                ivGroupPic.setImageBitmap(bitmap);
//                                ivGroupPic.setLayoutParams(new RelativeLayout.LayoutParams(
//                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
//                                        RelativeLayout.LayoutParams.WRAP_CONTENT));
                                Toast.makeText(this, selectedImage.toString(),
                                        Toast.LENGTH_LONG).show();
//                                mChatGroup.setGroupPic(Utils.getRealPathFromURI(AddNewGroupActivity.this, imageUri));
                                Bitmap scaledBitmap = getScaledBitmap(bitmap);
                                addImageParseFile(scaledBitmap);

                                ivGroupPic.setBackgroundColor(Color.TRANSPARENT);
                                ivGroupPic.setImageBitmap(scaledBitmap);

                            } catch (Exception e) {
//                                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
//                                        .show();
                                Log.e("Camera", e.toString());
                            }
                            break;

                        case CODE_IMAGE_FROM_GALLERY:
                            selectedImage = intent.getData();
//                            mChatGroup.setGroupPic(Utils.getRealPathFromURI(AddNewGroupActivity.this, imageUri));

                            String[] filePathColumn = {MediaStore.Images.Media.DATA};

                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            cursor.close();
                            Bitmap bitmapPic = BitmapFactory.decodeFile(picturePath);
                            Bitmap scaledBitmap = getScaledBitmap(bitmapPic);

                            ivGroupPic.setBackgroundColor(Color.TRANSPARENT);
                            ivGroupPic.setImageBitmap(scaledBitmap);

                            addImageParseFile(scaledBitmap);
                            break;
                    }

                    if (requestCode == REQUEST_CODE_GET_FB_IDS) {
                        finish();
                    }
                }
            }


            private void addImageParseFile(Bitmap bitmap) {
//                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

                byte[] bytes = bos.toByteArray();


                String fname = EventshareApplication.groupPicsDir + File.separator + Calendar.getInstance().getTimeInMillis() +".png";
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(fname);
                    mChatGroup.setGroupPicPath(fname, ChatGroups.GROUP_SMALL_PIC);
                    mChatGroup.setGroupPicPath(fname, ChatGroups.GROUP_LARGE_PIC);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    bos.writeTo(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ParseFile photoFile = new ParseFile(bytes);
                mChatGroup.put("groupPic", photoFile);
            }

            private Bitmap getScaledBitmap(Bitmap bitmapPic) {
                int scaleToPx = 400;
                int width = bitmapPic.getWidth();
                int height = bitmapPic.getHeight();

                float sy = (float)height / scaleToPx;
                float sx = (float)width / scaleToPx;

                float ratio = Math.min(sx, sy);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        bitmapPic,
                        (int)Math.round(height/ratio + 0.5),
                        (int)Math.round(width/ratio + 0.5),
                        false);
//                if (scaledBitmap.getHeight() > scaleToPx ||
//                        scaledBitmap.getWidth() > scaleToPx) {
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaleToPx, scaleToPx);
//                }

                return scaledBitmap;
            }


            private void updateChatGroup() {

                mChatGroup.setGroupName(etGroupName.getText().toString());
                mChatGroup.setAdmin(ParseUser.getCurrentUser().getObjectId());
                Log.d("TIVAN", "image to string: " + ivGroupPic.toString());

//                EditText keywords = (EditText) findViewById(R.id.event_keywords);

                if (mKeywords.getText().length() != 0) {
                    mChatGroup.addAllUnique(
                            "keywords",
                            Arrays.asList(mKeywords.getText().toString().replaceAll("\\s+", " ").split(" ")));
                }


                mChatGroup.put("eventDate", Utils.toISO8061Date(mEventCalander.getTime()));

                ParseACL groupACL = new ParseACL(ParseUser.getCurrentUser());
                groupACL.setPublicWriteAccess(false);  //happens anyway

                if (((CheckBox)findViewById(R.id.isVisible)).isChecked()) {
                    groupACL.setPublicReadAccess(true);
                    groupACL.setPublicWriteAccess(true);

                }
                mChatGroup.setACL(groupACL);

                if (mChatGroup.getParseFile("groupPic") == null) {
                    Bitmap noGroupPic = BitmapFactory.decodeFile(
                            EventshareApplication.tmpPicsDir +
                            File.separator +"no_group_image.png");
                    addImageParseFile(noGroupPic);
                }
            }

	    @Override
	    public void onSaveInstanceState(Bundle savedInstanceState) {

	        // Save UI state changes to the savedInstanceState.
	        // This bundle will be passed to onCreate if the process is
	        // killed and restarted.
	//        savedInstanceState.putString("etGroupName", etGroupName.getText().toString());
	//        savedInstanceState.putString("Admin", ParseUser.getCurrentUser().getObjectId());
	//
	//        if (mChatGroup.getGroupPic() != null) {
	//            savedInstanceState.putString("ivGroupPic", mChatGroup.getGroupPic());
	//        }
	//
	//        ArrayList<String> savedMemebers = new ArrayList<String>(mFriendsIds);
	//        savedInstanceState.putStringArrayList("chosenMembers", savedMemebers);
	//        super.onSaveInstanceState(savedInstanceState);
	        // etc.
	    }
	//    @Override
	//    public void onRestoreInstanceState(Bundle savedInstanceState) {
	    public void recreateActivity(Bundle savedInstanceState){
	        super.onRestoreInstanceState(savedInstanceState);
	        // Restore UI state from the savedInstanceState.
	        // This bundle has also been passed to onCreate.
	//        etGroupName.setText(savedInstanceState.getString("etGroupName"));
	//        Bitmap bitmapPic = BitmapFactory.decodeFile(savedInstanceState.getString("ivGroupPic"));
	//        ivGroupPic.setImageBitmap(bitmapPic);
	//        chosenFriendsList = DbWrapper.getUsersFromFBIds(savedInstanceState.getStringArrayList("savedMembers"));
	    }
	

            @Override
            public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.menu_add_new_group, menu);
                return true;
            }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            switch (id) {
                case R.id.action_settings: {
                    return true;
                }
                case R.id.action_add_group: {

                    //hide keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

                    if (validateInput() == false) {
                        break;
                    }
                    updateChatGroup();

                    Intent intent = new Intent(AddNewGroupActivity.this, AddNewGroupFriendsSelectorActivity.class);
                    AddNewGroupFriendsSelectorActivity.mChatGroup = mChatGroup;
                    startActivityForResult(intent, REQUEST_CODE_GET_FB_IDS);
                    break;
                }
            }
            return super.onOptionsItemSelected(item);
        }


    private boolean validateInput() {
        if (etGroupName.getText().length() == 0) {
            Toast.makeText(this, "Must enter the event description", Toast.LENGTH_LONG).show();
            return false;
        }

        if (((EditText) findViewById(R.id.tvGroupLocation)).getText().length() == 0) {
            Toast.makeText(this, "Must specify event location", Toast.LENGTH_LONG).show();
            return false;
        }

        if (((EditText) findViewById(R.id.event_date)).getText().length() == 0) {
            Toast.makeText(this, "Must specify the event date", Toast.LENGTH_LONG).show();
            return false;
        }

        if (((EditText) findViewById(R.id.event_time)).getText().length() == 0) {
            Toast.makeText(this, "Must specify the event time", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public void onComplete(LatLng point)  {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, "no network for GPS..");
            e.printStackTrace();
        }
        if (addresses != null && !addresses.isEmpty()) {
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            etLocation.setText(stateName + ", " + cityName);
        }
        mChosenLocation = point;
        ParseGeoPoint geoPoint = new ParseGeoPoint(point.latitude,point.longitude);
        mChatGroup.setGroupLocation(geoPoint);
    }
}


