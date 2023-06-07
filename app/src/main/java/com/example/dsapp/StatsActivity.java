package com.example.dsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;

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
                /*gets the result from the message , changes the ui or sth*/

                /*sets text in the activity*/

                return true;
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();

        Log.e(TAG, Arrays.toString(current_gpx) );
        Log.e(TAG, Arrays.toString(my_average) );
        Log.e(TAG, Arrays.toString(total_average) );

        /*take the stats and change the look of the UI*/
    }
}