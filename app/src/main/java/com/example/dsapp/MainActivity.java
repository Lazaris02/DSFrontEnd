package com.example.dsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button send_gpx;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_gpx = (Button) findViewById(R.id.send_gpx_button);


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

    }
}