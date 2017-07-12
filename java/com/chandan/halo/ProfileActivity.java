package com.chandan.halo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.chandan.halo.CONSTANT.CAMERA_PERMISSION_CODE;
import static com.chandan.halo.CONSTANT.SAVE_EXTERNAL;
import static com.chandan.halo.CONSTANT.SAVE_INTERNAL;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    Dialog dialog;
    Drawable drawable=null;
    String mCurrentPhotoPath;
    ImageView imageView;


    Uri cameraImgUri;
    String localImgpath;
    static final int IMAGE_LOCAL=0,IMAGE_SERVER=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        imageView =(ImageView)findViewById(R.id.prof_img);

        TextView LatLongTextView=(TextView)findViewById(R.id.prof_Location);
        TextView LastUpdateTimeTextView=(TextView)findViewById(R.id.prof_lastsync);
        SessionManager manager=new SessionManager(getApplicationContext());
        final String lat=manager.getLatitute();
        final String lng=manager.getLongitute();
        final String time=manager.getlastLocUpdate();
        LatLongTextView.setText(lat+" , "+lng);
        LastUpdateTimeTextView.setText(time);

         localImgpath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/HALO/Profile Picture/HALO_"
                +manager.getKeyPhone()+".jpeg";
        drawable=Drawable.createFromPath(localImgpath);
       // setProfileImg(localImgpath,IMAGE_LOCAL);
        setProfileImg(CONSTANT.profileImgUrl+"HALO_"+manager.getKeyPhone()+".jpeg");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.profil_fab);
        //fab.setBackgroundResource(;
        fab.setImageResource(R.drawable.pencil);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                fabProflieButton(view);
            }
        });
    }
    public void fabProflieButton(View view)
    {

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pic);
        dialog.setTitle("Select Picture");
        dialog.show();
        LinearLayout capture_image=(LinearLayout)dialog.findViewById(R.id.capture_image);
        capture_image.setOnClickListener(this);
        LinearLayout gallery_image=(LinearLayout)dialog.findViewById(R.id.gallery_image);
        gallery_image.setOnClickListener(this);
        /**LinearLayout remove_image=(LinearLayout)dialog.findViewById(R.id.remove_image);
        remove_image.setOnClickListener(this);*/

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==CAMERA_PERMISSION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the related task you need to do.
                dispatchTakePictureIntent();
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
                if(dialog!=null)
                    dialog.cancel();
            }
            return;
        }
    }

    protected void callPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         final AlertDialog.Builder alertDialog=new AlertDialog.Builder(ProfileActivity.this)
                                .setMessage("Permission required to Access Camera")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(ProfileActivity.this,
                                                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                                        return;

                                    }
                                })
                                 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                alertDialog.create();
                        alertDialog.show();
                    }
                });

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

                Toast.makeText(this, "Go to settings> Apps and enable permissions", Toast.LENGTH_LONG)
                        .show();
                // MY_PERMISSIONS_REQUEST_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.capture_image:
            {
                dialog.cancel();
                int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
                if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
                   callPermission();
                }
                else
                {
                    dispatchTakePictureIntent();
                }
                break;
            }
            case R.id.gallery_image:
            {
                dialog.cancel();
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPhoto.setType("image/*");
                startActivityForResult(pickPhoto , 301);
                break;
            }
            case R.id.remove_image:
            {
                dialog.cancel();
                break;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int req = requestCode;
        switch (requestCode) {
            case 300: {
                if (resultCode == RESULT_OK) {
                    //------------------------================================galleryAddPic(mCurrentPhotoPath);
//                    Bitmap photo = (Bitmap) data.getExtras().get("data");
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    new uploadImage().execute(mCurrentPhotoPath);

                }
                break;
            }
            case 301: {
                if (resultCode == RESULT_OK) {
                    cameraImgUri = data.getData();      //content://media/external/images/media/50223
                    //File f = new File(String.valueOf(cameraImgUri));
                    String realPath=getRealPathFromURI(ProfileActivity.this,cameraImgUri);///storage/B261-2985/DCIM/Camera/IMG_20170320_203640.jpg
                    new uploadImage().execute(realPath);

                }
                break;
            }
        }
    }
    private  void setProfileImg(String selectedImage)
    {
        if(selectedImage==null)
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

       /**
        int w = blurimg.getWidth();
        int h = blurimg.getHeight();
        Glide.with(this)
                .load(selectedImage)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .bitmapTransform(new BlurTransformation(this))
                .placeholder(drawable)
                .into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                blurimg.setBackground(resource);
            }
        });
        */
    }
    private File createImageFile(int type) throws IOException {

        if(!isExternalStorageWritable())
            return null;
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        SessionManager manager=new SessionManager(getApplicationContext());
        String imageFileName = "HALO_" + manager.getKeyPhone();
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

//        File imagefile = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpeg",         /* suffix */
//                storageDir      /* directory */
//        );   /**Creates temp file with random number appended at end*/


        return imagefile;
    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Toast.makeText(this,"MEDIA NOT MOUNTED",Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    private void galleryAddPic(String imageAbsolutePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageAbsolutePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile =createImageFile(SAVE_INTERNAL);
                // Save a file: path for use with ACTION_VIEW intents
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                 }
            // Continue only if the File was successfully created
            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.chandan.halo.fileprovider",
//                        photoFile);
                cameraImgUri=Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImgUri );
                startActivityForResult(takePictureIntent, 300);
            }
        }
    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
           // return openFile( new FileInputStream( f ),charset );
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    class uploadImage extends AsyncTask<String,String,JSONObject>
    {
        private ProgressBar mProgress;
        private int mProgressStatus;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressStatus = 0;
            mProgress=(ProgressBar)findViewById(R.id.pBar_profileImg);
            mProgress.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        File sourceFile=null;
        String sourceFilePath=null;

        @Override
        protected JSONObject doInBackground(String... params) {
            String filepath=params[0];
            return uploadImageData(filepath);
        }

        public JSONObject uploadImageData(String sourceFileName)//it is path
        {
            sourceFilePath=sourceFileName;
            JSONObject jsonObject=null;
            int serverResponseCode;
            String serverResponseMessage=null;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

             sourceFile=new File(sourceFileName);
            if (!sourceFile.isFile()) {
//                display not a file;
                Toast.makeText(getBaseContext(),"Not a Image File",Toast.LENGTH_SHORT).show();
                return null;
            }
            else
            {
                try {

                    mProgressStatus=15;
                    mProgress.setProgress(mProgressStatus);
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    // open a URL connection to the Servlet
                    URL url = new URL(CONSTANT.uploadImgUrl);
                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("fileToUpload", sourceFileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    int dot = sourceFileName.indexOf('.');
                    SessionManager manager= new SessionManager(getBaseContext());

                    String uniq_file_name="HALO_"+manager.getKeyPhone()+sourceFileName.substring(dot);
                    dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\""+ uniq_file_name + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        mProgressStatus+=3;
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    Log.d("ProfileActivity ", "bytes readed");
                    // Responses from the server (code and message)
                     serverResponseCode = conn.getResponseCode();
                     serverResponseMessage = conn.getResponseMessage();
                     //jsonObject=new JSONObject(serverResponseMessage.toString());

                    Log.d("ProfileActivity", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if(serverResponseCode == 200){

//                        runOnUiThread(new Runnable() {
//                            public void run() {
//
//                                String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//                                        +" http://www.androidexample.com/media/uploads/"
//                                        +uploadFileName;
//
//                                messageText.setText(msg);
//                                Toast.makeText(UploadToServer.this, "File Upload Complete.",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                        String line,result="";
                        while ((line = reader.readLine()) != null) {
                            result+=line;
                        }
                        Log.d("ProfileActivity Parser", "result: " + result.toString());
                        jsonObject= new JSONObject(result.toString());
                    }

                    mProgressStatus = 75;

                    mProgress.setProgress(mProgressStatus);
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {
                    dialog.dismiss();
                    ex.printStackTrace();

                } catch (Exception e) {

                    e.printStackTrace();
                }
                return jsonObject;

            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
           // Log.d("Profile Status message:",jsonObject.toString());
            if(jsonObject!=null)
            {
                try
                {
                    JSONObject jsonObject1=jsonObject.getJSONObject("Result");
                    String status=jsonObject1.getString("status");
                    String msg=jsonObject1.getString("message");
                    Log.d("Profile Status message:",msg);
                    if(status=="0")
                    {
                        mProgress.setProgress(80);
                        SessionManager manager=new SessionManager(getBaseContext());
                        setProfileImg(CONSTANT.profileImgUrl+"HALO_"+manager.getKeyPhone()+".jpeg");
                        mProgress.setProgress(100);
                        Toast.makeText(getBaseContext(),"Upload Success.",Toast.LENGTH_LONG).show();
                    }
                    else if(status=="1")
                    {
                        Toast.makeText(getBaseContext(),"Fail to Upload.File Size too large.",Toast.LENGTH_LONG).show();
                    }
                    else if(status=="2")
                    {
                        Toast.makeText(getBaseContext(),"Unsupported file type.",Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(getBaseContext(),"Some Error Occured.",Toast.LENGTH_SHORT).show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            mProgress.setVisibility(View.GONE);
            super.onPostExecute(jsonObject);
        }
        public void copyFile(File src,File dst)throws FileNotFoundException
        {
            try {
                InputStream in=new FileInputStream(src);
                OutputStream out =new FileOutputStream(dst);
                byte buffer[]=new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
