package com.final_project_386w.fitnesstrainer;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.util.*;
import android.os.*;
import android.content.*;
import android.net.*;
import android.media.*;
import android.hardware.*;
import java.util.*;

import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import java.util.Random;
import java.io.*;


public class AddPose extends AppCompatActivity implements SensorEventListener{

    private Button add_good_button, add_bad_button, save_model_button, main_menu_button, interval_button;
    private TextView good_count, bad_count;
    private EditText time_interval, model_name;
    private double interval = 3.5;
    private int mode = 1;
    private Handler handler = new Handler();
    private ToneGenerator tone;
    private SensorManager sensor_manager;
    private Sensor accelerometer;
    private List<Float> current_xvals = new ArrayList<Float>();
    private List<Float> current_yvals = new ArrayList<Float>();
    private List<Float> current_zvals = new ArrayList<Float>();
    private String class_val = "incorrect";
    private boolean get_data = false;
    private ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    private ArrayList<String> attr_class = new ArrayList<String>();
    private Instances data_set;
    private int correct_count = 0;
    private int incorrect_count = 0;
    private String model_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Trainer_Models/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pose);

        add_good_button = (Button) findViewById(R.id.add_good_button);
        add_bad_button = (Button) findViewById(R.id.add_bad_button);
        save_model_button = (Button) findViewById(R.id.save_model_button);

        good_count = (TextView) findViewById(R.id.good_count);
        bad_count = (TextView) findViewById(R.id.bad_count);

        time_interval = (EditText) findViewById(R.id.interval_field);
        interval_button = (Button) findViewById(R.id.interval_button);

        model_name = (EditText) findViewById(R.id.model_name_field);

        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        if (sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensor_manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        }

        main_menu_button = (Button) findViewById(R.id.main_button_pose);
        main_menu_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(AddPose.this, MainMenu.class);
                        startActivity(i);
                    }
                }
        );

        interval_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        interval = Double.parseDouble(time_interval.getText().toString());
                        attrs.clear();
                        attr_class.clear();
                        for (int i = 0; i < (int) (interval * 1000 / 100); ++i) {
                            attrs.add(new Attribute("x" + i));
                        }
                        for (int i = 0; i < (int) (interval * 1000 / 100); ++i) {
                            attrs.add(new Attribute("y" + i));
                        }
                        for (int i = 0; i < (int) (interval * 1000 / 100); ++i) {
                            attrs.add(new Attribute("z" + i));
                        }
                        attr_class.add("correct");
                        attr_class.add("incorrect");
                        attrs.add(new Attribute("category", attr_class));
                        data_set = new Instances("Accelerometer", attrs, 40);
                        // Set class index
                        data_set.setClassIndex(attrs.size() - 1);
                        Log.d("data_set_size", Integer.toString(attrs.size()));
                        Log.d("data_set_size", Integer.toString(data_set.size()));
                        Context context = getApplicationContext();
                        CharSequence text = "Interval set.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
        );

        add_good_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mode == 1) {
                            mode = 0;
                            add_good_button.setText("Stop");
                            class_val = "correct";
                            current_xvals.clear();
                            current_yvals.clear();
                            current_zvals.clear();
                            handler.postDelayed(make_beep, 5000);
                        } else {
                            current_xvals.clear();
                            current_yvals.clear();
                            current_zvals.clear();
                            add_good_button.setText("Good Gesture");
                            handler.removeCallbacks(make_beep);
                            handler.removeCallbacks(make_beep2);
                            mode = 1;
                        }
                    }
                }
        );
        add_bad_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mode == 1) {
                            mode = 0;
                            add_bad_button.setText("Stop");
                            class_val = "incorrect";
                            current_xvals.clear();
                            current_yvals.clear();
                            current_zvals.clear();
                            handler.postDelayed(make_beep, 5000);
                        } else {
                            current_xvals.clear();
                            current_yvals.clear();
                            current_zvals.clear();
                            add_bad_button.setText("Bad Gesture");
                            handler.removeCallbacks(make_beep);
                            handler.removeCallbacks(make_beep2);
                            mode = 1;
                        }
                    }
                }
        );

        save_model_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        handler.post(save);
                    }
                }
        );


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(get_data == true){
                current_xvals.add(event.values[0]);
                current_yvals.add(event.values[1]);
                current_zvals.add(event.values[2]);
        }

    }

    public Runnable make_beep2 = new Runnable() {
        public void run() {
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            //process data here
            get_data = false;
            int attr_count = (attrs.size() - 1)/3;
            if(current_xvals.size() >= attr_count) {
                Instance gesture = new DenseInstance(attrs.size());
                gesture.setDataset(data_set);

                for (int j = 0; j < attr_count; ++j) {
                    gesture.setValue((Attribute) attrs.get(j), current_xvals.get(j * current_xvals.size() / attr_count));
                }
                for (int j = attr_count; j < 2*attr_count; ++j) {
                    gesture.setValue((Attribute) attrs.get(j), current_yvals.get((j - attr_count) * current_yvals.size() / attr_count));
                }
                for (int j = 2*attr_count; j < 3*attr_count; ++j) {
                    gesture.setValue((Attribute) attrs.get(j), current_zvals.get((j - 2*attr_count) * current_zvals.size() / attr_count));
                }
                gesture.setClassValue(class_val);
                //gesture.setDataset(data_set);
                //gesture.setValue((Attribute) attrs.get(attrs.size()-1), "?");
                // add the instance
                data_set.add(gesture);
                if(class_val == "correct"){
                    correct_count+=1;
                    good_count.setText("Good Gestures: " + Integer.toString(correct_count));
                }
                if(class_val == "incorrect"){
                    incorrect_count+=1;
                    bad_count.setText("Bad Gestures: " + Integer.toString(incorrect_count));
                }
            }
            if(class_val == "correct"){
                add_good_button.setText("Good Gesture");
            } else {
                add_bad_button.setText("Bad Gesture");
            }
            current_xvals.clear();
            current_yvals.clear();
            current_zvals.clear();
            mode = 1;
        }
    };

    public Runnable make_beep = new Runnable() {
        public void run() {
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            get_data = true;
            handler.postDelayed(make_beep2, (int)(interval * 1000));
        }
    };

    public Runnable save = new Runnable() {
        public void run() {
            Classifier ibk = new IBk(1);
            try {
                ibk.buildClassifier(data_set);
                weka.core.SerializationHelper.write(model_path + model_name.getText().toString() + ".model", ibk);

                File time_record = new File(model_path, model_name.getText().toString() + ".txt");
                FileWriter writer = new FileWriter(time_record);
                writer.append(Double.toString(interval));
                writer.flush();
                writer.close();

                Context context = getApplicationContext();
                CharSequence text = "Model saved.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                //Evaluation eval = new Evaluation(data_set);
                //eval.crossValidateModel(bayes, data_set, 10, new Random(1));
                //Log.d("EVAL", Double.toString(eval.correct()));
            } catch (Exception e){
                Log.d("ERRRRR", e.toString());
            }

        }
    };
}
