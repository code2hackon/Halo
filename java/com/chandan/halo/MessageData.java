package com.chandan.halo;

import android.graphics.Bitmap;

/**
 * Created by chandan on 29-10-2016.
 */

public class MessageData {
    String to,from,status,messagetxt,type="",time,caption;
    Bitmap pic;
    public void setText(String text)
    {
        this.messagetxt = text;
    }
    public String getText()
    {
        return this.messagetxt;
    }
    public void setTo(String s)
    {
        this.to = s;
    }
    public String getTo()
    {
        return this.to;
    }
    public void setFrom(String s)
    {
        this.from = s;
    }
    public String getFrom()
    {
        return this.from;
    }
    public void setStatus(String s)
    {
        this.status = s;
    }
    public String getStatus()
    {
        return this.status;
    }
    public void setPic(Bitmap s)
    {
        this.pic = s;
    }
    public Bitmap getPic()
    {
        return this.pic;
    }
    public void setType(String s)
    {
        this.type = s;
    }
    public String getType()
    {
        return this.type;
    }
    public void setTime(String s)
    {
        this.time = s;
    }
    public String getTime()
    {
        return this.time;
    }
    public void setCaption(String caption){this.caption=caption;}
    public String getCaption(){ return this.caption;}

}
