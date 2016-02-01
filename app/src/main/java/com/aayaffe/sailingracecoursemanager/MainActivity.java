package com.aayaffe.sailingracecoursemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.aayaffe.sailingracecoursemanager.communication.Firebase;
import com.aayaffe.sailingracecoursemanager.communication.ICommManager;
import com.aayaffe.sailingracecoursemanager.communication.AviObject;
import com.aayaffe.sailingracecoursemanager.communication.ObjectTypes;
import com.aayaffe.sailingracecoursemanager.general.GeneralUtils;
import com.aayaffe.sailingracecoursemanager.general.Notification;
import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;
import com.aayaffe.sailingracecoursemanager.geographical.GeoUtils;
import com.aayaffe.sailingracecoursemanager.map.MapLayer;
import com.aayaffe.sailingracecoursemanager.map.MapUtils;
import com.aayaffe.sailingracecoursemanager.map.OpenSeaMap;
import com.aayaffe.sailingracecoursemanager.map.SamplesBaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

//import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.rendertheme.XmlRenderTheme;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends SamplesBaseActivity implements PopupMenu.OnMenuItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    // name of the map file in the external storage
    private static MapLayer map = new OpenSeaMap();
    private static ICommManager qb;
    private Handler handler = new Handler();
    private Marker myLoc = new Marker(null,null,0,0);
    private static Map<String,Marker> workerLocs = new HashMap<String, Marker>();
    private static Map<String,TextMarker> workerTexts = new HashMap<String, TextMarker>();
    private Polyline rbLine = new Polyline(null, AndroidGraphicFactory.INSTANCE);
    private int ID = 1;
    private static String TAG = "MainActivity";
    private ConfigChange unc = new ConfigChange();
    private ImageView windArrow;
    public static int REFRESH_RATE = 10000;
    private Notification notification = new Notification();
    GoogleApiClient mGoogleApiClient;
    SharedPreferences SP;
    AviLocation mLastLocation;
    LocationRequest mLocationRequest;
    Marks marks = new Marks();


    private Runnable runnable = new Runnable()
    {

        public void run()
        {
            AviObject o = new AviObject();
            o.name = SP.getString("username", "Manager1");
            o.location = mLastLocation;
            o.color = "Blue"; //TODO Set properly
            o.type = ObjectTypes.WorkerBoat;//TODO Set properly
            //TODO Think about last update time (Exists in Location also)
            qb.writeBoatObject(o);
            redrawLayers();
            Log.d(TAG, "Delaying runnable for " + REFRESH_RATE + " ms");
            handler.postDelayed(runnable, REFRESH_RATE);
        }
    };

    @Override
    public boolean onLongPress(LatLong tapLatLong, Point thisXY, Point tapXY) {
        Log.d(TAG,"LongPress in " + tapLatLong.toString());
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }


    @Override
    protected String getMapFileName() {
        return "germany.map";
    }


    @Override
    protected XmlRenderTheme getRenderTheme() {
        return null;
    }

    @Override
    protected void createLayers() {
        return;
    }


    @Override
    protected void createMapViews() {
        super.createMapViews();
        // we need to set a fixed size tile as the raster tiles come at a fixed size and not being blurry
        this.mapView.getModel().displayModel.setFixedTileSize(256);
    }
    public void fabOnClick(View v) {
        Log.d(TAG, "FAB Setting Clicked");
        Intent i = new Intent(getApplicationContext(), AppPreferences.class);
        startActivity(i);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(getLayoutId());
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SP.registerOnSharedPreferenceChangeListener(unc);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.configFAB);
        windArrow = (ImageView) findViewById(R.id.windArrow);

        buildGoogleApiClient();
        Location center = new Location("Manual");
        center.setLatitude(32.9);
        center.setLongitude(34.9);

        map.Init(this, this, getMapView(), SP, center,1/*TODO: Not used*/);
        //qb = new QuickBlox(this,getResources());
        qb = new Firebase(this);
        qb.login(SP.getString("username", "Manager1"), "Aa123456z", "1");
        runnable.run();
        if ((myLoc!=null)&&(myLoc.getLatLong()!=null)){
            map.setCenter(myLoc.getLatLong());
        }
        else {
            map.setCenter(32.9, 34.9);//TODO: Find better solution
        }
        map.setZoomLevel(8);
        notification.InitNotification(this);
        createControls();
    }
    public void PopUpMenu(Context c, Activity a)
    {
        PopupMenu popupMenu = new PopupMenu(c, findViewById(R.id.addFAB));
        popupMenu.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener)a);
        popupMenu.inflate(R.menu.map_popup_menu);
        popupMenu.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_buoy:
                Toast.makeText(this, map.getLastTapLatLong().toString(), Toast.LENGTH_SHORT).show();
                AviObject o = new AviObject();
                o.type = ObjectTypes.Buoy;
                o.name = "1";
                o.color = "RED";
                o.location = GeoUtils.toAviLocation(map.getLastTapLatLong());
                marks.marks.add(o);
                qb.writeBuoyObject(o);
                return true;
        }
        return false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        windArrow = (ImageView) findViewById(R.id.windArrow);
        Float rotation = Float.parseFloat(SP.getString("windDir", "90"));
        Log.d(TAG, "New wind arrow rotation is " + rotation);
        windArrow.setRotation(rotation + 90);
        Log.d(TAG, "New wind arrow icon rotation is " + windArrow.getRotation());
        redrawLayers();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.destroy();
    }
     public static void login(String id){
        if (qb!=null) {
            qb.login(id, "Aa123456z", "1");
            Log.d(TAG,"login to " + id);
        }
    }
    @Override
    public void redrawLayers()
    {
        super.redrawLayers();
        GraphicFactory gf = AndroidGraphicFactory.INSTANCE;
        Location myLocation = GeoUtils.toLocation(mLastLocation);
        List<AviObject> l = qb.getAllBoats();
        for (AviObject o: l) {
            if ((o != null)&&(!o.name.equals(SP.getString("username","Manager1")))) {
                Marker m;
                if (workerLocs.containsKey(o.name))
                {
                    m = workerLocs.get(o.name);
                    m.setLatLong(GeoUtils.toLatLong(o.location));
                }
                else{
                    Bitmap b = getBoatBitmap(o);
                    b = MapUtils.addBoatNumber(b,Character.getNumericValue(o.name.charAt(o.name.length() - 1)),getResources());

                    m = new Marker(GeoUtils.toLatLong(o.location),b,0,0);
                }
                workerLocs.put(o.name, m);
                try {

                    map.removeMark(m);
                    map.addMark(m);
                }catch (IllegalStateException e)
                {
                    Log.e(TAG,"Error adding layers",e);
                }

                if (myLocation!=null){
                    TextMarker tm = getTextMarker(gf, myLocation, o);
                    workerTexts.put(o.name, tm);
                    try {
                        map.removeMark(tm);
                        map.addMark(tm);
                    }catch (IllegalStateException e) {
                        Log.e(TAG, "Error adding layers", e);
                    }
                }
            }
            if (myLocation!=null) {
                if (SP.getString("username","Manager1").contains("Manager")){
                    myLoc.setBitmap(AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.managergold)));
                }else
                    myLoc.setBitmap(AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatgold)));
                myLoc.setLatLong(new LatLong(myLocation.getLatitude(), myLocation.getLongitude()));
                try {
                    if (!map.contains(myLoc)) {

                        map.addMark(myLoc);
                    }
                }catch (IllegalStateException e) {
                    Log.e(TAG, "Error adding layers", e);
                }
            }


        }
        List<AviObject> markList = qb.getAllBuoys();
        for (AviObject o : markList){
            //TODO: Delete old buoys first
            Marker m; //TODO Refactor as hell!
            Bitmap b = AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.buoyblack));
            m = new Marker(GeoUtils.toLatLong(o.location),b,0,0);
            workerLocs.put(o.name, m);
            try {
                map.removeMark(m);
                map.addMark(m);
            }catch (IllegalStateException e)
            {
                Log.e(TAG,"Error adding layers",e);
            }
        }

////            rbLine.setPaintStroke(paintStroke);
////            List<LatLong> geoPoints = rbLine.getLatLongs();
////            geoPoints.clear();
////            geoPoints.add(new LatLong(l.getLatitude(), l.getLongitude()));
////            geoPoints.add(new LatLong(myLocation.getLatitude(), myLocation.getLongitude()));
////            this.mapView.getLayerManager().getLayers().remove(rbLine);
////            this.mapView.getLayerManager().getLayers().add(rbLine);
//            //zoomToBounds(new LatLong(l.getLatitude(), l.getLongitude()),new LatLong(myLocation.getLatitude(), myLocation.getLongitude()));

    }

    @NonNull
    private TextMarker getTextMarker(GraphicFactory gf, Location myLocation, AviObject o) {
        Paint p = gf.createPaint();
        p.setColor(Color.BLACK);
        p.setTextSize((int) (16 * getResources().getDisplayMetrics().density));
        int distance;
        try {
            distance = myLocation.distanceTo(GeoUtils.toLocation(o.location)) < 5000 ? (int) myLocation.distanceTo(GeoUtils.toLocation(o.location)) : ((int) (myLocation.distanceTo(GeoUtils.toLocation(o.location)) / 1609.34));
        }catch (NullPointerException e)
        {
            distance = -1;
        }
        Log.v(TAG, "Distance to user " + o.name + " " + distance);
        String units = myLocation.distanceTo(GeoUtils.toLocation(o.location))<5000?"m":"NM";
        int bearing = myLocation.bearingTo(GeoUtils.toLocation(o.location)) > 0 ? (int) myLocation.bearingTo(GeoUtils.toLocation(o.location)) : (int) myLocation.bearingTo(GeoUtils.toLocation(o.location))+360;
        TextMarker tm;
        if (workerTexts.containsKey(o.name)){
            tm = workerTexts.get(o.name);
            tm.setText(bearing + "\\" + distance + units);
            tm.setLatLong(GeoUtils.toLatLong(o.location));
        }
        else{
            tm = new TextMarker(bearing + "\\" + distance + units,p,GeoUtils.toLatLong(o.location), AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatblue)));
        }
        return tm;
    }

    @NonNull
    private Bitmap getBoatBitmap(AviObject o) {
        Bitmap b = AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatred));
        if(o.color.contains("blue")) b =  AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatblue));
        if(o.color.contains("cyan")) b =  AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatcyan));
        if(o.color.contains("orange")) b =  AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatorange));
        if(o.color.contains("green")) b =  AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatgreen));
        if(o.color.contains("pink")) b =  AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatpink));
        if (o.type==ObjectTypes.RaceManager){
            b =AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.managerblue));
        }

        if((o.lastUpdate!=null)&&(GeneralUtils.dateDifference(o.lastUpdate)>2000))
        {
            b = AndroidGraphicFactory.convertToBitmap(ContextCompat.getDrawable(MainActivity.this.getApplicationContext(), R.drawable.boatred));
        }
        return b;
    }
    public static void resetMap(){
        for(Marker m: workerLocs.values()){
            map.removeMark(m);
        }
        for(TextMarker tm:workerTexts.values()){
            map.removeMark(tm);
        }
        workerLocs.clear();
        workerTexts.clear();
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            //mBuilder.setOngoing(false);
            notification.cancelAll();
            finish(); // finish activity
            System.exit(0);
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        //if (mRequestingLocationUpdates) {
            startLocationUpdates();
        //}
        mLastLocation = GeoUtils.toAviLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"GPS Location: "+ location);
        mLastLocation = GeoUtils.toAviLocation(location);
        Log.d(TAG,"OwnLocation: "+ mLastLocation.toLocation());
        mLastLocation.lastUpdate = new Date();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected())
        {LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);}
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        resetMap();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()/* && !mRequestingLocationUpdates*/) {
            startLocationUpdates();
        }
        redrawLayers();
    }
}


