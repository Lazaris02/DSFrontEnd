# Activity Tracker Application
A Fitness application that resemlbes Strava.

## General Description
The app receives .gpx files as input from the android device, then calculates the distance described in the .gpx file using the Map-Reduce technique. Finally the server (Master) returns some statistics back to the application (Individual performance and Compared to other users.)

The statistics calculated with the Map-Reduce process are:
1. Average speed
2. Total Distance
3. Total Ascent
4. Total Time


## Backend

### Key Functionality
Map : expects a key-value pair from a segment of the gpx file
and produced an intermediate result (another key-value pair) - this action is performed from the Master.
Use of intermidiate results to calculate the statistics: the 
key value pairs (Chunks) are sent to the workers using a round robin queue. The workers proceed to make the calculations and return another key-value pair back to the Master.
Reduce: After all the chunks that belong to a specific .gpx file have been processed , the master performs a final calculation and sends the final results back to the applicationn

### Notable features
The connection between Master and Client (Android Application) is implemented via TCP sockets.
The Connection between Master - Worker is implemented via TCP sockets
Each Worker is multithreaded , in this case each worker has 4 threads.
The Master distributes segments of the gpx files to Workers with a round-robin queue.

