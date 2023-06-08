package com.example.dsapp;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.widget.TextView;



public class StatsActivity extends AppCompatActivity {

    String[] current_gpx;
    Double[] my_average;
    Double[] total_average;
    TextView curr,my_avg,t_avg;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Bundle bundle = getIntent().getExtras();

        current_gpx = (String[]) bundle.get("cur_gpx");
        my_average = (Double[]) bundle.get("i_avg");
        total_average = (Double[]) bundle.get("t_avg");

        curr = (TextView) findViewById(R.id.current);
        my_avg = (TextView) findViewById(R.id.avg);
        t_avg = (TextView) findViewById(R.id.t_avg);

        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                /*gets the result from the message , changes the UI*/
                String get_curr_text = curr.getText().toString();
                String curr_append = message.getData().getString("curr");
                get_curr_text = get_curr_text+"\n"+curr_append;

                String get_my_avg_text = my_avg.getText().toString();
                String my_avg_append = message.getData().getString("my_avg");
                get_my_avg_text = get_my_avg_text+"\n"+my_avg_append;

                String get_t_avg_text = t_avg.getText().toString();
                String t_avg_append = message.getData().getString("t_avg");
                get_t_avg_text =get_t_avg_text+"\n"+t_avg_append;

                /*update the text*/
                curr.setText(get_curr_text);
                my_avg.setText(get_my_avg_text);
                t_avg.setText(get_t_avg_text);

                return true;
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        StatsThread statsThread = new StatsThread(current_gpx.clone(),my_average.clone(),total_average.clone(),handler);
        statsThread.start();
        /*todo might need to */
        /*take the stats and change the look of the UI*/
    }
}