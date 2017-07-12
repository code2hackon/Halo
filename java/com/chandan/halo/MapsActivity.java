package com.chandan.halo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationManager;
import android.media.Image;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.chandan.halo.CONSTANT.BROADCAST_ACTION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,View.OnClickListener {

    private static final String TAG = "MapsActivity";
    private static GoogleMap mMap;
    private UiSettings mUiSettings;
    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 112;
    //Provides the entry point to Google Play services.
    protected GoogleApiClient mGoogleApiClient;
     Marker mMarker;
    private Location bestLocation;
    public  static   boolean isMapsActivityResumed=false;
    BroadcastReceiver broadcastReceiver;

    private AlarmManager service;
    private PendingIntent pending;

    // Represents a geographical location.
    LocationRequest LocationRequest;
    Location CurrentLocation;
    String LastUpdateTime;
    TextView LatLongTextView,LastUpdateTimeTextView;
    LinearLayout linlay;

    private AddressResultReceiver mResultReceiver;
    private static final long REPEAT_TIME = 20000;
    private static long REPEAT_LOCATION_TIME = 100000;
    public static final int ALARM_SERVICE_LOCATION_REQUEST_CODE=401;
    private HashMap<Marker,LocationObject> hashMap;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ImageView sendLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//
//        Intent intent = getIntent();
//        name= intent.getStringExtra("name");
//        phno= intent.getStringExtra("phno");

        hashMap=new HashMap<Marker, LocationObject>();


        LatLongTextView=(TextView)findViewById(R.id.coordinate);
        LastUpdateTimeTextView=(TextView)findViewById(R.id.updateTime);
        recyclerView=(RecyclerView)findViewById(R.id.locationListView);
        linlay=(LinearLayout)findViewById(R.id.myLatLngLayout);
        sendLoc=(ImageView)findViewById(R.id.LatLngnMsg);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        mResultReceiver = new AddressResultReceiver(new Handler());


        SessionManager mngr=new SessionManager(getApplicationContext());
        final String lat=mngr.getLatitute();
        final String lng=mngr.getLongitute();
        final String time=mngr.getlastLocUpdate();
        LatLongTextView.setText(lat+" , "+lng);
        LastUpdateTimeTextView.setText(time);

        sendLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Location: "+lat+" , "+lng+"\nLast Sync:"+time);
                sendIntent.setType("text/plain");
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent chooser=Intent.createChooser(sendIntent, "Send Location");
                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(chooser);
            }
        });

        createLocationRequest();
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        linlay.setOnClickListener(this);
//                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                        .addLocationRequest(mLocationRequest);
//                //**************************
//                builder.setAlwaysShow(true); //this is the key ingredient
//                //**************************
//
//                PendingResult<LocationSettingsResult> result =
//                        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
//                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//                    @Override
//                    public void onResult(LocationSettingsResult result) {
//                        final Status status = result.getStatus();
//                        final LocationSettingsStates state = result.getLocationSettingsStates();
//                        switch (status.getStatusCode()) {
//                            case LocationSettingsStatusCodes.SUCCESS:
//                                // All location settings are satisfied. The client can initialize location
//                                // requests here.
//                                break;
//                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                // Location settings are not satisfied. But could be fixed by showing the user
//                                // a dialog.
//                                try {
//                                    // Show the dialog by calling startResolutionForResult(),
//                                    // and check the result in onActivityResult().
//                                    status.startResolutionForResult(
//                                           MapsActivity.this, 1000);
//                                } catch (IntentSender.SendIntentException e) {
//                                    // Ignore the error.
//                                }
//                                break;
//                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                                // Location settings are not satisfied. However, we have no way to fix the
//                                // settings so we won't show the dialog.
//                                break;
//                        }
//                    }
//                });
//

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }


    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        isMapsActivityResumed=false;
        stopLocationUpdates();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    protected void onStart() {
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!GpsStatus)
        {
           showDialogGPS();
        }
        mGoogleApiClient.connect();
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         int freq=Integer.parseInt(preference.getString("sync_frequency", "2"));
        REPEAT_LOCATION_TIME=freq*60*1000;
        callGPSService();
        super.onStart();
    }

    @Override
    protected void onStop() {
        service.cancel(pending);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter=new IntentFilter(BROADCAST_ACTION);
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<LocationObject> list= (ArrayList<LocationObject>) intent.getSerializableExtra("LocationArrayList");
               addNewMarkers(list);

            }
        };
        registerReceiver(broadcastReceiver,intentFilter);

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            if(bestLocation!=null)
            startIntentService(bestLocation);
        }

    }

    private void addNewMarkers(ArrayList<LocationObject> list) {

        for (LocationObject ob1:list)
        {
            Log.d("NewMarkers",ob1.getUserId()+" "+ob1.getLatitute()+" "+ob1.getLongitude()+" "+ob1.getLastupdated());
            boolean flag=false;
            double mLat=Double.parseDouble(ob1.getLatitute());
            double mLong=Double.parseDouble(ob1.getLongitude());
            String markerTag1=ob1.getUserId();


            SqliteHelper db=new SqliteHelper(getApplicationContext());
            String frndname=db.getNameFromPhoneNo(markerTag1);
            db.close();

            for(Map.Entry hm:hashMap.entrySet())
            {
                Marker marker= (Marker)hm.getKey();
                String markerTag2= (String) marker.getTag();
                if(markerTag1.equals(markerTag2))
                {
                    flag=true;
                    marker.setPosition(new LatLng(mLat,mLong));
                    marker.setTitle(frndname);
                    marker.setSnippet(markerTag1);
                }
            }
            if(!flag)
            {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLat, mLong)));
                marker.setTag(markerTag1);
                marker.setTitle(frndname);
                marker.setSnippet(markerTag1);
                hashMap.put(marker,ob1);
            }

        }
        MapUserAdapter adapter=new MapUserAdapter(getApplicationContext(),hashMap);
        recyclerView.setAdapter(adapter);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,  this);
            mGoogleApiClient.disconnect();
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mUiSettings = mMap.getUiSettings();

        // Keep the UI Settings state in sync with the checkboxes.
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mUiSettings.setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
        } else {
            //request missing location permission.
            mUiSettings.setMyLocationButtonEnabled(false);
            mMap.setMyLocationEnabled(false);
            requestLocationPermission();
        }

    }



    @Override
    public void onLocationChanged(Location location) {
        CurrentLocation = location;
        Date date =new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        LastUpdateTime = dateFormat.format(date);

        if(isBetterLocation(CurrentLocation,bestLocation)) {
            double _lat=CurrentLocation.getLatitude();
            double _long=CurrentLocation.getLongitude();
            LatLongTextView.setText(String.valueOf(_lat)+" , "+String.valueOf(_long));
            addMarker(_lat,_long,LastUpdateTime);
            startIntentService(CurrentLocation);
            bestLocation=CurrentLocation;
            SessionManager manager=new SessionManager(getApplicationContext());
            manager.setLatitiude(_lat+"");
            manager.setLongitute(_long+"");
            manager.setLastUpdateLoction(LastUpdateTime);
        }


    }
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if ((ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mUiSettings.setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display a dialog with rationale.
                showDialogOK("Location Services Permission required ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        if (!(ContextCompat.checkSelfPermission(MapsActivity.this,
                                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                                            requestLocationPermission();
                                        } else {
                                            mUiSettings.setMyLocationButtonEnabled(true);
                                            mMap.setMyLocationEnabled(true);
                                        }
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        // proceed with logic by disabling the related features or quit the app.
                                        break;
                                }
                            }
                        });
            } else {
                // Location permission has not been granted yet, request it.
                Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();

            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        LocationRequest = new LocationRequest();
        LocationRequest.setInterval(30000);
//        mLocationRequest.setFastestInterval(15000);
        LocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        //  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
        if (CurrentLocation != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "No Geocoder Available", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, LocationRequest,this);
    }
    private void addMarker(Double latitude,Double longitude,String time) {
        // To display the time as title for location markers
//        IconGenerator iconFactory = new IconGenerator(this);
//        iconFactory.setStyle(IconGenerator.STYLE_BLUE);
//        options.icon(BitmapDescriptorFactory.
//                fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
//        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        // Set marker
        LatLng currentLatLng = new LatLng(latitude,longitude);

        if(mMarker==null)
        {
            mMarker=mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You Were Here at "+time));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        }
        else{
            mMarker.setPosition(currentLatLng);
            mMarker.setTitle("You Were Here at "+time);
        }

        Log.d(TAG,"Marker added");
    }

    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Please turn on your GPS service to access your location data.");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    protected void startIntentService(Location loc) {

        if(loc!=null && new utilities(getApplicationContext()).isNetworkAvailable()) {
            Intent intent = new Intent(this, FetchAddressIntentService.class);
            intent.putExtra(CONSTANT.RECEIVER, mResultReceiver);
            intent.putExtra(CONSTANT.LOCATION_DATA_EXTRA, loc);
            startService(intent);
        };
    }

    public void zoomToMarker(LatLng point) {

        if(point!=null && mMap!=null)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point,10),2000,null);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.myLatLngLayout && bestLocation!=null)
        {
            zoomToMarker(new LatLng(bestLocation.getLatitude(),bestLocation.getLongitude()));
        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(CONSTANT.RESULT_DATA_KEY);
            LastUpdateTimeTextView.setText(mAddressOutput+" at "+LastUpdateTime);

            // Show a toast message if an address was found.
            if (resultCode == CONSTANT.SUCCESS_RESULT) {
            //   Toast.makeText(MapsActivity.this,"address_found",Toast.LENGTH_LONG).show();
            }

        }
    }
    void callGPSService()
    {
        service = (AlarmManager) getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getBaseContext(),LocationService.class);
//        i.putExtra("name",name);
//        i.putExtra("phno",phno);
         pending = PendingIntent.getService(getBaseContext(), ALARM_SERVICE_LOCATION_REQUEST_CODE, i,
                PendingIntent.FLAG_CANCEL_CURRENT);
        service.cancel(pending);
        service.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                REPEAT_TIME, REPEAT_LOCATION_TIME, pending);
    }




}
