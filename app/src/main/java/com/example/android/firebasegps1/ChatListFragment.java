package com.example.android.firebasegps1;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.logging.Handler;

/**
 * This will be the ChatRoom Fragment
 */

public class ChatListFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";
    public ArrayList<String> chatList = new ArrayList<>();
    public ArrayList<Location> locationsList = new ArrayList<>();
    Location roomLocation;
    ValueEventListener eventListener;
    /**
     * RecyclerView variables
     * */
    private int mPage;
    RecyclerView chatListRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ChatListRecyclerAdapter mAdapter;
    RecyclerView.OnItemTouchListener myNewListener;

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
        //chatList.add("FakeRoom");

        chatDataBase = FirebaseDatabase.getInstance();
        chatListReference = chatDataBase.getReference().child("chat_rooms");
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

        // may be unnecessary
        myNewListener = new RecyclerView.OnItemTouchListener() {
            GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                    //return super.onSingleTapUp(e);
                }
            });
            @Override
            public boolean onInterceptTouchEvent(final RecyclerView rv, MotionEvent e) {
                View v = rv.findChildViewUnder(e.getX(), e.getY());
                //Lets set a delay making the RecyclerView unclickable for 5 seconds, so firebase can catchup.
                int recyclerPosition = rv.getChildAdapterPosition(v);
                Log.v("ChatListFragment.java", "How many times is intercept called?");
                if(v != null && mGestureDetector.onTouchEvent(e) && v.isClickable()){
                    goToChatRoom(chatList.get(recyclerPosition));
                    Toast.makeText(rv.getContext(),"Access Granted to "+chatList.get(recyclerPosition),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }else {
                    Toast.makeText(rv.getContext(), "Access Denied to " + chatList.get(recyclerPosition),
                            Toast.LENGTH_SHORT).show();
                }
                return false;
            }
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {}
            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
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
        mAdapter = new ChatListRecyclerAdapter(chatList, locationsList);
        chatListRecyclerView.setAdapter(mAdapter);
        initializeChatRoomsList();

        /**
         * Lets try a different approach for now.
         * Adding onClickListner to RecyclerView. Used this tutorial:
         * https://www.android-examples.com/add-onitemclicklistener-to-recyclerview-in-android/
         */
        //chatListRecyclerView.addOnItemTouchListener(myNewListener);
        return view;
    }

    private void goToChatRoom(String chatRoomName) {
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Intent intent = new Intent(getContext(), ChatRoomTemplate.class);
        intent.putExtra("chatRoomName", chatRoomName);
        intent.putExtra("user_name", userName);
        startActivity(intent);
    }

    private void initializeChatRoomsList() {
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
                        Log.v("ChatListFragment.java", String.format("Lat:%s && Long:%s", latitude, longitude));
                        newLocation.setLongitude(longitude);
                        newLocation.setLatitude(latitude);
                        locationsList.add(newLocation);
                    }
                }
                Log.v("ChatListFragment.java", "Size of locationList is: " + locationsList.size());
                mAdapter.updateAdapter(chatList);
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
                for(DataSnapshot child:dataSnapshot.getChildren()){
                    Location newLocation = new Location("c");
                    if(!chatList.contains(child.getKey())) {
                        chatList.add(child.getKey());
                        double latitude = (double)child
                                .child("l").child("0").getValue();
                        double longitude = (double)child
                                .child("l").child("1").getValue();
                        Log.v("ChatListFragment.java", String.format("Lat:%s && Long:%s", latitude, longitude));
                        newLocation.setLongitude(longitude);
                        newLocation.setLatitude(latitude);
                        locationsList.add(newLocation);
                        Log.v("ChatListFragment.java", "Size of locationList2 is: " + locationsList.size());
                        Log.v("ChatListFragment", child.getKey() + " is a key");
                    }
                }
                if (!roomList.isEmpty()){
                    chatList = roomList;
                    mAdapter.updateAdapter(chatList);
                    //scroll down next
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
