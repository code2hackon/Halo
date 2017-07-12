package com.chandan.halo;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.chandan.halo.Parser.JParserAdv;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER;


//import static com.chandan.halo.R.id.thing_proto;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tab2 extends Fragment  {
    ListView mListView;
    private SelectUserAdapter mAdapter;
    Cursor phones;
    SqliteHelper db;
    public static Context applicationContext,activityContext;
    public static final String TAG="HALO";
    ArrayList<SelectUser> selectUsers;


    public Tab2() {
        // Required empty public constructor
    }

    public static Tab2 newInstance(int sectionNumber) {
        Tab2 fragment = new Tab2();
        Bundle args = new Bundle();
        args.putInt("SECTION_NUMBER_2", sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab2, container, false);
        mListView = (ListView) view.findViewById(R.id.my_list_view);
        activityContext=this.getContext();
        applicationContext=getContext().getApplicationContext();
        if(selectUsers==null)
            selectUsers = new ArrayList<SelectUser>();
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the ListView
        //    mRecyclerView.setHasFixedSize(true)


        // if(selectUsers.size()==0)

            new MyContactsData().execute();
            new LoadContact().execute();

        setHasOptionsMenu(true);
        return  view;

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void checkforAccounts(Context asynContext)
    {
         db =new SqliteHelper(asynContext);
        String frnd[] = db.getFriends();
        db.close();
      //  int l1=frnd[0].length;//col len
      //  int l2=frnd.length; //row len
        if(frnd.length>0) {

                for (SelectUser i : selectUsers)
                {
                    boolean flag=true;
                    for (int j=0;j<frnd.length;j++)
                        if (noextract(i.getPhone(),frnd[j]))
                        {
                            flag=false;
                            break;
                        }
                    i.setButtonVisibilty(flag);
                }
        }

    }



    class MyContactsData extends AsyncTask<Void,Void,Void> {
     //   Context mContext;
        @Override
        protected void onPreExecute() {
            Log.i(TAG,"myContactsData  started"+" ");
         //   mContext=getContext();
//            pDialog=new ProgressDialog(getContext());
//            pDialog.setTitle(" Please wait ...");
//            pDialog.setIndeterminate(true);
//            pDialog.setCancelable(false);
//            pDialog.show();

//            String selection = ""+ NUMBER +" IN (SELECT DISTINCT " + NUMBER  +" FROM "+
//                    "contacts)";
            phones=applicationContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            super.onPreExecute();
        }


        protected Void doInBackground(Void... params) {
            Log.i(TAG,"myContactsData doInBackground  started"+" ");
            String imageString = null;
            int len;
            if (phones != null) {

                SessionManager manager=new SessionManager(applicationContext);
                String num=manager.getKeyPhone();
                //   if(phones.getCount()==0)
                //     Toast.makeText(MainActivity.this, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                while (phones.moveToNext()) {

                    Bitmap bit_thumb = null;
                    String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(NUMBER));
                    phoneNumber = phoneNumber.replace(" ", "");
//                    if(phoneNumber.startsWith("1"))
//                        continue;
                    len=phoneNumber.length();
                    if(len==10 || len == 13)
                    phoneNumber="+91"+phoneNumber.substring(len-10);
                    else
                        continue;

                    if(noextract(num,phoneNumber))
                        continue;

//                    if(phoneNumber.length()!=13)
//                        continue;


                    //           phoneNumber=phoneNumber.trim();
              /**      String image_thumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    try {
                        if (image_thumb != null && mContext!=null) {
                            bit_thumb = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(image_thumb));
                            imageString = encodeImage(bit_thumb);
                        } else {
                            // Log.e("No Image Thumb", "--------------");
                            imageString = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    SelectUser selectUser = new SelectUser();
                    selectUser.setName(name);
                     selectUser.setPhone(phoneNumber);
                    selectUser.setThumbString(imageString);
                    selectUser.setButtonVisibilty(true);
                   // selectUsers.add(selectUser);
                    AddData(selectUsers,selectUser);
                }
            } else {
                Log.e("Cursor close 1", "----------------");
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG,"myContactsData  finished");
            super.onPostExecute(aVoid);
            checkforAccounts(applicationContext);
            mAdapter = new SelectUserAdapter( activityContext,selectUsers);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
//            pDialog.dismiss();


//            if(isFinished)    //network task is recreated only when it finishes previous task
//            {
//                isFinished=false;
//                new LoadContact().execute();
//            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new Tab3().setSelectUserData(selectUsers);
                }
            }).start();

        }

    }
        public static boolean  noextract(String str1,String str2)
        {
            //String st="";
            char ch[]=str1.toCharArray();
            String st="";
            for(char ch1 : ch)
            {
                if(Character.isDigit(ch1))
                {
                    st=st+ch1;
                }
            }

            ch=str2.toCharArray();
            String st2="";
            for(char ch1 : ch)
            {
                if(Character.isDigit(ch1))
                {
                    st2=st2+ch1;
                }
            }
            if(st.equals(st2))
            {
                return true;
            }
            else
            {
                return false;
            }
        }


        public boolean AddData(ArrayList<SelectUser> l1, SelectUser selectUser )
        {
            String Cmpstr=selectUser.getPhone().trim();

            // char ch[]=Cmpstr.toCharArray();
            boolean b=false;
            for(SelectUser selectUser1 :l1)
            {
                String CmpCurrent=selectUser1.getPhone().trim();
                //CmpCurrent=CmpCurrent.replace("?","");
                //char ch1[]=CmpCurrent.toCharArray();
                // int no=CmpCurrent.compareTo(Cmpstr);

                if(noextract(Cmpstr,CmpCurrent))
                {
                    b=true;
                    break;
                }

            }

            if(!b)
                l1.add(selectUser);
            return b;

        }


        public String encodeImage(Bitmap image){
            Bitmap bitmap = image;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte [] byte_arr = stream.toByteArray();
            Log.d("Image Base64",stream.toString());
            String image_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);
            return image_str;
        }

    class LoadContact extends AsyncTask<Void, String, JSONObject> {




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            HashMap<String, String> hashMap = new SqliteHelper(getContext()).getUserDetails();//needed plz not remove
//            getUserId = hashMap.get("username");

            Log.i(TAG,"LoadContact  started");
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            Log.i(TAG,"LoadContact doInBackground  started");
            JParserAdv jParserAdv = new JParserAdv();
            Gson gson = new Gson();
            HashMap<String, List<SelectUser>> obj = new HashMap<String, List<SelectUser>>();
            obj.put("userdata", selectUsers);
            String jsonList = gson.toJson(obj);
            HashMap<String, String> hm = new HashMap<>();

            SessionManager manager=new SessionManager(applicationContext);
            hm.put("phoneno", manager.getKeyPhone());
            hm.put("contacts", jsonList);

            JSONObject jsonObject=null;
            if(!hm.isEmpty())
             jsonObject = jParserAdv.makeHttpRequest(CONSTANT.contactsUrl, "POST", hm);
//            hm.clear();
            if (jsonObject != null) {
                return jsonObject;
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            Log.i(TAG,"loadContact  finished");
            //  adapter = new SelectUserAdapter(selectUsers, SearchFriend.this.getContext());
            // listView.setAdapter(adapter);


            // Select item on listclick

            //        listView.setFastScrollEnabled(true);
            ArrayList<String> regList =new ArrayList<String>();
            try {
                if (jsonObject != null) {
                    JSONObject jsonObject1 = jsonObject.getJSONObject("Result");
                    String  st = jsonObject1.getString("status");
                    if (st.equals("0")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Available");
                        // {"Available":["+919525422296","+917759081041"]}
                        String contactlist="";

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String phone_no = jsonArray.getString(i);
                            Log.v("Contacts Avail", phone_no);

                            regList.add(phone_no);
                            if(i < jsonArray.length()-1)
                                contactlist="'"+phone_no+"',"+contactlist;
                            else
                                contactlist=contactlist+"'"+phone_no+"'";
                        }



                        //name = "";
                        //    for (int i = 0; i < jsonObject1.length(); i++) {
                        //   regList.add(jsonObject1.getString(i));
                        //  name = name + jsonObject1.getString(i) + "\n";
                        //   }
                        // if(!regList.contains(jsonObject1.getString(i)))
                        //{
//                    try {
//                        FileOutputStream fileOutputStream = getContext().openFileOutput("myData.txt", Context.MODE_PRIVATE);
//                        fileOutputStream.write(name.getBytes());
//                        fileOutputStream.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                        //regList.add(jsonObject1.getString(i));
                        //}
                        //     db.addContacts(jsonObject1.getString(i));
                        //   regList=db.getContacts();
                        //                 db.close();
                        for (SelectUser i : selectUsers) {
                            boolean flag = true;
                            for (String j : regList)
                                if (noextract(i.getPhone(), j)) {
                                    flag = false;
                                    db = new SqliteHelper(applicationContext);
                                    db.addFriends(j,i.getName());
                                    break;
                                }
                            i.setButtonVisibilty(flag);
                        }


                        if(contactlist.length()>0)
                            db.deleteNoAccountContacts(contactlist);
                        db.close();
                        regList.clear();

//            mAdapter = new SelectUserAdapter( getContext(),selectUsers);
//            mListView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"Poor Internet Connection",Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(jsonObject);
        }
    }
}