package com.chandan.halo;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPermission extends AppCompatActivity implements View.OnClickListener
{

    public static final String TAG="show";
    private Button buttonRequestPermission;

    private static final int PERMISSION_CODE = 1;
    List<String> listPermissionsNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_permission);



        //below listener is else part for checkpermission in onstart()

        buttonRequestPermission = (Button) findViewById(R.id.button_permission);
        buttonRequestPermission.setOnClickListener(this);

    }
    public boolean checkmyPermision()
    {
        //also look in main activity checkmypermission()
        int contactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int accountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int writeStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        listPermissionsNeeded = new ArrayList<>();
        if (contactPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (accountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (writeStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (listPermissionsNeeded.isEmpty())
            return true;
        else
            return false;

    }
    private  void RequestPermissions() {

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_CODE);
            //return false;
        }

        //return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //  Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case PERMISSION_CODE: {
                Map<String, Integer> parms = new HashMap<>();
                // Initialize the map with both permissions
                parms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                parms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                parms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {

                    for (int i = 0; i < permissions.length; i++)
                        parms.put(permissions[i], grantResults[i]);


                    // Check for both permissions
                    if (parms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && parms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
                            && parms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                        startActivity(new Intent(MyPermission.this.getApplicationContext(),MainActivity.class));
                        finish();

                        //      Log.d(TAG, "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                                //       Log.d(TAG, "Some permissions are not granted ask again ");
                                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
        //                        // shouldShowRequestPermissionRationale will return true
                                //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                                if (    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) ||
                                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)||
                                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showDialogOK("SMS , Storage and Location Services Permission required for this app",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            if(!checkmyPermision())
                                                            {
                                                                RequestPermissions();
                                                            }
                                                            break;
                                                        case DialogInterface.BUTTON_NEGATIVE:
                                                            // proceed with logic by disabling the related features or quit the app.
                                                            break;
                                                    }
                                                }
                                            });
                                }
                                //permission is denied (and never ask again is  checked)
                                //shouldShowRequestPermissionRationale will return false
                                else {
                                    buttonRequestPermission.setText("Settings");
                                    Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                            .show();

                                    //proceed with logic by disabling the related features or quit the app.
                                }
                    }
                }
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
    protected void onStart() {
        super.onStart();
        if(checkmyPermision())
        {
            startActivity(new Intent(MyPermission.this.getApplicationContext(),MainActivity.class));
            finish();
        }

    }

    @Override
    public void onClick(View view) {
        String text=buttonRequestPermission.getText().toString();
        if(text.equals("Settings"))
        {
            Intent dialogIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            dialogIntent.setData(Uri.parse("package:" + getPackageName()));
          //  dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
            if(checkmyPermision())
                buttonRequestPermission.setText("Next>");
        }
        else
            RequestPermissions();
    }

}
