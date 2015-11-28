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
import java.io.*;
import android.util.*;


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
    private List<List<Float>> x_list = new ArrayList<List<Float>>();
    private List<List<Float>> y_list = new ArrayList<List<Float>>();
    private List<List<Float>> z_list = new ArrayList<List<Float>>();
    private List<Float> x_vals = new ArrayList<Float>();
    private List<Float> y_vals = new ArrayList<Float>();
    private List<Float> z_vals = new ArrayList<Float>();

    private FileDialog fileDialog;
    private String model_file;
    private String interval_file;
    private int sensor_ms = 0;
    private long sensor_time = 0L;
    private long sensor_start_time = 0L;

    private boolean get_data = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        File mPath = new File(Environment.getExternalStorageDirectory() + "/Trainer_Models/");
        fileDialog = new FileDialog(this, mPath);
        //fileDialog.setFileEndsWith(".model");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                //Log.d(getClass().getName(), "selected file " + file.toString());
                //Log.d(getClass().getName(), "selected file " + file.getName());
                model_file = file.getName();
                interval_file = model_file.substring(0, model_file.length()-6) + ".txt";
                //Log.d(getClass().getName(), "selected file " + interval_file);
            }
        });
        fileDialog.showDialog();
        tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        start_button = (Button) findViewById(R.id.start_button);
        evaluate_button = (Button) findViewById(R.id.evaluate_button);
        time_text = (TextView) findViewById(R.id.timer);
        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        load_button = (Button) findViewById(R.id.load_button);

        if (sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        }

        start_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mode == 1) {
                            x_vals.clear();
                            y_vals.clear();
                            z_vals.clear();
                            mode = 0;
                            start_button.setText("Stop");
                            start_time = SystemClock.uptimeMillis() + 5000L;
                            sensor_start_time = SystemClock.uptimeMillis() + 5000L;
                            handler.postDelayed(update_timer, 5000);
                            handler.postDelayed(make_beep, 5000);
                        } else {
                            Log.d("SIZE1", Integer.toString(x_list.get(0).size()));
                            Log.d("Size2", Integer.toString(y_list.get(0).size()));
                            Log.d("Size3", Integer.toString(z_list.get(0).size()));
                            start_button.setText("Start");
                            time_text.setText("00:00:00");
                            time_text.setTextColor(Color.BLACK);
                            get_data = false;
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
                    Bundle extras = new Bundle();

                    for(int j = 0; j < x_list.size(); ++j) {
                        extras.putFloatArray("x_vals" + Integer.toString(j), toArray(x_list.get(j)));
                    }
                    for(int j = 0; j < y_list.size(); ++j) {
                        extras.putFloatArray("y_vals" + Integer.toString(j), toArray(y_list.get(j)));
                    }
                    for(int j = 0; j < z_list.size(); ++j) {
                        extras.putFloatArray("z_vals" + Integer.toString(j), toArray(z_list.get(j)));
                    }
                    extras.putInt("x_size", x_list.size());
                    extras.putInt("y_size", y_list.size());
                    extras.putInt("z_size", z_list.size());
                    extras.putString("model_file", model_file);
                    extras.putString("interval_file", interval_file);
                    i.putExtras(extras);
                    startActivity(i);
                }
            }
        );

        load_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                            public void fileSelected(File file) {
                                //Log.d(getClass().getName(), "selected file " + file.toString());
                                //Log.d(getClass().getName(), "selected file " + file.getName());
                                model_file = file.getName();
                                interval_file = model_file.substring(0, model_file.length()-6) + ".txt";
                                //Log.d(getClass().getName(), "selected file " + interval_file);
                            }
                        });
                        fileDialog.showDialog();
                    }
                }
        );

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(get_data == true) {
            x_list.get(x_list.size()-1).add(event.values[0]);
            y_list.get(y_list.size()-1).add(event.values[1]);
            z_list.get(z_list.size()-1).add(event.values[2]);
        }


    }

    public Runnable update_timer = new Runnable() {
        public void run() {
            time_ms = SystemClock.uptimeMillis() - start_time;
            secs = (int) (time_ms / 1000);
            mins = secs / 60;
            secs = (secs % 60);
            milliseconds = (int) (time_ms % 1000);
            time_text.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%02d", milliseconds));
            time_text.setTextColor(Color.RED);
            handler.postDelayed(this, 0);
        }
    };

    public Runnable make_beep = new Runnable() {
        public void run() {
            get_data = false;
            x_list.add(new ArrayList<Float>());
            y_list.add(new ArrayList<Float>());
            z_list.add(new ArrayList<Float>());
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            get_data = true;
            handler.postDelayed(this, 3000);
        }
    };


    //SOF
    public static float[] toArray(List<Float> in){
        float[] result = new float[in.size()];
        for(int i=0; i<result.length; i++){
            result[i] = in.get(i);
        }
        return result;
    }

}
