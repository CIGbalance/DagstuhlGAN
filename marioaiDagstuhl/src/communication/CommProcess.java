package communication;

import basicMap.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;

import static basicMap.Settings.*;

public class CommProcess {
    private Scanner in;
    private PrintStream out;
    private static int THRESHOLD = 60000;

    public static void main(String[] args) {

        CommProcess commProcess = new CommProcess();



    }

    private String processCommRecv(){
        String ret = null;
        if (in.hasNextLine()) {
            // TODO: 24/11/2017 to be continued 
        }
        return null;
    }

    public void executeCmd(String cmdStr) throws IOException {
        ProcessBuilder builder;
        if (cmdStr == null) {
            printWarnMsg("The input cmd to execute is null.");
            return;
        }

        if (cmdStr == "") {
            printWarnMsg("The input cmd to execute is empty.");
            return;
        }
        // Now get the cmd and run
        printInfoMsg("Will run cmd: " + PY_NAME + cmdStr);
        String[] args = cmdStr.split(CMD_SEPARATOR);
        int nbArgs = args.length;
        // TODO: 24/11/2017 check the length and input format
        if (nbArgs == 1) {
            builder = new ProcessBuilder("python", PY_NAME, args[0], "<", "z.jsons" , ">", "levels.jsons");
            builder.redirectErrorStream(true);

            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            // TODO: 04/12/2017 change the stopping condition 
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
                System.out.println(line);
            }
        } else {
            System.err.println("The input to " + PY_NAME + " is missing.");
        }


}
