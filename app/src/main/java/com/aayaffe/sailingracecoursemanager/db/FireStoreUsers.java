package com.aayaffe.sailingracecoursemanager.db;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aayaffe.sailingracecoursemanager.BuildConfig;
import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.Users.User;
import com.aayaffe.sailingracecoursemanager.calclayer.DBObject;
import com.aayaffe.sailingracecoursemanager.events.Event;
import com.aayaffe.sailingracecoursemanager.general.GeneralUtils;
import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This file is part of an
 * Avi Marine Innovations project: SailingRaceCourseManager
 * first created by aayaffe on 30/06/2018.
 */
public class FireStoreUsers {
    private static final String TAG = "FireStoreUsers";
    private final FirebaseFirestore mFirestore;
    private final FirebaseStorage storage;
    private static Event currentEvent;

    public FireStoreUsers() {
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);
        storage = FirebaseStorage.getInstance();
        if (BuildConfig.DEBUG) {
            FirebaseFirestore.setLoggingEnabled(true);
        } else {
            FirebaseFirestore.setLoggingEnabled(false);
        }
    }

    public void addUser(User u) {
        if (u == null)
            return;
        DocumentReference dr = getUsersCollection().document(u.Uid);
        dr.set(u);
        Log.d(TAG,"Added new user: " + u.DisplayName);
    }
    public void getUser(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        if (uid == null || mFirestore == null)
            return;
        try {
            getUsersCollection().document(uid).get().addOnCompleteListener(listener);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user", e);
        }
    }

    public void getUser(UUID uid, OnCompleteListener<DocumentSnapshot> listener) {
        if (uid ==null)
            return;
        getUser(uid.toString(), listener);
    }

//    public boolean isAdmin(User u) {
//        return u != null && ds.child(c.getString(R.string.db_admins)).hasChild(u.Uid);
//    }

    private CollectionReference getUsersCollection(){
        return mFirestore.collection("users");
    }

}
