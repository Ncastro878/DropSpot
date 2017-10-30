package com.example.android.firebasegps1;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.MapFragment;

import static android.R.attr.x;

/**
 * Created by nick on 10/13/2017.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter implements MainActivity.UpdateUI {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"Map", "ChatRooms"};
    private Context context;

    /**
     * Lets make fragment variables
     */
    ChatListFragment mChatListFragment;
    MapFragment2 mMapFragment;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context){
        super(fm);
        this.context = context;
    }

    //this will get the certain fragment based on position
    //so either MapFragment or ChatRoomFragment
    @Override
    public Fragment getItem(int position) {
        if(position == 1) {
            mChatListFragment = ChatListFragment.newInstance(position);
            return mChatListFragment;
        }
        mMapFragment = MapFragment2.newInstance();
        return mMapFragment;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public void updateMe() {
        //TODO: RecyclerView/RecyclerAdapter is updating!
        //TODO: GET MAP TO UPDATE CORRECTLY!
        //TODO: Update Map & RecyclerView on backgound thread probably
        mChatListFragment.mAdapter.notifyDataSetChanged();
        if(mMapFragment.m_map != null && MainActivity.lastLocation != null){
            mMapFragment.populateMapPins();
        }

    }
}
