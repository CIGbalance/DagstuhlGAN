package communication;

import java.io.*;
import java.util.Scanner;

import static basicMap.Settings.printErrorMsg;
import static basicMap.Settings.printInfoMsg;
import static basicMap.Settings.printWarnMsg;
import static communication.Commands.*;

public abstract class Comm extends Thread {
    public static final int THRESHOLD = 60000; // milliseconds

    protected boolean end;

    protected String threadName = "thread";
    protected BufferedReader reader;
    protected PrintStream writer;
    protected Process process;

    /**
     * Default constructor
     */
    public Comm() {
        super();
        this.end = false;
    }

    public abstract void initBuffers();

    /**
     * Sends a message
     *
     * @param msg message to send.
     */
    public void commSend(String msg) throws IOException {
        printInfoMsg("[" + this.threadName + "] Comm:commSend will send "+ msg + " to GAN");
        writer.println(msg);
        writer.flush();
    }


    /**
     * This function is called at the end of the whole process. Closes the communication.
     */
    public boolean endComm(){
        try {
            commSend(END_COMM);
            String response = commRecv();
            if (response == null) {
                printWarnMsg("Null response. Failed to start the communication.");
                return false;
            }
            if (response.equalsIgnoreCase(END_FAILED)) {
                printWarnMsg("Failed to end the communication.");
                return false;
            }
            if (response.equalsIgnoreCase(END_SUCCEED)) {
                printInfoMsg("Successfully end the communication.");
                return true;
            }
            printWarnMsg("Unrecognized response.");
            return false;
        } catch(Exception e) {
            System.out.println("Error:");
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Receives a message.
     *
     * @return the response
     */
    public String commRecv() {
    	String msg = processCommRecv();
    	return msg;
    }

    private String processCommRecv(){
    	String msg = null;
    	//System.out.println("processCommRecv attempt: " + reader.ready() + " " + reader);
    	//System.out.println("processCommRecv attempt: " + scanner);
    	try {
    		msg = reader.readLine();
    		System.out.println("processCommRecv:"+msg);
    		//msg = scanner.nextLine();
    		if (msg != null) {
    			return msg;
    		} else {
    			printErrorMsg("processCommRecv: Null message.");
    			return null;
    		}
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
		printErrorMsg("processCommRecv: exception.");
		return null;
    }


}
