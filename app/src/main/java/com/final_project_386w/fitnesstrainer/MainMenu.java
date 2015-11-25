package com.final_project_386w.fitnesstrainer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends AppCompatActivity {
    Button train_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        train_button = (Button) findViewById(R.id.train_button);
        train_button.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        Intent i = new Intent(MainMenu.this, Train.class);
                        startActivity(i);
                    }
                }
        );

    }

}
