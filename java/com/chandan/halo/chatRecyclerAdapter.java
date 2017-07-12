package com.chandan.halo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class chatRecyclerAdapter extends RecyclerView.Adapter<chatRecyclerAdapter.ViewHolder>
{
    private ArrayList<chatUser> arraylist;
    Context context;
    TextView suggestchat;
    class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView onlineImg,contactImage;
        TextView textName,textMsg,textDate,circleText;
        LinearLayout touchItem;

        public ViewHolder(View itemView) {
            super(itemView);
            textName=(TextView)itemView.findViewById(R.id.nameChatView);
            textMsg=(TextView)itemView.findViewById(R.id.partialMsg);
            textDate=(TextView)itemView.findViewById(R.id.dateChatView);
           // onlineImg=(ImageView)itemView.findViewById(R.id.onlineChatView);
            contactImage=(ImageView)itemView.findViewById(R.id.chatCircleImage);
            circleText=(TextView)itemView.findViewById(R.id.chatCircleText);
            touchItem=(LinearLayout)itemView.findViewById(R.id.openchatActivity);
        }
    }

    public chatRecyclerAdapter(Context context,ArrayList<chatUser> chatUsersList)
    {
        this.arraylist=new ArrayList<chatUser>();
        this.context=context;
        this.arraylist.addAll(chatUsersList);
    }

    @Override
    public chatRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_view, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(chatRecyclerAdapter.ViewHolder holder, int position) {

        final chatUser item = arraylist.get(position);
        final String name=item.getName();
        final String phone=item.getNumber();
        holder.touchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MessageActivity.class);
                intent.putExtra("name",name);
                intent.putExtra("phno",phone);
                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);/**Calling startActivity() from outside of
                 an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag.*/
                context.startActivity(intent);
            }
        });

        int _color=name.lastIndexOf(' ')+(int)name.charAt(name.length()-1);
        _color=_color+(int)phone.charAt(phone.length()-1);
        _color=_color%10;
        GradientDrawable bgShape = (GradientDrawable)holder.circleText.getBackground();
        String color=CONSTANT.color[_color];
        bgShape.setColor(Color.parseColor(color));

        holder.circleText.setText(name.toUpperCase().charAt(0)+"");

        holder.textName.setText(name);
        holder.textMsg.setText(item.getPartMsg());
        String t1= item.getTime().substring(9,14);
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(t1);
            t1=new SimpleDateFormat("K:mm a").format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        holder.textDate.setText(t1);
//        holder.itemTitle.setText(titles[pos]);
//        holder.itemDetail.(details[pos]);
//        holder.itemImage.setImageResource(images[pos]);

    }

    @Override
    public int getItemCount() {

        int size= arraylist.size();
        return size;
    }
}
