package com.example.dsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SendActivity extends AppCompatActivity {

    Button choose;
    Button send;
    Button show_statistics;

    Intent gpx_intent = null;
    List<String> gpx_file = new LinkedList<>();

    Handler handler;


    /*for stats*/
    String[] personal_results;
    Double[] total_avg;
    Double[] individual_avg;




    private static final int PICK_GPX_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        choose = (Button) findViewById(R.id.choose_gpx);

        send = (Button) findViewById(R.id.send_to_server);

        show_statistics = (Button) findViewById(R.id.show_statistics);

        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                /*gets the result from the message , changes the ui or sth*/

                 /*sets text in the activity*/

                return true;
            }
        });

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
                /*checks if we have a gpx file ready (list length or null)
                * if we do send the gpx file
                * Empty the list*/
                if(gpx_file != null && gpx_file.size()!=0){

                    /*send the gpx through a socket*/
                    AsyncTask<Void,Void,String> myAsync = new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {

                            String host_ip = "10.0.2.2";
                            int port_num = 5377;

                            try {
                                Socket s = new Socket(host_ip,port_num);
                                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

                                Log.e(TAG, Arrays.toString(gpx_file.toArray()));

                                /*write in the socket*/
                                oos.writeObject(new ArrayList<>(gpx_file));
                                oos.flush();

                                //gpx_file.clear();

                                /*wait for the results from Master*/
                                personal_results = (String[]) ois.readObject();
                                total_avg = (Double[]) ois.readObject();
                                individual_avg = (Double[]) ois.readObject();

                            }catch (IOException exc){exc.printStackTrace();} catch (
                                    ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            return "Your route is calculated";

                        }

                        @Override
                        protected void onPostExecute(String s){}

                    };
                    myAsync.execute(); /*this starts a thread*/
                }else{
                    Log.e(TAG, "onClick: gpx_file is null or size 0");
                }
            }
        });

        show_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(),StatsActivity.class);

                /*put the data in the intent*/
                myIntent.putExtra("cur_gpx",personal_results.clone());
                myIntent.putExtra("i_avg",individual_avg.clone());
                myIntent.putExtra("t_avg",total_avg.clone());

                Log.e(TAG, "onClick: sending data to next activity");

                startActivityForResult(myIntent, 0);
            }
        });
    }

    // Request code for selecting a PDF document.


    /*opens the file*/
    private void openFile() {
        gpx_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        gpx_intent.addCategory(Intent.CATEGORY_OPENABLE);
        gpx_intent.setType("*/*"); /*make sure to click the correct file*/
        startActivityForResult(gpx_intent, PICK_GPX_FILE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == PICK_GPX_FILE
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.

            Uri uri = null;

            if(resultData == null){
                Log.e(TAG, "onActivityResult:resultData is null ");
            }
            if (resultData != null) {

                uri = resultData.getData();
                Log.e(TAG, uri.toString());

                File file = new File(uri.getPath());//create path from uri
                final String[] split = file.getPath().split(":");//split the path.
                String file_path = split[1];

                try {
                    gpx_file = Files.readAllLines(Paths.get(file_path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                /*todo grant access from settings */




            }
        }
    }
}