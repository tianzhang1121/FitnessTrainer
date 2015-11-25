package com.final_project_386w.fitnesstrainer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.*;
import weka3.core.*;
import weka3.*;
public class Evaluate extends AppCompatActivity {

    TextView loading;
    Button back_button, main_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);
    }
}
