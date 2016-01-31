package com.hoower.hoower.testhoower;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Matus on 1.10.15.
 */
public class SecondFragment extends ListFragment implements View.OnClickListener{

    ArrayList<Miesto> pole;
    SharedPreferences sharedpreferences;
    private Button obnov;
    public static final String PREFS_NAME = "hoower";


   // konstruktor na vytvaranie fragmentov
    public static SecondFragment newInstance() {
        SecondFragment fragmentSecond = new SecondFragment();
        return fragmentSecond;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Log.e("SecondFragment","create");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_barov, container, false);

        obnov = (Button) view.findViewById(R.id.obnov);
        obnov.setOnClickListener(this);


        return view;
    }

 public void obnovenie(){

     pole=new ArrayList<Miesto>();

     JSONObject json= new JSONObject();

     Log.e("SecondFragment","Obnov");

     sharedpreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
     try {
         json=new JSONObject(sharedpreferences.getString("json","")) ;  //// nacitat json zo shared referencii
         //Log.e("json",""+json);

/*
            rozparsovanie Jsonu
 */

         JSONArray array = json.optJSONArray("places");
         JSONObject prvok=null;
         for(int i=0;i<array.length();i++) {
             prvok = array.getJSONObject(i);

             double lati = prvok.getDouble("latitude");
             double longi = prvok.getDouble("longitude");
             String title = prvok.getString("name");

             Location loc1 = new Location("");
             loc1.setLatitude(lati);
             loc1.setLongitude(longi);

             Location loc2 = new Location("");
             loc2.setLatitude( Double.valueOf(sharedpreferences.getString("lat", "")));
             loc2.setLongitude(Double.valueOf(sharedpreferences.getString("lon", "")));

             float distanceInMeters = loc1.distanceTo(loc2);        // vzdialenosti medzi log a lat

             pole.add(new Miesto(title, (int) distanceInMeters));

             // Log.e("map", "" + prvok.getString("name"));

         }

         Collections.sort(pole);    // rozradenie dat podla vzdialenosti

     } catch (JSONException e) {
         e.printStackTrace();
         Log.e("second","chyba");
     }
     SampleAdapter adapter = new SampleAdapter(getActivity());

     for(int i=0;i<pole.size();i++){

         adapter.add(pole.get(i));  // naplnanie adaptera
     }

     setListAdapter(adapter);

 }

    @Override
    public void onClick(View InputFragmentView) {
        obnovenie();

    }

    /**
     * Class pre array adapter a parsovanie
     */
    public class Miesto implements Comparable{
        private String nazov;
        private int vzdialenost;

        public Miesto(String nazov, int vzdialenost) {
            this.nazov = nazov;
            this.vzdialenost = vzdialenost;
        }

        public void setNazov(String nazov) {
            this.nazov = nazov;
        }

        public void setVzdialenost(int vzdialenost) {
            this.vzdialenost = vzdialenost;
        }

        public String getNazov() {

            return nazov;
        }

        public int getVzdialenost() {
            return vzdialenost;
        }

        public Miesto(int vzdialenost, String nazov) {

            this.vzdialenost = vzdialenost;
            this.nazov = nazov;
        }

        /**
         * Metoda ktora urcuje ako sa bude parsovat
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(Object o) {
            int compareage=((Miesto)o).getVzdialenost();
        /* For Ascending order*/
            return this.vzdialenost-compareage;
        }
    }

    /**
     * Adapter na miesta
     *
     */
    public class SampleAdapter extends ArrayAdapter<Miesto> {

        public SampleAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.miesto, null);
            }
            TextView nazov = (TextView) convertView.findViewById(R.id.nazov);
            nazov.setText(getItem(position).getNazov());

            TextView vzdialenost = (TextView) convertView.findViewById(R.id.vzdialenost);
            vzdialenost.setText(String.valueOf(getItem(position).getVzdialenost()+" m"));

            return convertView;
        }

    }

}