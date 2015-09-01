package com.eventshare.eventshare;

import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.eventshare.eventshare.Activities.MainActivity;
import com.parse.ParseFile;
import com.parse.codec.binary.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;


public class Utils {

//    static boolean isAppOnForeground(Context context) {
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//        if (appProcesses == null) {
//            return false;
//        }
//        final String packageName = context.getPackageName();
//        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static void renameFile(String oldName, String newName) {
        File oldFile = new File(oldName);
        File newFile = new File(newName);
        oldFile.renameTo(newFile);
    }
    public static boolean writeImageStringToFile(String encodedImage, String filePath) {

        boolean resultSuccess = true;

        byte[] smallImageData = Base64.decodeBase64(encodedImage);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);

        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        }
        try {
            fos.write(smallImageData);
            Log.v("eventshare", "saved small image to device");
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            resultSuccess = false;
        }

        return resultSuccess;
    }

    public static boolean writeImageStringToFile(byte[] imageData, String filePath) {

        boolean resultSuccess = true;

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);

        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        }
        try {
            fos.write(imageData);
            Log.v("eventshare", "saved  image to device");
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            resultSuccess = false;
        }

        return resultSuccess;
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Deprecated
    public static String toISO8061Date(Date d) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        String nowAsISO = df.format(d);

        return nowAsISO;
    }


    public static Date toISO8061Date2(Date d) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);

        String nowAsISO = df.format(d);

        try {
            return df.parse(df.format(d));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
//        return nowAsISO;
    }

    /** Transform ISO 8601 string to Calendar. */
    public static Date getISO8061Date(final String iso8601string){
//        Calendar calendar = GregorianCalendar.getInstance();
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
//        calendar.setTime(date);
//        return calendar;
    }

//

    public static String getFormattedStringDate(Date d) {
        Calendar beginingOfDay = Calendar.getInstance();
        beginingOfDay.set(Calendar.HOUR_OF_DAY, 0);
        beginingOfDay.set(Calendar.MINUTE, 0);
        beginingOfDay.set(Calendar.SECOND, 0);

        Date bod = beginingOfDay.getTime();

        String res;

        if (d.before(bod)) {
            res = new SimpleDateFormat("dd.MM.yyyy").format(d);
        } else {
            res = new SimpleDateFormat("HH:mm").format(d);
        }
        return res;
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getDateFromISO8061(String input) {

        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:sssz" );

        //this is zero time so we need to add that TZ indicator for
        if ( input.endsWith( "Z" ) ) {
            input = input.substring( 0, input.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = input.substring( 0, input.length() - inset );
            String s1 = input.substring( input.length() - inset, input.length() );

            input = s0 + "GMT" + s1;
        }

        Date result = null;
        try {
             result = df.parse( input );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }



    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String getFieldFromJSON(JSONObject j, String field){
        String f = "@empty";

        try {
            f = j.getString(field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }



    private ParseFile getScaledParsePhotoFile(byte[] data) {

        // Resize photo from camera byte array
        Bitmap mealImage = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap mealImageScaled = Bitmap.createScaledBitmap(mealImage, 200, 200
                * mealImage.getHeight() / mealImage.getWidth(), false);

        // Override Android default landscape orientation and save portrait
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedScaledMealImage = Bitmap.createBitmap(mealImageScaled, 0,
                0, mealImageScaled.getWidth(), mealImageScaled.getHeight(),
                matrix, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rotatedScaledMealImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();

        // Save the scaled image to Parse
        return new ParseFile((new Date()).toString() + ".jpg", scaledData);

    }
}

