package com.example.android.firebasegps1;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * This will be the ChatRoom Fragment
 */

public class ChatListFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";
    public ArrayList<String> chatList = new ArrayList<>();
    public ArrayList<Location> locationsList = new ArrayList<>();
    public ArrayList<Long> bdaysList = new ArrayList<>();
    Location roomLocation;
    ValueEventListener eventListener;
    /**
     * RecyclerView variables
     * */
    private int mPage;
    RecyclerView chatListRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    public ChatListRecyclerAdapter mAdapter;

    /**
     * FireBase Variables
     * */
    private FirebaseDatabase chatDataBase;
    private DatabaseReference chatListReference;

    public static ChatListFragment newInstance(int page){
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);

        chatDataBase = FirebaseDatabase.getInstance();
        chatListReference = chatDataBase.getReference().child("chat_rooms");
        //I don't think roomLocation & eventListener serve a purpose anymore
        //potentially delete these
        //TODO: Check if roomLocation & eventListener can be deleted
        roomLocation = new Location("c");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double longitude = (double)dataSnapshot.child("1").getValue();
                double latitude = (double)dataSnapshot.child("0").getValue();
                Log.v("ChatListFragment.java", "roomInAllowed(), latitude is: " + latitude);
                roomLocation.setLatitude(latitude);
                roomLocation.setLongitude(longitude);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
    }
    /**
     * Creates the fragment functions, & inits the recyclerview stuff
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_fragment, container, false);

        addEventListener();
        chatListRecyclerView = (RecyclerView) view.findViewById(R.id.chat_list_recycler_view);
        chatListRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        chatListRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChatListRecyclerAdapter(chatList, locationsList, bdaysList);
        chatListRecyclerView.setAdapter(mAdapter);
        initChatroomListsWithData();

        /**
         * Lets try a different approach for now.
         * Adding onClickListner to RecyclerView. Used this tutorial:
         * https://www.android-examples.com/add-onitemclicklistener-to-recyclerview-in-android/
         */
        //chatListRecyclerView.addOnItemTouchListener(myNewListener);
        return view;
    }

    private void initChatroomListsWithData() {
        chatList.clear();
        locationsList.clear();
        bdaysList.clear();
        chatListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapShot:dataSnapshot.getChildren()){
                    Location newLocation = new Location("c");
                    if(!chatList.contains(snapShot.getKey())) {
                        Log.v("SingleValueEvent", "The key is: " + snapShot.getKey());
                        chatList.add(snapShot.getKey());
                        double latitude = (double)snapShot
                                .child("l").child("0").getValue();
                        double longitude = (double)snapShot
                                .child("l").child("1").getValue();
                        Long birthTime = (Long)snapShot.child("timeCreated").getValue();
                        Log.v("ChatListFragment.java", String.format("Lat:%s && Long:%s", latitude, longitude));
                        newLocation.setLongitude(longitude);
                        newLocation.setLatitude(latitude);
                        locationsList.add(newLocation);
                        bdaysList.add(birthTime);
                    }
                }
                Log.v("ChatListFragment.java", "Size of locationList is: " + locationsList.size());
                mAdapter.updateAdapter(chatList, locationsList, bdaysList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void addEventListener(){
        chatListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> roomList = new ArrayList<>();
                chatList.clear();
                locationsList.clear();
                bdaysList.clear();
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    Location newLocation = new Location("c");
                    if(!chatList.contains(child.getKey())) {
                        chatList.add(child.getKey());
                        double latitude = (double)child
                                .child("l").child("0").getValue();
                        double longitude = (double)child
                                .child("l").child("1").getValue();
                        Long birthTime = (Long) child.child("timeCreated").getValue();
                        Log.v("ChatListFragment.java", String.format("Lat:%s && Long:%s", latitude, longitude));
                        newLocation.setLongitude(longitude);
                        newLocation.setLatitude(latitude);
                        locationsList.add(newLocation);
                        bdaysList.add(birthTime);
                        Log.v("ChatListFragment.java", "Size of locationList2 is: " + locationsList.size());
                        Log.v("ChatListFragment", child.getKey() + " is a key");
                    }
                }
                if (!roomList.isEmpty()){
                    chatList = roomList;
                    mAdapter.updateAdapter(chatList, locationsList, bdaysList);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}