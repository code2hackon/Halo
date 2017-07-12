package com.chandan.halo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.chandan.halo.CONSTANT.SAVE_EXTERNAL;
import static com.chandan.halo.CONSTANT.SAVE_INTERNAL;

public class DetailsActivity extends AppCompatActivity {

    Drawable drawable=null;
    ImageView imageView;
    String phoneno,name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView location=(TextView)findViewById(R.id.frnd_loc);
        TextView time=(TextView)findViewById(R.id.frnd_lastSync);
        imageView=(ImageView)findViewById(R.id.prof_details);

        Intent intent=getIntent();
         phoneno=intent.getStringExtra("phno");
         name=intent.getStringExtra("name");
        getSupportActionBar().setTitle(name);
        SqliteHelper helper=new SqliteHelper(getApplicationContext());
        String det[]=helper.getLocation(phoneno);
        if(det!=null) {
            location.setText(det[0] + " , " + det[1]);
            time.setText("Last Sync: " + det[2]);
        }


       String localImgpath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/HALO/Profile Picture/HALO_"
                +phoneno+".jpeg";
        drawable= Drawable.createFromPath(localImgpath);
        // setProfileImg(localImgpath,IMAGE_LOCAL);
        setProfileImg(CONSTANT.profileImgUrl+"HALO_"+phoneno+".jpeg");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private  void setProfileImg(String selectedImage) {
        if (selectedImage == null)
            return;

        Glide.with(this)
                .load(selectedImage)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .placeholder(drawable)
                .skipMemoryCache(true)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        //fetching from server , so save file locally
                        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                        int quality = 100;
                        try {
                            File fileName = createImageFile(SAVE_EXTERNAL);
                            FileOutputStream out = new FileOutputStream(fileName);
                            resource.compress(format, quality, out);
                            //=====================================galleryAddPic(fileName.getAbsolutePath());
                            out.flush();
                            out.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        super.onResourceReady(resource, glideAnimation);
                    }

                });
    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Toast.makeText(this,"MEDIA NOT MOUNTED",Toast.LENGTH_SHORT).show();
        return false;
    }
    private File createImageFile(int type) throws IOException {

        if(!isExternalStorageWritable())
            return null;
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "HALO_" + phoneno;
        File storageDir;
        if(type==SAVE_INTERNAL)
        {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);//writes internal to internal/android/data/app/file/pitures
        }
        else if(type==SAVE_EXTERNAL)
        {
            storageDir=new File(Environment.getExternalStorageDirectory(),"HALO/Profile Picture");//writes to internal/Halo/profile pic
            storageDir.mkdirs();
        }
        else
            return null;
        File imagefile=new File(storageDir,imageFileName+".jpeg");
        return imagefile;
    }
}
