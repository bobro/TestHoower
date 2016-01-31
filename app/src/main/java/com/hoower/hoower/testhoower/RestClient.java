package com.hoower.hoower.testhoower;


import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Matus on 25.9.15.
 */
public class RestClient {


    String urlHL;
    String response;
    String param;


    public RestClient(String s) {

        urlHL = s;
        response = "";
        param="";
    }




    public String executePost() {  // matoda na stahovanie dat zo servera

        try {

            URL url = new URL(urlHL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoInput(true);
            conn.setDoOutput(true);



            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(param);    //  parametre v body POST
            writer.flush();
            writer.close();
            os.close();


            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                Log.e("http", "ok");
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }

            conn.connect();


        } catch (Exception e) {


        }
        return response;


    }
    public void setParam(String pParam){

        param=pParam;
    }
}
