package com.chandan.halo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class SelectUserAdapter extends BaseAdapter implements SectionIndexer {


    public List<SelectUser> _data;
    private ArrayList<SelectUser> arraylist;
    Context _c;
    ViewHolder v;
    final String TAG="contacts size = :";


    // RoundImage roundedImage;
    public SelectUserAdapter(Context context,List<SelectUser> selectUsers) {
        _data = selectUsers;
        _data.size();
        _c = context;
        this.arraylist = new ArrayList<SelectUser>();
        this.arraylist.addAll(_data);

    }

    @Override
    public int getCount() {
        Log.d(TAG,_data.size()+" leng");
        int c=_data.size();
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
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;

        if (view == null) {
        LayoutInflater li = (LayoutInflater) _c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = li.inflate(R.layout.contactview, null);
        }
        else {
          view = convertView;
         }
int _color=0;


        v = new ViewHolder();
        v.title = (TextView) view.findViewById(R.id.contact_name);
        v.phone = (TextView) view.findViewById(R.id.contact_ph);
        v.imageView = (ImageView) view.findViewById(R.id.pic);
        v.invite = (Button) view.findViewById(R.id.contact_invite);
        v.txtImg=(TextView)view.findViewById(R.id.imgText);


        final SelectUser data = (SelectUser) _data.get(i);
         String name=data.getName();
        String phone=data.getPhone();
        _color=name.lastIndexOf(' ')+(int)name.charAt(name.length()-1);
        v.title.setText(name);
        v.phone.setText(phone);
        _color=_color+(int)phone.charAt(phone.length()-1);
        _color=_color%10;

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linContact);

        if (!data.getButtonVisibility())
        {
            v.invite.setVisibility(View.INVISIBLE);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {
                    Intent intent = new Intent(_c,MessageActivity.class);
                    intent.putExtra("name",data.getName());
                    intent.putExtra("phno",data.getPhone());
                    Activity activity = (Activity) ( v1.getContext());
                    activity.startActivity(intent);

                    //Toast.makeText(_c, _data.get(i).getPhone() + "", Toast.LENGTH_SHORT).show();
                    // notifyDataSetChanged();
                }
            });

        }
        else
        {
            v.invite.setVisibility(View.VISIBLE);

            linearLayout.setOnClickListener(null);
        }




        // Set image if exists

        try {

            if (data.getThumbString() != null) {
                v.txtImg.setVisibility(View.INVISIBLE);
                v.imageView.setVisibility(View.VISIBLE);
                byte[] DecodedImage = new byte[1024];
                Bitmap CreateImageBitmap = null, ImageBitmap = null;
                DecodedImage = Base64.decode(data.getThumbString(), Base64.DEFAULT);
                ImageBitmap = BitmapFactory.decodeByteArray(DecodedImage, 0, DecodedImage.length);
                if (ImageBitmap == null) {
                    InputStream is = new ByteArrayInputStream(DecodedImage);
                    ImageBitmap = BitmapFactory.decodeStream(is);
                }
                CreateImageBitmap = Bitmap.createBitmap(ImageBitmap);
                v.imageView.setImageBitmap(CreateImageBitmap);
            } else {
                v.imageView.setVisibility(View.INVISIBLE);
                v.txtImg.setVisibility(View.VISIBLE);
                v.txtImg.setBackgroundResource(R.drawable.contact_default_circle);
                GradientDrawable bgShape = (GradientDrawable)v.txtImg.getBackground();
                String color=CONSTANT.color[_color];
                bgShape.setColor(Color.parseColor(color));
                v.txtImg.setText(name.toUpperCase().charAt(0)+"");
                //v.imageView.setImageResource(R.drawable.ic_contact_picture_holo_light);

            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        //   Set listener
        v.invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                i.putExtra("address", data.getName());
                i.putExtra("sms_body", "Welcome! Join Halo here to take ease of life!");
                i.setType("vnd.android-dir/mms-sms");
                _c.startActivity(i);

                //notifyDataSetChanged();
            }
        });



        view.setTag(data);
        return view;
    }

    // Filter Class
//    public void filter(String charText) {
//        charText = charText.toLowerCase(Locale.getDefault());
//        _data.clear();
//        if (charText.length() == 0) {
//            _data.addAll(arraylist);
//        } else {
//            for (SelectUser wp : arraylist) {
//                if (wp.getName().toLowerCase(Locale.getDefault())
//                        .contains(charText)) {
//                    _data.add(wp);
//                }
//            }
//        }
//
//    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView title, phone,txtImg;
        Button invite;
    }
}