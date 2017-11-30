package com.example.android.firebasegps1;

import android.content.Context;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by nick on 10/13/2017.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter implements MainActivity.UpdateUI {
    private final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"Map", "ChatRooms"};
    private Context context;

    private ChatListFragment mChatListFragment;
    private MapFragment mMapFragment;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context){
        super(fm);
        this.context = context;
    }

    //this will get a certain fragment based on position
    //so either MapFragment or ChatRoomFragment
    @Override
    public Fragment getItem(int position) {
        if(position == 1) {
            mChatListFragment = ChatListFragment.newInstance(position);
            return mChatListFragment;
        }
        mMapFragment = MapFragment.newInstance();
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
    public void updateMe( ) {
        //TODO: Update Map & RecyclerView on backgound thread probably
        mChatListFragment.mAdapter.notifyDataSetChanged();
        if(mMapFragment.m_map != null && MainActivity.lastLocation != null){
            mMapFragment.populateMapPins();
        }
    }

}
