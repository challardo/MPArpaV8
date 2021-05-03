package com.android.mparpa.Receiver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;



public class MusicService extends Service {
    private IBinder mBinder = new MyBinder();
    ActionPlaying actionPlaying;
    @Override
    public void onCreate(){
        super.onCreate();

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionName = intent.getStringExtra("ActionName");

        if(actionName != null){
            switch (actionName){
                case "playPause":
                   // Toast.makeText(this,"next",Toast.LENGTH_SHORT).show();
                    actionPlaying.playBtnClicked();
                    break;

                case "next":
                   // Toast.makeText(this,"next",Toast.LENGTH_SHORT).show();
                    actionPlaying.nextBtnClicked();
                    break;

                case "previous":
                   // Toast.makeText(this,"prev",Toast.LENGTH_SHORT).show();
                    actionPlaying.prevBtnClicked();
                    break;
            }
        }

        return START_STICKY;
    }

    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }
}
