package com.zorolee.android.geoclock;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private TextView positionTextView;
    private LocationManager locationManager;
    private String provider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        positionTextView = (TextView)findViewById(R.id.position_text_view);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        provider = LocationManager.NETWORK_PROVIDER;
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else {
            Toast.makeText(this,"No location provider to use",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                showLocation(location);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10,locationListener);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void showLocation(Location location) {
        String latitude = "" + location.getLatitude();
        String longitude = "" + location.getLongitude();
        positionTextView.setText("latitude is " + latitude + "\n" + "longitude is " + longitude);

    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private static final double EARTH_RADIUS = 6378.137;
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2){
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
//        s = Math.round(s * 10000) / 10000;
        return s;
    }
}
