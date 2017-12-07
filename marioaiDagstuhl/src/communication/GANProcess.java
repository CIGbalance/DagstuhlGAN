package communication;

import java.io.*;
import java.util.Scanner;

import static basicMap.Settings.*;
import static basicMap.Settings.PY_NAME;

public class GANProcess extends Comm {


    public GANProcess() {
        super();
        this.threadName = "GANThread";

    }

    /**
     * Launch GAN, this should be called only once
     */
    public void launchGAN() {
        // TODO: 07/12/2017   to change to the correct line command
        ProcessBuilder builder = new ProcessBuilder("python", PY_NAME);
        builder.redirectErrorStream(true);
        try {
            this.process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initBuffers() {
        //Initialize input and output
        if (this.process != null) {
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            //this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            this.scanner = new Scanner(this.process.getInputStream());
            this.writer = new PrintStream(this.process.getOutputStream());
        } else {
            printErrorMsg("GANProcess:initBuffers:Null process!");
        }
    }

    @Override
    public void start()
    {
        try {
            launchGAN();
            initBuffers();
            printInfoMsg(this.threadName + " has started");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}