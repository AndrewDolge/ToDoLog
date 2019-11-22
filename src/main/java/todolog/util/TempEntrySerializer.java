
package todolog.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
 /**
 * Class that serializes temporary string content and it's timestamp.
 * 
 */
public class TempEntrySerializer{

    private static Logger log = LogManager.getLogger(TempEntrySerializer.class.getName());

    private Map<Integer, TempContent> tempEntries;

    private Gson gson;

    private final TempContent nullTempContent = new TempContent(-1, null, -1);

    public TempEntrySerializer(){

        this.tempEntries = new HashMap<Integer, TempContent>();
        gson = new Gson();

    }//constructor


    public void readFromFile(String file){

        try(InputStream in = new FileInputStream(new File(file))){
            read(in);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void writeToFile(String file){
        File f = new File(file);


        try{
            if(!f.exists()){
                f.createNewFile();
            }
        }catch(Exception e){
            e.printStackTrace();
        }


        try(OutputStream out = new FileOutputStream(f)){

            write(out);

        }catch(Exception e){
            e.printStackTrace();
        }


    }


    public void write(OutputStream out) throws IOException{

        JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));

        writer.beginArray();

        getEntries().entrySet().forEach( 
            (tc) -> {
                gson.toJson(tc.getValue(), TempContent.class, writer);
        });

        writer.endArray();
        writer.close();
    }//write

    /**
     * Reads the entries from an input stream
     * 
     * 
     * @param in
     * @throws IOException
     */
    public void read(InputStream in) throws IOException{

        JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(in, "UTF-8")));

        reader.beginArray();

        //while there are still stashed entries
        while(reader.hasNext()){    
            //load the entry, and add it to the hashmap, replacing any previously loaded Entries.
           TempContent loaded = gson.fromJson(reader, TempContent.class);
            
           getEntries().put(loaded.getID(), loaded);

        }
        reader.endArray();

        reader.close();

    }//read

    /**
     * Adds the entry to the Temporary Serializer.
     * 
     * Stores the content and timstamp, with a key of the provided id.
     * 
     */
    public void put(int id,  String content,long logtime ){
       getEntries().put(id, new TempContent(id, content,logtime));
    }

    public long getLogTime(Integer id){
        return getEntries().getOrDefault(id,nullTempContent).getLogTime();
    }

    public String getContent(Integer id){
        return  getEntries().getOrDefault(id, nullTempContent).getContent();
    }

    public boolean exists(Integer id){
        return getEntries().containsKey(id);
    }

    /**
     * Returns a shallow copy of the entry list for modification.
     * @return
     */
    private Map<Integer, TempContent> getEntries(){
        return tempEntries == null ? null : tempEntries;
    }//getEntires


    /**
     * Internal class to hold the data of the temporary content.
     */
    private final class TempContent{
        private String content;
        private long   logTime;
        private Integer id;

        

        public TempContent(int id, String content, long logTime) {
            this.content = content;
            this.logTime = logTime;
            this.id = id;
        }

        public String getContent() {
            return content;
        }


        public long getLogTime() {
            return logTime;
        }

        public Integer getID(){
            return this.id;
        }
    }

}//class