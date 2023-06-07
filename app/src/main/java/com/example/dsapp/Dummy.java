package com.example.dsapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class Dummy extends Thread{
    Handler handler;
    List<String> gpx_file;

    public Dummy(List<String> gpx_file,Handler handler){
        this.gpx_file = gpx_file;
        this.handler = handler;
    }


    /*wrap the List into a message to be sent to the server*/
    @Override
    public void run(){
        try {
            sleep(500);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            /*we need to convert the list to an array list*/
            bundle.putStringArrayList("list", new ArrayList<>(gpx_file));
            msg.setData(bundle);
        }catch (InterruptedException exc){
            exc.printStackTrace();
        }
    }
}
