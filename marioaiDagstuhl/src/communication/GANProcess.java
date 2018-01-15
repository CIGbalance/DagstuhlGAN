package communication;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;

import static basicMap.Settings.*;
import static basicMap.Settings.PY_NAME;

public class GANProcess extends Comm {

	// Use Sebastian's Wasserstein GAN instead of Adam's generic GAN
	public static final boolean WASSERSTEIN = true;
	public static final String WASSERSTEIN_PATH = "pytorch" + File.separator + "generator_ws.py";
	public static final String WASSERSTEIN_GAN = "pytorch" + File.separator + "netG_epoch_24.pth";
	
    public GANProcess() {
        super();
        this.threadName = "GANThread";
    }

    /**
     * Launch GAN, this should be called only once
     */
    public void launchGAN() {
        // Run program with model architecture and weights specified as parameters
        ProcessBuilder builder = WASSERSTEIN ?
        		new ProcessBuilder("python", WASSERSTEIN_PATH, WASSERSTEIN_GAN) :
        		new ProcessBuilder("python", PY_NAME, GAN_ARCHITECTURE_FILE, GAN_WEIGHTS_FILE);
        builder.redirectError(Redirect.INHERIT); // Standard error will print to console
        	try {
        		System.out.println(builder.command());
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