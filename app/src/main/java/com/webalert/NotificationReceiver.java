package com.webalert;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.MODE_PRIVATE;

public class NotificationReceiver extends BroadcastReceiver {
    private SharedPreferences sharedPreferences;
    private boolean isStart;
    private NotificationService notificationService;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NR", "OnReceive 호출");
        notificationService=new NotificationService();
        notificationService.stopThread();

        sharedPreferences = context.getSharedPreferences("isServiceStart", MODE_PRIVATE);
        isStart=intent.getBooleanExtra("isServiceStart", false);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("isServiceStart", isStart);
        editor.commit();
        Intent serviceIntent=new Intent(context, NotificationService.class);
        context.stopService(serviceIntent);
        NotificationManagerCompat.from(context).cancel(1);

    }
}
