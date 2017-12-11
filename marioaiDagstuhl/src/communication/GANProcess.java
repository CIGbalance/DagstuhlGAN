package communication;

import java.io.*;
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
        // Run program with model architecture and weights specified as parameters
        ProcessBuilder builder = new ProcessBuilder("python", PY_NAME, GAN_ARCHITECTURE_FILE, GAN_WEIGHTS_FILE);
        try {
            this.process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Buffers used for communicating with process via stdin and stdout
     */
    @Override
    public void initBuffers() {
        //Initialize input and output
        if (this.process != null) {
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            this.writer = new PrintStream(this.process.getOutputStream());
            System.out.println("Process buffers initialized");
        } else {
            printErrorMsg("GANProcess:initBuffers:Null process!");
        }
    }

    @Override
    public void start() {
        try {
            launchGAN();
            initBuffers();
            printInfoMsg(this.threadName + " has started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}