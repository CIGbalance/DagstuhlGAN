package viewer;

import static reader.JsonReader.JsonToDoubleArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import basicMap.Settings;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import cmatest.MarioEvalFunction;
import communication.MarioProcess;
import reader.JsonReader;

/**
 * This file allows you to generate a level and play it for any latent vector
 * or your choice. The vector must have a length of 32 numbers separated
 * by commas enclosed in square brackets [ ]. For example,
 * [0.9881835842209917, -0.9986077315374948, 0.9995512051242508, 0.9998643432807639, -0.9976165917284504, -0.9995247114230822, -0.9997001909358728, 0.9995694511739592, -0.9431036754879115, 0.9998155541290887, 0.9997863689962382, -0.8761392912669269, -0.999843833016589, 0.9993230720045649, 0.9995470247917402, -0.9998847606084427, -0.9998322053148382, 0.9997707200294411, -0.9998905141832997, -0.9999512510490688, -0.9533512808031753, 0.9997703088007039, -0.9992229823819915, 0.9953917828622341, 0.9973473366437476, 0.9943030781608361, 0.9995290290713732, -0.9994945079679955, 0.9997109900652238, -0.9988379572928884, 0.9995070647543864, 0.9994132207570211]
 * 
 * Additionally, if you send in a 2D array where each sub-array has length 32, this will be interpreted as several
 * latent vectors. Each latent vector will create a separate level segment, and the segments will be stitched together
 * into one level for you to play.
 * 
 */
public class MarioLevelPlayer {

	public static final int BLOCK_SIZE = 16;
	public static final int LEVEL_HEIGHT = 14;	

	public static void main(String[] args) throws IOException {
		Settings.setPythonProgram();
		// This is used because it contains code for communicating with the GAN
		MarioEvalFunction eval = new MarioEvalFunction();

		Level level;
		// Read input level
		String strLatentVector = "";
		if (args.length > 0) {
			StringBuilder builder = new StringBuilder();
			for (String str : args) {
				builder.append(str);
			}
			strLatentVector = builder.toString();
			Settings.printInfoMsg("Passed vector(s): " + strLatentVector);
			// If the input starts with two square brackets, then it must be an array of arrays,
			// and hence a series of several latent vectors rather than just one. In this case,
			// patch all of the levels together into one long level.
			if(strLatentVector.subSequence(0, 2).equals("[[")) {
				// remove opening/closing brackets
				strLatentVector = strLatentVector.substring(1,strLatentVector.length()-1);
				String levels = "";
				while(strLatentVector.length() > 0) {
					int end = strLatentVector.indexOf("]")+1;
					String oneVector = strLatentVector.substring(0,end);
					System.out.println("ONE VECTOR: " + oneVector);
					levels += eval.stringToFromGAN(oneVector); // Use the GAN
					strLatentVector = strLatentVector.substring(end); // discard processed vector
					if(strLatentVector.length() > 0) {
						levels += ",";
						strLatentVector = strLatentVector.substring(1); // discard leading comma
					}
				}
				levels = "["+levels+"]"; // Put back in brackets
				System.out.println(levels);
				List<List<List<Integer>>> allLevels = JsonReader.JsonToInt(levels);
				// This list contains several separate levels. The following code
				// merges the levels by appending adjacent rows
				ArrayList<List<Integer>> oneLevel = new ArrayList<List<Integer>>();
				// Create the appropriate number of rows in the array
				for(List<Integer> row : allLevels.get(0)) { // Look at first level (assume all are same size)
					oneLevel.add(new ArrayList<Integer>()); // Empty row
				}
				// Now fill up the rows, one level at a time
				for(List<List<Integer>> aLevel : allLevels) {
					int index = 0;
					for(List<Integer> row : aLevel) { // Loot at each row
						oneLevel.get(index++).addAll(row);
					}	
				}
				// Now create the Mario level from the combined list representation
				level = LevelParser.createLevelJson(oneLevel);
			} else { // Otherwise, there must be a single latent vector, and thus a single level
				double[] latentVector = JsonToDoubleArray(strLatentVector);
				level = eval.levelFromLatentVector(latentVector);
			}
		} else {
			System.out.println("Generating level with default vector");
			level = eval.levelFromLatentVector(new double[] {0.9881835842209917, -0.9986077315374948, 0.9995512051242508, 0.9998643432807639, -0.9976165917284504, -0.9995247114230822, -0.9997001909358728, 0.9995694511739592, -0.9431036754879115, 0.9998155541290887, 0.9997863689962382, -0.8761392912669269, -0.999843833016589, 0.9993230720045649, 0.9995470247917402, -0.9998847606084427, -0.9998322053148382, 0.9997707200294411, -0.9998905141832997, -0.9999512510490688, -0.9533512808031753, 0.9997703088007039, -0.9992229823819915, 0.9953917828622341, 0.9973473366437476, 0.9943030781608361, 0.9995290290713732, -0.9994945079679955, 0.9997109900652238, -0.9988379572928884, 0.9995070647543864, 0.9994132207570211});
		}

		MarioProcess marioProcess = new MarioProcess();
		marioProcess.launchMario(new String[0], true); // true means there is a human player       
		marioProcess.simulateOneLevel(level);
		
                eval.exit();
		System.exit(0);
	}
}
