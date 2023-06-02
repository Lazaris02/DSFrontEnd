import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

public class Worker extends Thread {
    private int worker_id;
    private int port;

    private Chunk chunk;
    private ArrayList<String[]> waypoints;

    private static String server_ip;

    /*Constructor*/

    public Worker(int id){
        this.worker_id = id;
        this.port=6769;
    }



    @Override
    public void run(){
        ObjectOutputStream out= null ;
        ObjectInputStream in = null ;
        Socket requestSocket= null ;



        try {
            String host = server_ip;
            /* Create socket for contacting the server on worker port*/

            requestSocket = new Socket(host, this.port);

            /* Create the streams to send and receive data from server */

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            chunk=(Chunk) in.readObject();

            waypoints=extractChunk(chunk);

            System.out.println("Creating intermediate results"+" "+Thread.currentThread().getName());

                /*In each chunk I have the same creator and gpx id*/

            String gpx_id =waypoints.get(0)[0];
            String creator =waypoints.get(0)[5];





            LocalDateTime time1=getStartTime(waypoints);
            LocalDateTime time2=getFinalTime(waypoints);

            List<Double> elevationlist=getElevations(waypoints);
            List<Double> latlist=getLat(waypoints);
            List<Double> lonlist=getLon(waypoints);
            double totalDis=0;
            for (int i=1;i<latlist.size();i++){totalDis+=distance(latlist.get(i-1),latlist.get(i),lonlist.get(i-1),lonlist.get(i));}

            double elevation;
            double totalTi=totalTime(time1,time2);

            double avSpeed=averageSpeed(totalDis,totalTi);

            if(elevationlist.size()>1){elevation=getTotElevation(elevationlist);}
            else elevation=0;

            String[] results = {gpx_id,creator,Double.toString(totalDis),Double.toString(totalTi),
                    Double.toString(avSpeed),Double.toString(elevation)};


            out.writeObject(results);
            out.flush();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close(); out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private static ArrayList<String[]> extractChunk(Chunk chunk){
        return chunk.getData();
    }



    private static LocalDateTime getStartTime(ArrayList<String[]> waypoints){
        return LocalDateTime.parse(waypoints.get(0)[4].substring(0,waypoints.get(0)[4].length()-1));
    }

    private static LocalDateTime getFinalTime(ArrayList<String[]> waypoints){
        return LocalDateTime.parse(waypoints.get(waypoints.size()-1)[4].substring(0,waypoints.get(waypoints.size()-1)[4].length()-1));
    }

    private List<Double> getElevations(ArrayList<String[]> waypoints) {
        List<Double> ele=new ArrayList<>();
        for(String[] w:waypoints){
            ele.add(Double.parseDouble(w[3]));
        }
        return ele;
    }
    private List<Double> getLat(ArrayList<String[]> waypoints) {
        List<Double> lat=new ArrayList<>();
        for(String[] w:waypoints){
            lat.add(Double.parseDouble(w[1]));
        }
        return lat;
    }

    private List<Double> getLon(ArrayList<String[]> waypoints) {
        List<Double> lon=new ArrayList<>();
        for(String[] w:waypoints){
            lon.add(Double.parseDouble(w[2]));
        }
        return lon;
    }

    private static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344 * 1000;
            return dist;
        }

    }

    private static double totalTime(LocalDateTime time1,LocalDateTime time2){
        Duration duration=Duration.between(time1,time2);
        return duration.toSeconds();
    }
    private static double averageSpeed(double distance,double time){
        double avspeed= distance/time;// m/s
        return 3.6*avspeed;//km/h
    }
    private double getTotElevation(List<Double> elevations) {
        double sum=0;
        for(int i =1;i<elevations.size();i++){
            if(elevations.get(i)> elevations.get(i - 1)){sum+=elevations.get(i)-elevations.get(i - 1);}
        }
        return sum;
    }


    public static void main(String[] args){


        server_ip = args[0];/*input the ip of the master or localhost if on same device*/
        /*Threads of worker*/

        int thread_1_id = (int)(Math.random()+10000);
        int thread_2_id =(int)(Math.random()+10000);
        int thread_3_id = (int)(Math.random()+10000);
        int thread_4_id =(int)(Math.random()+10000);

        Worker worker_thread_1 = new Worker(thread_1_id);
        Worker worker_thread_2 = new Worker(thread_2_id);
        Worker worker_thread_3 = new Worker(thread_3_id);
        Worker worker_thread_4 = new Worker(thread_4_id);

        worker_thread_1.start();
        worker_thread_2.start();
        worker_thread_3.start();
        worker_thread_4.start();

        while(true){

            if(!worker_thread_1.isAlive()){
                worker_thread_1 = new Worker((int)(Math.random()+10000));
                worker_thread_1.start();
            }
            if(!worker_thread_2.isAlive()){
                worker_thread_2 = new Worker((int)(Math.random()+10000));
                worker_thread_2.start();
            }
            if(!worker_thread_3.isAlive()){
                worker_thread_3 = new Worker((int)(Math.random()+10000));
                worker_thread_3.start();
            }
            if(!worker_thread_4.isAlive()){
                worker_thread_4 = new Worker((int)(Math.random()+10000));
                worker_thread_4.start();
            }


        }
    }

}
