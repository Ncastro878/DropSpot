package com.example.android.firebasegps1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by nick on 10/13/2017.
 */

public class MapFragment2 extends Fragment implements OnMapReadyCallback {

    /**
     * My Map Variables
     */
    GoogleMap m_map;
    boolean mapReady=false;
    MarkerOptions brickTown;
    /**
     *Default Camera Position
     */
    static final CameraPosition WF = CameraPosition.builder()
            .target(new LatLng(33.9137,-98.4934))
            .zoom(10)
            .bearing(0)
            .tilt(45)
            .build();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        /**
         * Set up the MapFragment callback to OnMapReady()
         * */
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        super.onActivityCreated(savedInstanceState);
    }

    public static MapFragment2 newInstance(){
        MapFragment2 fragment = new MapFragment2();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment,container, false);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        /**
         * Initializing my Markers to add to map
         */
        brickTown = new MarkerOptions()
                .position(new LatLng(33.868589, -98.532809))
                .title("BrickTown Brewery")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin40));
        mapReady=true;
        m_map=googleMap;
        //m_map.addMarker(brickTown);
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(WF));
        m_map.addMarker(brickTown);
    }
}
