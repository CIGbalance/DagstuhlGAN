package reader;


import ch.idsia.mario.engine.GlobalOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vv + Amy
 */
public class JsonReader {
    private List<List<List<Integer>>> json;
    int current;
    
    public JsonReader(String filename){
        if(GlobalOptions.JsonAsString){
            json = JsonToInt(filename);
        }else{
            json = JsonToIntFromFile(filename);
        }
        current = 0;
    }
    
    public int getNumber(){
        return json.size();
    }
    
    public List<List<Integer>> getLevel(int ind){
        return json.get(ind);
    }
    
    public boolean hasNext(){
        return current<=json.size();
    }
    
    public List<List<Integer>> next(){
        current++;
        return json.get(current-1);
    }
    
    /**
     * Creates integer list representation of multiple levels from a json
     * file whose path is designated by the parameter.
     * @param fileLocation Path to json file
     * @return List of levels: A level is a list of rows, and each integer
     *         represents a tile/sprite type.
     */
    public static List<List<List<Integer>>> JsonToIntFromFile(String fileLocation)
    {    	
    	//array, array, array int
    	List<String> lines = new ArrayList<String>();
    	try 
    	{
    		lines = Files.readAllLines(Paths.get(fileLocation), Charset.defaultCharset());
    	} 
    	catch (IOException e1) 
    	{
    		e1.printStackTrace();
    	}

    	return JsonToIntFromFile(lines);
    }

    /**
     * Jacob: Designed entry point into method that does not require a file,
     * only the contents of a file
     * @param lines Array of the individual lines within the file
     * @return List of levels: A level is a list of rows, and each integer
     *         represents a tile/sprite type.
     */
    public static List<List<List<Integer>>> JsonToIntFromFile(List<String> lines)
    {
        StringBuilder jsonStringBuilder = new StringBuilder(); 
    	for(String s: lines) // Need to use StringBuilder here for efficiency
    		jsonStringBuilder.append(s);
    	
    	String myJSONString=jsonStringBuilder.toString();
    	JsonArray jarray1 = new Gson().fromJson(myJSONString, JsonArray.class);//first array
    
    	List<List<List<Integer>>> myReturnList = new ArrayList<List<List<Integer>>>();
    	
    	for(int i = 0; i < jarray1.size();i++)
    	{
    		List<List<Integer>> myFirstSubList = new ArrayList<List<Integer>>();
    		JsonArray jarrayi = ((JsonArray)jarray1.get(i));
    		for(int j = 0; j < jarrayi.size();j++)
    		{
    			List<Integer> mySecondSubList = new ArrayList<Integer>();
    			JsonArray jarrayj = ((JsonArray)jarrayi.get(j));
    			for(JsonElement je: jarrayj)
    			{
    				mySecondSubList.add(je.getAsInt());
    			}
    			myFirstSubList.add(mySecondSubList);
    		}
    		myReturnList.add(myFirstSubList);
    	}	
    	return myReturnList;
    }



    // The method above may unnecessarily duplicate some functionality of this method
    public static List<List<List<Integer>>> JsonToInt(String myJSONString)
    {   JsonArray jarray1 = new Gson().fromJson(myJSONString, JsonArray.class);//first array
    
    	List<List<List<Integer>>> myReturnList = new ArrayList<List<List<Integer>>>();
    	
    	for(int i = 0; i < jarray1.size();i++)
    	{
    		List<List<Integer>> myFirstSubList = new ArrayList<List<Integer>>();
    		JsonArray jarrayi = ((JsonArray)jarray1.get(i));
    	    System.out.println(jarrayi);
    	    for(int j = 0; j < jarrayi.size();j++)
    		{
    			List<Integer> mySecondSubList = new ArrayList<Integer>();
    			JsonArray jarrayj = ((JsonArray)jarrayi.get(j));
    			for(JsonElement je: jarrayj)
    			{
    				mySecondSubList.add(je.getAsInt());
    			}
    			myFirstSubList.add(mySecondSubList);
    		}
    		myReturnList.add(myFirstSubList);
    	}	
    	return myReturnList;
    }

	public static double[] JsonToDoubleArray(String myJSONString) {
		JsonArray jarray1 = new Gson().fromJson(myJSONString, JsonArray.class);//first array
		List<Double> myList = new ArrayList<>();
		for(JsonElement je: jarray1)
		{
			myList.add(je.getAsDouble());
		}
		double[] myArray = new double[myList.size()];
		for (int i=0; i<myList.size(); i++) {
			myArray[i] = myList.get(i);
		}
		return myArray;
	}
}
