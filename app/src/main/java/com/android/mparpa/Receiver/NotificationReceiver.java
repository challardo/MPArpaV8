package com.android.mparpa.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.mparpa.Activity.MainActivity;

import static com.android.mparpa.Receiver.ApplicationClass.ACTION_NEXT;
import static com.android.mparpa.Receiver.ApplicationClass.ACTION_PLAY;
import static com.android.mparpa.Receiver.ApplicationClass.ACTION_PREVIOUS;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();

        Intent serviceIntent= new Intent(context, MusicService.class);
        if(actionName != null){
            switch (actionName){
                case ACTION_PLAY:
                   // Toast.makeText(context,"Play",Toast.LENGTH_SHORT).show();
                    serviceIntent.putExtra("ActionName","playPause");
                    context.startService(serviceIntent);
                    break;

                case ACTION_NEXT:
                   // Toast.makeText(context,"next",Toast.LENGTH_SHORT).show();
                    serviceIntent.putExtra("ActionName","next");
                    context.startService(serviceIntent);

                    break;

                case ACTION_PREVIOUS:
                  // Toast.makeText(context,"prev",Toast.LENGTH_SHORT).show();
                    serviceIntent.putExtra("ActionName","previous");
                    context.startService(serviceIntent);

                    break;
            }

        }
    }
}

