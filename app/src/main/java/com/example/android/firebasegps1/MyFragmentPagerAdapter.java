package com.example.android.firebasegps1;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.MapFragment;

/**
 * Created by nick on 10/13/2017.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"Map", "ChatRoom"};
    private Context context;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context){
        super(fm);
        this.context = context;
    }

    //this will get the certain fragment based on position
    //so either MapFragment or ChatRoomFragment
    @Override
    public Fragment getItem(int position) {
        if(position == 1)
            return PageFragment.newInstance(position);
        return MapFragment2.newInstance();
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }


}
