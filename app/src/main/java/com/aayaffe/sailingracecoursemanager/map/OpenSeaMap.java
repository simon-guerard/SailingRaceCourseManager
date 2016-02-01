package com.aayaffe.sailingracecoursemanager.map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.widget.PopupMenu;

import com.aayaffe.sailingracecoursemanager.MainActivity;
import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.SamplesApplication;
import com.aayaffe.sailingracecoursemanager.geographical.GeoUtils;


import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aayaffe on 04/10/2015.
 */
public class OpenSeaMap implements MapLayer {
    private static final String TAG = "OpenSeaMap";
    public MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    protected List<TileCache> tileCaches = new ArrayList(); //TODO: ADd function of tilecache form MapViewerTemplate
    private Context c;
    private Activity a;
    private LatLong lastTapLatLong;


    private String _mapFile = "germany.map";
    private SharedPreferences sp;
    private TileDownloadLayer downloadLayer;

    protected void createTileCaches() {
        boolean persistent = sp.getBoolean(SamplesApplication.SETTING_TILECACHE_PERSISTENCE, true);

        this.tileCaches.add(AndroidUtil.createTileCache(c, getPersistableId(),
                this.mapView.getModel().displayModel.getTileSize(), this.getScreenRatio(),
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(), persistent
        ));
    }
    protected String getPersistableId() {
        return this.getClass().getSimpleName();
    }
    protected float getScreenRatio() {
        return 1.0F;
    }
    protected void createLayers() {

//        this.downloadLayer = new TileDownloadLayer(this.tileCaches.get(0),
//                this.mapView.getModel().mapViewPosition, OpenStreetMapMapnik.INSTANCE,
//                AndroidGraphicFactory.INSTANCE) {
        OnlineTileSource onlineTileSource = new OnlineTileSource(new String[]{
                "otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com",
                "otile4.mqcdn.com"}, 80);
        onlineTileSource.setName("MapQuest").setAlpha(false)
                .setBaseUrl("/tiles/1.0.0/map/").setExtension("png")
                .setParallelRequestsLimit(8).setProtocol("http")
                .setTileSize(256).setZoomLevelMax((byte) 18)
                .setZoomLevelMin((byte) 0);
        this.downloadLayer = new TileDownloadLayer(this.tileCaches.get(0),
                this.mapView.getModel().mapViewPosition, onlineTileSource,
                AndroidGraphicFactory.INSTANCE){
            @Override
            public boolean onLongPress(LatLong tapLatLong, Point thisXY,
                                       Point tapXY) {
                Log.d(TAG, "LongPress in Parent, Location: " + tapLatLong.toString());
                lastTapLatLong = tapLatLong;
                ((MainActivity)a).PopUpMenu(c,a);
                return true;
            }
        };
        mapView.getLayerManager().getLayers().add(this.downloadLayer);
//        TileRendererLayer tileRendererLayer = new TileRendererLayer(
//                this.tileCaches.get(0), getMapFile(),
//                this.mapView.getModel().mapViewPosition,
//                false, true,
//                org.mapsforge.map.android.graphics.AndroidGraphicFactory.INSTANCE) {
//            @Override
//            public boolean onLongPress(LatLong tapLatLong, Point thisXY,
//                                       Point tapXY) {
//                Log.d(TAG,"LongPress in Parent");
//                this.onLongPress(tapLatLong, thisXY, tapXY);
//                return true;
//            }
//        };
//        mapView.getLayerManager().getLayers().add(tileRendererLayer);


        mapView.getModel().mapViewPosition.setZoomLevelMin(OpenStreetMapMapnik.INSTANCE.getZoomLevelMin());
        mapView.getModel().mapViewPosition.setZoomLevelMax(OpenStreetMapMapnik.INSTANCE.getZoomLevelMax());
        mapView.getMapZoomControls().setZoomLevelMin(OpenStreetMapMapnik.INSTANCE.getZoomLevelMin());
        mapView.getMapZoomControls().setZoomLevelMax(OpenStreetMapMapnik.INSTANCE.getZoomLevelMax());
    }

    @Override
    public void Init(Activity a, Context c, MapView mv, SharedPreferences sp, Location center, int zoom)
    {
        this.c =c;
        this.a = a;
        mapView = mv;
        this.sp = sp;
        createTileCaches();
        createLayers();
    }

    @Override
    public void setCenter(Location l) {
        setCenter(GeoUtils.toLatLong(l));
    }
    @Override
    public void setCenter(LatLong ll){
        mapView.getModel().mapViewPosition.setCenter(ll);
    }
    @Override
    public void setCenter(double lat, double lon) {
        setCenter(new LatLong(lat, lon));
    }
    @Override
    public void setZoomLevel(int zoom){
        mapView.getModel().mapViewPosition.setZoomLevel((byte) zoom);
    }
    @Override
    public void destroy(){
        mapView.destroyAll();
    }
    @Override
    public Marker addMark(Marker m){
        mapView.getLayerManager().getLayers().add(m);
        return m;
    }
    @Override
    public boolean contains(Marker m){
        return mapView.getLayerManager().getLayers().contains(m);
    }
    @Override
    public Marker removeMark(Marker m){
        mapView.getLayerManager().getLayers().remove(m);
        return m;
    }
    private void zoomToBounds(LatLong ll1, LatLong ll2)
    {
        BoundingBox bb = getBoundingBox(ll1,ll2);
        Dimension dimension = this.mapView.getModel().mapViewDimension.getDimension();
        this.mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                bb.getCenterPoint(),
                LatLongUtils.zoomForBounds(dimension, bb, this.mapView.getModel().displayModel.getTileSize())));
    }
    private BoundingBox getBoundingBox(LatLong ll1, LatLong ll2){
        double minLat,maxLat,minLon,maxLon;
        if (ll1.latitude<ll2.latitude)
        {
            minLat = ll1.latitude;
            maxLat = ll2.latitude;
        }
        else {
            minLat = ll2.latitude;
            maxLat = ll1.latitude;
        }
        if (ll1.longitude<ll2.longitude)
        {
            minLon = ll1.longitude;
            maxLon = ll2.longitude;
        }
        else {
            minLon = ll2.longitude;
            maxLon = ll1.longitude;
        }
        BoundingBox bb = new BoundingBox(minLat,
                minLon, maxLat, maxLon);
        return bb;
    }


    @Override
    public void loadMap(){
        // tile renderer layer using internal render theme
        MapDataStore mapDataStore = getMapFile();
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
//         only once a layer is associated with a mapView the rendering starts
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
    }


    protected MapDataStore getMapFile() {
        return new MapFile(new File(this.getMapFileDirectory(), _mapFile));
    }
    protected File getMapFileDirectory() {
        return Environment.getExternalStorageDirectory();
    }


    @Override
    public LatLong getLastTapLatLong() {
        return lastTapLatLong;
    }

}
