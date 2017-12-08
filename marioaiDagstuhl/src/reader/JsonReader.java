/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reader;


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
        json = JsonToIntFromFile(filename);
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
    
    
    public List<List<List<Integer>>> JsonToIntFromFile(String fileLocation)
    {    	
    	//array, array, array int
    	List<String> lines = new ArrayList();
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
     * @param lines
     * @return
     */
    public static List<List<List<Integer>>> JsonToIntFromFile(List<String> lines)
    {
        StringBuilder jsonStringBuilder = new StringBuilder(); 
    	for(String s: lines) // Need to use StringBuilder here for efficiency
    		jsonStringBuilder.append(s);
    	
    	String myJSONString=jsonStringBuilder.toString();
    	JsonArray jarray1 = new Gson().fromJson(myJSONString, JsonArray.class);//first array
    
    	List<List<List<Integer>>> myReturnList = new ArrayList();
    	
    	for(int i = 0; i < jarray1.size();i++)
    	{
    		List<List<Integer>> myFirstSubList = new ArrayList();
    		JsonArray jarrayi = ((JsonArray)jarray1.get(i));
    		for(int j = 0; j < jarrayi.size();j++)
    		{
    			List<Integer> mySecondSubList = new ArrayList();
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
    
    public List<List<List<Integer>>> JsonToInt(String myJSONString)
    {    	
    	JsonArray jarray1 = new Gson().fromJson(myJSONString, JsonArray.class);//first array
    
    	List<List<List<Integer>>> myReturnList = new ArrayList();
    	
    	for(int i = 0; i < jarray1.size();i++)
    	{
    		List<List<Integer>> myFirstSubList = new ArrayList();
    		JsonArray jarrayi = ((JsonArray)jarray1.get(i));
    		for(int j = 0; j < jarrayi.size();j++)
    		{
    			List<Integer> mySecondSubList = new ArrayList();
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
}
