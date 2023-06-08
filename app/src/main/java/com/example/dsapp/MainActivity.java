package com.example.dsapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button send_gpx;

   Button request_storage;

   private int storage_permission_code = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_gpx = (Button) findViewById(R.id.send_gpx_button);
        request_storage = (Button) findViewById(R.id.request_storage);


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

        request_storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "You have already granted storage permission", Toast.LENGTH_SHORT).show();
                }else{
                    requestStoragePermission();
                }
            }
        });

    }
    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                new AlertDialog.Builder(this).setTitle("Permission needed")
                        .setMessage("This permission is needed for storage")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int w){
                                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, storage_permission_code);
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, storage_permission_code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == storage_permission_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GIVEN", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}