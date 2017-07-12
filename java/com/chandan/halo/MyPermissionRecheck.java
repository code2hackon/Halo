package com.chandan.halo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by chandan on 21-10-2016.
 */

public class MyPermissionRecheck {

    public static boolean checkmyPermision(Context context) {
        int contactReadPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
        int accountPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS);
        int writeStorage = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (    contactReadPermission != PackageManager.PERMISSION_GRANTED ||
                accountPermission != PackageManager.PERMISSION_GRANTED||writeStorage != PackageManager.PERMISSION_GRANTED )
            return false;
        return true;
    }
}
