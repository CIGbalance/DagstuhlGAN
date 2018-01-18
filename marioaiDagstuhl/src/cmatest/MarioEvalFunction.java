package cmatest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import ch.idsia.tools.EvaluationInfo;
import communication.GANProcess;
import communication.MarioProcess;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import reader.JsonReader;

public class MarioEvalFunction implements IObjectiveFunction {

	// This is the distance that Mario traverses when he beats the short levels
	// that we are generating. It would need to be changed if we train on larger
	// levels or in any way change the level length.
	public static final int LEVEL_LENGTH = 704;

	private GANProcess ganProcess;
	private MarioProcess marioProcess;

	// changing floor will change the reason for termination
	// (in conjunction with the target value)
	// see cma.options.stopFitness
	static double floor = 0.0;

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

	// Incomplete, and not necessary. We only need this if
	// we switch to bulk processing.
	//    public void sendZVectorToGan(double[][] x) throws IOException {
	//        // TODO: 07/12/2017 add send v to Gan
	//        String gsonVector = sampleVectorInGson();
	//        // send it to GAN
	//        ganProcess.commSend(gsonVector);
	//    }

	/**
	 * Takes a json String representing several levels (or just one)
	 * and returns an array of all of those Mario levels.
	 * @param json Json String representation of multiple Mario levels
	 * @return Array of those levels
	 */
	// TODO: This method could be used to generate multiple levels from the json
	//       returned by generator.py, but it currently only makes sense to send
	//       generator.py one vector at a time because the CMA-ES objective function 
	//       interface explicitly expects to be able to generate a single fitness 
	//       value when given a single input vector (valueOf method below). 
	//       As long as we adhere to that interface
	//       we won't be able to do bulk processing.
	public static Level[] marioLevelsFromJson(String json) {
		List<String> input = new ArrayList<String>(1); // Will only contain one line
		input.add(json); // "File" with only one line (though there could be multiple levels)
		List<List<List<Integer>>> allLevels = JsonReader.JsonToIntFromFile(input);
		Level[] result = new Level[allLevels.size()];
		int index = 0;
		for(List<List<Integer>> listRepresentation : allLevels) {
			result[index++] = LevelParser.createLevelJson(listRepresentation);
		}
		return result;
	}    

	/**
	 * Gets objective score for single latent vector.
	 */
	@Override
	public double valueOf(double[] x) {
		// Interpret x to a level
		try {
			// Brackets required since generator.py expects of list of multiple levels, though only one is being sent here
			ganProcess.commSend("[" + Arrays.toString(x) + "]");
			String levelString = ganProcess.commRecv(); // Response to command just sent
			Level[] levels = marioLevelsFromJson(levelString); // Really only one level in this array
			Level level = levels[0];

			// Do a simulation
			EvaluationInfo info = this.marioProcess.simulateOneLevel(level);
			// Fitness is negative since CMA-ES tries to minimize
			if(info.computeDistancePassed() < LEVEL_LENGTH) { // Did not beat level
				// Only optimize distance passed in this case
				return -info.computeDistancePassed();    			
			} else { // Did beat level
				//System.out.println("Beat level!");
				// Also maximize time, since this would imply the level is more challenging/interesting
				return -info.computeDistancePassed() - info.timeSpentOnLevel; 			
			}

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
