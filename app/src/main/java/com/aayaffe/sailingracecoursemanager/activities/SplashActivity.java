package com.aayaffe.sailingracecoursemanager.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.Users.User;
import com.aayaffe.sailingracecoursemanager.db.CommManagerEventListener;
import com.aayaffe.sailingracecoursemanager.db.FireStoreUsers;
import com.aayaffe.sailingracecoursemanager.db.FirebaseBackgroundService;
import com.aayaffe.sailingracecoursemanager.db.FirebaseDB;
import com.aayaffe.sailingracecoursemanager.db.IDBManager;
import com.aayaffe.sailingracecoursemanager.dialogs.DialogUtils;
import com.aayaffe.sailingracecoursemanager.general.Versioning;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private Versioning versioning;
    private CommManagerEventListener onConnectEventListener;
    private IDBManager commManager;
    private FireStoreUsers usersDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersDb = new FireStoreUsers();
        versioning = new Versioning(this);
        Intent serviceIntent = new Intent(this,FirebaseBackgroundService.class);
        startService(serviceIntent);
        Log.d(TAG,"After starting service.");
        onConnectEventListener = new CommManagerEventListener() {
            @Override
            public void onConnect(Date time) {
                Log.d(TAG,"onConnect");
                if (versioning.getInstalledVersion()<versioning.getSupportedVersion())
                {
                    alertOnUnsupportedVersion();
                } else {
                    ((FirebaseDB)commManager).setUser();
                    Intent intent = new Intent(SplashActivity.this, ChooseEventActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onDisconnect(Date time) {
                Log.d(TAG,"commManager disconnected");
            }
        };
        commManager = FirebaseDB.getInstance(this);
        commManager.setCommManagerEventListener(onConnectEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(commManager!=null)
            commManager.removeCommManagerEventListener(onConnectEventListener);
        Intent serviceIntent = new Intent(this,FirebaseBackgroundService.class);
        stopService(serviceIntent);
    }

    private void alertOnUnsupportedVersion() {
        Log.d(TAG, "Version not supported, starting dialog");
        Dialog d = DialogUtils.createDialog(SplashActivity.this, R.string.version_not_supported_dialog_title,
                R.string.version_not_supported_dialog_message, (dialog, which) -> {
                    final String appPackageName = SplashActivity.this.getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException e) {
                        Log.e(TAG,"Error FirebaseDB OnConnect",e);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }, (dialog, which) -> finish());
        d.show();
    }
}