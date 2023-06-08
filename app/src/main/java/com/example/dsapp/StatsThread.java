package com.example.dsapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
        return " distance: "+d[0].toString()+" time: "+d[1].toString()+" elevation: "+d[2].toString();
    }

    private String createStatsString(String[] s){
        return "lat: "+s[0]+" lon: "+s[1]+" ele: "+s[2]+" time: "+s[3];
    }


}
