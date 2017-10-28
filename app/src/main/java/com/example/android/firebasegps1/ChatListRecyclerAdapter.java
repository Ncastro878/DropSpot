package com.example.android.firebasegps1;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.R.id.list;
import static com.example.android.firebasegps1.MainActivity.lastLocation;

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
         ViewHolder vh = new ViewHolder(v);
        //lets try these 2 lines out
        v.setTag(R.id.CLICKABLE, false);
        v.setClickable(false);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mChatTextView.setText(listOfRooms.get(position));
        if(allowedDistance(listOfLocations.get(position))){
            holder.itemView.setTag(R.id.CLICKABLE, true);
            holder.itemView.setClickable(true);
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
}
