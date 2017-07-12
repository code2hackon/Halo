package com.chandan.halo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Chandan on 05-07-2017.
 */

public class MapUserAdapter extends RecyclerView.Adapter<MapUserAdapter.MyViewHolder> {
    private HashMap<Marker,LocationObject> hashMap ;
    private Context mContext;
    private MyViewHolder v;
    private Marker[] mKeys;

    public MapUserAdapter(Context context, HashMap<Marker,LocationObject> list)
    {
        int size=list.size();
        hashMap=new HashMap<>();
        hashMap.putAll(list);
        mKeys=list.keySet().toArray(new Marker[size]);
        mContext=context;
    }
    @Override
    public int getItemCount() {
        return hashMap.size();
    }

    @Override
    public MapUserAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.locationdetailsview, viewGroup, false);
        MapUserAdapter.MyViewHolder viewHolder = new MapUserAdapter.MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder hold, int position) {
        Marker marker=mKeys[position];
        LocationObject locationObject=hashMap.get(marker);
        final Double lat=Double.parseDouble(locationObject.getLatitute());
        final Double lng=Double.parseDouble(locationObject.getLongitude());
        final String tit="Name: "+marker.getTitle();
        hold.title.setText(tit);
        final String pos="Phone No.: "+marker.getSnippet()+"\nLocation: "+lat+" , "+lng;
        hold.latlong.setText(pos);
        final String time="Last Sync: "+locationObject.getLastupdated();
        hold.time.setText(time);

        hold.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng=new LatLng(lat,lng);
                new MapsActivity().zoomToMarker(latLng);
            }
        });
        hold.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, tit+pos+time);
                sendIntent.setType("text/plain");
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent chooser=Intent.createChooser(sendIntent, "Send Location");
                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(chooser);
            }
        });
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, latlong,time;
        ImageView message;
        LinearLayout ll;
        public MyViewHolder(View view) {
            super(view);
            ll=(LinearLayout)view.findViewById(R.id.locLayout);
            title = (TextView) view.findViewById(R.id.locdetDetails);
            latlong = (TextView) view.findViewById(R.id.locdetLatLong);
            time=(TextView)view.findViewById(R.id.locdetTime);
            message=(ImageView)view.findViewById(R.id.LocationMsg);
        }
    }
}
