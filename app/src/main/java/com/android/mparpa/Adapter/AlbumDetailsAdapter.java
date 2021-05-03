package com.android.mparpa.Adapter;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.mparpa.Activity.MainActivity;
import com.android.mparpa.Model.SongsList;
import com.android.mparpa.MusicFiles;
import com.android.mparpa.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<com.android.mparpa.Adapter.AlbumDetailsAdapter.MyHolder> {
    private Context mContext;
    public static ArrayList<SongsList> albumFiles;
    View view;

    public AlbumDetailsAdapter(Context mContext, ArrayList<SongsList> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(mContext).inflate(R.layout.playlist_items,parent,false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        holder.album_name.setText(albumFiles.get(position).getTitle());
        holder.album_artist.setText(albumFiles.get(position).getSubTitle());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if(image !=null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_image);
        }
        else{
            Glide.with(mContext)
                    .load(R.drawable.album_placeholder)
                    .into(holder.album_image);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, MainActivity.class);
                intent.putExtra("sender","albumDetails");
                intent.putExtra("position",position);

                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }


    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name, album_artist;
        public MyHolder(@NonNull View itemView) {
            super(itemView);


           album_image=itemView.findViewById(R.id.iv_music_list);
            album_name=itemView.findViewById(R.id.tv_music_name);
            album_artist=itemView.findViewById(R.id.tv_music_subtitle);

        }
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}

