package com.android.mparpa.Adapter;

import android.content.ContentResolver;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.mparpa.Fragments.AlbumFragment;
import com.android.mparpa.Fragments.AllSongFragment;
import com.android.mparpa.Fragments.CurrentSongFragment;
import com.android.mparpa.Fragments.FavSongFragment;
import com.android.mparpa.Fragments.testFragment;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    private ContentResolver contentResolver;
    private String title[] = {"Songs","Albums", "Favorites"};

    public ViewPagerAdapter(FragmentManager fm, ContentResolver contentResolver) {
        super(fm);
        this.contentResolver = contentResolver;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return AllSongFragment.getInstance(position, contentResolver);
            case 1:
                return testFragment.getInstance(position, contentResolver);
            case 2:
                return FavSongFragment.getInstance(position);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
