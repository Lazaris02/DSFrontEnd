import java.io.*;
import java.net.*;
import java.util.List;

public class ActionsForClients extends Thread {
    /* This class is used to perform the actions the app clients ask for */
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Master master; /*The thread needs to know the master thread it belongs to so that master
                            can send the mapped pairs to the workers*/

    private static int id =0;

    public ActionsForClients(Socket connection, Master master){
        try{
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            this.master = master;

        }catch(IOException exc){exc.printStackTrace();}
    } // Constructor

    /* Getters */
   private static synchronized String createGpxId(){
       return String.valueOf(++id);
   }

    /*Functions used in run*/
    private String getCreator(String line){
        /*gets the "creator of the string"*/
        String[] words = line.split("creator=");
        return words[1].split("\"")[1];
    }

    @Override
    public void run() {
        /*Read the gpx file from the input stream
         * read the name of the client from the gpx file
         * start a for loop and send lines from the gpx file + id of gpx file
         * String client_id + String [] waypoint_lines
         * when the gpx file is done the thread blocks until I have reduced
         * the intermediate results
         * after that the reduce process starts and the file*/

        try{
            List<String> file = (List<String>) in.readObject();

            /*find the "creator" and use it as an id */


            file.remove(0); // first line is useless
            String line = file.remove(0); // second line contains the creator

            String creator = getCreator(line);// should return user1 f.e
            String gpxId = createGpxId(); // we also need an id for the gpx file

            String[] key = {gpxId,creator};

            /*Split into waypoints -- every waypoint is within <wpt> </wpt>
             */

            String [] waypoint_lines = new String[4];

            /*The last waypoint of the chunk must also be the first to not lose part of the distance*/

            int chunk_size = master.getChunk_size();
            int chunk_counter = 0;

            boolean last_waypoint = false;
            line = file.remove(0);


            while(!line.contains("</gpx>")){

                int i =0;
                while(!line.contains("</wpt>")){
                    if(i!=0)
                        line = file.remove(0);
                    waypoint_lines[i] = line;
                    i++;
                } /*Creates the waypoint*/


                line = file.remove(0);
                if(line.contains("</gpx>")){last_waypoint = true;} /*Checks if it is the last one*/

                master.map(key,waypoint_lines,last_waypoint); /*send the waypoint for mapping*/

                chunk_counter++;
                if(chunk_counter == chunk_size){
                    master.map(key.clone(),waypoint_lines.clone(),last_waypoint);
                    chunk_counter =1;
                }

            }

            String[] final_results;

            /*Takes the reduced results and sends them back*/
            while((final_results=Master.getCustRes(gpxId))==null){/*Blocks here*/}

            System.out.println("Sending back to Client..."+" "+Thread.currentThread().getName());

            out.writeObject(Master.getCustRes(gpxId));
            out.flush();

            out.writeObject(master.getTotalStats());
            out.flush();

            /*key[1]---->creator f.e creator1*/
            out.writeObject(master.getIndividualStats(key[1]));
            out.flush();

        }catch (IOException exc){
            exc.printStackTrace();
        }catch (ClassNotFoundException exc){
            throw new RuntimeException(exc);
        }
    }


}
