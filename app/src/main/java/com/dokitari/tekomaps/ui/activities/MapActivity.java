package com.dokitari.tekomaps.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dokitari.tekomaps.R;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Observable;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapActivity extends AppCompatActivity implements Listener {

    private GeoPoint startPoint;
    private MapView map;
    private String mTargetAddress, parisKmlUrl = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=52.215676&flon=5.963946&tlat=52.2573&tlon=6.1799%27%3Ehttp://www.yournavigation.org/api/1.0/gosmore.php?format=kml&amp;flat=52.215676&amp;flon=5.963946&amp;tlat=52.2573&amp;tlon=6.1799";
    private double mCurrentLatitude, mCurrentLongitude, mTargetLatitude, mTargetLongitude;
    EasyWayLocation easyWayLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        if (intent!=null){
            mTargetLatitude = intent.getDoubleExtra("lat", 0.0);
            mTargetLongitude = intent.getDoubleExtra("long", 0.0);
            mTargetAddress = intent.getStringExtra("address");
            Toast.makeText(this, " Here " + mTargetAddress + " " + mTargetLatitude + " " + mTargetLongitude, Toast.LENGTH_SHORT).show();
        }else {
            //Fire Errror

        }

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        //Get Location
        easyWayLocation = new EasyWayLocation(this);
        easyWayLocation.setListener(this);
        mCurrentLatitude = easyWayLocation.getLatitude();
        mCurrentLongitude = easyWayLocation.getLongitude();
        Toast.makeText(this, "Long " + mCurrentLongitude + " Latitude " + mCurrentLatitude, Toast.LENGTH_SHORT).show();

        if (mCurrentLatitude != 0 && mCurrentLongitude != 0)
            startPoint = new GeoPoint(mCurrentLatitude, mCurrentLongitude);
        else
            startPoint = new GeoPoint(55.7936572, 49.118653199999926);
        IMapController mapController = map.getController();
        mapController.setZoom(18.5);
        mapController.setCenter(startPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.ic_map_marker));
        startMarker.setTitle("Start point");
        map.getOverlays().add(startMarker);


        route();
    }

    @SuppressLint("CheckResult")
    private void route() {
        RoadManager roadManager = new OSRMRoadManager(this);

        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(startPoint);
        GeoPoint endPoint = new GeoPoint(55.816344, 49.093849);
        waypoints.add(endPoint);


        final Road[] road = new Road[1];
        Flowable.fromCallable(() -> roadManager.getRoad(waypoints)).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    road[0] = result;
                    Log.e("Answered", "Answered " + result);

                    //build a Polyline with the route shape
                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road[0]);
                    //Add this Polyline to the overlays of your map
                    map.getOverlays().add(roadOverlay);
                    map.invalidate();

                    Drawable nodeIcon = getResources().getDrawable(R.mipmap.ic_map_marker);
                    for (int i = 0; i < road[0].mNodes.size(); i++) {
                        RoadNode node = road[0].mNodes.get(i);
                        Marker nodeMarker = new Marker(map);
                        nodeMarker.setPosition(node.mLocation);
                        nodeMarker.setIcon(nodeIcon);
                        nodeMarker.setTitle("Step " + i);
                        map.getOverlays().add(nodeMarker);

                        nodeMarker.setSnippet(node.mInstructions);
                        Log.e("Try", "Try " + node.mManeuverType);
                        nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
                        Drawable icon = getResources().getDrawable(R.mipmap.ic_continue);
                        nodeMarker.setImage(icon);
                    }


                });
        Flowable.fromCallable(() -> stylingKML());
        //retreive the road between those points
    }

    private boolean stylingKML() {
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseKMLUrl(parisKmlUrl);
//        FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, null, kmlDocument);


        Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_default);
        Bitmap defaultBitmap = ((BitmapDrawable) defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);
        FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, defaultStyle, null, kmlDocument);

        map.getOverlays().add(kmlOverlay);
        map.invalidate();
        BoundingBox bb = kmlDocument.mKmlRoot.getBoundingBox();
        map.getController().setCenter(bb.getCenter());
        return true;
    }

    @Override
    public void locationOn() {

    }

    @Override
    public void onPositionChanged() {

    }

    @Override
    public void locationCancelled() {

    }
}
