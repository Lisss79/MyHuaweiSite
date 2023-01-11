package com.example.myhuaweisite;

import static com.example.myhuaweisite.MainActivity.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MapStyleOptions;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;

public class ObjectDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private SupportMapFragment mSupportMapFragment;
    private MapView mMapView;
    private HuaweiMap hMap;
    private double latitude;
    private double longitude;
    private String name;
    private String address;
    private String apiKey;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Marker myPlaceMarker;
    private Marker marker;
    private AddressOperations addressOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        latitude = data.getDoubleExtra(LATITUDE_KEY, 0);
        longitude = data.getDoubleExtra(LONGITUDE_KEY, 0);
        name = data.getStringExtra(NAME_KEY);
        address = data.getStringExtra(ADDRESS_KEY);
        apiKey = data.getStringExtra(API_KEY);

        // Initialize the SDK.
        MapsInitializer.initialize(this, "RU");
        MapsInitializer.setApiKey(apiKey);
        setContentView(R.layout.activity_object_detail);
        Toolbar toolbar = findViewById(R.id.toolbarObjectDetail);
        toolbar.setTitle("Info about the object");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mMapView = findViewById(R.id.mapViewObjectDetail);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        addressOperations = new AddressOperations(this, hMap);
    }

    public void addMarker(double lat, double lon, String name, String address) {
        if (null != myPlaceMarker) {
            myPlaceMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lon))
                .title(name)
                .snippet(address);
        myPlaceMarker = hMap.addMarker(options);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        System.out.println("onMapReady");
        hMap = huaweiMap;
        if(isDarkMode()) {
            MapStyleOptions style;
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
            hMap.setMapStyle(style);
        }
        hMap.getUiSettings().setMyLocationButtonEnabled(true);
        hMap.setMyLocationEnabled(true);
        hMap.getUiSettings().setCompassEnabled(true);
        LatLng latlng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latlng);
        hMap.moveCamera(cameraUpdate);
        addMarker(latitude, longitude, name, address);

        hMap.setOnMapLongClickListener(new HuaweiMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (null != marker) {
                    marker.remove();
                }
                MarkerOptions markerOptions = addressOperations.getMarkerOptions(latLng);
                marker = hMap.addMarker(markerOptions);
                marker.showInfoWindow();
            }
        });

        hMap.setOnMapClickListener(new HuaweiMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (null != marker) {
                    marker.remove();
                }
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    private boolean isDarkMode() {
        boolean nightMode;
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_YES) nightMode = true;
        else nightMode = false;
        return nightMode;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}