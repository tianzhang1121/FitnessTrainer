package com.final_project_386w.fitnesstrainer;

import android.graphics.Color;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.content.*;
import android.net.*;
import android.media.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.*;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.*;


public class Train extends AppCompatActivity implements SensorEventListener{
    private Button start_button, evaluate_button, main_button, save_button, load_button;
    private TextView time_text;
    private long start_time = 0L;
    private long time_ms = 0L;
    private int mode = 1;
    private int secs = 0;
    private int mins = 0;
    private int milliseconds = 0;
    private Handler handler = new Handler();
    private ToneGenerator tone;
    private SensorManager sensor_manager;
    private Sensor accelerometer;
    Vector x_vals = new Vector();
    Vector y_vals = new Vector();
    Vector z_vals = new Vector();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        start_button = (Button) findViewById(R.id.start_button);
        evaluate_button = (Button) findViewById(R.id.evaluate_button);
        time_text = (TextView) findViewById(R.id.timer);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        } else {

        }

        start_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mode == 1) {
                            x_vals.clear();
                            y_vals.clear();
                            z_vals.clear();
                            start_button.setText("Stop");
                            start_time = SystemClock.uptimeMillis();
                            handler.postDelayed(update_timer, 5000);
                            handler.postDelayed(make_beep, 5000);
                            mode = 0;
                        } else {
                            milliseconds = 0;
                            start_button.setText("Start");
                            time_text.setText("00:00:00");
                            time_text.setTextColor(Color.BLACK);
                            handler.removeCallbacks(update_timer);
                            handler.removeCallbacks(make_beep);
                            mode = 1;
                        }
                    }

                }
        );

        evaluate_button.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(Train.this, Evaluate.class);
                    startActivity(i);
                }
            }
        );

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(milliseconds != 0){
            if((milliseconds % 3500) == 0){
                x_vals.addElement(event.values[0]);
                y_vals.addElement(event.values[1]);
                z_vals.addElement(event.values[2]);
            }
        }

    }

    public Runnable update_timer = new Runnable() {
        public void run() {
            time_ms = SystemClock.uptimeMillis() - start_time;
            secs = (int) (time_ms / 1000);
            mins = secs / 60;
            secs = (secs % 60) - 5;
            milliseconds = (int) (time_ms % 1000);
            time_text.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%02d", milliseconds));
            time_text.setTextColor(Color.RED);
            handler.postDelayed(this, 0);
        }
    };
    public Runnable make_beep = new Runnable() {
        public void run() {
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            handler.postDelayed(this, 5000);
        }
    };

}
