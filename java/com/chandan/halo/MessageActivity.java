package com.chandan.halo;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.chandan.halo.Parser.JParserAdv;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.chandan.halo.CONSTANT.CAMERA_PERMISSION_CODE;
import static com.chandan.halo.CONSTANT.STATUS_FAILED;
import static com.chandan.halo.CONSTANT.STATUS_SERVER;
import static com.chandan.halo.CONSTANT.STATUS_WAIT;
import static com.chandan.halo.CONSTANT.STATUS_WAITING;


public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 44 ;
    /**
    0:-not send 1:-server 2:-delivered  3:-read   4:-done  5-failed 6 done all
     */

    public static final String TAG="HALO:";
    ImageButton Camera,Send_button;
    LinearLayout view;
    EditText msg_text;
    static ListView mListView;
    static ArrayList<MessageData>  msgList;
    static MessageAdapter mAdapter;
    static String _to="",_from="";
    int mIndex;
    static boolean isStarted=false,isActive=false;//isStarted used to mark read,isActive to update UI
    private  String phno,name;

    private static final String JPEG_FILE_PREFIX = "IMG_";

    @Override
    protected void onRestart() {
        Log.i(TAG,"MessageActivity onRestart");
        super.onRestart();
    }

    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private String mCurrentPhotoPath;



    @Override
    protected void onStop() {
        isStarted=false;
        Log.i(TAG,"MessageActivity onStop");
        super.onStop();
    }
    @Override
    protected void onStart() {
        isStarted=true;
        Log.i(TAG,"MessageActivity onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"MessageActivity onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"MessageActivity onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"MessageActivity onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"MessageActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
         name= intent.getStringExtra("name");
         phno= intent.getStringExtra("phno");

        SqliteHelper db = new SqliteHelper(getApplicationContext());
        Log.d("MessageActivity","updateStatusToRead");
        db.updateStatusToRead(phno);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back arrow button
        getSupportActionBar().setTitle(name);


        mListView = (ListView) findViewById(R.id.list_message);
        mListView.setDivider(null);
        msgList=new  ArrayList<MessageData>();
        mListView.smoothScrollToPosition(mListView.getCount() - 1);// updating listview to show to last data

        SqliteHelper sqliteHelper=new SqliteHelper(getApplicationContext());
        String frnds_data[]=sqliteHelper.getFriends(); // get friends list with account from db


//        for(int i=0;i<frnds_data.length;i++)
//        {
//            if(phno.equals(frnds_data[i]))
//            {
                _to =phno;
//                db.updateStatusToRead(_to);
//                break;
//            }
//        }

        SessionManager manager=new SessionManager(getApplicationContext());
        _from=manager.getKeyPhone();


        String where=" WHERE _To = '"+_to+"' OR  _From = '"+_to+"'";
        Log.d("MessageActivity","retrieve old messages from local db");
        ArrayList oldlist=new SqliteHelper(getApplicationContext()).retrieveMessage(where); // retrieve old messages from local db


        int d=0;
        while (d<oldlist.size()) {
            HashMap hashMap1= (HashMap) oldlist.get(d);
            MessageData mData = new MessageData();
            mData.setTo(hashMap1.get("to").toString());
            mData.setFrom(hashMap1.get("from").toString());
            mData.setStatus(hashMap1.get("status").toString());
            mData.setText(hashMap1.get("message").toString());
      //      mData.setPic(hashMap1.get("pic").toString()); TODO CHANGE TO BITMAP
            mData.setType(hashMap1.get("type").toString());
            mData.setTime(hashMap1.get("time").toString());
            msgList.add(mData);
            d++;
        }
        mAdapter = new MessageAdapter(this, msgList);

       // new loadOlderMessage().execute();


        Camera = (ImageButton)findViewById(R.id.camera);
        Send_button = (ImageButton) findViewById(R.id.send_button);
        msg_text = (EditText) findViewById(R.id.msg_text);

        Camera.setOnClickListener(this);
        Send_button.setOnClickListener(this);

        db = new SqliteHelper(getApplicationContext());

        view = (LinearLayout) findViewById(R.id.msg_area);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {                //Adjust msgbox area
                if(view.getHeight()>240)
                    view.getLayoutParams().height=240;
            }
        });

        

        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount  = mListView.getCheckedItemCount();
                // Set title according to total checked items
                mode.setTitle(checkedCount  + "  Selected");
                // Calls  toggleSelection method to toggle selection
                mAdapter.toggleSelection(position);
            }
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.multiple_delete, menu);
                return true;
            }
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch  (item.getItemId()) {
                    case R.id.selectAll:
                        //
                        final int checkedCount  = msgList.size();
                        // If item  is already selected or checked then remove or
                        // unchecked  and again select all
                        mAdapter.removeSelection();
                        for (int i = 0; i <  checkedCount; i++) {
                            mListView.setItemChecked(i,   true);//  listviewadapter.toggleSelection(i);
                        }
                        // Count no.  of selected item and print it
                        mode.setTitle(checkedCount  + "  Selected");

                        return true;
                    case R.id.delete:
                        // Add  dialog for confirmation to delete selected item
                        // record.
                        AlertDialog.Builder  builder = new AlertDialog.Builder(MessageActivity.this);
                        builder.setMessage("Are you sure to delete selected message ?");
                        builder.setNegativeButton("No", new  DialogInterface.OnClickListener() {
                            @Override
                            public void  onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub
                            }
                        });
                        builder.setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
                            @Override
                            public void  onClick(DialogInterface dialog, int which) {
                                // TODO  Auto-generated method stub
                                SparseBooleanArray selected = mAdapter.getSelectedIds();
                                for (int i =  (selected.size() - 1); i >= 0; i--) {
                                    if  (selected.valueAt(i)) {
                                        MessageData  selecteditem = (MessageData) mAdapter.getItem(selected.keyAt(i));
                                        // Remove  selected items following the ids
                                        SqliteHelper helper=new SqliteHelper(getApplicationContext());
                                        helper.deleteSelectedMessages(selecteditem.getFrom(),selecteditem.getTo(),selecteditem.getTime());
                                        mAdapter.remove(selecteditem);


                                    }
                                }

                                mode.finish();
                                selected.clear();

                            }
                        });
                        AlertDialog alert =  builder.create();
                     //   alert.setIcon(R.drawable.questionicon);// dialog  Icon
                        alert.setTitle("Confirm");
                        alert.show();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.removeSelection();
            }
        });

        mListView.setSelection(mAdapter.getCount() - 1); // updating listview to show to last data
          mListView.smoothScrollToPosition(mListView.getCount() - 1);// updating listview to show to last data

    }



    private  void RequestPermissions() {

        ActivityCompat.requestPermissions(this, (new String[]{Manifest.permission.CAMERA}), CAMERA_PERMISSION_CODE);
        }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivity(intent);
                } else {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                        new AlertDialog.Builder(this)
                                .setMessage("Camera Permission required to capture Image")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                       RequestPermissions();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Snackbar.make(Camera, "Camera Permission Denied", Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        Snackbar.make(Camera, "Go System Settings For Camera Permission", Snackbar.LENGTH_SHORT).show();
                    }


                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_clearChat: {
                SqliteHelper db = new SqliteHelper(getApplicationContext());
                db.deleteAllMessages(_to);
                msgList.clear();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_viewContact: {
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra("name",name);
                intent.putExtra("phno",phno);
                startActivity(intent);
                break;
            }
            case R.id.action_userMap:
            {
                Intent intent =new Intent(this,MapsActivity.class);
                intent.putExtra("name",name);
                intent.putExtra("phno",phno);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {

            switch (view.getId()) {
                case R.id.camera: {
                    int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                    if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, (new String[]{Manifest.permission.CAMERA}), CAMERA_PERMISSION_CODE);
                    } else {

                    }
                }

                break;

                case R.id.send_button: {
                    String time, message, type, pic;
                    message = msg_text.getText().toString().trim();
                    if (!message.isEmpty()) {
                        msg_text.setText("");
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSSS");
                        Calendar calobj = Calendar.getInstance();
                        time = dateFormat.format(calobj.getTime());//locale
                        type = "text";
                        pic = "";
                        MessageData mData = new MessageData();
                        mData.setTime(time);
                        mData.setText(message);
                        mData.setTo(_to);
                        mData.setFrom(_from);
                        mData.setStatus(STATUS_WAIT);
                        msgList.add(mData);

                        mAdapter.notifyDataSetChanged();
                        //mListView.setSelection(mAdapter.getCount() - 1); // updating listview to show to last data
                        //  mListView.smoothScrollToPosition(mListView.getCount() - 1);// updating listview to show to last data
                        SqliteHelper db = new SqliteHelper(getApplicationContext());
                        db.insertMessage(_to, _from, STATUS_WAIT, message, pic, type, time); // insert to local db
                        new sendToServer().execute(_to, _from, STATUS_WAIT, message, pic, type, time);


                    }

                    //mListView.setSelection(mAdapter.getCount() - 1); // updating listview to show to last data

                    break;
                }

            }
    }


    class sendToServer extends AsyncTask<String,String,JSONObject>
    {
        private String time;
        @Override
        protected JSONObject doInBackground(String... params) {
            HashMap<String,String> hashMap=new HashMap<>();// no parms can be null

            hashMap.put("_to",params[0]);
            hashMap.put("_from",params[1]);
            hashMap.put("status",params[2]);
            hashMap.put("message",params[3]);
            hashMap.put("pic",params[4]);
            hashMap.put("type",params[5]);
            hashMap.put("time",params[6]);
            time=params[6];

            Log.d("sendToServer:\t\t",params[0]+" "+params[1]+" "+params[2]+" "+params[3]+" "+params[4]+" "+params[5]+" "+params[6]);
            JParserAdv jParser=new JParserAdv();
            JSONObject jsonObject=jParser.makeHttpRequest(CONSTANT.sendUrl,"GET",hashMap);

            if(jsonObject != null){
                return jsonObject;
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            MessageData j=null;
            String newStatus="";
            for(MessageData i:msgList)  // sended to server so update status=1 by finding matching time
            {
                if(i.getTime().equals(time))
                {
                    j=i;
                    break;
                }
            }

            try {
                JSONObject jsonObject1 = jsonObject.getJSONObject("Result");
                String st = jsonObject1.getString("status");
                if (st.equals(STATUS_SERVER)) {  //Send_button, "Success..."
                   newStatus=STATUS_SERVER;
                }
                else if(st.equals(STATUS_FAILED)){
                    newStatus=STATUS_FAILED;
                //    Snackbar.make(Send_button, "Failed...", Snackbar.LENGTH_SHORT).show();
                }
            }catch (Exception ex) {
                newStatus=STATUS_WAITING;
                Snackbar.make(Send_button, "Check Your Connection!", Snackbar.LENGTH_SHORT).show();
            }
            if(j!=null && !newStatus.equals("")) {
                j.setStatus(newStatus);
                mAdapter.notifyDataSetChanged();
                Log.d("MessageActivity","updatestatus");
                new SqliteHelper(getApplicationContext()).updateStatus(newStatus, "_To", j.getTo(), j.getTime());
            }

        }
    }


}