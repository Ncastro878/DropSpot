package com.example.android.firebasegps1;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.os.Build.VERSION_CODES.M;
import static com.example.android.firebasegps1.MainActivity.lastLocation;
import static java.security.AccessController.getContext;

/**
 * Created by nick on 10/13/2017.
 */

public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListRecyclerAdapter.ViewHolder> {

    private ArrayList<String> listOfRooms = new ArrayList<>();
    private ArrayList<Location> listOfLocations = new ArrayList<>();
    private ArrayList<Long> listOfBDays = new ArrayList<>();

    private static final long ONE_DAY_IN_SECONDS = 86400;

    public ChatListRecyclerAdapter(ArrayList<String> newList, ArrayList<Location> locations,
                                   ArrayList<Long> bdays) {
        listOfRooms = newList;
        listOfLocations = locations;
        listOfBDays = bdays;
    }

    @Override // appears done
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_row_layout, parent, false);
        v.setOnClickListener(new MyOnClickListener());
         ViewHolder vh = new ViewHolder(v);
        v.setClickable(false);
        v.setBackgroundResource(R.color.reddish);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mChatTextView.setText(listOfRooms.get(position));
        holder.itemView.setTag(R.id.CLICKABLE, position);
        if(allowedDistance(listOfLocations.get(position))){
            //holder.itemView.setTag(R.id.CLICKABLE, true);
            holder.itemView.setClickable(true);
            holder.itemView.setBackgroundResource(R.color.greenish);
        }else{
            holder.itemView.setClickable(false);
            holder.itemView.setBackgroundResource(R.color.reddish);
        }
    }

    private boolean allowedDistance(Location room) {
        try{
        Location currentLocation = MainActivity.lastLocation;
        Log.v("ChatListRecyclerAdapter", "CurrLoc lat =" + currentLocation.getLatitude());
        Log.v("ChatListRecyclerAdapter", "roomLoc lat =" + room.getLatitude());
        if(currentLocation.distanceTo(room) < 1690){
            return true;
        }
        return false;
        }
        catch (Exception e){
            Log.e("ChatListRecyclerAdapter", "ERROR ERROR : " + e);
        }
        return false;
    }

    @Override
    public int getItemCount() {
        if(listOfRooms != null)
            return listOfRooms.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView mChatTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mChatTextView = (TextView) itemView.findViewById(R.id.chat_row_text_view);
        }
    }
    public void updateAdapter(ArrayList<String> chatList, ArrayList<Location> locationList,
                                ArrayList<Long> bdays){
        if(chatList != null && chatList.size() > 0 && locationList != null){
            listOfBDays = bdays;
            listOfRooms = chatList;
            listOfLocations = locationList;
            notifyDataSetChanged();
        }
        //test
        //notifyDataSetChanged();
    }

    public class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            String roomName = listOfRooms.get((int)view.getTag(R.id.CLICKABLE));
            Long timeCreatedMinusNow = getChatRoomTimeDifference(roomName);
            if(view.isClickable() && view != null){
                if(timeCreatedMinusNow > ONE_DAY_IN_SECONDS){
                    Toast.makeText(view.getContext(), "This room has expired. Sorry", Toast.LENGTH_SHORT).show();
                    //TODO: DELETE THIS ROOM - make smoother
                    view.setBackgroundResource(R.color.reddish);
                    view.setClickable(false);
                    FirebaseDatabase.getInstance().getReference()
                            .child("chat_rooms").child(roomName).removeValue();
                    listOfBDays.remove(listOfBDays.get(listOfRooms.indexOf(roomName)));
                    listOfRooms.remove(roomName);
                    notifyDataSetChanged();
                }else {
                    goToChatRoom(roomName, view);
                    Toast.makeText(view.getContext(), "Access Granted to " + roomName,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Long getChatRoomTimeDifference(String roomName) {
        int position = listOfRooms.indexOf(roomName);
        Long roomBday = listOfBDays.get(position);
        Long currentTime = System.currentTimeMillis()/1000;
        Log.v("ChatListRecyclerAdapter", "Time difference is " + (currentTime-roomBday) );
        return currentTime - roomBday;
    }

    private void goToChatRoom(String chatRoomName, View view) {
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Intent intent = new Intent(view.getContext(), ChatRoomTemplate.class);
        intent.putExtra("chatRoomName", chatRoomName);
        intent.putExtra("user_name", userName);
        view.getContext().startActivity(intent);
    }
}
