package com.dokitari.tekomaps.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dokitari.tekomaps.R;
import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
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

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapActivity extends AppCompatActivity implements Listener {

    private GeoPoint startPoint;
    private MapView map;
    private String mTargetAddress, parisKmlUrl = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=52.215676&flon=5.963946&tlat=52.2573&tlon=6.1799%27%3Ehttp://www.yournavigation.org/api/1.0/gosmore.php?format=kml&amp;flat=52.215676&amp;flon=5.963946&amp;tlat=52.2573&amp;tlon=6.1799";
    private double mCurrentLatitude, mCurrentLongitude, mTargetLatitude, mTargetLongitude;
    private EasyWayLocation mEasyWayLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        if (intent != null) {
            mTargetLatitude = intent.getDoubleExtra("lat", 0.0);
            mTargetLongitude = intent.getDoubleExtra("long", 0.0);
            mTargetAddress = intent.getStringExtra("address");
        } else {
            //Fire Error
        }

        mEasyWayLocation = new EasyWayLocation(this);
        mEasyWayLocation.setListener(this);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mEasyWayLocation.beginUpdates();
                        Log.e("Here ", "INner " + mEasyWayLocation.getLatitude());
                        mCurrentLatitude = mEasyWayLocation.getLatitude();
                        mCurrentLongitude = mEasyWayLocation.getLongitude();
                        onMapInitialSetUp();
                        route();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();

    }

    @SuppressLint("CheckResult")
    private void route() {
//        RoadManager roadManager = new OSRMRoadManager(this);
        RoadManager roadManager = new MapQuestRoadManager("3obG2zqwNicScurHGF1X28I4VGzjb67V");
        roadManager.addRequestOption("routeType=bicycle");
        Log.e("Here ", "Current " + mCurrentLongitude + " " + mCurrentLatitude + " Target " + mTargetLongitude + " " + mTargetLatitude);

        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(startPoint);
        GeoPoint endPoint = new GeoPoint(mTargetLatitude, mTargetLongitude);
        waypoints.add(endPoint);

        Log.e("Here ", " start " + startPoint.getLatitude() + " End " + endPoint.getLatitude());

        final Road[] road = new Road[1];

        Flowable.fromCallable(() -> roadManager.getRoad(waypoints))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    road[0] = result;
                    Log.e("Answered", "Answered " + result);

                    if (road[0].mStatus != Road.STATUS_OK) {
                        //handle error... warn the user, etc.
                        Log.e("Here ", "Error while getting data!" + result.mStatus);
                    }
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
//        Flowable.fromCallable(() -> stylingKML());
//        retreive the road between those points
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
        mCurrentLatitude = mEasyWayLocation.getLatitude();
        mCurrentLongitude = mEasyWayLocation.getLongitude();
    }

    @Override
    public void locationCancelled() {
        Log.e("Here ", " Location Cancelled");
    }

    private void onMapInitialSetUp() {
        startPoint = new GeoPoint(mCurrentLatitude, mCurrentLongitude);
        GeoPoint targetPoint = new GeoPoint(mTargetLatitude, mTargetLongitude);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.mipmap.ic_map_marker));
        startMarker.setTitle("Start point");
        map.getOverlays().add(startMarker);


        Marker targetMarker = new Marker(map);
        targetMarker.setPosition(targetPoint);
        targetMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        targetMarker.setIcon(getResources().getDrawable(R.mipmap.alert_location));
        targetMarker.setTitle("Target point");
        map.getOverlays().add(targetMarker);

        IMapController mapController = map.getController();
        mapController.setZoom(18.5);
        mapController.setCenter(startPoint);
    }
}
