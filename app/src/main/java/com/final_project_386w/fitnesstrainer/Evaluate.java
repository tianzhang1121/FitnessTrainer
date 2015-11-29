package com.final_project_386w.fitnesstrainer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.AssetManager;
import java.io.*;
import android.util.Log;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.*;
import android.os.*;
import java.util.*;

public class Evaluate extends AppCompatActivity {

    private TextView correct, incorrect, total, rating;
    private Button back_button;
    private Handler handler = new Handler();
    private String model_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Trainer_Models/";
    private ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    private ArrayList<String> attr_class = new ArrayList<String>();
    private double interval = 3;
    private List<Float> x_vals;
    private List<Float> y_vals;
    private List<Float> z_vals;
    private String model_file;
    private String interval_file;
    private Bundle extras;
    private Instances data_set;
    private Instances labeled_set;
    private int inst_num;
    private int x_size;
    private int y_size;
    private int z_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);
        extras = getIntent().getExtras();
        model_file = extras.getString("model_file");
        interval = extras.getDouble("interval");
        total = (TextView) findViewById(R.id.total_count);
        correct = (TextView) findViewById(R.id.correct_count);
        incorrect = (TextView) findViewById(R.id.incorrect_count);
        rating = (TextView) findViewById(R.id.rating);
        back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(Evaluate.this, Train.class);
                        startActivity(i);
                    }
                }
        );

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
        Log.d("ATTRS", Integer.toString(attrs.size()));
        handler.postDelayed(evaluate, 0);
    }
    public Runnable evaluate = new Runnable() {
        public void run() {
            try {
                x_size = extras.getInt("x_size");
                y_size = extras.getInt("y_size");
                z_size = extras.getInt("z_size");

                data_set = new Instances("Accelerometer", attrs, x_size);
                // Set class index
                data_set.setClassIndex(attrs.size() - 1);
                int attr_count = (attrs.size() - 1) / 3;
                Log.d("count", Integer.toString(attr_count));
                for(int i = 0; i < x_size; ++i) {
                    x_vals = toList(extras.getFloatArray("x_vals" + Integer.toString(i)));
                    y_vals = toList(extras.getFloatArray("y_vals" + Integer.toString(i)));
                    z_vals = toList(extras.getFloatArray("z_vals" + Integer.toString(i)));
                    if (x_vals.size() >= attr_count) {
                        Instance gesture = new DenseInstance(attrs.size());
                        gesture.setDataset(data_set);
                        for (int j = 0; j < attr_count; ++j) {
                            gesture.setValue((Attribute) attrs.get(j), x_vals.get(j * x_vals.size() / attr_count));
                        }
                        for (int j = attr_count; j < 2 * attr_count; ++j) {
                            gesture.setValue((Attribute) attrs.get(j), y_vals.get((j - attr_count) * y_vals.size() / attr_count));
                        }
                        for (int j = 2 * attr_count; j < 3 * attr_count; ++j) {
                            gesture.setValue((Attribute) attrs.get(j), z_vals.get((j - 2 * attr_count) * z_vals.size() / attr_count));
                        }
                        gesture.setClassMissing();
                        data_set.add(gesture);
                    }
                    Log.d("Got", Integer.toString(i));
                }
                Log.d("Got", "here");
                Classifier cModel = (Classifier) weka.core.SerializationHelper.read(model_path + model_file);
                labeled_set = new Instances(data_set);

                for (int i = 0; i < data_set.numInstances(); i++) {
                    double clsLabel = cModel.classifyInstance(data_set.instance(i));
                    labeled_set.instance(i).setClassValue(clsLabel);
                    Log.d("INSTANCE", labeled_set.instance(i).toString(attrs.size()-1));
                }

                int correct_num = labeled_set.attributeStats(attrs.size()-1).nominalCounts[0];
                int incorrect_num = labeled_set.attributeStats(attrs.size()-1).nominalCounts[1];
                int total_num = labeled_set.size();
                total.setText("Total: " + Integer.toString(total_num));
                correct.setText("Correct: " + Integer.toString(correct_num));
                incorrect.setText("Incorrect: " + Integer.toString(incorrect_num));
                double user_rating = (double) correct_num / total_num;
                if(user_rating >= .90){
                    rating.setText("Rating: A");
                } else if(user_rating >= .80) {
                    rating.setText("Rating: B");
                } else if(user_rating >= .70) {
                    rating.setText("Rating: C");
                } else if(user_rating >= .60){
                    rating.setText("Rating: D");
                } else {
                    rating.setText("Rating: F");
                }

            } catch(Exception e){
                Log.e("MYAPP", "exception", e);
            }
        }
    };
    public static List<Float> toList(float[] in){
        List<Float> result = new ArrayList<Float>(in.length);
        for(float f : in){
            result.add(f);
        }
        return result;
    }
}
