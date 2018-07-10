package viewer;

import static reader.JsonReader.JsonToDoubleArray;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import basicMap.Settings;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.mario.engine.LevelRenderer;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import cmatest.MarioEvalFunction;
import reader.JsonReader;
import competition.icegic.robin.AStarAgent;

/**
 * This file allows you to generate a level image for any latent vector
 * or your choice. The vector must have a length of 32 numbers separated
 * by commas enclosed in square brackets [ ]. For example,
 * [0.9881835842209917, -0.9986077315374948, 0.9995512051242508, 0.9998643432807639, -0.9976165917284504, -0.9995247114230822, -0.9997001909358728, 0.9995694511739592, -0.9431036754879115, 0.9998155541290887, 0.9997863689962382, -0.8761392912669269, -0.999843833016589, 0.9993230720045649, 0.9995470247917402, -0.9998847606084427, -0.9998322053148382, 0.9997707200294411, -0.9998905141832997, -0.9999512510490688, -0.9533512808031753, 0.9997703088007039, -0.9992229823819915, 0.9953917828622341, 0.9973473366437476, 0.9943030781608361, 0.9995290290713732, -0.9994945079679955, 0.9997109900652238, -0.9988379572928884, 0.9995070647543864, 0.9994132207570211]
 * 
 */
public class MarioLevelViewer {

	public static final int BLOCK_SIZE = 16;
	public static final int LEVEL_HEIGHT = 14;
	
	/**
	 * Return an image of the level, excluding 
	 * the background, Mario, and enemy sprites.
	 * @param level
	 * @return
	 */
	public static BufferedImage getLevelImage(Level level, boolean excludeBufferRegion) {
		EvaluationOptions options = new CmdLineOptions(new String[0]);
		ProgressTask task = new ProgressTask(options);
		// Added to change level
                options.setLevel(level);
		task.setOptions(options);

		int relevantWidth = (level.width - (excludeBufferRegion ? 2*LevelParser.BUFFER_WIDTH : 0)) * BLOCK_SIZE;
		BufferedImage image = new BufferedImage(relevantWidth, LEVEL_HEIGHT*BLOCK_SIZE, BufferedImage.TYPE_INT_ARGB);
		// Skips buffer zones at start and end of level
		LevelRenderer.renderArea((Graphics2D) image.getGraphics(), level, 0, 0, excludeBufferRegion ? LevelParser.BUFFER_WIDTH*BLOCK_SIZE : 0, 0, relevantWidth, LEVEL_HEIGHT*BLOCK_SIZE);
		return image;
	}

	/**
	 * Save level as an image
	 * @param level Mario Level
	 * @param name Filename, not including jpg extension
	 * @param clipBuffer Whether to exclude the buffer region we add to all levels
	 * @throws IOException
	 */
	public static void saveLevel(Level level, String name, boolean clipBuffer) throws IOException {
		BufferedImage image = getLevelImage(level, clipBuffer);


		File file = new File(name + ".png");
		ImageIO.write(image, "png", file);
		System.out.println("File saved: " + file);
	}

	public static void main(String[] args) throws IOException {
		Settings.setPythonProgram();
		// This is used because it contains code for communicating with the GAN
                String GanPath = "/media/vv/DATA/svn/gbea/code-experiments/rw-problems/gan-mario/GAN/overworlds-10-5000/netG_epoch_4999_5641.pth";
                
		MarioEvalFunction eval = new MarioEvalFunction(GanPath, "10", 27, new AStarAgent());

		Level level;
		// Read input level
		String strLatentVector = "[[0.399468679,0.558408489,0.648763207,-0.475929351,-0.17576051,0.157533175,0.322191532,0.103245111,0.0822296638,-0.894546112],[-0.679256326,-0.643430687,0.146795224,-0.663548282,-0.767950146,-0.265928491,-0.907324051,0.545989637,-0.283232998,-0.737325743],[0.547165207,-0.409930497,-0.92882174,0.564390761,0.594278304,0.608712547,-0.803062922,-0.11755018,-0.915811674,-0.0204875417],[0.400866802,0.81029031,-0.900207679,0.737290572,-0.0333398143,-0.422349962,-0.495400763,0.887581723,-0.876503077,0.467294395],[0.449801353,-0.358109518,0.367348703,0.513199543,-0.931694362,0.23088336,0.230901976,0.0306934656,-0.778126148,-0.768768305],[-0.118502759,-0.92448363,0.928024756,0.456140581,0.279709475,-0.338058615,-0.974212895,0.573614058,0.255235263,0.833261622],[-0.481838742,-0.775280449,-0.05769006,0.770488205,-0.693994697,-0.922075671,0.935639745,-0.800797006,0.0140820693,0.49699932],[0.0556630278,0.914185724,0.0024008508,0.526156424,-0.606877576,0.249522793,0.0172895353,-0.233472308,0.771374213,0.189263971],[0.135723986,-0.305053359,0.575282448,0.45330423,-0.0418926899,0.172883926,-0.94344013,0.497231268,0.551034248,0.602603982],[-0.064456038,0.378262589,-0.526901629,-0.412364395,0.319589535,-0.622127062,-0.548949834,-0.139416146,-0.0521241853,-0.169519469],[-0.767006857,0.793759404,0.241370542,-0.311046783,0.363733449,-0.0519150191,-0.885912042,-0.726715415,-0.96184896,0.509438327]]";
                if(true){
		/*if (args.length > 0) {
			StringBuilder builder = new StringBuilder();
			for (String str : args) {
				builder.append(str);
			}
			strLatentVector = builder.toString();*/
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

		saveLevel(level, "LevelClipped", true);
		saveLevel(level, "LevelFull", false);
		eval.exit();
		System.exit(0);
	}
}
