package com.chandan.halo;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Chandan on 04-07-2017.
 */

public class LocationObject implements Serializable {
    String userId;
    String latitute,longitude,lastupdated;

    public  void setUserId(String userId)
    {
        this.userId=userId;
    }
    public void setLatitute(String latitute)
    {
        this.latitute=latitute;
    }
    public void setLongitude(String longitude)
    {
        this.longitude=longitude;
    }
    public void setLastupdated(String lastupdated)
    {
        this.lastupdated=lastupdated;
    }

    public String getUserId()
    {
        return userId;
    }
    public String getLatitute()
    {
        return latitute;
    }
    public String getLongitude()
    {
        return longitude;
    }
    public String getLastupdated()
    {
        return lastupdated;
    }

}

