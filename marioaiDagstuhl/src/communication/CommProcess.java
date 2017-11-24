package communication;

import basicMap.Settings;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import static basicMap.Settings.*;

public class CommProcess {
    private Scanner in;
    private PrintStream out;
    private static int THRESHOLD = 60000;

    private String processCommRecv(){
        String ret = null;
        if (in.hasNextLine()) {
            // TODO: 24/11/2017 to be continued 
        }
        return null;
    }

    public void executeCmd(String cmdStr) throws IOException {
        Process client;
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
        if (nbArgs == 5) {
            builder = new ProcessBuilder(PY_NAME, args[0], args[1], args[2]);
        } else {
            builder = new ProcessBuilder(PY_NAME, args[0], args[1], args[2]);
        }
        builder.redirectErrorStream(true);
        client = builder.start();
    }
}
