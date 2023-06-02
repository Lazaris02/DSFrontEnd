import java.io.*;
import java.net.*;
import java.util.*;


public class Master extends Thread implements Server {
    private  static int num_of_workers;

    private static final int client_port = 5377;

    private static final int worker_port = 6769;

    private final int port;

    private static final int chunk_size = 4;


    /*The socket that receives the requests*/
    private ServerSocket serverSocket;


    /* The socket that handles the requests */
    private  Socket provider;



    /*These collections are used for Mapping*/

    private static HashMap<String,Chunk> chunks; /*We hold the chunks here until ready*/
    private static LinkedList<Chunk> readyChunks; /*Insert the ready for Workers chunks here*/


    /*These collections are used for Round Robin*/
    private static HashMap<Integer,LinkedList<Thread>> workers;
    private static List<String> worker_ips;
    private int rrIterator;


    /*These collections are used to trigger the "Reduce" phase properly*/
    private static HashMap<String,Integer[]> gpxCounter;
    private static HashMap <String,Integer> readyForReduce;



    /*These objects are used for the Reduce process + Statistics*/
    private static HashMap <String,ArrayList<String[]>> total_results=new HashMap<>();
    private static Integer total_gpx = 0;

    private static HashMap <String,String[]> customer_results=new HashMap<>();

    private static HashMap<String,String[]> individual_stats=new HashMap<>();

    private static HashMap<String,Integer> gpx_per_user = new HashMap<>();


    /* Constructor */

    public Master(int port){
        this.port = port;
        if(this.port == client_port){
            chunks = new HashMap<>();
            gpxCounter = new HashMap<>();
        }
        if(this.port == worker_port){
            readyChunks = new LinkedList<>();
            readyForReduce = new HashMap<>();
            initWorkers();
        }
    }




    /*Getters -- Setters*/


    /*Initializes the worker queue -- int:worker num
    *                                 LinkedList:queue of threads for the specific worker*/
    private void initWorkers(){
        rrIterator =0;
        workers = new HashMap<>();
        for(int i =0; i<num_of_workers; i++){
            LinkedList<Thread> worker_threads = new LinkedList<>();
            workers.put(i,worker_threads);
        } /*Default initialization*/

        worker_ips = new LinkedList<>();
    }


    public int getChunk_size(){return chunk_size;}


    /*increases the number of chunks for the given gpx*/
    private static void increaseChunkNumber(String gpx_id){
        if(!gpxCounter.containsKey(gpx_id)){
            gpxCounter.put(gpx_id,new Integer[]{1,0});
        }else{
           Integer[] toChange = gpxCounter.get(gpx_id);
           toChange[0]++;
           gpxCounter.put(gpx_id,toChange);
        }
    }




    /*locks the FINAL chunk number (the one we need) for each gpx*/
    public static void lockValue(String key) {
       Integer[] toChange = gpxCounter.get(key);
       toChange[1] = 1;
       gpxCounter.put(key,toChange);
    }



    /*returns the chunk number for the gpx we request*/
    private static int getGpxCounterValue(String gpx_id){ return gpxCounter.get(gpx_id)[0];}


    /*checks if the gpx_counter is ready to be used -- indicates that the whole gpx has been chunked*/
    private static boolean gpxCounterReady(String gpx_id){return gpxCounter.get(gpx_id)[1] == 1;}



    private static int getReduceCounter(String gpx_id){return readyForReduce.get(gpx_id);}

    public static synchronized String[] getCustRes(String id){
        return customer_results.get(id);
    }

    public Double[] getTotalStats(){ /*TODO change here*/
        int counter;
        double dis=0,time=0,elevation=0;

        synchronized (customer_results) {
            /*calculates the total stats (from all gpx we have)*/
            for (Map.Entry<String, String[]> entry : customer_results.entrySet()) {
                String[] value = entry.getValue();
                dis += Double.parseDouble(value[0]);
                time += Double.parseDouble(value[1]);
                elevation += Double.parseDouble(value[3]);
            }
        }

        synchronized (total_gpx){counter = total_gpx.intValue();}

        return new Double[]{dis / counter , time /counter , elevation / counter};/*stats for all users*/

    }

    public Double[] getIndividualStats(String customer_id){
        String[] ITS;
        int num_of_gpx;
        synchronized (individual_stats) {
                 ITS = individual_stats.get(customer_id).clone();
            }

        synchronized (gpx_per_user) {
            num_of_gpx = gpx_per_user.get(customer_id);
        }

            return new Double[]{Double.parseDouble(ITS[0]) / num_of_gpx, Double.parseDouble(ITS[1]) / num_of_gpx,
                    Double.parseDouble(ITS[3]) / num_of_gpx};

    }



    /*increases the reduced chunk number for a specific gpx*/
    public static synchronized void chunkMapped(String gpx_id){
        if(!readyForReduce.containsKey(gpx_id)){
            readyForReduce.put(gpx_id,1);
        }else{
            int value = readyForReduce.get(gpx_id);
            readyForReduce.put(gpx_id,++value);
        }
    }




    /*checks if a gpx is ready to be reduced*/
    /*a gpx is ready to be reduced as long as the following conditions are met
    * 1. We have the total number of chunks the specific gpx contains
    * 2. The num of  total chunks of the specific gpx == The num of Reduced chunks of the specific gpx*/

    public static synchronized boolean startReduce(String gpx_id){
       return (gpxCounterReady(gpx_id) && (getGpxCounterValue(gpx_id) == getReduceCounter(gpx_id)));
    }



    /*Map function*/

    public void map(String[] key, String[] waypoint_lines,boolean last_waypoint){

        /*key[0]--> gpx_id
        *key[1]-->creator*/

        /*extracts the parameters*/

        String lat = extractLat(waypoint_lines[0]);
        String lon = extractLon(waypoint_lines[0]);
        String ele = extractEle(waypoint_lines[1]);
        String time = extractTime(waypoint_lines[2]);

        String gpx_id = String.valueOf(key[0]);
        String creator = String.valueOf(key[1]);

        /*Creates the array*/

        String [] key_values = {gpx_id,lat,lon,ele,time,creator};

        addToChunk(key[0],key_values,last_waypoint); /*adds to chunk also checks if chunk is ready*/


    }

    /*Helper functions for map -- extracts the values we need properly*/

    private String extractLat(String line){
        String[] find_lat_lon = line.split("lon");
        String lat = find_lat_lon[0].split("lat=\"")[1];
        lat = lat.substring(0,lat.length()-2); /*gets lat*/
        return lat;
    }

    private String extractLon(String line){
        String[] find_lat_lon = line.split("lon");
        return find_lat_lon[1].split("\"")[1];
    }

    private String extractEle(String line){
        return line.strip().substring(5,line.strip().length()-6);
    }

    private String extractTime(String line){return line.strip().substring(6,line.strip().length()-7);}




    /*Chunk Scheduling and Manipulation*/



    /*adds the waypoint to the chunk it needs to be
    * checks if the chunk is full and needs to be sent to the ready queue*/
    private synchronized void addToChunk(String key, String[] key_values,boolean last_waypoint){
        if(chunks.containsKey(key)){
            chunks.get(key).addData(key_values);
        }else{
            Chunk c = new Chunk();
            c.addData(key_values);
            chunks.put(key,c);
        }
        addToReadyChunks(key,last_waypoint);
    }


    /*adds the chunk to the ready queue -- to be sent for mapping*/
    private  void addToReadyChunks(String key, boolean last_waypoint){

        /*adds a chunk to the ready queue(if ready)+ notifies the master to send it to a worker*/

        if(chunks.get(key).getData().size() == chunk_size || last_waypoint) {

            increaseChunkNumber(key);
            if(last_waypoint){lockValue(key);} /*for reduce function*/

            synchronized (readyChunks) {
                readyChunks.add(new Chunk(chunks.get(key))); /*Deep copy the chunk*/
            }
                chunks.remove(key);


            /*Since there is a chunk available we also notify the master to give it to a worker*/

           startWorkerThread();

        }
    }


    /*Checks if there is a chunk ready to be sent for mapping
    * if there is then it removes it from the ready queue and sends it*/
    public  Chunk fetchChunk(){
        synchronized (readyChunks){
            if (readyChunks.isEmpty()){
                return null;
            }
            return readyChunks.remove();
        }
    }



    /*Reduce Function*/
    public void reduce(String gpx_id,String user_id){
        double totalDist=0;
        double totalTime=0;
        double avSpeed;
        double totalElevation=0;



        synchronized (total_gpx){total_gpx++;} /*TODO change here*/

        for (String[] res:total_results.get(gpx_id)){
            totalDist+=Double.parseDouble(res[2]);
            totalElevation+=Double.parseDouble(res[5]);
            totalTime += Double.parseDouble(res[3]);
        }

        avSpeed=totalDist/totalTime; //m/s
        avSpeed=3.6*avSpeed; //km/h

        String[] temp=new String[]{Double.toString(totalDist),Double.toString(totalTime),
                Double.toString(avSpeed),Double.toString(totalElevation)};


        put_cust_results(gpx_id,temp);/*for total results*/
        put_individual_stats(user_id,temp);
    }


    public synchronized void addResult(String key,String[] c_result){
        if(total_results.containsKey(key)){
            total_results.get(key).add(c_result);
        }
        else{
            ArrayList<String[]> perm=new ArrayList<>();
            perm.add(c_result);
            total_results.put(key,perm);
        }

    }


    private synchronized void put_cust_results(String client_id,String[] res){ /*TODO might need sync here*/
            customer_results.put(client_id, res);
    }

    private synchronized void put_individual_stats(String customer_id,String[] res){
        /*we hold 2 separate hashmaps
        * 1 contains the sum of the stats we need to preserve for each creator
        * 2 contains the number of gpx that belong to the specific user
        * When it is time to return them we just get the values from hashmap 1 and divide by the number of gpx files
        * which is given to us by hashmap 2*/
        int gpx_number;

        if(!individual_stats.containsKey(customer_id)){
            gpx_number = 1;
            synchronized (gpx_per_user) {
                gpx_per_user.put(customer_id, gpx_number);
            }
            synchronized (individual_stats) {
                individual_stats.put(customer_id, res.clone());
            }
        }
        else {
            synchronized (gpx_per_user) {
                gpx_number = gpx_per_user.get(customer_id);
                gpx_per_user.put(customer_id, ++gpx_number);
            }

            String[] old_results = individual_stats.get(customer_id);
            String[] new_results = new String[old_results.length];
            for (int i = 0; i < old_results.length; i++) {
                new_results[i] = Double.toString(Double.parseDouble(old_results[i]) + Double.parseDouble(res[i]));
            }
            synchronized (individual_stats) {
                individual_stats.put(customer_id, new_results);
            }
        }

    }





    /* Server Implementation */

    public void openServer(){
        try {
            Thread t;

            /* Create Server Sockets */

            /* The sockets that receive the requests */
            ServerSocket serverSocket = new ServerSocket(port, 10);

            while (true) {
                /* Accept the connections via the providerSockets */
                /* Handle the request depending on the port number*/

                provider = serverSocket.accept();
                if(this.port == client_port) {
                    t = new ActionsForClients(provider, this);
                    t.start();
                }else if(this.port == worker_port){
                    String ip = provider.getInetAddress().toString();
                    if(worker_ips.contains(ip) || !(worker_ips.size() == num_of_workers)) {
                        t = new ActionsForWorkers(provider, this);
                        addToRRQ(ip, t);
                    }
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
               provider.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*For round robbin*/

    private void addToRRQ(String ip,Thread worker_thread){

        /*The index of the ip signifies the ID of worker in the hashmap*/
        /*for example if an ip is in the second Node of the list it means that it belongs to our second worker*/

        if(!worker_ips.contains(ip)) {
            System.out.println(ip+" Accepted as Worker");
            worker_ips.add(ip);
        }

        workers.get(worker_ips.indexOf(ip)).add(worker_thread);
    }


    private synchronized void startWorkerThread(){
        while(workers.get(rrIterator).isEmpty()){/*Blocks if workerQueue is empty*/}
        Thread t = workers.get(rrIterator).remove();
        t.start();
        if(num_of_workers>=2){rrIterator = (++rrIterator)%num_of_workers;}

    }


    @Override
    public void run(){
        openServer();
    }


                /*OPEN THE SERVER*/
    public static void main(String[] args){

        /*I am trying to run two separate threads of the server
         *The first thread handles the client requests
         * The second thread handles the worker requests*/

        num_of_workers = Integer.parseInt(args[0]);



        Thread m_client = new Master(client_port); /*Handles the clients*/

        Thread m_worker = new Master(worker_port); /*Handles the workers*/

        m_client.start();
        m_worker.start();
        
    }


}





