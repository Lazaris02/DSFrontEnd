package com.example.dsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;

public class SendActivity extends AppCompatActivity {

    Button choose;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        choose = (Button) findViewById(R.id.choose_gpx);

        send = (Button) findViewById(R.id.send_to_server);
    }

    @Override
    protected void onStart(){

        super.onStart();
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                /*get the gpx
                * open connection with the server
                * send it + wait for result*/
            }
        });
    }

    // Request code for selecting a PDF document.
    private static final int PICK_GPX_FILE = 2;

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); /*make sure to click the correct file*/
        startActivityForResult(intent, PICK_GPX_FILE);
    }
}