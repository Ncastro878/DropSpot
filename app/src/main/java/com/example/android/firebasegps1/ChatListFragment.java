package com.example.android.firebasegps1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
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
    /**
     * RecyclerView variables
     * */
    private int mPage;
    RecyclerView chatListRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ChatListRecyclerAdapter mAdapter;

    /**
     * FireBase Variables
     * */
    private FirebaseDatabase chatDataBase;
    private DatabaseReference chatListReference;
    private ChildEventListener chatListChildEventListener;

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
        chatList.add("FakeRoom");

        chatDataBase = FirebaseDatabase.getInstance();
        chatListReference = chatDataBase.getReference().child("chat_rooms");

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
        mAdapter = new ChatListRecyclerAdapter(chatList);
        chatListRecyclerView.setAdapter(mAdapter);
        initializeChatRoomsList();

        /**
         * Adding onClickListner to RecyclerView. Used this tutorial:
         * https://www.android-examples.com/add-onitemclicklistener-to-recyclerview-in-android/
         */
        chatListRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                    //return super.onSingleTapUp(e);
                }
            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View v = rv.findChildViewUnder(e.getX(), e.getY());
                if(v != null && mGestureDetector.onTouchEvent(e)){
                    int recyclerPosition = rv.getChildAdapterPosition(v);
                    Toast.makeText(getActivity(), "ChatRoom is : " + chatList.get(recyclerPosition), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {}
            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
        return view;
    }

    private void initializeChatRoomsList() {
        chatListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapShot:dataSnapshot.getChildren()){
                    if(!chatList.contains(snapShot.getKey())) {
                        Log.v("SingleValueEvent", "The key is: " + snapShot.getKey());
                        chatList.add(snapShot.getKey());
                    }
                }
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
                    if(!chatList.contains(child.getKey())) {
                        chatList.add(child.getKey());
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
