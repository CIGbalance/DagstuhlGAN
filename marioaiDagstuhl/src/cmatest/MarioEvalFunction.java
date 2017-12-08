package cmatest;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import ch.idsia.tools.EvaluationInfo;
import cmatest.marioobjectives.MarioLevelObjective;
import communication.Comm;
import communication.GANProcess;
import communication.MarioProcess;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import reader.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import static basicMap.Settings.PY_NAME;
import static basicMap.Settings.printErrorMsg;
import static basicMap.Settings.printInfoMsg;
import static communication.Commands.START_COMM;
import static communication.ZCreator.printVectorInGson;

public class MarioEvalFunction implements IObjectiveFunction {
    private GANProcess ganProcess;
    private MarioProcess marioProcess;

    public static void main(String[] args) throws IOException {
        MarioEvalFunction mef = new MarioEvalFunction();

    }

    // changing floor will change the reason for termination
    // (in conjunction with the target value)
    // see cma.options.stopFitness


    public MarioEvalFunction() throws IOException {
        // set up process for GAN
        ganProcess = new GANProcess();
        ganProcess.start();
        // set up mario game
        marioProcess = new MarioProcess();
        marioProcess.start();        
		// consume all start-up messages that are not data responses
		String response = "";
		while(!response.equals("READY")) {
			response = ganProcess.commRecv();
		}
    }

    static double floor = 0.0;

    public void sendZVectorToGan(double[][] x) throws IOException {
        // TODO: 07/12/2017 add send v to Gan
        String gsonVector = printVectorInGson(x);
        // send it to GAN
        ganProcess.commSend(gsonVector);
    }

    public void getGsonLevelFromGAN() {
        String response = ganProcess.commRecv();
        if (response == null || response == "") {
            printErrorMsg("No level in Gson from GAN.");
        } else {
            printInfoMsg("getGsonLevelFromGAN: received " + response);
        }
    }

    @Override
    public double valueOf(double[] x) {
        // Interpret x to a level
    	try {
    		ganProcess.commSend("[" + Arrays.toString(x) + "]");
    		String levelString = ganProcess.commRecv(); // Response to command just sent
//    		System.out.println("----------------------------------");
//    		System.out.println(levelString); // debugging
//    		if(levelString.equals("")) System.out.println(ganProcess.commRecv());
//    		System.out.println("----------------------------------");
    		// For debugging
//    		while(response != null) {
//    			response = ganProcess.commRecv();
//    		}    		
    		List<String> input = new ArrayList<String>(1); // Will only contain one level
    		input.add(levelString); // "File" with only one line
    		List<List<List<Integer>>> allLevels = JsonReader.JsonToIntFromFile(input);
    		List<List<Integer>> listForm = allLevels.get(0); // Assumes there is only one level
            Level level = LevelParser.createLevelJson(listForm);

    		// Do a simulation
    		EvaluationInfo info = this.marioProcess.simulateOneLevel(level);
    		// Jacob 2017-12-07: This assumes the fitness is the distance travelled
    		//                   in the level. May need to generalize
    		return info.computeDistancePassed();

		} catch (IOException e) {
			// Error occurred
			e.printStackTrace();
			System.exit(1);
			return Double.NaN;
		}
    }

    @Override
    public boolean isFeasible(double[] x) {
        return true;
    }



}
