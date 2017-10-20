package com.example.android.firebasegps1;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
            .zoom(12)
            .bearing(0)
            .tilt(45)
            .build();

    /**
     * Firebase variables
     */
    private FirebaseDatabase chatDataBase;
    private DatabaseReference chatListReference;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        /**
         * Set up the MapFragment callback to OnMapReady()
         * */
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /**
         * Setup Firebase Database references
         */
        chatDataBase = FirebaseDatabase.getInstance();
        chatListReference = chatDataBase.getReference().child("chat_rooms");
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
        populateMapPins();
    }

    private void populateMapPins() {
        chatListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double longitude = 0, latitude = 0;
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    String roomName = child.getKey();
                    Log.v("MapFragment2", "Room name is: " + roomName);
                    for(DataSnapshot data: child.getChildren()){
                        Log.v("MapFragment21", "data key is: " + data.getKey());
                        if(data.getKey().equals( "l")){
                            Log.v("MapFragment22", "Data key matches l ");
                            longitude = Double.parseDouble(data.child("0").getValue().toString());
                            latitude =  Double.parseDouble(data.child("1").getValue().toString());
                            Log.v("MapFragment2", String.format("%s is lat, %s is long",latitude, longitude));
                        }
                        MarkerOptions newMarker = new MarkerOptions()
                                .position(new LatLng(longitude, latitude))
                                .title(roomName)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin48));
                        m_map.addMarker(newMarker);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

}
