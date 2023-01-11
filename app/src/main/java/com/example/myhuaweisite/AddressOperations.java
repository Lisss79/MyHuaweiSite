package com.example.myhuaweisite;

import static com.example.myhuaweisite.MainActivity.NO_ERROR;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddressOperations {
    private HuaweiMap hMap;
    private Context context;
    private Handler addressHandler;
    private String addressText;
    private Thread addrThread;

    public AddressOperations(Context context, HuaweiMap hMap) {
        this.hMap = hMap;
        this.context = context;
    }

    public MarkerOptions getMarkerOptions(LatLng latLng) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_ball);
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(icon)
                .position(latLng);
        addressText = "Error";
        getAddress(latLng);
        try {
            addrThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        markerOptions.title(addressText);
        return markerOptions;
    }

    /**
     * Получение адреса по координатам
     */
    private void getAddress(LatLng latLng) {
        double lat = latLng.latitude;
        double lng = latLng.longitude;
        //latitude = 55.914876563430454;
        //longitude = 36.79219668459067;

        Runnable addressRunnable = () -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1);
                // If an address is found, read it into resultMessage
                Address address = addresses.get(0);

                String street = address.getThoroughfare();
                String houseName = address.getSubThoroughfare();
                addressText = street + ", " + houseName;

            } catch (IOException e) {
                // Ошибка – адрес не получен
                e.printStackTrace();
                addressText = "No data :-(";
                Message msg = new Message();
            }
        };
        addrThread = new Thread(addressRunnable, "AddrThread");
        addrThread.start();

    }

}
