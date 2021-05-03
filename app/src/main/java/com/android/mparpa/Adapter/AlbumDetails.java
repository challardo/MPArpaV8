package com.android.mparpa.Adapter;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.android.mparpa.Activity.MainActivity.mediaPlayer;
import static com.android.mparpa.Activity.MainActivity.songList;

import com.android.mparpa.Model.SongsList;
import com.android.mparpa.MusicFiles;
import com.android.mparpa.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;



public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<SongsList> albumSongs=new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer.stop();
        setContentView(R.layout.activity_album_details);
        recyclerView=findViewById(R.id.recyclerView);
        albumPhoto=findViewById(R.id.albumPhoto);
        albumName=getIntent().getStringExtra("albumName");
        int j=0;

        for(int i=0;i<songList.size();i++)
        {
            if(albumName.equals(songList.get(i).getAlbum()))
            {
                albumSongs.add(j,songList.get(i));
                j++;

            }
        }


        byte[] image=getAlbumArt(albumSongs.get(0).getPath());
        if(image!=null)
        {
            Glide.with(this)
                    .load(image)
                    .into(albumPhoto);
        }
        else
        {
            Glide.with(this)
                    .load(R.drawable.album_placeholder)
                    .into(albumPhoto);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!(albumSongs.size()<1))
        {
            albumDetailsAdapter=new AlbumDetailsAdapter(this,albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL,false));
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