package com.aayaffe.sailingracecoursemanager.db;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aayaffe.sailingracecoursemanager.BuildConfig;
import com.aayaffe.sailingracecoursemanager.calclayer.DBObject;
import com.aayaffe.sailingracecoursemanager.events.Event;
import com.aayaffe.sailingracecoursemanager.general.GeneralUtils;
import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * This file is part of an
 * Avi Marine Innovations project: SailingRaceCourseManager
 * first created by aayaffe on 30/06/2018.
 */
public class FireStoreEvents {
    private static final String TAG = "FireStoreEvents";
    private final FirebaseFirestore mFirestore;
    private final FirebaseStorage storage;
    private static Event currentEvent;

    public FireStoreEvents() {
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


    public void writeEvent(Event neu) {
        DocumentReference dr = getEventsCollection().document(neu.getUuid());
        dr.set(neu);
    }

    public int writeBoatObject(DBObject o) {
        if (o == null || o.userUid == null || o.userUid.isEmpty() || currentEvent == null)
            return -1;
        if (/*isEventExist(currentEvent)*/true /*TODO:Check for event existence*/) {
            DocumentReference dr = getEventBoatsCollection(currentEvent.getUuid()).document(o.userUid);
            dr.set(o);
            Log.v(TAG, "writeBoatObject has written boat:" + o.getName());
            return 0;
        }
        return -1;
    }

    public void getBoat(String eventUuid, String uuid, OnCompleteListener<DocumentSnapshot> listener) {
        if (currentEvent == null || uuid == null || uuid.isEmpty())
            return;
        getEventBoatsCollection(currentEvent.getUuid()).document(uuid).get().addOnCompleteListener(listener);
    }

    public void getBoatByUserUid(String eventUuid, String uid, OnCompleteListener<QuerySnapshot> listener){
        if (GeneralUtils.isStringNullOrEmpty(eventUuid,uid))
            return;
        getEventBoatsCollection(eventUuid).whereEqualTo("userUid",uid).get().addOnCompleteListener(listener);
    }

    public void updateBoatLocation(Event e, DBObject boat, AviLocation loc) {
        if (loc == null)
            return;
//        Map<String, Object> updateMap = new HashMap();
//        updateMap.put("aviLocation", loc);
//
//        getEventBoatsCollection(e.getUuid()).document(boat.userUid).update(updateMap);
        DocumentReference dr = getEventBoatsCollection(e.getUuid()).document(boat.userUid);

        Map<String, Object> updateMap = new HashMap();
        updateMap.put("aviLocation.sog", loc.sog);
        updateMap.put("aviLocation.cog", loc.cog);
        updateMap.put("aviLocation.lat", loc.lat);
        updateMap.put("aviLocation.lon", loc.lon);
        updateMap.put("aviLocation.lastUpdate", loc.lastUpdate);

        dr.update(updateMap);
    }

    private CollectionReference getEventsCollection(){
        return mFirestore.collection("events");
    }

    @Nullable
    private DocumentReference getEventDocument(@NonNull String uuid){
        return getEventsCollection().document(uuid);
    }

    @Nullable
    private CollectionReference getEventBoatsCollection(@NonNull String uuid){
        return getEventDocument(uuid).collection("boats");
    }

    public void setCurrentEvent(Event e) {
        currentEvent = e;
    }

}
