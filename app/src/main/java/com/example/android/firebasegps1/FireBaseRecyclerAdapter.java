package com.example.android.firebasegps1;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by nick on 7/6/2017.
 */

class FireBaseRecyclerAdapter extends RecyclerView.Adapter<FireBaseRecyclerAdapter.ViewHolder> {

    private ArrayList<String> messagesList = new ArrayList<>();
    private ArrayList<String> usernamesList= new ArrayList<>();
    private ArrayList<String> imageUrlList = new ArrayList<>();

    /**
     * This is the ViewHolder inner class and its constructor
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;
        public TextView mSmallTextView;
        public ImageView mUserImageTextView;
        public ViewHolder(LinearLayout v){
            super(v);
            mTextView = (TextView) v.findViewById(R.id.chat_text_view);
            mSmallTextView = (TextView) v.findViewById(R.id.real_username_text_view);
            mUserImageTextView = (ImageView) v.findViewById(R.id.user_image_view);
        }
    }

    /**
     * This is the rest of the methods and constructors for MyAdapter
     * @param dataSet is an array of strings
     */
    public FireBaseRecyclerAdapter(ArrayList<String> dataSet, ArrayList<String> nameList, ArrayList imgs) {
        messagesList = dataSet;
        usernamesList = nameList;
        imageUrlList = imgs;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FireBaseRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FireBaseRecyclerAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(messagesList.get(position));
        holder.mSmallTextView.setText(usernamesList.get(position));
        holder.mSmallTextView.append(": ");
        if (imageUrlList.get(position) != null) {
            Picasso.with(holder.mUserImageTextView.getContext())
                .load(imageUrlList.get(position)).into(holder.mUserImageTextView);
        }
     }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(messagesList != null) {
            return messagesList.size();
        }
        return 0;
    }

    public void updateAdapter(ArrayList<String> namesList, ArrayList<String> msgList, ArrayList<String> imgs){
        if(namesList != null && namesList.size() > 0 && msgList != null && msgList.size() > 0){
            notifyDataSetChanged();
        }
    }
}
