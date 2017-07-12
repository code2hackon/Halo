package com.chandan.halo;

/**
 * Created by Chandan on 16-01-2017.
 */

public class chatUser {
    public String name,partMsg,time;
    public String thumbString;
    public Boolean isOnline;
    public String number;
    public String getThumbString() {
        return thumbString;
    }

    public void setThumbString(String thumbString) {
        this.thumbString = thumbString;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }
    public String getPartMsg() {
        return partMsg;
    }

    public void setPartMsg(String partMsg) {
        this.partMsg = partMsg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getTime()
    {return time;}
    public void setTime(String time)
    {this.time=time;}
    public void setNumber(String number)
    {
        this.number=number;
    }
    public String getNumber()
    {
        return  this.number;
    }

}
