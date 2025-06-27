package com.example.buoi10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 110;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0, longitude = 0;
    private boolean isLocationReady = false;

    private TextView txtLocation;
    private Button btnGetLocation, btnOpenMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        txtLocation = findViewById(R.id.txtLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnOpenMap.setOnClickListener(v -> openGoogleMaps());


    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Chưa cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(MainActivity.this, "Không lấy được vị trí", Toast.LENGTH_SHORT).show();
                    return;
                }
                isLocationReady = true;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    String geoInfo = "Lat: " + latitude + "\nLng: " + longitude;

                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            geoInfo += "\n" + address.getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    txtLocation.setText(geoInfo);
                    fusedLocationClient.removeLocationUpdates(this);
                } else {
                    Toast.makeText(MainActivity.this, "Vị trí trả về là null", Toast.LENGTH_SHORT).show();
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void openGoogleMaps() {
        if (!isLocationReady) {
            Toast.makeText(this, "Vui lòng đợi vị trí được lấy xong", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(Vị trí của tôi)";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Từ chối quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
