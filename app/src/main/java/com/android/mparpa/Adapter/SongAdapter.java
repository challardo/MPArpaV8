package com.android.mparpa.Adapter;

import android.content.Context;

import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.mparpa.Model.SongsList;
import com.android.mparpa.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<SongsList> implements Filterable {

    private Context mContext;
    private ArrayList<SongsList> songList = new ArrayList<>();

    public SongAdapter(Context mContext, ArrayList<SongsList> songList) {
        super(mContext, 0, songList);
        this.mContext = mContext;
        this.songList = songList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.playlist_items, parent, false);
        }
        SongsList currentSong = songList.get(position);
        TextView tvTitle = listItem.findViewById(R.id.tv_music_name);
        TextView tvSubtitle = listItem.findViewById(R.id.tv_music_subtitle);

        ImageView coverArt = listItem.findViewById(R.id.iv_music_list);

       byte[] image = getAlbumArt(songList.get(position).getPath());
        if(image !=null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(coverArt);
        }
        else{
            Glide.with(mContext)
                    .load(R.drawable.album_placeholder)
                    .into(coverArt);
        }




        tvTitle.setText(currentSong.getTitle());
        tvSubtitle.setText(currentSong.getSubTitle());
        return listItem;
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
