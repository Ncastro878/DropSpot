package com.example.android.firebasegps1;

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
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static com.example.android.firebasegps1.MainActivity.lastLocation;
import static java.security.AccessController.getContext;

/**
 * Created by nick on 10/13/2017.
 */

public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListRecyclerAdapter.ViewHolder> {

    int CLICKABLE = 5;
    private ArrayList<String> listOfRooms = new ArrayList<>();
    private ArrayList<Location> listOfLocations = new ArrayList<>();

    public ChatListRecyclerAdapter(ArrayList<String> newList, ArrayList<Location> locations) {
        listOfRooms = newList;
        listOfLocations = locations;
    }

    @Override // appears done
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_row_layout, parent, false);
        v.setOnClickListener(new MyOnClickListener());
         ViewHolder vh = new ViewHolder(v);
        //lets try these 2 lines out
       // v.setTag(R.id.CLICKABLE, false);
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
        }
    }

    private boolean allowedDistance(Location room) {
        Location currentLocation = MainActivity.lastLocation;
        Log.v("ChatListRecyclerAdapter", "CurrLoc lat =" + currentLocation.getLatitude());
        Log.v("ChatListRecyclerAdapter", "roomLoc lat =" + room.getLatitude());
        if(currentLocation.distanceTo(room) < 1690){
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        if(listOfRooms != null)
            return listOfRooms.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView mChatTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mChatTextView = (TextView) itemView.findViewById(R.id.chat_row_text_view);
        }
    }
    public void updateAdapter(ArrayList<String> list){
        if(list != null && list.size() > 0){
            notifyDataSetChanged();
        }
    }

    public class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(view.isClickable() && view != null){
                goToChatRoom(listOfRooms.get((int)view.getTag(R.id.CLICKABLE)), view);
                Toast.makeText(view.getContext(),"Access Granted to "+listOfRooms.get((int)view.getTag(R.id.CLICKABLE)),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToChatRoom(String chatRoomName, View view) {
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Intent intent = new Intent(view.getContext(), ChatRoomTemplate.class);
        intent.putExtra("chatRoomName", chatRoomName);
        intent.putExtra("user_name", userName);
        view.getContext().startActivity(intent);
    }
}
