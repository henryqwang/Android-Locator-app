package com.example.android.hikerswatch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.math.BigDecimal;
import android.icu.math.MathContext;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.renderscript.Matrix2f;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView latTextView;
    TextView longTextView;
    TextView accuracyTextView;
    TextView altitudeTextView;
    TextView addressTextView;

    LocationManager locationManager;
    LocationListener locationListener;

    Geocoder geocoder;

    //Nearby address fetching & display update
    public void updateLocationInfo(Location location){
        latTextView.setText("Latitude: " + location.getLatitude());
        longTextView.setText("Longitude: " + location.getLongitude());
        accuracyTextView.setText("Accuracy: " + location.getAccuracy());
        altitudeTextView.setText("Altitude: " + location.getAltitude());

        geocoder = new Geocoder(getApplicationContext(), Locale.CANADA);//This will look for addresses near the coordinates

        String addressContent = "Could not find address"; //Default message, in case no address is found
        try {
            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); //1 means only a max of 1 address returned
            if(listAddresses != null && listAddresses.size() > 0){ //We have at least one address found:

                Address currentAddress = listAddresses.get(0);
                addressContent = "";

                //Add each address info if available
                if(currentAddress.getSubThoroughfare() != null){
                    addressContent += currentAddress.getSubThoroughfare();
                }
                if(currentAddress.getThoroughfare() != null){
                    addressContent += "\n"+currentAddress.getThoroughfare();
                }
                if(currentAddress.getPostalCode() != null){
                    addressContent += "\n"+currentAddress.getPostalCode();
                }
                if(currentAddress.getCountryName() != null){
                    addressContent += "\n"+currentAddress.getCountryName();
                }
            }

            //Display address content regardless if relevant address has been found
            addressTextView.setText(addressContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //The method that access current location and update location listener
    public void startListening(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }
    //Describes process after user gives permission the system to access location (via "Allow" button on pop-up)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //In this case, we know that there's only granted result
            startListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latTextView = (TextView)findViewById(R.id.latTextView);
        longTextView = (TextView)findViewById(R.id.longTextView);
        accuracyTextView = (TextView)findViewById(R.id.accuracyTextView);
        altitudeTextView = (TextView)findViewById(R.id.altitudeTextView);
        addressTextView = (TextView)findViewById(R.id.addressTextView);

        //Set up manager and listener pair
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(Build.VERSION.SDK_INT < 23){
            //SDK not high enough: directly access location without permission request
            startListening();
        }else{
            //SDK sufficiently high: check initial permissions
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //If initially does NOT have permission: system pop-up to ask user
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                //Initially have permission already: access location, update listener and display
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null){
                    updateLocationInfo(location);
                }
            }
        }
    }
}
