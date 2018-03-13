package org.disastermngt4a;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thoma on 3/10/2018.
 */

public class AsyncHttpPost extends AsyncTask<String, Void, JSONArray>  {
    private HashMap<String, String> mData; // post data
    private GoogleMap mMap;
    private ClusterManager<DatabaseEntity> disasterReports;

    public AsyncHttpPost(Context context, HashMap<String, String> data, GoogleMap map){
        mData = data;
        mMap = map;
        disasterReports = new ClusterManager<DatabaseEntity>(context,mMap);
        mMap.setOnCameraIdleListener(disasterReports);

    }

    @Override
    protected JSONArray doInBackground(String... params){

        JSONArray arr = null;
        HttpClient client = new DefaultHttpClient();


        try {
            HttpPost post = new HttpPost(params[0]); // the url

            // set up your post data
            ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            Iterator<String> it = mData.keySet().iterator();
            while (it.hasNext()){
                String key = it.next();
                System.out.println("KEY" + key);
                nameValuePair.add(new BasicNameValuePair(key, mData.get(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));

            HttpResponse response = client.execute(post);

            byte[] result = EntityUtils.toByteArray(response.getEntity());
            String str = new String(result, "UTF-8");
            arr = new JSONArray(str);
        }
        catch(UnsupportedEncodingException e){
            android.util.Log.v("INFO", e.toString());
        }
        catch(Exception e){
            android.util.Log.v("INFO", e.toString());
        }

        return arr;
    }

    @Override
    protected void onPostExecute(JSONArray Result){

        if (mData.get("tab_id").equalsIgnoreCase("1")){
            System.out.println("REQUEST");
            onQueryReportExecute(Result);
        }
    }
    private void onQueryReportExecute(JSONArray Result){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        System.out.println("RESULT COUNT: " + Result.length());

        //TODO
        // Get the vals, create DatabaseEntry objects,


        for (int i = 0; i < Result.length(); i++){
            try{
                JSONObject report = Result.getJSONObject(i);

                System.out.println("CURR REP");
                System.out.println(report.toString());

                Double lng = Double.parseDouble(report.getString("longitude"));
                Double lat = Double.parseDouble(report.getString("latitude"));

                String reportType = report.getString("report_type");
                System.out.println("REPORT TYPE: " + reportType);
                String disasterType = report.getString("disaster");
                disasterType = disasterType.toUpperCase(); // Capitalize
                System.out.println("DISASTER TYPE: " + disasterType);
                String resourceType = report.getString("resource_type");
                System.out.println("RESOURCE TYPE: " + resourceType);

                String snippetMessage = "";

                if (reportType.equalsIgnoreCase("damage")){

                    // Format snippet message for a damage report
                    snippetMessage = "report damage:" + report.getString("damage_type");

                }
                else if (reportType.equalsIgnoreCase("request")){

                    snippetMessage = "request resource: " + report.getString("resource_type");
                }

                else if (reportType.equalsIgnoreCase("donation")){
                    snippetMessage = "donate resource: " + report.getString("resource_type");

                }

                // Build it
                LatLng latlng = new LatLng(lat, lng);
                builder.include(latlng);

                int markerVal = 0;

                switch(reportType){

                    case "damage":
                        markerVal = R.mipmap.doityourself;
                        break;
                    case "request":
                        markerVal = R.mipmap.fire_station;
                        break;
                    case "donation":
                        markerVal = R.mipmap.retail;
                        break;
                    default:
                        markerVal = R.mipmap.map_marker;

                }

                DatabaseEntity newEntity = new DatabaseEntity(latlng,disasterType,snippetMessage);
                disasterReports.addItem(newEntity);

                // Marker time
                /*mMap.addMarker(new MarkerOptions().position(latlng).title(disasterType
                ).icon(BitmapDescriptorFactory.fromResource(markerVal)).snippet(snippetMessage));*/

                /*// Set up your marker
                mMap.addMarker(new MarkerOptions().position(latlng).title("Title").icon
                        (BitmapDescriptorFactory.fromResource(R.mipmap.map_marker)).snippet("Snippet Message"));*/
            }
            catch(JSONException e){
                android.util.Log.v("INFO", e.toString());
            }
        }

        if (Result.length() > 0){

            System.out.println("HEY CAMERA UPDATE");
            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of map (pixels)
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);

            mMap.moveCamera(cu);



        }

    }
}
