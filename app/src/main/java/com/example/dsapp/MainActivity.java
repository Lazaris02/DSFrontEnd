package com.example.dsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button send_gpx;
    Button show_statistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_gpx = (Button) findViewById(R.id.send_gpx_button);
        show_statistics = (Button) findViewById(R.id.show_statistics);

    }

    @Override
    protected void onStart(){
        super.onStart();
        /*we need two on click listeners one for each button*/
        send_gpx.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent myIntent = new Intent(view.getContext(),SendActivity.class);
                startActivityForResult(myIntent,0);
            }
        });
        show_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(),StatsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }
}