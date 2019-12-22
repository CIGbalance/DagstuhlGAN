package communication;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;

import static basicMap.Settings.*;

public class GANProcess extends Comm {
    public String GANPath = null;
    public String GANDim = null;

    public GANProcess() {
        super();
        this.threadName = "GANThread";
    }
    
    public GANProcess(String GANPath, String GANDim) {
        super();
        this.threadName = "GANThread";
        this.GANPath = GANPath;
        this.GANDim = GANDim;
    }

    /**
     * Launch GAN, this should be called only once
     */
    public void launchGAN() {
        System.out.println(PYTHON_PROGRAM);
    	
    		/*if(!(new File(PYTHON_PROGRAM).exists())) {
    			throw new RuntimeException("Before launching this program, you need to configure Settings.PYTHON_PROGRAM " +
    									  "to point to the correct version of Python you intend to use on your system. If " +
    									  "using the Wasserstein GAN, this Python version must support PyTorch.");
    		}*/
    	
        // Run program with model architecture and weights specified as parameters
        ProcessBuilder builder = null;
        if(this.GANPath == null){
        	// 10 is the original number of tiles in the GECCO 2018 GAN, but the default later changed to 13. This setting overrides it.
            builder = new ProcessBuilder(PYTHON_PROGRAM, WASSERSTEIN_PATH, WASSERSTEIN_GAN, GAN_DIM, "10");
            this.GANPath = WASSERSTEIN_PATH;
            this.GANDim = GAN_DIM; // Set here to avoid a null pointer later
        }else{
            builder = new ProcessBuilder(PYTHON_PROGRAM, WASSERSTEIN_PATH, this.GANPath, this.GANDim);
        }
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