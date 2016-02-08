package com.piyushagade.chimey;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class RunningActivity extends Activity implements AdapterView.OnItemSelectedListener {

    TimePicker to, from;
    Button  b_to, b_from, play;
    int from_hr, to_hr;
    String from_min, to_min;
    private Button stop_alarm;
    private  static final String ALARM_FILE = "com.piyushagade.chimey.alarm";
    private SharedPreferences sp_alarm;
    SharedPreferences.Editor ed_alarm;
    Spinner sound_spinner, interval_spinner, behaviour_spinner;
    private String interval_selected, behaviour_selected;
    private int sound_selected;

    Boolean service_on_state;
    private MediaPlayer sound;
    private  boolean can_play = false;
    private int playID;
    private ImageView clock_1, clock_2;
    private RelativeLayout rl_text;
    private TextView tv_min;

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_running);


        clock_1 = (ImageView) findViewById(R.id.clock_1);
        clock_2 = (ImageView) findViewById(R.id.clock_2);
        rl_text = (RelativeLayout) findViewById(R.id.rl_text);
        tv_min =  (TextView) findViewById(R.id.interval);

        stop_alarm = (Button)findViewById(R.id.stop_alarm);

        // Get Shared Preferences Preferences
        sp_alarm = getSharedPreferences(ALARM_FILE, 0);
        ed_alarm = sp_alarm.edit();

        // Set data
        to_hr = sp_alarm.getInt("TO_HR", 0);
        from_hr = sp_alarm.getInt("FROM_HR", 0);
        to_min  = sp_alarm.getString("TO_MIN", "");
        from_min  = sp_alarm.getString("FROM_MIN", "");
        interval_selected = sp_alarm.getString("INTERVAL", "");

        if(to_hr != 0 && from_hr != 0 &&
                !to_min.equals("") && !from_min.equals("")) {
            String st = "";
            if (to_hr >= 12) {
                st = String.valueOf(to_hr - 12) + ":" + to_min + " PM";
            } else {
                st = String.valueOf(to_hr) + ":" + to_min + " AM";
            }

            st = "";
            if (from_hr >= 12) {
                st = String.valueOf(from_hr - 12) + ":" + from_min + " PM";
            } else {
                st = String.valueOf(from_hr) + ":" + from_min + " AM";
            }
        }

        //Animations
        Animation animation_1 = AnimationUtils.loadAnimation(this, R.anim.rotate_clock_1);
        Animation animation_2 = AnimationUtils.loadAnimation(this, R.anim.rotate_clock_2);
        Animation animation_3 = AnimationUtils.loadAnimation(this, R.anim.blink);

        clock_1.startAnimation(animation_1);
        clock_2.startAnimation(animation_2);
        rl_text.startAnimation(animation_3);

        switch (interval_selected) {
            case "Every 30 minutes":
                tv_min.setText("30 minutes");
                break;
            case "Every 1 hour":
                tv_min.setText("1 hour");
                break;
            case "Every 2 hours":
                tv_min.setText("2 hours");
                break;
            case "Every 4 hours":
                tv_min.setText("4 hours");
                break;
            case "Every 8 hours":
                tv_min.setText("8 hours");
                break;
            case "Every 12 hours":
                tv_min.setText("12 hours");
                break;
        }


        //Timepicker Dialog
//        b_to.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                final Dialog to_dialog = new Dialog(RunningActivity.this);
//                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                lp.copyFrom(to_dialog.getWindow().getAttributes());
//
//                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//                to_dialog.setContentView(R.layout.to_dialog);
//                to_dialog.show();
//
//                Window window = to_dialog.getWindow();
//                window.setBackgroundDrawableResource(R.drawable.menu_dialog_back);
//                to_dialog.setCancelable(true);
//                to_dialog.getWindow().setAttributes(lp);
//
//                to=(TimePicker)to_dialog.findViewById(R.id.to);
//                to.setIs24HourView(false);
//
//                TextView set = (TextView) to_dialog.findViewById(R.id.to_set);
//                set.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        NumberFormat formatter = new DecimalFormat("00");
//                        to_hr = to.getCurrentHour();
//                        to_min = formatter.format(to.getCurrentMinute());
//                        String st = "";
//                        if(to_hr>=12){
//                            st = String.valueOf(to_hr - 12) + ":" + to_min + " PM";
//                        }
//                        else{
//                            st = String.valueOf(to_hr) + ":" + to_min + " AM";
//                        }
//
//                        b_to.setText(st);
//                        to_dialog.dismiss();
//
//                        //Update SharedPreferences Alarm
//                        ed_alarm.putInt("TO_HR", to_hr);
//                        ed_alarm.putString("TO_MIN", to_min);
//                    }
//                });
//            }
//
//        });

        //Service Manager
        if(isServiceRunning(TimerService.class)) {
            stop_alarm.setText("Stop Chimey");
        }
        else{
            stop_alarm.setText("Start Chimey");
        }

        stop_alarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(getBaseContext(), TimerService.class));
                finish();

            }
        });

    }

    private void makeSnack(Object t){
        View v = findViewById(R.id.rl_bg);
        Snackbar.make(v, String.valueOf(t), Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void playSound(int ID){
        if(ID!=0 && can_play) {
            sound = MediaPlayer.create(RunningActivity.this, ID);
            sound.setLooping(false);
            sound.start();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
