package com.chandan.halo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chandan on 04-07-2016.
 */
public class SqliteHelper extends SQLiteOpenHelper {

    static String getEmailId,getUserId;
    private static String DATABASE_NAME="MyHelper";
    private static int DATABASE_VERSION=1;



    private static String TABLE_NAME_FRIENDS="FriendsTable";
    private static final String User_Id = "user_id";
    private static final String Phone_No = "phone_no";
    private static final String User_name="username";
    private static final String User_pic="photo";
    private static final String isOnline="lastActive";

    private static String TABLE_NAME_MSG="MsgTable";
    private static final String _ID = "_Id";
    private static final String _TO = "_To";
    private static final String _FROM = "_From";
    private static final String STATUS ="Status";
    private static final String MESSAGE ="Message";
    private static final String PIC ="Pic";
    private static final String TYPE ="Type";
    private static final String TIME ="Time";


    private static String TABLE_NAME_LOCATION="LocationTable";
    private static final String UserId = "userid";
    private static final String Latitude="latitute";
    private static final String Longitude="longitute";
    private static final String LastUpdated="lastActive";


    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        String CREATE_TABLE_MSG="CREATE TABLE "+TABLE_NAME_MSG+"("+_ID+ " INTEGER PRIMARY KEY ,"+_TO+" TEXT,"+_FROM+" TEXT,"
                +STATUS+" TEXT,"+MESSAGE+" TEXT,"+PIC+" TEXT,"+TYPE+" TEXT,"+TIME+" DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_TABLE_MSG);//According to the SQLite docs,'A column declared INTEGER
        // PRIMARY KEY will autoincrement.' (http://www.sqlite.org/autoinc.html)

        String CREATE_TABLE_FRIENDS = "CREATE TABLE "+TABLE_NAME_FRIENDS+"("+
                Phone_No+" TEXT PRIMARY KEY,"+User_name+" TEXT,"+isOnline+" TEXT,"+User_pic+" TEXT"+")";
        db.execSQL(CREATE_TABLE_FRIENDS);

        String CREATE_TABLE_LOCATION = "CREATE TABLE "+TABLE_NAME_LOCATION+"("+
                UserId+" TEXT PRIMARY KEY,"+Latitude+" TEXT,"+Longitude+" TEXT,"+LastUpdated+" TEXT"+")";
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MSG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOCATION);
        onCreate(db);
    }

    public void deleteNoAccountContacts(String s)
    {
        if(s.length()<10)
            return;
        SQLiteDatabase db=this.getWritableDatabase();

        String query="DELETE FROM  "+ TABLE_NAME_FRIENDS+" WHERE "+Phone_No+" NOT IN ("+s+")";
        db.execSQL(query);
     /**    query="DELETE FROM  "+ TABLE_NAME_LOCATION+" WHERE "+UserId+" NOT IN ("+s+")";
        db.execSQL(query);*/
        db.close();
    }

    public  void addFriends(String phno,String name)
    {
        ContentValues contentValues=new ContentValues();
        SQLiteDatabase db=this.getWritableDatabase();
        contentValues.put(Phone_No,phno);
        contentValues.put(User_name,name);
        db.insertWithOnConflict(TABLE_NAME_FRIENDS,null,contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public String[] getFriends()
    {

        String Query = "SELECT  "+Phone_No+" FROM " + TABLE_NAME_FRIENDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        int n= cursor.getCount();

        String s[]= new String[n];
        int i=0;
        while (cursor.moveToNext())
        {
            s[i]=cursor.getString(0);
            i++;
        }
        cursor.close();
        db.close();
        return s;
    }
    public String getNameFromPhoneNo(String phno) {
        String Query = "SELECT  "+User_name+" FROM " + TABLE_NAME_FRIENDS +" WHERE "+Phone_No+" = '"+phno+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery(Query,null);
        if(cursor.moveToFirst())
            return cursor.getString(0);
        else
            return "";
    }

    public void insertMessage(String to,String from,String status,String msg,String pic,String type,String  time)
    {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        contentValues.put(_TO,to);
        contentValues.put(_FROM,from);
        contentValues.put(STATUS,status);
        contentValues.put(MESSAGE,msg);
        contentValues.put(PIC,pic);
        contentValues.put(TYPE,type);
        contentValues.put(TIME,time);

        long res = db.insert(TABLE_NAME_MSG,null,contentValues);
        if(res!=-1)
        {
            String s="{ To:"+to+" From:"+ from+" Status:"+status+" Message:"+msg+" Pic:"+pic+" Type:"+type+" Time:"+time+" }";
            Log.d("Message inserted \t\t", "data: " + s);
        }
        db.close();

    }
    public ArrayList retrieveMessage(String WHERE)
    {

        String Query = "SELECT  * FROM " + TABLE_NAME_MSG+" "+WHERE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        // Move to first row
        ArrayList list=new ArrayList();
        HashMap<String,String> hashMap;
        if(cursor!=null)
        while (cursor.moveToNext()){
            hashMap= new HashMap();
            hashMap.put("to", cursor.getString(1));
            hashMap.put("from", cursor.getString(2));
            hashMap.put("status", cursor.getString(3));
            hashMap.put("message", cursor.getString(4));
            hashMap.put("pic", cursor.getString(5));
            hashMap.put("type", cursor.getString(6));
            hashMap.put("time", cursor.getString(7));
            list.add(hashMap);
            String s="";
            for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                s=s+"{"+entry.getKey()+" : "+entry.getValue()+", ";
            }
            Log.d("Message retrieve\t\t", "data: " + s+"}");
        }
        cursor.close();
        db.close();

        return list;
    }
    public ArrayList<chatUser> chatListMessages(String user)
    {
        SQLiteDatabase db= this.getReadableDatabase();
        String query="SELECT * FROM "+ TABLE_NAME_MSG+
        " WHERE " +_ID+" IN "+
            "(SELECT MAX("+_ID+") from "+
                            " (SELECT "+_ID+","+_TO+","+_FROM+" FROM "+TABLE_NAME_MSG+" WHERE "+_TO+"='"+user+ "'"+
                                   " UNION ALL "+
                            " SELECT "+_ID+","+_FROM+","+_TO+" FROM  "+TABLE_NAME_MSG+" WHERE "+_FROM+"='"+user+"') t1"+
                   " GROUP BY "+_TO+","+_FROM +") ORDER BY "+_ID+" DESC";

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<chatUser> list=null;
        chatUser temp;
        if(cursor!=null)
        {
            list=new ArrayList<chatUser>();
            while (cursor.moveToNext()){
                temp=new chatUser();
               // hashMap.put("to", cursor.getString(1));
//                hashMap.put("from", cursor.getString(2));
//                hashMap.put("status", cursor.getString(3));
               // hashMap.put("message", cursor.getString(4));
//                hashMap.put("pic", cursor.getString(5));
//                hashMap.put("type", cursor.getString(6));
               // hashMap.put("time", cursor.getString(7));
                String k1=cursor.getString(1);
                String k2=cursor.getString(2);
                if(user.equals(k1))
                temp.setNumber(k2);
                else
                temp.setNumber(k1);
                temp.setPartMsg( cursor.getString(4));
                temp.setTime(cursor.getString(7));
                list.add(temp);
            }
        }
        db.close();
        return list;
    }
    public void updateStatus(String newStatus,String _To_OR_From,String value,String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE "+TABLE_NAME_MSG+" SET "+STATUS+"="+newStatus+ " WHERE "+
                _To_OR_From+"='"+value+"' AND "+TIME+"='"+time+"'";
        db.execSQL(sql);
        db.close();
        Log.d("updateStatus",sql);
    }
    public void updateNewStatus(String newStatus,String from,String to,String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE "+TABLE_NAME_MSG+" SET "+STATUS+"="+newStatus+ " WHERE "+
                _FROM+"='"+from+"' AND "+_TO+"='"+to+"' AND "+TIME+"='"+time+"'";
        db.execSQL(sql);
        db.close();
        Log.d("updateNewStatus",sql);
    }

    public void updateStatusToRead(String from)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE "+TABLE_NAME_MSG+" SET "+STATUS+"="+CONSTANT.STATUS_READ+ " WHERE "+
                _FROM+"='"+from+"' AND "+STATUS+"='"+CONSTANT.STATUS_DELIVERED+"'";
        Log.d("SqliteHelper",sql);
        db.execSQL(sql);
        db.close();
        Log.d("updateStatusToRead",sql);
    }


    public void deleteAllMessages(String user_Id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+TABLE_NAME_MSG+" where _To='"+user_Id+"' OR _From='"+user_Id+"'" );
        db.close();
    }
    public void deleteSelectedMessages(String from,String to,String time)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+TABLE_NAME_MSG+" where "+
                _FROM+"='"+from+"' AND "+_TO+"='"+to+"' AND "+TIME+"='"+time+"'");
        db.close();
    }

    public void insertLocation(String user_Id,String latitude,String longitude,String lastUpdated)
    {
        ContentValues contentValues = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();

        contentValues.put(UserId,user_Id);
        contentValues.put(Latitude,latitude);
        contentValues.put(Longitude,longitude);
        contentValues.put(LastUpdated,lastUpdated);
        db.insertWithOnConflict(TABLE_NAME_LOCATION,null,contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    public String[] getLocation(String phoneno)
    {
        String Query = "SELECT "+Latitude+","+Longitude+","+
                LastUpdated+" FROM " + TABLE_NAME_LOCATION+" WHERE "+UserId+" = '"+phoneno+"' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        String s[]= new String[3];
        if (cursor.moveToFirst())
        {
            s[0]=cursor.getString(0);
            s[1]=cursor.getString(1);
            s[2]=cursor.getString(2);
        }
        else
            s=null;
        cursor.close();
        db.close();
        return s;
    }

    public void deleteUser(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_NAME_MSG,null,null);
        db.delete(TABLE_NAME_FRIENDS,null,null);
        db.delete(TABLE_NAME_LOCATION,null,null);
        db.close();
    }
}
