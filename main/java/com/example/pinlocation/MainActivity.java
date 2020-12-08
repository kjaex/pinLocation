package com.example.pinlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;

import static com.example.pinlocation.Constants.ERROR_DIALOG_REQUEST;
import static com.example.pinlocation.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.pinlocation.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //needed to ask for location permission
    private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServicesOK()){
            //initializes button that allows access to the map
            init();
        }

    }

    //checking if we have access to google map services
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapEnabled()){
                return true;
            }
        }
        return false;
    }

    //checking if google services is installed
    private boolean isServicesOK(){
        Log.d(TAG, "checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        //if yes available return true
        if(available == ConnectionResult.SUCCESS){
            Log.d(TAG, "Google Play Services Available");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //A Resolvable Error Occurred
            Log.d(TAG,"an error occurred, we can fix it");
            //helps user install google services; dialog right from google
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "You can't make map requests",Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    //checking if GPS enabled
    private boolean isMapEnabled(){
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        //if its disabled
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    //if GPS is not enabled alert user/ask to enable
    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("GPS is needed. Do you want to enable it?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,@SuppressWarnings("unused") final int which) {
                Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //helps us know if user allows gps or not
                startActivityForResult(enableGpsIntent,PERMISSIONS_REQUEST_ENABLE_GPS);
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    //when result from startActivity is retrieved -> continues here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode,resultCode,data);

        Log.d(TAG, "onActivityResult has been called");
        switch(requestCode){
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted)
                {
                    Log.d(TAG, "Access Granted");
                }
                else{
                    getLocationPermission();
                }
            }
        }
    }

    //Ask for location permission to locate device
    private void getLocationPermission() {

        //if accepted
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
        }else{
            //else ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        mLocationPermissionGranted = false;

        switch(requestCode){
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                //if request is cancelled, result is empty
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mLocationPermissionGranted = true;
                    init();
                }
            }
        }
    }

    //initializes button that brings us to map
    private void init(){
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }

    //makes sure gps is enabled and mLocationPermissionGranted is set to true
    @Override
    protected void onResume(){
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted)
            {
                Log.d(TAG, "Access Granted");
            }else{
                getLocationPermission();
            }
        }
    }


}