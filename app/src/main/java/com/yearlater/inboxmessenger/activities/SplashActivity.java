package com.yearlater.inboxmessenger.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.yearlater.inboxmessenger.R;
import com.yearlater.inboxmessenger.activities.authentication.AuthenticationActivity;
import com.yearlater.inboxmessenger.activities.main.MainActivity;
import com.yearlater.inboxmessenger.activities.setup.SetupUserActivity;
import com.yearlater.inboxmessenger.utils.DetachableClickListener;
import com.yearlater.inboxmessenger.utils.network.FireManager;
import com.yearlater.inboxmessenger.utils.PermissionsUtil;
import com.yearlater.inboxmessenger.utils.SharedPreferencesManager;
import com.yearlater.inboxmessenger.activities.main.MainActivity;

//this is the First Activity that launched when user starts the App
public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 451;
    private ImageView inkDrive;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(R.color.status_header_color));
        }
        Log.e("Android", "version"+Build.VERSION.SDK_INT);
//
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent is used to switch from one activity to another.

                if (!SharedPreferencesManager.hasAgreedToPrivacyPolicy()) {
                    startPrivacyPolicyActivity();
                    //check if user isLoggedIn
                } else if (!FireManager.isLoggedIn()) {
                    startLoginActivity();

                    //request permissions if there are no permissions granted
                } else if (FireManager.isLoggedIn() && !PermissionsUtil.hasPermissions13(SplashActivity.this) && Build.VERSION.SDK_INT == 33) {
                    Log.e("android","13");
                    requestPermissions13();

                }else if(FireManager.isLoggedIn() && !PermissionsUtil.hasPermissions(SplashActivity.this) && Build.VERSION.SDK_INT < 33){
                    Log.e("android","12");
                    requestPermissions();

                }
                else {
                    startNextActivity();

                }
                // the current activity will get finished.

            }
        }, 500);
    }

    private void requestPermissions() {

        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions, PERMISSION_REQUEST_CODE);
    }
    private void requestPermissions13() {
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions13, PERMISSION_REQUEST_CODE);
    }


    private void startLoginActivity() {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startPrivacyPolicyActivity() {
        Intent intent = new Intent(this, AgreePrivacyPolicyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startNextActivity() {
        if (!SharedPreferencesManager.hasAgreedToPrivacyPolicy()) {
            startPrivacyPolicyActivity();
        } else if (SharedPreferencesManager.isUserInfoSaved()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, SetupUserActivity.class);
            startActivity(intent);
            finish();
        }

    }


    private void showAlertDialog() {

        DetachableClickListener positiveClickListener = DetachableClickListener.wrap(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions();

            }
        });

        DetachableClickListener negativeClickListener = DetachableClickListener.wrap(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });


        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle(R.string.missing_permissions)
                .setMessage(R.string.you_have_to_grant_permissions)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setNegativeButton(R.string.no_close_the_app, negativeClickListener)
                .create();

        //avoid memory leaks
        positiveClickListener.clearOnDetach(builder);
        negativeClickListener.clearOnDetach(builder);
        builder.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            Log.e("requesting premission","ok");
            if (!FireManager.isLoggedIn()) {
                startLoginActivity();
                Log.e("requesting premission","granted");
            }
            else
                startNextActivity();
        } else
            showAlertDialog();
    }

}



