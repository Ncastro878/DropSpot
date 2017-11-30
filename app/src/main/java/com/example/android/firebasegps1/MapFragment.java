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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by nick on 10/13/2017.
 */

//TODO: When chatroom is deleted, update map and pins, deleting the old chatroom pin.
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final float METERS_IN_A_MILE = 1609;
    /**
     * My Map Variables
     */
    GoogleMap m_map;
    boolean mapReady = false;
    MarkerOptions brickTown;
    ArrayList<MarkerOptions> markersList = new ArrayList<>();
    /**
     *Default Camera Position
     */
    static final CameraPosition WICHITA_FALLS = CameraPosition.builder()
            .target(new LatLng(33.9137,-98.4934))
            .zoom(12)
            .bearing(0)
            .tilt(45)
            .build();

    /**
     * Firebase variables
     */
    private FirebaseDatabase firebaseChatDataBase;
    private DatabaseReference chatListReference;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        /**
         * Set up the MapFragment callback to OnMapReady()
         * */
        com.google.android.gms.maps.MapFragment mapFragment = (com.google.android.gms.maps.MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /**
         * Setup Firebase Database references
         */
        firebaseChatDataBase = FirebaseDatabase.getInstance();
        chatListReference = firebaseChatDataBase.getReference().child("chat_rooms");
        super.onActivityCreated(savedInstanceState);
    }

    public static MapFragment newInstance(){
        MapFragment fragment = new MapFragment();
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
        //example of how to make map markers
        brickTown = new MarkerOptions()
                .position(new LatLng(33.868589, -98.532809))
                .title("BrickTown Brewery")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin40));
        mapReady = true;
        m_map = googleMap;
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(WICHITA_FALLS));
        populateMapPins();
    }

    //TODO: maybe updating this on background thread will cure laggy UI when location updates
    void populateMapPins() {
        chatListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double longitude = 0, latitude = 0;
                markersList.clear();
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    String roomName = child.getKey();
                    Log.v("MapFragment", "Room name is: " + roomName);
                    for(DataSnapshot data: child.getChildren()){
                        Log.v("MapFragment21", "data key is: " + data.getKey());
                        if(data.getKey().equals( "l")){
                            Log.v("MapFragment22", "Data key matches l ");
                            longitude = Double.parseDouble(data.child("0").getValue().toString());
                            latitude =  Double.parseDouble(data.child("1").getValue().toString());
                            Log.v("MapFragment", String.format("%s is lat, %s is long",latitude, longitude));
                        }
                        MarkerOptions newMarker = new MarkerOptions()
                                .position(new LatLng(longitude, latitude))
                                .title(roomName)
                                .icon(getCorrectPinColor(latitude, longitude));
                        markersList.add(newMarker);
                    }
                }
                updateMap(markersList);
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

    private BitmapDescriptor getCorrectPinColor(double longitude, double latitude) {
        Location newLocation = new Location("");
        newLocation.setLongitude(longitude);
        newLocation.setLatitude(latitude);

        if(newLocation.distanceTo(MainActivity.lastLocation) > METERS_IN_A_MILE){
            String ugh = String.format("newLocation lat:%s, long:%s", latitude, longitude);
            String eck = String.format("currentLocation lat:%s, long:%s", MainActivity.lastLocation.getLatitude(),
                    MainActivity.lastLocation.getLongitude());
            Log.v("MapFragment.java", eck);
            Log.v("MapFragment.java", ugh);
            return BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin48);
        }
        return BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin_48_green);
    }
}
