package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PSX on 1/30/2018.
 */

public class Geofencing implements ResultCallback {
    public static final String TAG = "PSX";
    private static final float GEOFENCE_RADIUS = 50; // 50 meter
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000;  // 24 ora
    
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private PendingIntent mGeofencePendingIntent;
    private List<Geofence> mGeofenceList;
    
    
    public Geofencing(Context context, GoogleApiClient client){
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
        Log.d(TAG, "Geofencing.Geofencing" );
    }
    
    public void registerAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() || mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
            ).setResultCallback(this);
        
        } catch (SecurityException securityException) {
            Log.d(TAG, "Geofencing.registerAllGeofences SecurityException->" + securityException.getMessage());
        }
        Log.d(TAG, "Geofencing.registerAllGeofences" );
    }
    
    public void unRegisterAllGeofences(){
        if(mGoogleApiClient == null || ! mGoogleApiClient.isConnected() ) {
            Log.d(TAG, "Geofencing.unRegisterAllGeofences empty return" );
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
            ).setResultCallback(this);
            
        } catch (SecurityException securityException) {
            Log.d(TAG, "Geofencing.unRegisterAllGeofences SecurityException->" + securityException.getMessage());
        }
        Log.d(TAG, "Geofencing.unRegisterAllGeofences" );
    }
    
    
    public void updateGeofencesList(PlaceBuffer places){
        Log.d(TAG, "Geofencing.updateGeofencesList" );
        mGeofenceList = new ArrayList<>();
        if(places == null || places.getCount() == 0) return ;
        for(Place place : places){
           String placeUID = place.getId();
           double placeLat = place.getLatLng().latitude;
           double placeLng = place.getLatLng().longitude;
           
           Geofence geofence = new Geofence.Builder()
               .setRequestId(placeUID)
               .setExpirationDuration(GEOFENCE_TIMEOUT)
               .setCircularRegion(placeLat, placeLat, GEOFENCE_RADIUS)
               .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
               .build();
           mGeofenceList.add(geofence);
        }
    }
 
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    
    private PendingIntent getGeofencePendingIntent(){
        if(mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
    
    @Override
    public void onResult(@NonNull Result result) {
        Log.d(TAG, "Geofencing.registerAllGeofences onResult-> " + result.getStatus().toString());
    }
}
