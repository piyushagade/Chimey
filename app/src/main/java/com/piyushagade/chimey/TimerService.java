package com.piyushagade.chimey; /**
 * Created by Piyush Agade on 02-07-2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Calendar;

public class TimerService extends Service {
    private String interval_selected, behaviour_selected;
    private int sound_selected;

    private static final String ALARM_FILE = "com.piyushagade.chimey.alarm";
    private MediaPlayer sound;
    private Handler customHandler;
    private int interval_ms;
    private boolean destroyed = false, dnd = false;
    private int hour, minutes;
    private int apm;
    private int to_hr, from_hr, to_min, from_min, from_apm, to_apm;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get Data from Shared Preferences
        Context ctx = getApplicationContext();
        SharedPreferences alarm_pref = getSharedPreferences(ALARM_FILE, 0);
        behaviour_selected = alarm_pref.getString("BEHAVIOUR", "");
        interval_selected = alarm_pref.getString("INTERVAL", "");
        sound_selected = alarm_pref.getInt("SOUND", 0);

        to_hr = alarm_pref.getInt("TO_HR", 0);
        from_hr = alarm_pref.getInt("FROM_HR", 0);
        to_min = Integer.valueOf(alarm_pref.getString("TO_MIN", ""));
        from_min = Integer.valueOf(alarm_pref.getString("FROM_MIN", ""));





        switch (interval_selected) {
            case "Every 30 minutes":
                interval_ms = 1000 * 60 * 30;
                break;
            case "Every 1 hour":
                interval_ms = 1000 * 60 * 60;
                break;
            case "Every 2 hours":
                interval_ms = 1000 * 60 * 60 * 2;
                break;
            case "Every 4 hours":
                interval_ms = 1000 * 60 * 60 * 4;
                break;
            case "Every 8 hours":
                interval_ms = 1000 * 60 * 60 * 8;
                break;
            case "Every 12 hours":
                interval_ms = 1000 * 60 * 60 * 12;
                break;
        }

        customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread, 0);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private Notification notification;
    private NotificationManager myNotificationManager;
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            //Get value
            SharedPreferences alarm_pref = getSharedPreferences(ALARM_FILE, 0);
            behaviour_selected = alarm_pref.getString("BEHAVIOUR", "");
            interval_selected = alarm_pref.getString("INTERVAL", "");
            sound_selected = alarm_pref.getInt("SOUND", 0);

            to_hr = alarm_pref.getInt("TO_HR", 0);
            from_hr = alarm_pref.getInt("FROM_HR", 0);
            to_min = Integer.valueOf(alarm_pref.getString("TO_MIN", ""));
            from_min = Integer.valueOf(alarm_pref.getString("FROM_MIN", ""));

            //Figure out if its dnd
            Calendar c = Calendar.getInstance();
            minutes = c.get(Calendar.MINUTE);
            hour = c.get(Calendar.HOUR);
            apm = c.get(Calendar.AM_PM);    // 1 = PM, 0 = AM

            if(apm == 1) hour = hour + 12;

            if(from_hr>to_hr){
                to_hr = to_hr + 24;
            }

            long lower = from_hr * 60 + from_min;
            long current = hour * 60 + minutes;
            long upper = to_hr * 60 + to_min;
            if (current > lower && current < upper) dnd = true;
            else dnd = false;

            if (!destroyed && !dnd) {
                myNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification();
                Intent intent = new Intent(getApplicationContext(), TimerService.class);
                PendingIntent contentintent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.sound = Uri.parse("android.resource://com.piyushagade.chimey/" + sound_selected);
                myNotificationManager.notify(1, notification);
            }

            customHandler.postDelayed(this, interval_ms);
        }
    };

    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
    }

}