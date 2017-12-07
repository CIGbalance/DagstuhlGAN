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
//    protected BufferedReader reader;
    protected Scanner reader;
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
        printInfoMsg("Comm:commSend will send "+ msg + " to GAN");
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
        float timeout = 0;
        String response = null;
        while (timeout < THRESHOLD && response == null)
        {
            response = processCommRecv();
        }
        if (response == null){
            System.err.println("SocketComm: commRecv: No message received. Time threshold exceeded.");
        }
        return response;
    }

    private String processCommRecv(){
        String msg = null;
        if (reader.hasNextLine()) {
            msg = reader.nextLine();
            if (msg != null) {
                PrintWriter out = null;
                try {
                    out = new PrintWriter("commwriter.txt");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                out.write(this.threadName + " read " + msg);
                return msg;
            } else {
                return null;
            }
        } else {
            printErrorMsg("No output from python");
        }
       return null;
    }

    /**
     * This function is called at the beginning of the game for
     * initialization.
     * Will give up if no "START_DONE" received after having received 11 responses
     */
    public boolean startComm() {

        try {
            commSend(START_COMM);
            String response = commRecv();
            if (response==null) {
                printWarnMsg("Null response. Failed to start the communication.");
                return false;
            }
            if(response.equalsIgnoreCase(START_FAILED)) {
                printWarnMsg("Failed to start the communication.");
                return false;
            }
            if (response.equalsIgnoreCase(START_SUCCEED)) {
                printInfoMsg("Successfully start the communication.");
                return true;
            }
            printWarnMsg("Unrecognized response.");
            return false;
        } catch (IOException e) {
            System.out.println("Error:");
            e.printStackTrace();
        }
        return false;
    }


}
