package org.disastermngt4a;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by thoma on 3/12/2018.
 */

public class DatabaseEntity implements ClusterItem {

    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public DatabaseEntity(LatLng xy, String title, String snippet){
        mPosition = xy;
        mTitle = title;
        mSnippet = snippet;
    }


    @Override
    public LatLng getPosition() {return mPosition;}

    @Override
    public String getTitle() {return mTitle;}


    @Override
    public String getSnippet() {return mSnippet;}


    public void setTitle(String title){
        mTitle = title;
    }

    public void setSnippet(String snippet){
        mSnippet = snippet;
    }

}


