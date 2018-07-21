package com.aayaffe.sailingracecoursemanager.geographical;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aayaffe.sailingracecoursemanager.calclayer.BuoyType;
import com.aayaffe.sailingracecoursemanager.db.FireStoreEvents;
import com.aayaffe.sailingracecoursemanager.events.Event;
import com.aayaffe.sailingracecoursemanager.calclayer.DBObject;
import com.aayaffe.sailingracecoursemanager.db.IDBManager;
import com.aayaffe.sailingracecoursemanager.general.GeneralUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

/**
 * Avi Marine Innovations - www.avimarine.in
 *
 * Created by Amit Y. on 31/12/2016.
 */
public class GPSService extends Service {

    private static final String TAG = "GPSService";
    private IDBManager commManager;
    private FireStoreEvents eventsDb;
    private IGeo geo;
    private DBObject myBoat;
    private Event event;
    private String uid;
    //Time between gps updates in milliseconds
    private long updateInterval = 1000;
    private Handler handler = new Handler();
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (myBoat==null) {
//                myBoat = commManager.getBoatByUserUid(uid);
                eventsDb.getBoatByUserUid(commManager.getCurrentEvent().getUuid(), uid, new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                myBoat = document.toObject(DBObject.class);
                                break;
                            }
                            if (myBoat!=null){
                                myBoat.setLoc(geo.getLoc());
                                myBoat.lastUpdate = new Date().getTime();
                                commManager.updateBoatLocation(event, myBoat, myBoat.getAviLocation());
                                eventsDb.updateBoatLocation(event, myBoat, myBoat.getAviLocation());
                            }
                        } else {
                            Log.e(TAG, "Cannot get boat from firestore");
                        }
                    }
                });
            }
//            if (myBoat!=null){
//                myBoat.setLoc(geo.getLoc());
//                myBoat.lastUpdate = new Date().getTime();
//                commManager.updateBoatLocation(event, myBoat, myBoat.getAviLocation());
//                eventsDb.updateBoatLocation(event, myBoat, myBoat.getAviLocation());
//            }
            handler.postDelayed(runnable, updateInterval);
        }
    };



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public GPSService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GPSService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public void update(long interval, DBObject myBoat, Event event, IDBManager commManager, FireStoreEvents eventsDb, IGeo geo, String uid) {
        if (interval<0 || GeneralUtils.isNull(event,commManager,geo))
            return;
        this.commManager = commManager;
        this.eventsDb = eventsDb;
        this.geo = geo;
        this.myBoat = myBoat;
        this.event= event;
        this.updateInterval = interval;
        this.uid = uid;
        Log.d(TAG, "Started GPSService");
        runnable.run();
    }

    public void stop(){
        handler.removeCallbacks(runnable);
        Log.d(TAG, "Stoped GPSService");
    }
}
