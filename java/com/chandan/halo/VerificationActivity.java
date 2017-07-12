package com.chandan.halo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chandan.halo.Parser.JParserAdv;
import com.msg91.sendotp.library.SendOtpVerification;
import com.msg91.sendotp.library.Verification;
import com.msg91.sendotp.library.VerificationListener;

import org.json.JSONObject;

import java.util.HashMap;

import static java.security.AccessController.getContext;


public class VerificationActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, VerificationListener {

    private static final String TAG = Verification.class.getSimpleName();
    private Verification mVerification;
    TextView resend_timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        SessionManager manager=new SessionManager(getBaseContext());
        int state=manager.getState();

        switch (state) {
     /**       case -1:
            {
                Intent intent = getIntent();
                if (intent != null) {
                    String phoneNumber = intent.getStringExtra(PhoneActivity.INTENT_PHONENUMBER);
                    String countryCode = intent.getStringExtra(PhoneActivity.INTENT_COUNTRY_CODE);
                    String number="+"+countryCode+phoneNumber;
                    manager.setKeyPhone(number);
                }
                TextView phoneText = (TextView) findViewById(R.id.numberText);
                phoneText.setText(manager.getKeyPhone());
                manager.setState(1);
                showCompleted();
                break;
            }
        */    case 0: {
                resend_timer = (TextView) findViewById(R.id.resend_timer);
                resend_timer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ResendCode();
                    }
                });
                startTimer();
                enableInputField(true);
                initiateVerification();
                break;
            }
            case 1:
            {
                register();
                break;
            }
            case 2: {
                hideProgressBarAndShowMessage(R.string.failed);
                manager.setState(2);
                enableInputField(true);
                showChangeNumber();
                break;
            }
            case 4: {
                manager.setState(4);
                hideProgressBarAndShowMessage(R.string.NotRegistered);
                TextView phoneText = (TextView) findViewById(R.id.numberText);
                phoneText.setText(manager.getKeyPhone());
                onRegisterFailed();
                break;
            }
        }
    }

    void createVerification(String phoneNumber, boolean skipPermissionCheck, String countryCode) {
        if (!skipPermissionCheck && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);
            hideProgressBar();
        } else {
            mVerification = SendOtpVerification.createSmsVerification(this, phoneNumber, this, countryCode, true);
            mVerification.initiate();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "This application needs permission to read your SMS to automatically verify your phone",
                        Toast.LENGTH_LONG)
                        .show();
            }
            enableInputField(true);
        }
        initiateVerificationAndSuppressPermissionCheck();
    }

    void initiateVerification() {
        initiateVerification(false);
    }

    void initiateVerificationAndSuppressPermissionCheck() {
        initiateVerification(true);
    }

    void initiateVerification(boolean skipPermissionCheck) {
        Intent intent = getIntent();
        if (intent != null) {
            String phoneNumber = intent.getStringExtra(PhoneActivity.INTENT_PHONENUMBER);
            String countryCode = intent.getStringExtra(PhoneActivity.INTENT_COUNTRY_CODE);
            TextView phoneText = (TextView) findViewById(R.id.numberText);
            phoneText.setText("+" + countryCode + phoneNumber);
            createVerification(phoneNumber, skipPermissionCheck, countryCode);
        }
    }

    public void ResendCode() {
        startTimer();
        initiateVerificationAndSuppressPermissionCheck();
    }

    public void onSubmitClicked(View view) {
        String code = ((EditText) findViewById(R.id.inputCode)).getText().toString();
        if (!code.isEmpty()) {
            if (mVerification != null) {
                mVerification.verify(code);
                showProgress();
                TextView messageText = (TextView) findViewById(R.id.verifytextView);
                messageText.setText("Verification in progress");
                enableInputField(false);
            }
        }
    }

    void enableInputField(boolean enable) {
        View container = findViewById(R.id.inputContainer);
        if (enable) {
            container.setVisibility(View.VISIBLE);
            EditText input = (EditText) findViewById(R.id.inputCode);
            input.requestFocus();
        } else {
            container.setVisibility(View.GONE);
        }
        TextView resend_timer = (TextView) findViewById(R.id.resend_timer);
        resend_timer.setClickable(false);
    }

    void hideProgressBarAndShowMessage(int message) {
        hideProgressBar();
        TextView messageText = (TextView) findViewById(R.id.verifytextView);
        messageText.setText(message);
    }

    void hideProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.INVISIBLE);
        TextView progressText = (TextView) findViewById(R.id.progressText);
        progressText.setVisibility(View.INVISIBLE);
    }

    void showProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.VISIBLE);
    }

    void showCompleted() {
        ImageView checkMark = (ImageView) findViewById(R.id.checkmarkImage);
        Button nextAfterVerify=(Button)findViewById(R.id.nextAfterVerify);
        checkMark.setVisibility(View.VISIBLE);
        nextAfterVerify.setVisibility(View.VISIBLE);
        nextAfterVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    @Override
    public void onInitiated(String response) {
        Log.d(TAG, "Initialized!" + response);
    }

    @Override
    public void onInitiationFailed(Exception exception) {
        Log.e(TAG, "Verification initialization failed: " + exception.getMessage());
        hideProgressBarAndShowMessage(R.string.failed);

    }

    public void showChangeNumber(){
        Button button=(Button)findViewById(R.id.changeNumber);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager manager=new SessionManager(getBaseContext());
                manager.setState(0);
                startActivity(new Intent(VerificationActivity.this,PhoneActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onVerified(String response) {
        Log.d(TAG, "Verified!\n" + response);
        enableInputField(false);/////////////
        Button button=(Button)findViewById(R.id.changeNumber);
        button.setVisibility(View.GONE);
        hideProgressBarAndShowMessage(R.string.verified);
        SessionManager manager=new SessionManager(getBaseContext());
        Intent intent = getIntent();
        if (intent != null) {
            String phoneNumber = intent.getStringExtra(PhoneActivity.INTENT_PHONENUMBER);
            String countryCode = intent.getStringExtra(PhoneActivity.INTENT_COUNTRY_CODE);
            String number="+"+countryCode+phoneNumber;
            manager.setKeyPhone(number);
        }
            manager.setState(1);
            showCompleted();
    }

    @Override
    public void onVerificationFailed(Exception exception) {
        Log.e(TAG, "Verification failed: " + exception.getMessage());
        hideProgressBarAndShowMessage(R.string.failed);
        SessionManager manager=new SessionManager(getBaseContext());
        manager.setState(2);
        enableInputField(true);
        showChangeNumber();
    }

    private void startTimer() {
        resend_timer.setClickable(false);
        resend_timer.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.sendotp_grey));
        new CountDownTimer(300000, 1000) {
            int secondsLeft = 0;
            String sec, min;

            public void onTick(long ms) {
                if (Math.round((float) ms / 1000.0f) != secondsLeft) {
                    secondsLeft = Math.round((float) ms / 1000.0f);
                    min = ("0" + (int) (secondsLeft / 60));
                    min = min.substring(min.length() - 2);
                    sec = "0" + (int) (secondsLeft % 60);
                    sec = sec.substring(sec.length() - 2);
                    resend_timer.setText("Resend via call ( " + min + ":" + sec + " )");
                }
            }

            public void onFinish() {
                resend_timer.setClickable(true);
                resend_timer.setText("Resend via call");
                resend_timer.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.send_otp_blue));
            }
        }.start();
    }

    public void register(){
        TextView messageText = (TextView) findViewById(R.id.verifytextView);
        messageText.setText(R.string.PlzWaitReg);

        SessionManager manager=new SessionManager(getBaseContext());
        String number=manager.getKeyPhone();
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        try
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            AsyncTask asyncTask=new RegisterNumber().execute(number,androidId);
        }catch(Exception ex){
            Toast.makeText(this,"Error Occured. Try Later.", Toast.LENGTH_SHORT).show();
        }
    }


class RegisterNumber extends AsyncTask<String,String,JSONObject> //params,progress,result
    {
        @Override
        protected void onPreExecute() {
            ImageView checkMark = (ImageView) findViewById(R.id.checkmarkImage);
            Button nextAfterVerify=(Button)findViewById(R.id.nextAfterVerify);
            checkMark.setVisibility(View.GONE);
            nextAfterVerify.setVisibility(View.GONE);
            showProgress();
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... params) {

            JParserAdv jParser=new JParserAdv();
            HashMap<String,String> hashMap=new HashMap<>();
            hashMap.put("phoneno",params[0]);
            hashMap.put("deviceId",params[1]);


            JSONObject jsonObject=jParser.makeHttpRequest(CONSTANT.registerUrl,"GET",hashMap);

            if(jsonObject != null){
                return jsonObject;
            }
            return null;
        }








        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            SessionManager manager=new SessionManager(getBaseContext());
            try {

                JSONObject jsonObject1 = jsonObject.getJSONObject("Result");
               String  st = jsonObject1.getString("status");
                if (st.equals("0")) {
                    manager.setState(3);
                    hideProgressBarAndShowMessage(R.string.Registered);
                    Thread.sleep(2000);
                    startActivity(new Intent(VerificationActivity.this,MyPermission.class));
                    finish();
                }
                else
                {

                    manager.setState(4);
                    hideProgressBarAndShowMessage(R.string.NotRegistered);
                    onRegisterFailed();
                }
            }catch (Exception ex) {

                hideProgressBarAndShowMessage(R.string.NotRegistered);
                manager.setState(4);
                onRegisterFailed();
            }
            super.onPostExecute(jsonObject);

        }
    }
    public void  onRegisterFailed()
    {
        Button button=(Button)findViewById(R.id.RetryRegisration);
        button.setText("Retry Registration");
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button=(Button)findViewById(R.id.RetryRegisration);
                button.setText("Please Wait");
                register();
            }
        });
    }
}
