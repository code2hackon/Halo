package com.chandan.halo;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by chandan on 29-10-2016.
 */


public class MessageAdapter extends BaseAdapter {
    Context mContext;
    List<MessageData> _data;
    View_holder view_holder;

    private  SparseBooleanArray mSelectedItemsIds;
    public void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        notifyDataSetChanged();
    }
    public void toggleSelection(int position) {
       boolean newValue = !mSelectedItemsIds.get(position);
        if (newValue)
        {
            mSelectedItemsIds.put(position,  newValue);
        }
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public void remove(Object selecteditem) {
        _data.remove(selecteditem);
        notifyDataSetChanged();
    }

    static class View_holder {
        TextView msgtext,time,captionData;
        ImageView status,imgData;
        LinearLayout rl,ll,frameLay,textLay;
    }
    public  MessageAdapter(Context context, List<MessageData> msgData)
    {
        _data = msgData;
        mContext = context;
        mSelectedItemsIds = new  SparseBooleanArray();
    }
    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view = convertView;

        if (view == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = li.inflate(R.layout.message_view, null);
        }
        else {
            view = convertView;
        }

        LinearLayout rel=(LinearLayout) view.findViewById(R.id.rel);
        if(mSelectedItemsIds.get(i))
         rel.setBackgroundColor(Color.parseColor("#FFB2CED7"));
        else
            rel.setBackgroundColor(Color.parseColor("#f5f2f2"));

//        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.rel);
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
//        params.setMargins(0, 0, 30, 0);
//        layout.setLayoutParams(params);

        view_holder = new View_holder();
        view_holder.msgtext = (TextView) view.findViewById(R.id.msgtextView);
        view_holder.rl= (LinearLayout) view.findViewById(R.id.rel);
        view_holder.ll = (LinearLayout)view.findViewById(R.id.linear);
        view_holder.textLay = (LinearLayout)view.findViewById(R.id.textBoxLayout);
        view_holder.frameLay = (LinearLayout)view.findViewById(R.id.frameLayout);
        view_holder.time=(TextView)view.findViewById(R.id.time);
        view_holder.status=(ImageView) view.findViewById(R.id.statusImage);

        view_holder.captionData=(TextView)view.findViewById(R.id.captionData);
        view_holder.imgData=(ImageView)view.findViewById(R.id.imgData);



        final MessageData data = (MessageData) _data.get(i);

        String s1=data.getFrom();
        String type=data.getType();

        if(s1==null)
        {
            Log.d("MessageAdapter:\t\t\t{","s1 is null"+data.getText());

        }
        if(type.equals("IMG"))
        {
            view_holder.textLay.setVisibility(View.GONE);
            view_holder.frameLay.setVisibility(View.VISIBLE);
            view_holder.imgData.setImageBitmap(data.getPic());
            view_holder.captionData.setText(data.getCaption());

        }
        else
        {
            view_holder.frameLay.setVisibility(View.GONE);
            view_holder.textLay.setVisibility(View.VISIBLE);
            view_holder.msgtext.setText(data.getText());
        }



        SessionManager manager=new SessionManager(mContext.getApplicationContext());

        if(data.getFrom().compareTo(manager.getKeyPhone())==0) {
            view_holder.rl.setGravity(Gravity.RIGHT);
            view_holder.ll.setBackgroundResource(R.drawable.rounded_corner_right);
            view_holder.status.setVisibility(View.VISIBLE);
            switch (data.getStatus().toString()) {
                case "9": {
                    view_holder.status.setBackgroundResource(R.mipmap.message_unsent);
                    break;
                }
                case "1":{
                    view_holder.status.setBackgroundResource(R.mipmap.msg_status_server_receive);
                    break;}
                case "4": {
                    view_holder.status.setBackgroundResource(R.mipmap.msg_status_client_received);
                    break;}
                case "6": {
                    view_holder.status.setBackgroundResource(R.mipmap.msg_status_client_read);
                    break;}
                case "5": {
                    view_holder.status.setBackgroundResource(R.mipmap.msg_status_failed);
                    break;}
                default:
                    view_holder.status.setBackgroundResource(R.mipmap.message_unsent);
            }
        }
        else
        {
            view_holder.rl.setGravity(Gravity.LEFT);
            view_holder.ll.setBackgroundResource(R.drawable.rounded_corner_left);
            view_holder.status.setVisibility(View.INVISIBLE);
        }


        String t1= data.getTime().substring(9,14);
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(t1);
            t1=new SimpleDateFormat("K:mm a").format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
      //  view_holder.time.setText("from="+data.getFrom()+"==uid="+MainActivity.mUserId+" /"+t1+"/ to ="+data.getTo()+" st="+data.getStatus());
        view_holder.time.setText(t1);

        view.setTag(data);

        return view;
    }
}
