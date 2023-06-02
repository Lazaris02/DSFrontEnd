import java.io.BufferedReader;
import java.io.Serializable;
import java.nio.Buffer;
import java.util.ArrayList;


public class Chunk implements Serializable {


    private ArrayList<String[]> data;
    public Chunk() {
        data = new ArrayList<>();
    }

    public Chunk(Chunk c){
        this.data = new ArrayList<>();

        this.data.addAll(c.getData());

    } /*Copy constructor*/


    public ArrayList<String[]> getData(){return this.data;}

    public void addData(String[] key_value){
        if(data == null){data = new ArrayList<>();}
        data.add(key_value);
    }

    public void empty_data(){data.clear();}

}
