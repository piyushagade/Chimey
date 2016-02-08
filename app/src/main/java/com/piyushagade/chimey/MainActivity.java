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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    TimePicker to, from;
    Button  b_to, b_from, play;
    int from_hr, to_hr;
    String from_min, to_min;
    private Button set_alarm;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);



        //Buttons
        b_from = (Button)findViewById(R.id.from);
        b_to = (Button)findViewById(R.id.to);
        set_alarm = (Button)findViewById(R.id.set_alarm);
        play = (Button)findViewById(R.id.play);

        // Get Shared Preferences Preferences
        sp_alarm = getSharedPreferences(ALARM_FILE, 0);
        ed_alarm = sp_alarm.edit();

        // Set data
        to_hr = sp_alarm.getInt("TO_HR", 0);
        from_hr = sp_alarm.getInt("FROM_HR", 0);
        to_min  = sp_alarm.getString("TO_MIN", "");
        from_min  = sp_alarm.getString("FROM_MIN", "");
        interval_selected = sp_alarm.getString("INTERVAL", "");
        behaviour_selected = sp_alarm.getString("BEHAVIOUR", "");
        sound_selected = sp_alarm.getInt("SOUND", 0);

//        switch (interval_selected) {
//            case "Every 30 minutes":
//                interval_spinner.setSelection(0);
//                break;
//            case "Every 1 hour":
//                interval_spinner.setSelection(1);
//                break;
//            case "Every 2 hours":
//                interval_spinner.setSelection(2);
//                break;
//            case "Every 4 hours":
//                interval_spinner.setSelection(3);
//                break;
//            case "Every 8 hours":
//                interval_spinner.setSelection(4);
//                break;
//            case "Every 12 hours":
//                interval_spinner.setSelection(5);
//                break;
//        }

//        switch (behaviour_selected) {
//            case "Sound once":
//                behaviour_spinner.setSelection(0);
//                break;
//            case "According to current hour":
//                behaviour_spinner.setSelection(1);
//                break;
//            case "Binary beep":
//                behaviour_spinner.setSelection(2);
//                break;
//        }

//        switch (sound_selected) {
//            case R.raw.chimes: sound_spinner.setSelection(0);
//                break;
////            case R.raw.chirp: sound_spinner.setSelection(1);
////                break;
//            case R.raw.beep: sound_spinner.setSelection(2);
//                break;
////            case R.raw.data: sound_spinner.setSelection(3);
////                break;
////            case R.raw.r2d2: sound_spinner.setSelection(4);
////                break;
////            case R.raw.darth: sound_spinner.setSelection(5);
////                break;
//
//        }

        if(to_hr != 0 && from_hr != 0 &&
                !to_min.equals("") && !from_min.equals("")) {
            String st = "";
            if (to_hr >= 12) {
                st = String.valueOf(to_hr - 12) + ":" + to_min + " PM";
            } else {
                st = String.valueOf(to_hr) + ":" + to_min + " AM";
            }
            b_to.setText(st);

            st = "";
            if (from_hr >= 12) {
                st = String.valueOf(from_hr - 12) + ":" + from_min + " PM";
            } else {
                st = String.valueOf(from_hr) + ":" + from_min + " AM";
            }
            b_from.setText(st);
        }

        //Timepicker Dialog
        b_to.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog to_dialog = new Dialog(MainActivity.this);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(to_dialog.getWindow().getAttributes());

                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                to_dialog.setContentView(R.layout.to_dialog);
                to_dialog.show();

                Window window = to_dialog.getWindow();
                window.setBackgroundDrawableResource(R.drawable.menu_dialog_back);
                to_dialog.setCancelable(true);
                to_dialog.getWindow().setAttributes(lp);

                to=(TimePicker)to_dialog.findViewById(R.id.to);
                to.setIs24HourView(false);

                //Set
                TextView set = (TextView) to_dialog.findViewById(R.id.to_set);
                set.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        NumberFormat formatter = new DecimalFormat("00");
                        to_hr = to.getCurrentHour();
                        to_min = formatter.format(to.getCurrentMinute());
                        String st = "";
                        if (to_hr >= 12) {
                            st = String.valueOf(to_hr - 12) + ":" + to_min + " PM";
                        } else {
                            st = String.valueOf(to_hr) + ":" + to_min + " AM";
                        }

                        b_to.setText(st);
                        to_dialog.dismiss();

                        //Update SharedPreferences Alarm
                        ed_alarm.putInt("TO_HR", to_hr);
                        ed_alarm.putString("TO_MIN", to_min);
                    }
                });

                //Cancel
                TextView cancel = (TextView) to_dialog.findViewById(R.id.to_back);
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        to_dialog.dismiss();
                    }
                });
            }

        });

        //Service Manager
        if(isServiceRunning(TimerService.class)) {
            Intent i = new Intent(MainActivity.this, RunningActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            finish();
        }
        else{
            set_alarm.setText("Start Chimey");
        }

        //Onclick Listeners
        b_from.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Dialog from_dialog = new Dialog(MainActivity.this);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(from_dialog.getWindow().getAttributes());

                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                from_dialog.setContentView(R.layout.from_dialog);
                from_dialog.show();

                Window window = from_dialog.getWindow();
                window.setBackgroundDrawableResource(R.drawable.menu_dialog_back);
                from_dialog.setCancelable(true);
                from_dialog.getWindow().setAttributes(lp);

                // Time Picker
                from=(TimePicker)from_dialog.findViewById(R.id.from);
                from.setIs24HourView(false);

                //Set
                TextView set = (TextView) from_dialog.findViewById(R.id.from_set);
                set.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        NumberFormat formatter = new DecimalFormat("00");
                        from_hr = from.getCurrentHour();
                        from_min = formatter.format(from.getCurrentMinute());
                        String st = "";
                        if(from_hr>=12){
                            st = String.valueOf(from_hr - 12) + ":" + from_min + " PM";
                        }
                        else{
                            st = String.valueOf(from_hr) + ":" + from_min + " AM";
                        }

                        b_from.setText(st);
                        from_dialog.dismiss();

                        //Update SharedPreferences Alarm
                        ed_alarm.putInt("FROM_HR", from_hr);
                        ed_alarm.putString("FROM_MIN", from_min);
                    }
                });

                //Cancel
                TextView cancel = (TextView) from_dialog.findViewById(R.id.from_back);
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        from_dialog.dismiss();
                    }
                });
            }

        });

        set_alarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Get values
                switch((int)sound_spinner.getSelectedItemId()){
                    case 0: sound_selected = R.raw.chimes; break;
                    case 1: sound_selected = R.raw.chirp; break;
                    case 2: sound_selected = R.raw.beep; break;
                    case 3: sound_selected = R.raw.data; break;
                    case 4: sound_selected = R.raw.r2d2; break;
                    case 5: sound_selected = R.raw.darth; break;
                }
                interval_selected = interval_spinner.getSelectedItem().toString();
                behaviour_selected = behaviour_spinner.getSelectedItem().toString();


                //Update SharedPreferences Alarm
                ed_alarm.putInt("FROM_HR", from_hr);
                ed_alarm.putString("FROM_MIN", from_min);
                ed_alarm.putInt("TO_HR", to_hr);
                ed_alarm.putString("TO_MIN", to_min);
                ed_alarm.putInt("SOUND", sound_selected);
                ed_alarm.putString("INTERVAL", interval_selected);
                ed_alarm.putString("BEHAVIOUR", behaviour_selected);
                ed_alarm.commit();

                //Service Manager
                if(!isServiceRunning(TimerService.class)) {
                    startService(new Intent(getBaseContext(), TimerService.class));
                }

                Intent i = new Intent(MainActivity.this, RunningActivity.class);
                startActivity(i);
                finish();
            }
        });

        //Play sound
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playSound(playID);

            }
        });

        // Spinner element
        sound_spinner = (Spinner) findViewById(R.id.sound);
        interval_spinner = (Spinner) findViewById(R.id.interval);
        behaviour_spinner = (Spinner) findViewById(R.id.behaviour);


        // Spinner click listener
        sound_spinner.setOnItemSelectedListener(MainActivity.this);
        interval_spinner.setOnItemSelectedListener(MainActivity.this);

        // Spinner Drop down elements
        List<String> sounds = new ArrayList<String>();
        sounds.add("Chimes");
        sounds.add("Chirp");
        sounds.add("Beep");
        sounds.add("Data");
        sounds.add("R2-D2");
        sounds.add("Darth Vader");

        List<String> interval = new ArrayList<String>();
        interval.add("Every 30 minutes");
        interval.add("Every 1 hour");
        interval.add("Every 2 hours");
        interval.add("Every 4 hours");
        interval.add("Every 8 hours");
        interval.add("Every 12 hours");
        interval.add("Custom");


        final List<String> behaviour = new ArrayList<String>();
        behaviour.add("Sound once");
        behaviour.add("According to current hour");
        behaviour.add("Binary beep");

        ArrayAdapter<String> soundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sounds);
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sound_spinner.setAdapter(soundAdapter);

        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, interval);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interval_spinner.setAdapter(intervalAdapter);

        ArrayAdapter<String> behaviourAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, behaviour);
        behaviourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        behaviour_spinner.setAdapter(behaviourAdapter);

        sound_spinner.postDelayed(new Runnable() {
            public void run() {
                can_play = true;
            }
        }, 30);


        sound_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (sound_spinner.getSelectedItem().toString().equals("R2-D2") ||
                        sound_spinner.getSelectedItem().toString().equals("Darth Vader")) {
                    behaviour.remove(1); // Hour wise
                    behaviour.remove(1); // Binary
                } else {

                    behaviour.clear();
                    behaviour.add("Sound once");
                    behaviour.add("According to current hour");
                    behaviour.add("Binary beep");
                }

                playID = R.raw.beep;
                switch (sound_spinner.getSelectedItem().toString()) {
                    case "Chimes":playID = R.raw.chimes; break;
                    case "Chirp": playID = R.raw.chirp; break;
                    case "Beep":playID = R.raw.beep; break;
                    case "Data": playID = R.raw.data; break;
                    case "R2-D2": playID = R.raw.r2d2; break;
                    case "Darth Vader": playID = R.raw.darth; break;
                    case "":playID = 0;
                }


                ed_alarm.putInt("SOUND", sound_selected);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                sound.stop();
                return;
            }
        });


        interval_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ed_alarm.putString("INTERVAL", interval_selected);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                sound.stop();
                return;
            }
        });

        behaviour_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ed_alarm.putString("BEHAVIOUR", behaviour_selected);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                sound.stop();
                return;
            }
        });


    }

    private void makeToast(Object data) {
        Toast.makeText(getApplicationContext(), String.valueOf(data), Toast.LENGTH_LONG).show();
    }

    private void makeSnack(Object t){
        View v = findViewById(R.id.rl_bg);
        Snackbar.make(v, String.valueOf(t), Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
            sound = MediaPlayer.create(MainActivity.this, ID);
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
