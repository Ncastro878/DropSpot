package com.example.android.firebasegps1;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by nick on 10/13/2017.
 */

public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListRecyclerAdapter.ViewHolder> {

    private ArrayList<String> listOfRooms = new ArrayList<>();

    public ChatListRecyclerAdapter(ArrayList<String> newList){
        listOfRooms = newList;
    }

    @Override // appears done
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_row_layout, parent, false);
         ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mChatTextView.setText(listOfRooms.get(position));
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
