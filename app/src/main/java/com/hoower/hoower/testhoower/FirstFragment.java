package com.hoower.hoower.testhoower;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Matus on 1.10.15.
 */
public class FirstFragment extends Fragment {


    private Double radius;
    private LocationManager1 loc;


    protected MapView mapView;
    protected GoogleMap map;
    public static final String PREFS_NAME = "hoower";
    SharedPreferences sharedpreferences;



    public static FirstFragment newInstance() {
        FirstFragment fragmentFirst = new FirstFragment();

        return fragmentFirst;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        radius=1000.0;              // radius podla ktoreho berem data zo servera
        loc = new LocationManager1(this.getActivity());         // inicializacia lokalizacie
        sharedpreferences =  getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment, container, false);

        // definicia mapy v xml
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // incializacia mapy
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);


        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onStart() {
        super.onStart();
        loc.mGoogleApiClient.connect(); // pripojenei sa na mgoogle clienta


    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        if (loc.mGoogleApiClient.isConnected() && loc.mRequestingLocationUpdates) {
           // loc.startLocationUpdates();               // zapnutie update
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (loc.mGoogleApiClient.isConnected()) {
            //loc.stopLocationUpdates();            // stopnutie clienta
        }
    }

    @Override
    public void onStop() {
        loc.mGoogleApiClient.disconnect();      // odpojenie sa od klienta

        super.onStop();
    }


    public class LocationManager1 implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

        private Context mContext;
        protected GoogleApiClient mGoogleApiClient;
        private Location mCurrentLocation;
        protected LocationRequest mLocationRequest;
        protected Boolean mRequestingLocationUpdates;


        public LocationManager1(Context context) {
            mContext = context;
            Location loc = new Location("nothing");
            loc.setLatitude(30);    //inicializacia lokacie
            loc.setLongitude(30);   // inicializacia lokacie
            mCurrentLocation = loc; // nastavenie novej na aktualnu
            //
            if (checkIfGooglePlayServicesAreAvailable()) {

                Log.e("FirstFragment", "build");
                buildGoogleApiClient();     // pripojenei sa na google api

                mLocationRequest = new LocationRequest();
                mRequestingLocationUpdates = true;          // povolenie obnovovat data (zatial vypnute)


            } else {

                Log.e("FirstFragment","Google play services - nepouzitelne");
            }
        }

        private synchronized void buildGoogleApiClient() {          // pripojenie google clienta
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            //createLocationRequest();          // vytvorenie requestov
        }

        protected void createLocationRequest() {        // requesty na dany cas
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.e("FirstFragment", "connect map ");
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient); // prebratie dat o lokacii
            if(mCurrentLocation!=null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15);     // presun kamery
                map.moveCamera(cameraUpdate);


                DownloadRest down = new DownloadRest(mCurrentLocation);     // zacatie stahovania dat o podnikoch
                down.execute();
            }
            //startLocationUpdates();       // zacatie obnovovanie dat o lokacii

        }

        private boolean checkIfGooglePlayServicesAreAvailable() {
            int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
            if (errorCode != ConnectionResult.SUCCESS) {
                GooglePlayServicesUtil.getErrorDialog(errorCode, (MainActivity) mContext, 0).show();
                return false;
            }
            Log.e("FirstFragment", "google play services -available");
            return true;
        }

        protected void startLocationUpdates() {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        }
        protected void stopLocationUpdates() {

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }



        @Override
        public void onLocationChanged(Location location) {
            if(location.distanceTo(mCurrentLocation)<20) {      // zisti ci sa lokacia zmenila o 20 metrov
                mCurrentLocation = location;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     // datum kedy prebehol update
                String currentDateandTime = sdf.format(new Date());
                Log.e("time - nacitanie", "" + currentDateandTime);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15);     // presun kamery
                map.moveCamera(cameraUpdate);



                DownloadRest down = new DownloadRest(mCurrentLocation);     // stiahnutie dat o podnikoch
                down.execute();

            }else{
                mCurrentLocation = location;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());
                Log.e("time - nic", "" + currentDateandTime);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

            mGoogleApiClient.connect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e("FirstFragment", "connect failed");

        }
    }


    /**
     * Trieda urcena na stahovanie dat (json) o podnikoch
     */
    private class DownloadRest extends AsyncTask<Void,Void,Void> {

        String response="";
        Location loc;

        DownloadRest(Location location){
            loc=location;
        }

        @Override
        protected Void doInBackground(Void... params) {


            String urlParameters =null;         // vytvorenie json na POST pre server

            try {
                urlParameters = "latitude=" + URLEncoder.encode(Double.toString(loc.getLatitude()), "UTF-8") +
                        "&longitude=" + URLEncoder.encode(Double.toString(loc.getLongitude()), "UTF-8")+
                        "&radius=" + URLEncoder.encode(Double.toString(radius), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            RestClient client = new RestClient("https://powerful-headland-5485.herokuapp.com/test");


            client.setParam(urlParameters);
            response = client.executePost();

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("download",""+response);
            //Log.e("response", "" + response);
            SharedPreferences.Editor editor = sharedpreferences.edit(); // editovanie dat o polohe a podnikoch
            editor.putString("json",response);
            editor.putString("lat", "" + loc.getLatitude());
            editor.putString("lon",""+ loc.getLongitude());
            editor.commit();
            ParsovanieFoursquare pars = new ParsovanieFoursquare(response);     // parsovanie dat z foursquare
            pars.parsovanie();
        }
    }

public class ParsovanieFoursquare{

    JSONObject json;
    String data;

    public ParsovanieFoursquare(String pData){

        data=pData;

    }

    public void parsovanie(){
        try {
            map.clear();        // vycisti mapu od okolnych podnikov
            json = new JSONObject(data);
            JSONArray array = json.optJSONArray("places");
            JSONObject prvok=null;
            for(int i=0;i<array.length();i++){
                prvok = array.getJSONObject(i);

                double lati=prvok.getDouble("latitude");;
                double longi=prvok.getDouble("longitude");
                String title=prvok.getString("name");

                map.addMarker(new MarkerOptions().position(new LatLng(lati, longi)).title(title));      // pridaj marker na mapu

               // Log.e("map", "" + prvok.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}



}