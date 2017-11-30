package com.example.android.firebasegps1;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    public static final int RC_SIGNIN = 1 ;

    private TextView titleTextView;
    private EditText mNewMessageEditText;
    private FloatingActionButton mFloatingActionButton;

    private String mUserName;

    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mNewRequestEventListener;
    public FirebaseUser user;
    private DatabaseReference chatRoomReference;
    private ChildEventListener chatRoomListner;

    private ArrayList<String> receivedMsgs;
    private ArrayList<String> receivedUserNames;
    private ArrayList<String> receivedImageUrls;
    private String chatRoomName;

    private RecyclerView mRecyclerView;
    private ChatRoomAdapter mFireBaseAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room_template);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        mNewMessageEditText = (EditText) findViewById(R.id.edit_text_view);
        titleTextView = (TextView) findViewById(R.id.title_text_view);

        /**
         * Firebase Authorizations & References
         */
        mFirebaseAuth = FirebaseAuth.getInstance();
        user = mFirebaseAuth.getCurrentUser();
        receivedMsgs = new ArrayList<>();
        receivedUserNames = new ArrayList<>();
        receivedImageUrls = new ArrayList<>();

        //received intent data
        Intent intentReceived = getIntent();
        if(intentReceived.hasExtra("chatRoomName")){
            chatRoomName = intentReceived.getStringExtra("chatRoomName");
            Log.v("ChatRoomTemplate", chatRoomName + " is the chatroom name.");
            titleTextView.setText("Welcome to " + chatRoomName + " Chatroom");
        }
        if(intentReceived.hasExtra("user_name")) {
            mUserName = intentReceived.getStringExtra("user_name");
            Log.v("UserName Received, ", mUserName + " is the users name.");
        }

        /**
         * This inits the RecyclerView variables
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mFireBaseAdapter = new ChatRoomAdapter(receivedMsgs, receivedUserNames, receivedImageUrls);
        mRecyclerView.setAdapter(mFireBaseAdapter);

        /**
         * Let's initialize the chatroom references
         * so we can get the chat messages inside it.
         */
        chatRoomReference = FirebaseDatabase.getInstance().getReference()
                .child("chat_rooms").child(chatRoomName).child("chat_messages");

        chatRoomReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessageObject msgObj = dataSnapshot.getValue(ChatMessageObject.class);
                receivedUserNames.add(msgObj.getUsername());
                receivedMsgs.add(msgObj.getMessage());
                receivedImageUrls.add(msgObj.getUserImageUrl());
                Log.v("ChatRoomActivity.java", msgObj.toString());

                mFireBaseAdapter.updateAdapter(receivedUserNames, receivedMsgs, receivedImageUrls);
                mRecyclerView.smoothScrollToPosition(mFireBaseAdapter.getItemCount());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessage = mNewMessageEditText.getText().toString();
                if( newMessage != "" && newMessage != null){
                    //Map<String, String> map = new HashMap<String, String>() {};
                    //map.put(mUserName, newMessage);
                    //chatRoomReference.push().child(mUserName).setValue(newMessage);
                    ChatMessageObject msgObject = new ChatMessageObject();
                    msgObject.setMessage(newMessage);
                    msgObject.setUsername(mUserName);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.getPhotoUrl() != null)
                        msgObject.setUserImageUrl(user.getPhotoUrl().toString());
                    chatRoomReference.push().setValue(msgObject);

                    mNewMessageEditText.setText("");
                    Toast.makeText(ChatRoomActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
