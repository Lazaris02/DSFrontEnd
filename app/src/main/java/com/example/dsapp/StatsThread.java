package com.example.dsapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsThread extends Thread{
    Handler handler;
    String[] for_current;
    Double[] for_my_avg;
    Double[] for_total_avg;

    public StatsThread(String[] for_current,Double[] for_my_avg,Double[] for_total_avg,Handler handler){
        this.for_current = for_current;
        this.for_my_avg = for_my_avg;
        this.for_total_avg=for_total_avg;
        this.handler = handler;
    }


    /*wrap the List into a message to be sent to the server*/
    @Override
    public void run(){
        try {
            sleep(500);

            Message msg = new Message();

            /*create a bundle with the UI changes
            * store the values in the bundle
            * give it to the handler to change the UI*/

            Bundle bundle = new Bundle();
            bundle.putString("curr",createStatsString(for_current));
            bundle.putString("my_avg",createStatsString(for_my_avg));
            bundle.putString("t_avg",createStatsString(for_total_avg));

            /*store the values in the message*/

            msg.setData(bundle);

            /*send message to handler*/

            handler.sendMessage(msg);

        }catch (InterruptedException exc){
            exc.printStackTrace();
        }
    }

    private String createStatsString(Double[] d){

        DecimalFormat df = new DecimalFormat("#.##");
        d[0] = Double.valueOf(df.format(Double.parseDouble(d[0].toString())));
        d[1] = d[1]/60;
        d[1] = Double.parseDouble(df.format(Double.parseDouble(d[1].toString())));
        d[2] = Double.valueOf(df.format(Double.parseDouble(d[2].toString())));

        return "distance: "+d[0]+" mtr \n"+" time: "+d[1]+" mins \n "+" elevation: "+d[2]+" mtr";
    }

    private String createStatsString(String[] s){

        DecimalFormat df = new DecimalFormat("#.##");
        double value = Double.parseDouble(s[0]);
        double time = Double.parseDouble(s[1])/60; // in minutes
        double speed = Double.parseDouble(s[2]);
        double elevation = Double.parseDouble(s[3]);

        elevation = Double.parseDouble(df.format(elevation));
        s[2] = String.valueOf(Double.valueOf(df.format(speed)));
        time = Double.parseDouble(df.format(time));
        s[0] = df.format(value);
        s[1] = Double.toString(time);
        s[3] = Double.toString(elevation);


        return "distance: "+s[0]+" mtr \n"+" time: "+s[1]+"mins \n "+" speed: "+s[2]+"km/h \n"+" elevation: "+s[3]+" mtr ";
    }


}
