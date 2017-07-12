package com.chandan.halo;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.chandan.halo.Parser.JParserAdv;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.chandan.halo.CONSTANT.BROADCAST_ACTION;

public class LocationService extends Service {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private static final int LOCATION_NOTIFICATION_ID = 2;



    private static final String TAG="Location";

    private static final String TAG21 = "BroadcastService";

    private final Handler handler21 = new Handler();

    @Override
    public void onCreate() {
        Log.i(TAG,"Location Service Created");

        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Location StartCommand ");
//        frndName= intent.getStringExtra("name");
//        frndPhno= intent.getStringExtra("phno");

        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!GpsStatus)
        {
            showDialogGPS();
        }
        else
        {
            handleIntent();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void handleIntent() {
        Log.i(TAG,"Location handleIntent");

        SessionManager manager=new SessionManager(getApplicationContext());
        String latitude=manager.getLatitute();
        String longitude=manager.getLongitute();
        String mLastUpdateTime=manager.getlastLocUpdate();
        String phoneno=manager.getKeyPhone();


        if(latitude.length()!=0 && longitude.length()!=0 )
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            new GPSService().execute(phoneno,latitude,longitude,mLastUpdateTime);
            //.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//execute();
        }catch (Exception ex){}
    }

    private void showDialogGPS() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle("Halo Location Service")
                        .setContentText("Gps access required to notify live location updates.")
                        .setAutoCancel(true);
        Intent resultIntent =  new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(LOCATION_NOTIFICATION_ID, mBuilder.build());

    }

    class GPSService extends AsyncTask<String,String,JSONObject>
    {


        @Override
        protected void onPreExecute() {
            Log.i(TAG,"Location GPS Service PreExecute");
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            HashMap<String, String> hm = new HashMap<>();
            //  hm.put("userId",params[0]);
            hm.put("userId",params[0]);
            hm.put("latitude",params[1]);
            hm.put("longitude",params[2]);
            hm.put("time",params[3]);
            JParserAdv jParser=new JParserAdv();

            JSONObject jsonObject=jParser.makeHttpRequest(CONSTANT.locationURL,"GET",hm);
            Log.i(TAG,"Location Background "+params[0]+" "+params[1]+" "+params[2]+" "+params[3]+" "+jsonObject);
            if(jsonObject != null){
                return jsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            Log.i(TAG,"Location PostExecute");
            if(jsonObject==null){
                return;
            }
            try {
                JSONObject jsonObject1 = jsonObject.getJSONObject("Result");
                int  st = Integer.parseInt(jsonObject1.getString("status"));
                if(st==1 || st==2)
                {

                    String userid,latitude,longitude,lastUpdated;
                    JSONArray jsonArray = jsonObject.getJSONArray("LocationData");
                    ArrayList<LocationObject> locationArrayList=new ArrayList<LocationObject>();
                    for(int j=0;j<jsonArray.length();j++)
                    {

                        try {
                            JSONObject statusJsonObject = (JSONObject) jsonArray.get(j);
                            userid = statusJsonObject.getString("user_id");
                            latitude = statusJsonObject.getString("latitude");
                            longitude = statusJsonObject.getString("longitude");
                            lastUpdated = statusJsonObject.getString("time");
                            Log.i(TAG, "Location Connection Success");

                            SqliteHelper db=new SqliteHelper(getApplicationContext());
                            db.insertLocation(userid,latitude,longitude,lastUpdated);

                            LocationObject ob=new LocationObject();
                            ob.setUserId(userid);
                            ob.setLatitute(latitude);
                            ob.setLongitude(longitude);
                            ob.setLastupdated(lastUpdated);
                            locationArrayList.add(ob);
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    Intent intent=new Intent(BROADCAST_ACTION);
                    intent.putExtra("LocationArrayList", locationArrayList);
                    sendBroadcast(intent);

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG,"Location Connection Failed");
            }
            super.onPostExecute(jsonObject);
        }
    }



}