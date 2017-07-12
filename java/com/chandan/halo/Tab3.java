package com.chandan.halo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tab3 extends Fragment {
    public View view;
    public static RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    static RecyclerView.Adapter adapter;
    public static final String TAG = "HALO";
    Context tab2Context;
    private ArrayList<SelectUser> selectUsers;
    static boolean isTab3active = false;
    ArrayList<chatUser> list=new ArrayList<chatUser>();
    public static TextView textView;
    public static Context activityContext;

    @Override
    public void onResume() {
        isTab3active = true;
        new loadChatContacts(getActivity().getApplicationContext()).execute();
        super.onResume();
    }

    @Override
    public void onPause() {
        isTab3active = false;
        super.onPause();
    }

    public Tab3() {
        // Required empty public constructor
    }

    public static Tab3 newInstance(int sectionNumber) {
        Tab3 fragment = new Tab3();
        Bundle args = new Bundle();
        args.putInt("SECTION_NUMBER_3", sectionNumber);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_tab3, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.chatRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        textView=(TextView)view.findViewById(R.id.chatSuggest);
        recyclerView.setLayoutManager(layoutManager);

        if(list.size()==0)
        {
            textView.setVisibility(View.VISIBLE);
        }
        else
        {
            textView.setVisibility(View.GONE);
        }
        adapter=new chatRecyclerAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);

        activityContext=getActivity().getBaseContext();
        return view;
    }



    void setSelectUserData(List<SelectUser> ls) {
        selectUsers = new ArrayList<>();
        selectUsers.addAll(ls);
    }

    class loadChatContacts extends AsyncTask<Void, Void, Void> {
        Context applicationContext;
        /** used applicationContext for SessionManager,Sqlite(that belongs to application)
         *  used activityContext for RecycleAdapter(that belongs to activity)*/

        public loadChatContacts(Context context) {
            applicationContext = context;
        }

        String phoneno;

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(TAG, " loadChatContacts doInBackground started");

            SessionManager manager = new SessionManager(applicationContext);
            phoneno = manager.getKeyPhone();
            list = new SqliteHelper(applicationContext).chatListMessages(phoneno);
            Log.v("loadChatContacts", String.valueOf(list.size()));
            for(int i=0;i<list.size();i++)
            {
                Log.v("loadChatContacts", list.get(i).getPartMsg());
                String name = null;

                // define the columns I want the query to return
                String[] projection = new String[] {
                        ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.PhoneLookup._ID};

                // encode the phone number and build the filter URI
                Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(list.get(i).getNumber()));

                // query time
                if(applicationContext==null)return null;
                Cursor cursor = applicationContext.getContentResolver().query(contactUri, projection, null, null, null);

                if(cursor != null) {
                    if (cursor.moveToFirst()) {
                        name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        list.get(i).setName(name);
                    } else {
                        list.get(i).setName(list.get(i).getNumber());
                    }
                    cursor.close();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, " loadChatContacts finished");
            super.onPostExecute(aVoid);

            if(list.size()==0)
            {
                textView.setVisibility(View.VISIBLE);
            }
            else
            {
                textView.setVisibility(View.GONE);
            }
            adapter=new chatRecyclerAdapter(activityContext, list);
            recyclerView.setAdapter(adapter);

                adapter.notifyDataSetChanged();


        }
    }
}




