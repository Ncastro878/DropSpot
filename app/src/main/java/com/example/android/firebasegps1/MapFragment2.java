package com.example.android.firebasegps1;

import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import java.util.ArrayList;

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
    ArrayList<MarkerOptions> markersList = new ArrayList<>();
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

    //TODO: maybe update this on background thread cuz laggy UI when update location
    void populateMapPins() {
        chatListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double longitude = 0, latitude = 0;
                markersList.clear();
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
                                .icon(getCorrectPin(latitude, longitude));
                        //if(!markersList.contains(newMarker))
                            markersList.add(newMarker);
                        updateMap(markersList);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateMap(ArrayList<MarkerOptions> markersList) {
        m_map.clear();
        for(MarkerOptions marker:markersList){
            m_map.addMarker(marker);
        }
    }

    private BitmapDescriptor getCorrectPin(double longitude, double latitude) {
        Location newLocation = new Location("");
        newLocation.setLongitude(longitude);
        newLocation.setLatitude(latitude);
        if(newLocation.distanceTo(MainActivity.lastLocation) > 1609){
            String ugh = String.format("newLocation lat:%s, long:%s", latitude, longitude);
            String eck = String.format("currentLocation lat:%s, long:%s", MainActivity.lastLocation.getLatitude(),
                    MainActivity.lastLocation.getLongitude());

            Log.v("MapFragment2.java", eck);
            Log.v("MapFragment2.java", ugh);
            return BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin48);
        }
        return BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin_48_green);
    }
}
