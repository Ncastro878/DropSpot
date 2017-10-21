package com.example.android.firebasegps1;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.R.attr.button;
import static android.R.attr.key;
import static android.R.id.button1;
import static android.R.id.list;
import static android.R.string.no;
import static com.example.android.firebasegps1.MapFragment2.WF;
import static com.firebase.ui.auth.AuthUI.*;
import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        LocationListener, GoogleApiClient.OnConnectionFailedListener  {

    // TODO: test the signout function
    /**
     * -Test multiple users
     * -make users list in db
     * -make friend requests possible
     * -TextView of friends
     * -send friends gps coords
     * -set up dialogue/pop-up box / ride requests
     * -design app layout
     *
     */
    private final String LOG_TAG = "Nick'Firebase App";
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 28;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public String mDisplayName;
    private final String ANONYMOUS = "anonymous";
    private String latestKey = null;

    public static final int RC_SIGNIN = 1 ;
    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mPendingFriendRequestReference;
    private DatabaseReference mUsernamesReference;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ChildEventListener mNewRequestEventListener;

    //Location Services variables
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocatioRequest;
    EditText dialogEditText;

    DatabaseReference mGeoFireChatroomReference;
    GeoFire mChatroomGeoFire;
    GeoQuery mGeoQuery;
    FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        //Get the viewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);

        //Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsernamesReference = mFirebaseDatabase.getReference().child("usernames");

        //Location Services initialization
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //GeoFire Variables
        mGeoFireChatroomReference = FirebaseDatabase.getInstance().getReference("chat_rooms");
        mChatroomGeoFire = new GeoFire(mGeoFireChatroomReference);

        //TODO: Use recurring queries to query current location. erase me
        mGeoQuery = mChatroomGeoFire.queryAtLocation(new GeoLocation(33.9137,98.4934), 10);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //Lets connect the PendingFR-Reference
                    mDisplayName = user.getDisplayName();
                    mPendingFriendRequestReference =
                            mFirebaseDatabase.getReference().child("PendingFriendRequests");
                    String userID = user.getUid();

                    // We add user to users list & usernames list
                    mUsernamesReference.child(user.getDisplayName()).setValue(user.getUid());

                }else{
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                    new IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .build(),
                            RC_SIGNIN);
                }
            }
        };
        /*
        //TODO: create a new chatroom in firebase Chat Directory
        mNewChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //createChatRoomPractice();
                String newRoomName;
                if(editTextView1.length() != 0) {
                    newRoomName = editTextView1.getText().toString();
                    createChatRoomForReal(newRoomName);
                    dropPinOnMap(newRoomName);
                }
            }
        }); */

        /** //TODO: Save this intent for future use
        //TODO: Create intent to go to chatroom
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatRoomTemplate.class);
                if(latestKey != null){
                    Log.d("MainActivity", latestKey + " is the latestKey, man");
                    intent.putExtra("chatRoomName", latestKey);
                    intent.putExtra("user_name", mUsername);
                    startActivity(intent);
                }else if(latestKey == null){
                    Toast.makeText(MainActivity.this, "No room available yet", Toast.LENGTH_LONG).show();
                }
            }
        }); */

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: CREATE DIALOG BOX HERE.
                createRoomDialog();
            }
        });
    }

    private void createRoomDialog() {
        //Make the dialogBuilder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_layout, null);

        //lets init the editText here 1st, then in the builder setup
        //add characteristics
        builder.setView(v)
                .setMessage("Create ChatRoom")
                .setTitle("Make a new room")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO: Do something with chat name string
                        String chatName;
                        dialogEditText = (EditText) v.findViewById(R.id.chat_room_edit_text);
                        if(dialogEditText.getText().toString() != "" ) {
                            chatName = dialogEditText.getText().toString();
                            String toastMsg = chatName + " was created successfully.";
                            Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                            //initChatRoom(chatName);
                            createChatRoomForReal(chatName);
                            dropPinOnMap(chatName);
                        }else{
                            Toast.makeText(MainActivity.this, "enter a name", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO:CANCEL
                        Toast.makeText(MainActivity.this, "Creation Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }

    private void dropPinOnMap(String roomName) {
        Location currentLocation = requestCurrentLocation();
        double longtude = currentLocation.getLongitude();
        double latude = currentLocation.getLatitude();
        MarkerOptions newMarker = new MarkerOptions()
                .position(new LatLng(latude, longtude))
                .title(roomName)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin48));
        //m_map.addMarker(newMarker);

        MapFragment2 mMapFragment2 = (MapFragment2) getSupportFragmentManager().getFragments().get(0);
        mMapFragment2.m_map.addMarker(newMarker);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGNIN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Failure! :(", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void onSignedOutCleanup() {
        mDisplayName = ANONYMOUS;
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if(mNewRequestEventListener != null){
            mPendingFriendRequestReference.removeEventListener(mNewRequestEventListener);
            mNewRequestEventListener = null;
        }
    }

    /**
     * This will add 2 listeners. 2 ChildEventListeners
     * The 2nd will be linked to the PendingFriendRequests branch.
     */
    private void attachDatabaseReadListener() {
        if(mNewRequestEventListener == null){
            mNewRequestEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //Entry will be:     PendingFriend: False
                    Object addedEntryKey = dataSnapshot.getKey();
                    Object addedEntryValue = dataSnapshot.getValue();
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.notifications_activity_menu_option:
                Intent intent = new Intent(MainActivity.this, ChatRoomTemplate.class);
                if(latestKey != null){
                    intent.putExtra("chatRoomName", latestKey);
                }
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        //TODO: List out local chatrooms in area.
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        mGeoQuery.removeAllListeners();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, "Location changed to :" + location.toString() );
        //TODO: Gonna try to geoquery updated locations, makesure setCenter() works vs setLocation()
        double myLong = location.getLongitude();
        double myLat = location.getLatitude();
        mGeoQuery.setCenter(new GeoLocation(myLong, myLat));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocatioRequest = LocationRequest.create();
        mLocatioRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocatioRequest.setInterval(5000);
        /*try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocatioRequest, this);
        }catch(SecurityException e){
            Log.e(LOG_TAG, "error.error: " + e);
        }*/

        //Getting last location info
        Location lastLocation = requestCurrentLocation();
        double myLat = (lastLocation.getLatitude());
        double myLong = (lastLocation.getLongitude());
        mGeoQuery = mChatroomGeoFire.queryAtLocation(new GeoLocation(myLat,myLong), 10);
        addEventListener();
    }

    @Override
    public void onConnectionSuspended(int i) {Log.i(LOG_TAG, " hi friend");}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, " hi friend, connection failed");    }

    public void requestPermissions(){
        // Here, thisActivity is the current activity
        Log.v(LOG_TAG, "Requesting location permission from user.");
        if (ContextCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}


    private void updateChatRoomList(String nameOfRoom, GeoLocation newLocation) {
        double newLat = newLocation.latitude;
        double newLong = newLocation.longitude;
        String chatroomUpdate = nameOfRoom+ " has Lat: " +newLat +
        " and Long: " + newLong;
    }

    private void createChatRoomForReal(final String  name) {
        Location currentLocation = requestCurrentLocation();
        double currentLat = currentLocation.getLatitude();
        double currentLong = currentLocation.getLongitude();
        mChatroomGeoFire.setLocation(name, new GeoLocation(currentLat, currentLong), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(error != null){
                    Log.e("MainActivity", "There was an error saving the location to GeoFire: " + error);
                }else{
                    Log.v("MainActivity", "Location saved on server successfully!");
                    initChatRoom(name);
                }
            }
        });
        //Toast message created.
        Toast.makeText(this, "Chatroom " + name + " created. Success!", Toast.LENGTH_LONG).show();
    }

    private Location requestCurrentLocation(){
        //The required permission request to get lastLocation
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(LOG_TAG, "LastLocation: " + (lastLocation == null ? "NO LastLocation" : lastLocation.toString()));
        return lastLocation;
    }

    private void addEventListener(){
        //Added GeoQueryEventListener
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                updateChatRoomList(key, location);
                Log.v("MainActivity", String.format("Key %s entered the search area at [%f,%f]",
                        key, location.latitude, location.longitude));
                latestKey = key;
            }
            @Override
            public void onKeyExited(String key) {
                Log.v("MainActivity",String.format("Key %s is no longer in the search area", key));
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {}
            @Override
            public void onGeoQueryReady() {}
            @Override
            public void onGeoQueryError(DatabaseError error) {}
        });
    }

    private void initChatRoom(String chatRoomName){
        Log.v("MainActivity", "InitChatroom called with: " + chatRoomName);
        //mFirebaseDatabase.getReference()
          //      .child("chat_rooms").child(chatRoomName)
            //    .child("chat_messages").push().setValue("Welcome");

        //time in seconds since unix epoch, 1970
        long currentTime = System.currentTimeMillis() / 1000;

        mFirebaseDatabase.getReference()
                .child("chat_rooms").child(chatRoomName)
                .child("timeCreated").setValue(currentTime);

    }
}
