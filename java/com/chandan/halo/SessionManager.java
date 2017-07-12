package com.chandan.halo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by chandan on 04-07-2016.
 */
public class SessionManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    private static final String PREF_NAME = "AppLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static  final String KEY_STATE="state";
    private static  final String KEY_PHONE="phoneno";

    private static  final String KEY_Latitute="latitute";
    private static  final String KEY_Longitute="longitute";
    private static  final String KEY_lastLocUpdate="lastLocUpdate";


    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, 0);//0 means private mode
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();
    }
    public void setState(int state) {
        editor.putInt(KEY_STATE, state);
        editor.commit();
    }

    public void setKeyPhone(String phone) {
        editor.putString(KEY_PHONE, phone);
        editor.commit();
    }

    public void setLatitiude(String latitute)
    {
        editor.putString(KEY_Latitute,latitute);
        editor.commit();
    }
    public void setLongitute(String longitute)
    {
        editor.putString(KEY_Longitute,longitute);
        editor.commit();
    }
    public void setLastUpdateLoction(String lastUpdateLoction)
    {
        editor.putString(KEY_lastLocUpdate,lastUpdateLoction);
        editor.commit();
    }

    public int getState(){
        return pref.getInt(KEY_STATE, 0);//(key,default value)
    }

    public String getKeyPhone(){
        return pref.getString(KEY_PHONE, null);//(key,default value)
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);//(key,default value)
    }

    public String getLatitute()
    {
        return pref.getString(KEY_Latitute,"");
    }
    public String getLongitute()
    {
        return pref.getString(KEY_Longitute,"");
    }
    public String getlastLocUpdate()
    {
        return pref.getString(KEY_lastLocUpdate,"");
    }
}
