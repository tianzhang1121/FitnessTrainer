package com.final_project_386w.fitnesstrainer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    TextView loading;
    Button back_button, main_button;
    private Handler handler = new Handler();
    String model_path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Trainer_Models/";
    ArrayList<Attribute> attrs = new ArrayList<Attribute>();
    ArrayList<String> attr_class = new ArrayList<String>();
    double interval = 3.5;
    List<Float> x_vals;
    List<Float> y_vals;
    List<Float> z_vals;
    String model_file;
    String interval_file;
    Bundle extras;
    Instances data_set;
    Instances labeled_set;
    int inst_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);
        loading = (TextView) findViewById(R.id.statistics);
        for (int i = 0; i < (int) (interval * 1000 / 100); ++i) {
            attrs.add(new Attribute("x" + i));
            attrs.add(new Attribute("y" + i));
            attrs.add(new Attribute("z" + i));
        }
        attr_class.add("correct");
        attr_class.add("incorrect");
        attrs.add(new Attribute("category", attr_class));
        extras = getIntent().getExtras();
        x_vals = toList(extras.getFloatArray("x_vals"));
        y_vals = toList(extras.getFloatArray("y_vals"));
        z_vals = toList(extras.getFloatArray("z_vals"));
        Log.d("SIZE!!!!!!!!!", ((Integer) x_vals.size()).toString());

        model_file = extras.getString("model_file");
        interval_file = extras.getString("interval_file");
        handler.postDelayed(evaluate, 0);
    }
    public Runnable evaluate = new Runnable() {
        public void run() {
            try {
                // Create an empty training set
                inst_num = (int) ((x_vals.size() * 100)/(interval * 1000));
                data_set = new Instances("Accelerometer", attrs, inst_num);
                // Set class index
                data_set.setClassIndex(attrs.size()-1);

                for(int i = 0; i < inst_num; ++i ) {
                    Instance gesture = new DenseInstance(attrs.size());
                    gesture.setDataset(data_set);
                    for(int j = 0; j < attrs.size()-1; j += 3) {
                        gesture.setValue((Attribute) attrs.get(j), x_vals.get(j/3));
                        gesture.setValue((Attribute) attrs.get(j+1), y_vals.get(j/3));
                        gesture.setValue((Attribute) attrs.get(j+2), z_vals.get(j/3));
                    }
                    gesture.setClassMissing();
                    //gesture.setDataset(data_set);
                    //gesture.setValue((Attribute) attrs.get(attrs.size()-1), "?");
                    // add the instance
                    data_set.add(gesture);
                }

                Classifier cModel = (Classifier) weka.core.SerializationHelper.read(model_path + model_file);
                labeled_set = new Instances(data_set);

                for (int i = 0; i < data_set.numInstances(); i++) {
                    double clsLabel = cModel.classifyInstance(data_set.instance(i));
                    labeled_set.instance(i).setClassValue(clsLabel);
                    Log.d("INSTANCE", labeled_set.instance(i).toString(attrs.size()-1));
                }
                //Log.d("RESULT", labeled_set.toSummaryString());
                //loading.setText(labeled_set.toSummaryString());

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
