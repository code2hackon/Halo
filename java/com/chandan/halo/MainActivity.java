package com.chandan.halo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends AppCompatActivity {



    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private AlarmManager serviceMessage;
    private PendingIntent pendingIntent;
    public static final String TAG="HALO";

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    Cursor phones;
    private ViewPager mViewPager;
    private SessionManager session;
    private SqliteHelper db;

    public static boolean isDestroyed=false;
    public static String mPhno;

    public static final int ALARM_SERVICE_MESSAGE_REQUEST_CODE=400;




    public SelectUserAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isDestroyed=false;
        SessionManager manager=new SessionManager(getApplicationContext());
        mPhno=manager.getKeyPhone();
//        HashMap<String, String> hashMap = new SqliteHelper(this).getUserDetails(); // Getting user details
//        mPhno=hashMap.get("phoneno");
        Log.i(TAG,"MainActivity onCreate" );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(0);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(2);

//        Intent intent = getIntent();
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            doMySearch(query);
//        }




        final Thread thread =new Thread(new Runnable() {
            @Override
            public void run() {
                callMessageServices();
            }
        });
        thread.start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabHome);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean strPreference = preference.getBoolean("example_switch", false);
                if(strPreference) {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                    overridePendingTransition(R.anim.push_down_in,
                            R.anim.push_up_out);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Share Location Off Settings -> Location",Toast.LENGTH_LONG).show();
                }

            }
        });



    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView =
//                (SearchView) MenuItemCompat.getActionView(searchItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {

            //noinspection SimplifiableIfStatement
            case R.id.action_settings:
//Toast.makeText(this,"",Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                return true;

//            case R.id.logoutButton: {
//                db = new SqliteHelper(getApplicationContext());
//                session = new SessionManager(getApplicationContext());
//                session.setLogin(false);
//                db.deleteUser();
////                Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
////                startActivity(intent1);
////                finish();
//            }
           // case R.id.action_search:

        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position)
            {
                case 0:
                    return Tab3.newInstance(0);
//                case 1:
//                return Tab3.newInstance(2);
                case 1:
                    return Tab2.newInstance(1);
                default:
                    return null;

            }
       //    return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
//                case 0:
//                    return "TRACK";
                case 0:
                    return "CHAT";
                case 1:
                    return "CONTACTS";
            }
            return null;
        }
    }

    public void callMessageServices()
    {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isDestroyed){

                    try {
                        Log.i(TAG,"MessageService :thread started");
                        Thread.sleep(10000);
                        startService(new Intent(MainActivity.this,MessageService.class));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        t.start();
    }


    @Override
    protected void onStart() {
        Log.i(TAG,"MainActivity onStart" );
        if(!MyPermissionRecheck.checkmyPermision(this))
        {
            if (serviceMessage != null) {
                serviceMessage.cancel(pendingIntent);
                Log.i(TAG,"MessageService alarm stopped" );
            }
            startActivity(new Intent(this,MyPermission.class));
            finish();
        }
        if (serviceMessage != null) {
            serviceMessage.cancel(pendingIntent);
            Log.i(TAG,"MessageService alarm stopped" );
        }
        super.onStart();
    }
    @Override
    protected void onStop() {
        Log.i(TAG,"MainActivity onStop" );
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"MainActivity onDestroy" );
       // MainActivity.mUserId=null;
        isDestroyed=true;

        stopService(new Intent(MainActivity.this,MessageService.class));
        Log.i(TAG,"MessageService thread stopped" );
        serviceMessage = (AlarmManager) getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getBaseContext(), MessageService.class);
         pendingIntent = PendingIntent.getService(getBaseContext(), ALARM_SERVICE_MESSAGE_REQUEST_CODE
                 , i,PendingIntent.FLAG_CANCEL_CURRENT);
        serviceMessage.cancel(pendingIntent);
        Random rand=new Random();
        int repeatInterval=(rand.nextInt((90 - 60) + 1) + 60)*1000;
        serviceMessage.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), repeatInterval, pendingIntent);
        Log.i(TAG,"MessageService alarm started" );
        super.onDestroy();
    }


}
