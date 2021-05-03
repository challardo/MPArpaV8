package com.android.mparpa.Activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.mparpa.Adapter.ViewPagerAdapter;
import com.android.mparpa.DB.FavoritesOperations;
import com.android.mparpa.Fragments.AllSongFragment;
import com.android.mparpa.Fragments.CurrentSongFragment;
import com.android.mparpa.Fragments.FavSongFragment;
import com.android.mparpa.Model.SongsList;
import com.android.mparpa.R;
import com.android.mparpa.Receiver.ActionPlaying;
import com.android.mparpa.Receiver.MusicService;
import com.android.mparpa.Receiver.NotificationReceiver;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static com.android.mparpa.Adapter.AlbumDetailsAdapter.albumFiles;
import static com.android.mparpa.Receiver.ApplicationClass.ACTION_NEXT;
import static com.android.mparpa.Receiver.ApplicationClass.ACTION_PLAY;
import static com.android.mparpa.Receiver.ApplicationClass.ACTION_PREVIOUS;
import static com.android.mparpa.Receiver.ApplicationClass.CHANNEL_ID_2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AllSongFragment.createDataParse, FavSongFragment.createDataParsed, CurrentSongFragment.createDataParsed, ServiceConnection, ActionPlaying {
    public MusicService musicService;
    private Menu menu;
    private ImageView albumCover, albumBottomCover;
    private ImageButton imgBtnPlayPause, imgbtnReplay, imgBtnPrev, imgBtnNext, imgBtnShuffle;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SeekBar seekbarController;
    private SeekBar bottomSeekBar;
    private LinearLayout mDrawerLayout;
    private TextView tvCurrentTime, tvTotalTime;
    private SlidingUpPanelLayout slideUpPanel;
    MediaSessionCompat mediaSessionCompat;
    public static ArrayList<SongsList> songList;

    private int currentPosition;
    private String searchText = "";
    private SongsList currSong;

    private boolean checkFlag = false, repeatFlag = false, playContinueFlag = false, favFlag = true, playlistFlag = false;
    private final int MY_PERMISSION_REQUEST = 100;
    private int allSongLength;
    private boolean shuffleboolean;
    public static MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;
    // estos 2 metodos son del demo anterior
    public static ArrayList<SongsList> albums=new ArrayList<>();


    //objetos del reproductor extra
    public TextView durationPlayed, durationTotal;
    public ImageView shuffle,preview,next,repeat;
    public FloatingActionButton bottomPlaypause;
    public TextView title, artista;
    public static Uri uri;

    //random
    public Random r;
    //voice
    public String oyenteVoice;
    private static final int REQ_CODE_SPEECH_INPUT=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        grantedPermission();
        try {
            getIntentMethod();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bottomListeners();
        mediaSessionCompat = new MediaSessionCompat(this,"PlayerAudio");
    }
public void bottomListeners(){
    preview.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(checkFlag){
                prevBtnClicked();
            }}
    });
    next.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkFlag) {
                nextBtnClicked();
            }
        }
    });
}



    private void init() {
        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        imgbtnReplay = findViewById(R.id.img_btn_replay);
        imgBtnShuffle = findViewById(R.id.img_btn_random);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        FloatingActionButton refreshSongs = findViewById(R.id.btn_refresh);
        seekbarController = findViewById(R.id.seekbar_controller);
        bottomSeekBar = findViewById(R.id.seekBar);
        viewPager = findViewById(R.id.songs_viewpager);
      //  NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        imgBtnPlayPause = findViewById(R.id.img_btn_play);
       // Toolbar toolbar = findViewById(R.id.toolbar);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();

        //Inflar reproductor
        durationPlayed = findViewById(R.id.durationPlayed);
        durationTotal = findViewById(R.id.durationTotal);
        albumCover = findViewById(R.id.iv_music_list);
        albumBottomCover = findViewById(R.id.cover_art);
        bottomPlaypause = findViewById(R.id.play_pause);
        title = findViewById(R.id.song_name);
        artista = findViewById(R.id.song_artist);
        next = findViewById(R.id.id_next);
        preview = findViewById(R.id.id_prev);
        // toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));
//        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        imgbtnReplay.setOnClickListener(this);
        refreshSongs.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnShuffle.setOnClickListener(this);

        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);

        repeat = findViewById(R.id.id_repeat);
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatFlag) {
                    //Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(false);
                    imgbtnReplay.setImageResource(R.drawable.ic_repeat_off);
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                    repeatFlag = false;
                } else {
                  //  Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(true);
                    imgbtnReplay.setImageResource(R.drawable.ic_repeat_on);
                    repeat.setImageResource(R.drawable.ic_repeat_on);
                    repeatFlag = true;
                }
            }
        });
        shuffle = findViewById(R.id.id_shuffle);
       shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shuffleboolean)
                {
                    shuffleboolean=false;
                    imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_off);
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                    //Toast.makeText(this, "Shuffle Removed..", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    shuffleboolean=true;
                    imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                    shuffle.setImageResource(R.drawable.ic_shuffle_on);
                   // Toast.makeText(this, "Shuffle added..", Toast.LENGTH_SHORT).show();
                }
            }
        });



        bottomPlaypause.setOnClickListener(this);

        SlidingUpPanelLayout mPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        mPanelLayout.setDragView(this.findViewById(R.id.dragArea));


    }
    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }


    private void grantedPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        } else {
            setPagerLayout();
        }
    }

    /**
     * permiso para acceder al almacenamiento del dispositivo
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                        setPagerLayout();
                    } else {
                        Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        finish();
                    }
                }
        }
    }



    private void setPagerLayout() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getContentResolver());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                queryText();
                setPagerLayout();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_voice:
                inputVoice();
                Toast.makeText(this, "Command Voice", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_favorites:
                if (checkFlag)
                    if (mediaPlayer != null) {
                        if (favFlag) {
                            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                            item.setIcon(R.drawable.ic_favorite_filled);
                            //Se enciende el corazon por 2 segundos
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    favFlag = false;
                                    item.setIcon(R.drawable.favorite_icon);
                                }
                            },2000);
                            SongsList favList = new SongsList(songList.get(currentPosition).getId(),songList.get(currentPosition).getTitle(),
                                    songList.get(currentPosition).getSubTitle(),songList.get(currentPosition).getDuration(),songList.get(currentPosition).getAlbum(),songList.get(currentPosition).getPath() );
                            FavoritesOperations favoritesOperations = new FavoritesOperations(this);
                            favoritesOperations.addSongFav(favList);
                            setPagerLayout();
                            favFlag = false;
                        } else {
                            item.setIcon(R.drawable.favorite_icon);
                            favFlag = true;
                        }
                    }
                return true;
        }

        return super.onOptionsItemSelected(item);

    }


    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.img_btn_play:
                if (checkFlag) {
                    playBtnClicked();

                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.play_pause:
                if (checkFlag) {
                   playBtnClicked();

                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_refresh:
                Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
                setPagerLayout();
                break;
            case R.id.img_btn_replay:

                if (repeatFlag) {
                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(false);
                    imgbtnReplay.setImageResource(R.drawable.ic_repeat_off);
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                    repeatFlag = false;
                } else {
                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(true);
                    imgbtnReplay.setImageResource(R.drawable.ic_repeat_on);
                    repeat.setImageResource(R.drawable.ic_repeat_on);
                    repeatFlag = true;
                }
                break;
            case R.id.id_repeat:

                if (repeatFlag) {
                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(false);
                    imgbtnReplay.setImageResource(R.drawable.ic_repeat_off);
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                    repeatFlag = false;
                } else {
                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(true);
                    imgbtnReplay.setImageResource(R.drawable.ic_repeat_on);
                    repeat.setImageResource(R.drawable.ic_repeat_on);
                    repeatFlag = true;
                }
                break;
            case R.id.img_btn_previous:
                if (checkFlag) {

                    prevBtnClicked();
                } else {
                    Toast.makeText(this, "Select a Song . .", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.img_btn_next:

                if (checkFlag) {
                   nextBtnClicked();
                }
                else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.img_btn_random:
                if(shuffleboolean)
                {
                    shuffleboolean=false;
                    imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_off);
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                    Toast.makeText(this, "Shuffle Removed..", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    shuffleboolean=true;
                    imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                    shuffle.setImageResource(R.drawable.ic_shuffle_on);
                    Toast.makeText(this, "Shuffle added..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.id_shuffle:
                if(shuffleboolean)
                {
                    shuffleboolean=false;
                    imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_off);
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                    Toast.makeText(this, "Shuffle Removed..", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    shuffleboolean=true;
                    imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                    shuffle.setImageResource(R.drawable.ic_shuffle_on);
                    Toast.makeText(this, "Shuffle added..", Toast.LENGTH_SHORT).show();
                }
                break;
               /* if (!playContinueFlag) {

                }*/

        }

    }

    //animacion cuando cambia de cancion en activity play
    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }
    /**
     * agregar la cancion al reproductor de musica
     *
     * @param name
     * @param artist
     * @param path
     */
//Aqui se mandan todos los parametros que llenan al reproductor (falta la imagen)
    private void attachMusic(String name,String artist, String path) {
        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
        bottomPlaypause.setImageResource(R.drawable.ic_play);
        //setTitle(name);
        title.setText(name);
        artista.setText(artist);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;

        if (art != null)
        {
            bitmap =  BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this,albumBottomCover,bitmap);

        }
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.album_placeholder)
                    .into(albumBottomCover);
        }

        //albumBottomCover.setImageResource(songList.get(currentPosition).getPath());
        //artist.setText(songList.get(currentPosition).getSubTitle());

        menu.getItem(1).setIcon(R.drawable.favorite_icon);
        favFlag = true;

        try {

            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            setControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgBtnPlayPause.setImageResource(R.drawable.play_icon);
                bottomPlaypause.setImageResource(R.drawable.ic_play);

                    if (currentPosition + 1 < songList.size()) {
                        attachMusic(songList.get(currentPosition + 1).getTitle(),songList.get(currentPosition + 1).getSubTitle(), songList.get(currentPosition + 1).getPath());
                        currentPosition += 1;
                        showNotification(R.drawable.ic_pause);
                    } else {
                        Toast.makeText(MainActivity.this, "PlayList Ended", Toast.LENGTH_SHORT).show();
                    }

            }
        });
    }

    /*
     * cambiar controles conforme a la cancion
     */

    private void setControls() {
        seekbarController.setMax(mediaPlayer.getDuration());
        bottomSeekBar.setMax(mediaPlayer.getDuration());

        mediaPlayer.start();
        playCycle();
        checkFlag = true;
        if (mediaPlayer.isPlaying()) {

            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            bottomPlaypause.setImageResource(R.drawable.ic_pause);
            //aqui va el boton de abajo de pausa
            tvTotalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
            durationTotal.setText(getTimeFormatted(mediaPlayer.getDuration()));

        }

        seekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(getTimeFormatted(progress));
                    durationPlayed.setText(getTimeFormatted(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bottomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(getTimeFormatted(progress));
                    durationPlayed.setText(getTimeFormatted(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void playCycle() {
        try {
            seekbarController.setProgress(mediaPlayer.getCurrentPosition());
            bottomSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            durationPlayed.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();

                    }
                };
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }






    @Override
    public void getLength(int length) {
        this.allSongLength = length;
    }

    /**
     * @param name
     * @param artist
     * @param path
     *
     */
    @Override
    public void onDataPass(String name, String artist, String path) {
        Toast.makeText(this, artist, Toast.LENGTH_LONG).show();
        attachMusic(name,artist, path);

    }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {
        this.songList = songList;
        this.currentPosition = position;
        this.playlistFlag = songList.size() == allSongLength;
        this.playContinueFlag = !playlistFlag;
    }

    @Override
    public String queryText() {
        return searchText.toLowerCase();
    }

    @Override
    public SongsList getSong() {
        currentPosition = -1;
        return currSong;
    }

    @Override
    public boolean getPlaylistFlag() {
        return playlistFlag;
    }

    @Override
    public void currentSong(SongsList songsList) {
        this.currSong = songsList;
    }

    @Override
    public int getPosition() {
        return currentPosition;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
    }

    public void getIntentMethod() throws IOException {
        currentPosition=getIntent().getIntExtra("position",-1);
        String sender=getIntent().getStringExtra("sender");
        if(sender!=null && sender.equals("albumDetails"))
        {
            songList=albumFiles;

        }
        else
        {
            //songList=mFiles;
        }

        if(songList!=null)
        {
            title.setText(songList.get(currentPosition).getTitle());
            artista.setText(songList.get(currentPosition).getSubTitle());
            bottomPlaypause.setImageResource(R.drawable.ic_pause);
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);

            uri= Uri.parse(songList.get(currentPosition).getPath());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            byte[] art = retriever.getEmbeddedPicture();
            Bitmap bitmap;

            if (art != null)
            {
                bitmap =  BitmapFactory.decodeByteArray(art,0,art.length);
                ImageAnimation(this,albumBottomCover,bitmap);

            }
            else{
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.album_placeholder)
                        .into(albumBottomCover);
            }
            mediaPlayer.stop();
            try {

                mediaPlayer.reset();
                mediaPlayer.setDataSource(uri.toString());
                mediaPlayer.prepare();
                setControls();

            }
            catch (Exception e){
                e.printStackTrace();
            }

        }


    }

    public void showNotification(int playPauseBtn){
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,0);

        Intent previntent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, previntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseintent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseintent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextintent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextintent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(songList.get(currentPosition).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);

        }
        else{
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.album_placeholder);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(songList.get(currentPosition).getTitle())
                .setContentText(songList.get(currentPosition).getSubTitle())
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder =(MusicService.MyBinder) iBinder;
        musicService = binder.getService();
        musicService.setCallBack(this);
        Log.e("Connected", musicService + "");

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
        Log.e("disconnected", musicService + "");
    }

    public void checkRandomShuffle()
    {
        r=new Random();
        if(shuffleboolean)
        {
            currentPosition=r.nextInt(songList.size() - 1 + 1) + 1;
        }
    }
    @Override
    public void nextBtnClicked() {
        checkRandomShuffle();
        if (currentPosition + 1 < songList.size()) {
            attachMusic(songList.get(currentPosition + 1).getTitle(),songList.get(currentPosition + 1).getSubTitle(), songList.get(currentPosition + 1).getPath());
            currentPosition += 1;
            showNotification(R.drawable.ic_pause);

        } else {
            Toast.makeText(this, "Playlist Ended", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void prevBtnClicked() {
        checkRandomShuffle();
        if (mediaPlayer.getCurrentPosition() > 10) {
            if (currentPosition - 1 > -1) {
                attachMusic(songList.get(currentPosition - 1).getTitle(),songList.get(currentPosition - 1).getSubTitle(), songList.get(currentPosition - 1).getPath());
                currentPosition = currentPosition - 1;
                showNotification(R.drawable.ic_pause);
            } else {
                attachMusic(songList.get(currentPosition).getTitle(),songList.get(currentPosition).getSubTitle(), songList.get(currentPosition).getPath());
                showNotification(R.drawable.ic_pause);
            }
        } else {
            attachMusic(songList.get(currentPosition).getTitle(),songList.get(currentPosition).getSubTitle(), songList.get(currentPosition).getPath());
            showNotification(R.drawable.ic_pause);
        }
    }

    @Override
    public void playBtnClicked() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            imgBtnPlayPause.setImageResource(R.drawable.play_icon);
            bottomPlaypause.setImageResource(R.drawable.ic_play);
            showNotification(R.drawable.ic_play);
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            bottomPlaypause.setImageResource(R.drawable.ic_pause);
            playCycle();
            showNotification(R.drawable.ic_pause);
        }
    }
    //Metodo para la voz---------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:{
                if(resultCode==RESULT_OK&& null!=data)
                {
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    oyenteVoice=result.get(0).toUpperCase();
                    if(oyenteVoice.equals("STOP")||oyenteVoice.equals("PAUSA"))
                        playBtnClicked();
                    else if(oyenteVoice.equals("PLAY"))
                        playBtnClicked();
                        //else if(oyenteVoice.equals("SIGUIENTE"))
                    else if(oyenteVoice.equals("NEXT")||oyenteVoice.equals("SIGUIENTE"))
                        nextBtnClicked();
                        //else if(oyenteVoice.equals("ANTERIOR"))
                    else if(oyenteVoice.equals("BACK")||oyenteVoice.equals("ANTERIOR"))
                        prevBtnClicked();
                    else
                    {
                        String title,art;

                        for(int cont=0;cont<songList.size();cont++)
                        {
                            title=songList.get(cont).getTitle().toUpperCase();
                            art=songList.get(cont).getSubTitle().toUpperCase();
                        //    Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
                            if(oyenteVoice.contains(title)||oyenteVoice.contains(art))
                            {
                                Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
                                currentPosition=cont-1;
                                nextBtnClicked();
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    private void inputVoice() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"English(US)");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());//toma el idioma del telefono
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Dime algo como : Play, Pausa, Siguiente, Anterior");
        try {
            startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        }catch (ActivityNotFoundException e){
        }
    }
}
