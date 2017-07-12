package com.chandan.halo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;

/**
 * Created by Chandan on 22-01-2017.
 */

public class utilities {

    Context context;
    public utilities(Context context)
    {
        this.context=context;
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean noextract(String str1, String str2) {
        //String st="";
        char ch[] = str1.toCharArray();
        String st = "";
        for (char ch1 : ch) {
            if (Character.isDigit(ch1)) {
                st = st + ch1;
            }
        }

        ch = str2.toCharArray();
        String st2 = "";
        for (char ch1 : ch) {
            if (Character.isDigit(ch1)) {
                st2 = st2 + ch1;
            }
        }
        if (st.equals(st2)) {
            return true;
        } else {
            return false;
        }
    }



}
