package com.chandan.halo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.chandan.halo.Parser.JParserAdv;
import com.google.android.gms.common.api.BooleanResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.chandan.halo.CONSTANT.STATUS_DONE;
import static com.chandan.halo.CONSTANT.STATUS_DONE_ALL;
import static com.chandan.halo.CONSTANT.STATUS_READ;
import static com.chandan.halo.CONSTANT.STATUS_SERVER;
import static com.chandan.halo.CONSTANT.STATUS_WAIT;
import static com.chandan.halo.CONSTANT.STATUS_WAITING;
import static com.chandan.halo.MessageActivity._to;
import static com.chandan.halo.MessageActivity.mAdapter;
import static com.chandan.halo.MessageActivity.mListView;
import static com.chandan.halo.MessageActivity.msgList;

public class MessageService extends Service {
    private String phoneno;
    private static final String TAG="HALO:";
    public MessageService() {
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"MessageService onStartCommand");
        if(phoneno!=null)
        handleIntent();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return  null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"MessageService onCreate");
        SessionManager manager=new SessionManager(getBaseContext());
        phoneno= manager.getKeyPhone();
        super.onCreate();
    }
    private boolean isConnected()
    {
        ConnectivityManager cm =
            (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    private void handleIntent() {

        if(isConnected())
        {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                new receiveMsg().execute();//.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//execute();
            }catch (Exception ex){}
        }
    }

    class receiveMsg extends AsyncTask<String,String,JSONObject>
    {
        ArrayList list;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            SqliteHelper db = new SqliteHelper(getApplicationContext());
            String ss = MessageActivity._to;  // update to opened message activity
            if(MessageActivity.isStarted)
            {
                Log.d("MessageService","updateStatusToRead");
                db.updateStatusToRead(ss);    // update status=STATUS_READ if Message Activity is opened
            }


            String where=" WHERE status IN('"+STATUS_WAITING+"','"+STATUS_READ+"')";
            Log.d("MessageService","retrieve old STATUS_WAITING n STATUS_READ messages from local db");
            list=new SqliteHelper(getApplicationContext()).retrieveMessage(where); // retrieve old messages from local db
            Gson gson = new Gson();
            String msgData = gson.toJson(list);
            JParserAdv jParserAdv = new JParserAdv();
            HashMap<String, String> hm = new HashMap<>();

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            Calendar calobj = Calendar.getInstance();
            String lastActive = dateFormat.format(calobj.getTime());//locale
            hm.put("phoneno",phoneno);
            hm.put("lastActive",lastActive);
            if(msgData!=null)
            hm.put("msgData",msgData);
            Log.i(TAG,msgData);
            JSONObject jsonObject = jParserAdv.makeHttpRequest(CONSTANT.receiveUrl, "POST", hm);
            Log.i(TAG,"jsonObject = "+jsonObject);
            if (jsonObject != null) {
                return jsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            SqliteHelper db = new SqliteHelper(getApplicationContext());
            if(jsonObject!=null)
            {
                try {
                    JSONObject result=jsonObject.getJSONObject("Result");
                    int statusRes=Integer.parseInt(result.getString("status"));

                    if(statusRes==2)
                    {
// CASE1
                        /*---------------------status   waiting    and   read  begins ----------------------*/
                        JSONArray waitNread=jsonObject.getJSONArray("waitNread");
                        int len=waitNread.length();
                        for(int i=0;i<len;i++)
                        {
                            JSONObject object=waitNread.getJSONObject(i);
                            String to=object.getString("to");
                            String from=object.getString("from");
                            String time=object.getString("time");
                            String status=object.getString("status");

                            if(msgList!=null)
                                for(MessageData mdat:msgList) {
                                    if (mdat.getTime().equals(time) && mdat.getTo().equals(to) && mdat.getFrom().equals(from)) {
                                        if (status.equals(STATUS_WAITING))
                                            mdat.setStatus(STATUS_SERVER);
                                         else if (status.equals(STATUS_READ))
                                            mdat.setStatus(STATUS_DONE_ALL); // send readed for incoming msg to server so update status=doneall by finding matching time
                                    break;
                                    }
                                }

                            if (status.equals(STATUS_WAITING)) {
                                    Log.d("MessageService","updateNewStatus STATUS_WAITING to STATUS_SERVER");
                                    new SqliteHelper(getApplicationContext()).updateNewStatus(STATUS_SERVER, from, to, time);
                            } else if (status.equals(STATUS_READ)) {
                                Log.d("MessageService","updateNewStatus STATUS_READ to STATUS_DONE_ALL");
                                // send readed for incoming msg to server so update status=doneall by finding matching time
                                new SqliteHelper(getApplicationContext()).updateNewStatus(STATUS_DONE_ALL, from, to, time);
                            }

                        }
                        if(len>0 && mAdapter!=null)
                            mAdapter.notifyDataSetChanged();
                        /*---------------------status   waiting    and   read  ends ----------------------*/
//CASE2  RECCEIVED MESSAGES
                        /*---------------------status  received message starts ----------------------*/
                        JSONArray receivedMsg=jsonObject.getJSONArray("receivedMsg");
                        String _to,_from="",status,message="",type,time,pic;
                        len=receivedMsg.length();
                        for(int i=0;i<len;i++) {
                            JSONObject jsonObject1 = (JSONObject) receivedMsg.get(i);
                            _to = jsonObject1.getString("_to");
                            _from = jsonObject1.getString("_from");
                            status = jsonObject1.getString("status");  // status=2 STATUS_DELIVERED for incoming msg
                            message = jsonObject1.getString("message");
                            pic = jsonObject1.getString("pic");
                            type = jsonObject1.getString("type");
                            time = jsonObject1.getString("time");
                            // message=message+" SERVICE";
                            db.insertMessage(_to, _from, status, message, pic, type, time); // insert to local db
                            // ss will be "" when message activity is closed

                            boolean shouldShowNotifications=true;

                            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String strRingtonePreference = preference.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
                            Uri soundUri = Uri.parse(strRingtonePreference);
                            boolean strVibratePreference = preference.getBoolean("notifications_new_message_vibrate", false);

                            if (MessageActivity.isStarted && (MessageActivity._to).equals(_from)) {

                                MessageData mData = new MessageData();
                                mData.setTo(_to);
                                mData.setFrom(_from);
                                mData.setStatus(status);
                                mData.setText(message);
                                //  mData.setPic(pic);
                                mData.setType(type);
                                mData.setTime(time);
                                shouldShowNotifications=false;
                                msgList.add(mData);
                                mAdapter.notifyDataSetChanged();
                            }


                            if(Tab3.isTab3active)
                            {
                                shouldShowNotifications=false;
                                Tab3 ob=new Tab3();
                                ob.new loadChatContacts(getApplicationContext()).execute();
                            }

                            if(shouldShowNotifications) {
                                String name = _from;
                                String[] projection = new String[]{
                                        ContactsContract.PhoneLookup.DISPLAY_NAME,
                                        ContactsContract.PhoneLookup._ID};
                                Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(_from));
                                Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
                                if (cursor != null) {
                                    if (cursor.moveToFirst())
                                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                                    cursor.close();
                                }
                                // Make notification for new messsage received
                                Intent intent = new Intent(MessageService.this, MessageActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("phno", _from);
                                PendingIntent pIntent = PendingIntent.getActivity(MessageService.this, (int) System.currentTimeMillis(), intent, 0);
                                NotificationManager notificationManager = (NotificationManager)
                                        getSystemService(NOTIFICATION_SERVICE);
                                NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(getApplicationContext())
                                        .setAutoCancel(true)
                                        .setContentTitle("Message from " + name)
                                        .setContentText(message)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentIntent(pIntent)
                                        .setSound(soundUri);
                                if (strVibratePreference)
                                    mbuilder.setVibrate(new long[]{100, 100});
                                Notification notification = mbuilder.build();
                                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                                notificationManager.notify(0, notification);
                            }
                            else
                            {
                                try {
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
                                    r.play();
                                    Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                                    v.vibrate(new long[]{100, 100},0);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        /*---------------------status  received message ends ----------------------*/
         //---------------------------------------------------------------------------------------
//CASE3:
                   /*---------------------status  responded message starts ----------------------*/
                        JSONArray respondedMsg=jsonObject.getJSONArray("respondedMsg");
                        for(int j=0;j<respondedMsg.length();j++)
                        {
                            JSONObject statusJsonObject = (JSONObject) respondedMsg.get(j);
                            _to = statusJsonObject.getString("_to");
                            status = statusJsonObject.getString("status");
                            time = statusJsonObject.getString("time");

                            if(status.equals(CONSTANT.STATUS_DELIVERED))// we get outgoing msg as delivered from server
                            {
                                Log.d("MessageService","updateStatus STATUS_DELIVERED to STATUS_DONE");
                                db.updateStatus(CONSTANT.STATUS_DONE, "_To", _to,time);
                            }
                            else if(status.equals(STATUS_READ)) // we get our messages as readed from server
                            {                                             //so we are done with these(3 ticks)
                                Log.d("MessageService","updateStatus STATUS_READ to STATUS_DONE_ALL");
                                db.updateStatus(CONSTANT.STATUS_DONE_ALL, "_To", _to,time);

                            }

                            if(msgList!=null && mAdapter!=null)  // update to user interface
                            {
                                for(MessageData k:msgList)  // sended to server so update status=1 by finding matching time
                                    if(k.getTime().equals(time)  && k.getTo().equals(_to))
                                    {
                                        if(status.equals(CONSTANT.STATUS_DELIVERED))// we get our messages as delivered from server
                                        {                                           //so we are delivered with these(2 ticks)
                                            k.setStatus(CONSTANT.STATUS_DONE);
                                        }
                                        else if(status.equals(STATUS_READ)) // we get our messages as readed from server
                                        {                                             //so we are done with these(3 ticks)
                                            k.setStatus(CONSTANT.STATUS_DONE_ALL);
                                        }
                                        //      mListView.setSelection(mAdapter.getCount() - 1);
                                        mAdapter.notifyDataSetChanged();
                                        break;
                                    }
                            }
                        }

    //--------------------------------------------------------------------------------------------------------------------------
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


//  /**          try {
//                if (jsonObject != null) {
//                    int d=0;
//                    while (d<list.size())
//                    {
//                        HashMap hashMap1= (HashMap) list.get(d);
//                        MessageData mData = new MessageData();
//                        mData.setTo(hashMap1.get("to").toString());
//                        mData.setFrom(hashMap1.get("from").toString());
//                        mData.setStatus(hashMap1.get("status").toString());
//                        mData.setText(hashMap1.get("message").toString());
//                        mData.setPic(hashMap1.get("pic").toString());
//                        mData.setType(hashMap1.get("type").toString());
//                        mData.setTime(hashMap1.get("time").toString());
//                        String s_t = hashMap1.get("status").toString();
//                        String time=hashMap1.get("time").toString();
//                        if(msgList!=null) {
//                            for(MessageData i:msgList) {
//                                if(i.getTime().equals(time)) {
//                                if(s_t.equals(STATUS_WAITING)) {
//                                    // sended  waited msg to server so update status=1 for by finding matching time
//                                        i.setStatus(STATUS_SERVER);
//                                        new SqliteHelper(getApplicationContext()).updateStatus(STATUS_SERVER,"_To",i.getTo(),i.getTime());
//                                    }
//                                else if(s_t.equals(STATUS_READ)) {
//                                    // send readed for incoming msg to server so update status=doneall by finding matching time
//                                        i.setStatus(STATUS_DONE_ALL);
//                                        new SqliteHelper(getApplicationContext()).updateStatus(STATUS_DONE_ALL,"_To",i.getTo(),i.getTime());
//                                    }
//
//                                break;
//                                }
//                            }
//                        }
//                        d++;
//                    }
//                    if(mAdapter!=null)
//                        mAdapter.notifyDataSetChanged();
//
//                    JSONArray jsonArray = jsonObject.getJSONArray("Result");
//                    String _to,_from="",status,message="",type,time,pic;
//                    for(int i=0;i<jsonArray.length();i++)
//                    {
//                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
//                        _to = jsonObject1.getString("_to");
//                        _from = jsonObject1.getString("_from");
//                        status = jsonObject1.getString("status");  // status=2 STATUS_DELIVERED for incoming msg
//                        message = jsonObject1.getString("message");
//                        pic = jsonObject1.getString("pic");
//                        type = jsonObject1.getString("type");
//                        time = jsonObject1.getString("time");
//                        // message=message+" SERVICE";
//                        db.insertMessage(_to, _from, status, message, pic, type, time); // insert to local db
//                        // ss will be "" when message activity is closed
//                        if (MessageActivity.isStarted && (MessageActivity._to).equals(_from)) {
//
//                            MessageData mData = new MessageData();
//                            mData.setTo(_to);
//                            mData.setFrom(_from);
//                            mData.setStatus(status);
//                            mData.setText(message);
//                          //  mData.setPic(pic);
//                            mData.setType(type);
//                            mData.setTime(time);
//
//                            msgList.add(mData);
//                            mAdapter.notifyDataSetChanged();
//                        }
//
//
//                        // Make notification for new messsage received
//                        NotificationManager notificationManager = (NotificationManager)
//                                getSystemService(NOTIFICATION_SERVICE);
//
//                        NotificationCompat.Builder mbuilder = new NotificationCompat.Builder(getApplicationContext())
//                                .setContentTitle("Message From " + _from)
//                                .setContentText(message)
//                                .setAutoCancel(true)
//                                .setSmallIcon(R.mipmap.ic_launcher)
//                                .setVibrate(new long[]{1000, 500, 1000, 500, 1000});
//                        notificationManager.notify(110, mbuilder.build());
//                    }
//
//                    JSONArray statusJsonArray = jsonObject.getJSONArray("statusData");
//                        for(int j=0;j<statusJsonArray.length();j++)
//                        {
//                            JSONObject statusJsonObject = (JSONObject) statusJsonArray.get(j);
//                            _to = statusJsonObject.getString("_to");
//                            status = statusJsonObject.getString("status");
//                            time = statusJsonObject.getString("time");
//
//                            if(status.equals(CONSTANT.STATUS_DELIVERED))// we get outgoing msg as delivered from server
//                            {
//                                db.updateStatus(CONSTANT.STATUS_DONE, "_To", _to,time);
//                            }
//                            else if(status.equals(STATUS_READ)) // we get our messages as readed from server
//                            {                                             //so we are done with these(3 ticks)
//                                 db.updateStatus(CONSTANT.STATUS_DONE_ALL, "_To", _to,time);
//                            }
//
//                            if(msgList!=null && mAdapter!=null)  // update to user interface
//                            {
//                                for(MessageData k:msgList)  // sended to server so update status=1 by finding matching time
//                                    if(k.getTime().equals(time)  && k.getTo().equals(_to))
//                                    {
//                                        if(status.equals(CONSTANT.STATUS_DELIVERED))// we get our messages as delivered from server
//                                        {                                           //so we are delivered with these(2 ticks)
//                                            k.setStatus(CONSTANT.STATUS_DONE);
//                                        }
//                                        else if(status.equals(STATUS_READ)) // we get our messages as readed from server
//                                        {                                             //so we are done with these(3 ticks)
//                                            k.setStatus(CONSTANT.STATUS_DONE_ALL);
//                                        }
//                                        //      mListView.setSelection(mAdapter.getCount() - 1);
//                                        mAdapter.notifyDataSetChanged();
//                                        break;
//                                    }
//                            }
//                        }
//                }
//
//            } catch (JSONException e) {e.printStackTrace();}
//
//            */
            db.close();
            super.onPostExecute(jsonObject);
        }


    }
}
