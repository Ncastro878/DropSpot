package com.example.android.firebasegps1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
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
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.firebase.ui.auth.AuthUI.*;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "Nick'Firebase App";
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 20;

    public String mDisplayName;
    private final String ANONYMOUS = "anonymous";

    public static final int RC_SIGNIN = 1;
    private static final int RC_PHOTO_PICKER = 4;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private DatabaseReference mUsernamesReference;
    private DatabaseReference mGeoFireChatroomReference;
    GeoFire mChatroomGeoFire;

    /**
     * Lets test this interface callback out
     */
    public interface UpdateUI {
        void updateMe();
    }

    /**
     * uses this tutorial for Location Updates:
     * https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
     */
    private LocationRequest mLocationRequest;
    static Location lastLocation;

    EditText dialogEditText;
    Button changeLocationButton;
    FloatingActionButton mFloatingActionButton;

    UpdateUI pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        startLocationUpdates();
        initViewPager();
        initFireBaseVariables();
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        changeLocationButton = (Button) findViewById(R.id.change_location);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //Lets connect the PendingFR-Reference
                    mDisplayName = user.getDisplayName();
                    // We add user to users list & usernames list
                    mUsernamesReference.child(mDisplayName).setValue(user.getUid());
                } else {
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

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createRoomDialog();
            }
        });
        //Test button to change location to see how the app reacts.
        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location mLocation = new Location("");
                mLocation.setLatitude(40.7128);
                mLocation.setLongitude(74.0060);
                onLocationChanged(mLocation);
            }
        });
    }

    private void initFireBaseVariables() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
        mUsernamesReference = mFirebaseDatabase.getReference().child("usernames");

        //GeoFire Variables
        mGeoFireChatroomReference = FirebaseDatabase.getInstance().getReference("chat_rooms");
        mChatroomGeoFire = new GeoFire(mGeoFireChatroomReference);
    }

    private void initViewPager() {
        //Get the viewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter((PagerAdapter) pagerAdapter);

        //Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(10000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void onLocationChanged(Location newLastLocation) {
        lastLocation = newLastLocation;
        pagerAdapter.updateMe();
    }

    public void requestCurrentLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = getFusedLocationProviderClient(this);
        //Try-Catch block for SecurityException if Location Permission not granted.
        //Should maybe disable buttons to prevent this exception. Do later.
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                onLocationChanged(location);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("MainActivity.java", "Error occured: " + e);
                    e.printStackTrace();
                }
            });
        }catch(SecurityException e){
            Log.v(LOG_TAG, e.toString());
        }
    }

    private void createRoomDialog() {
        //Make the dialogBuilder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_layout, null);

        //lets init the editText here 1st, then in the builder setup add characteristics
        builder.setView(v)
                .setMessage("Create ChatRoom")
                .setTitle("Make a new room")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String chatName;
                        dialogEditText = (EditText) v.findViewById(R.id.chat_room_edit_text);
                        if(dialogEditText.getText().toString() != "" ) {
                            chatName = dialogEditText.getText().toString();
                            String toastMsg = chatName + " was created successfully.";
                            Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                            createNewChatRoom(chatName);
                            dropPinOnMap(chatName);
                        }else{
                            Toast.makeText(MainActivity.this, "enter a name", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Creation Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }

    private void dropPinOnMap(String roomName) {
        requestCurrentLocation();
        Location currentLocation = lastLocation;
        double longitude = currentLocation.getLongitude();
        double latitude = currentLocation.getLatitude();
        MarkerOptions newMarker = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(roomName)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.new_map_pin48));
        MapFragment mMapFragment = (MapFragment) getSupportFragmentManager().getFragments().get(1);
        mMapFragment.m_map.addMarker(newMarker);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGNIN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "You have successfully signed in.", Toast.LENGTH_SHORT).show();
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Failure to sign in. Sorry.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            //i.e. content://local_images/foo/4, so the "4".
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri userPhotoDownloadUrl = taskSnapshot.getDownloadUrl();
                    updateUsersPhoto(userPhotoDownloadUrl);
                }
            });
        }
    }

    private void updateUsersPhoto(Uri downloadedPhotoUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadedPhotoUri)
                .build();
        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.v("MainActivity.java", "PictureUrl upload was successful");
                    Log.v("MainActivity.java", "Url is: " + task.getResult().toString());
                }
            }

        });
    }

    private void onSignedOutCleanup() {
        mDisplayName = ANONYMOUS;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {super.onStop();}

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
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    //This code is here in case I discover I need to disable features when not granted
    //location permission. For instance, denied permission causes the app to crash when certain
    //events happen. Would be easier to just disable those features(i.e. buttons,etc), to
    //prevent the annoyance of crashing.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0){
                    //permission granted
                    //do nothing.
                }else{
                    //permission denied
                    //disable certain features if needed.
                }
                return;
            }
        }
    }

    private void createNewChatRoom(final String  name) {
        requestCurrentLocation();
        Location currentLocation = lastLocation;
        double currentLat = currentLocation.getLatitude();
        double currentLong = currentLocation.getLongitude();
        mChatroomGeoFire.setLocation(name, new GeoLocation(currentLat, currentLong), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(error != null){
                    Log.e("MainActivity", "There was an error saving the location to GeoFire: " + error);
                }else{
                    Log.v("MainActivity", "Location saved on server successfully!");
                    addChatRoomToFirebase(name);
                }
            }
        });
        Toast.makeText(this, "Chatroom " + name + " created. Success!", Toast.LENGTH_LONG).show();
    }

    private void addChatRoomToFirebase(String chatRoomName){
        Log.v("MainActivity", "InitChatroom called with: " + chatRoomName);
        //time in seconds since unix epoch, 1970
        long currentUnixTime = System.currentTimeMillis() / 1000;
        mFirebaseDatabase.getReference()
                .child("chat_rooms").child(chatRoomName)
                .child("timeCreated").setValue(currentUnixTime);
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
            case R.id.upload_photo_menu_option:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent,"Complete action using"), RC_PHOTO_PICKER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
