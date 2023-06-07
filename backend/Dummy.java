import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dummy extends Thread{
    private String file_path;
    private static String server_ip;

    public Dummy(String file_path){
        this.file_path = file_path;
    }

    @Override
    public void run(){
        ObjectOutputStream out= null ;
        ObjectInputStream in = null ;
        Socket requestSocket= null ;

        try {
            String host = server_ip;
            /* Create socket for contacting the server on port 4321*/
            requestSocket = new Socket(host, 5377);

            /* Create the streams to send and receive data from server */
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            /*Stores the gpx in an array of Strings*/
            List<String> file = Files.readAllLines(Paths.get(file_path));


            /*Sends the gpx file*/

            out.writeObject(file);
            out.flush();

            /*Results from Master*/

            String[] results=(String[]) in.readObject();
            Double[] stats=(Double[]) in.readObject();
            Double[] individual_results = (Double[]) in.readObject();

            System.out.println("User's total stats: "+Arrays.toString(results)+"\n"+
                    "User's average stats: "+Arrays.toString(individual_results)+"\n"+
                    "Total average stats: "+Arrays.toString(stats)+Thread.currentThread().getName()+"\n\n" );


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




    public static void main(String[] args) {
        /*Initializes the client - gets gpx file
        * starts their thread.
        * Waits for results
        * Prints results*/

            server_ip = args[0]; /*input the ip of the master --or localhost if on same device*/



            /*READS THE GPX FILES*/

        String path_1 = "./gpx_files/route1.gpx";
        String path_2 = "./gpx_files/route2.gpx";
        String path_3 = "./gpx_files/route3.gpx";
        String path_4 = "./gpx_files/route4.gpx";
        String path_5 = "./gpx_files/route5.gpx";
        String path_6 = "./gpx_files/route6.gpx";






        /*INIT CLIENTS*/

            Dummy dummy1 = new Dummy(path_1);
            Dummy dummy2 = new Dummy(path_2);
            Dummy dummy3 = new Dummy(path_3);
            Dummy dummy4 = new Dummy(path_4);
            Dummy dummy5 = new Dummy(path_5);
            Dummy dummy6 = new Dummy(path_6);

            dummy1.start();
            dummy2.start();
            dummy3.start();
            dummy4.start();
            dummy5.start();
            dummy6.start();

    }

}