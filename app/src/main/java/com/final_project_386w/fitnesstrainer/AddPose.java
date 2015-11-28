package com.final_project_386w.fitnesstrainer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;


public class AddPose extends AppCompatActivity {

    Button add_good_button, add_bad_button, save_model_button, main_menu_button, interval_button;
    TextView good_count, bad_count;
    EditText set_interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pose);

        add_good_button = (Button) findViewById(R.id.add_good_button);
        add_bad_button = (Button) findViewById(R.id.add_bad_button);
        save_model_button = (Button) findViewById(R.id.save_model_button);

        good_count = (TextView) findViewById(R.id.good_count);
        bad_count = (TextView) findViewById(R.id.bad_count);


    }
}
