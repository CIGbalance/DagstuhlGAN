package viewer;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import basicMap.Settings;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.mario.engine.LevelRenderer;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import cmatest.MarioEvalFunction;
import com.google.gson.Gson;

import static jdk.nashorn.internal.objects.NativeArray.join;
import static reader.JsonReader.JsonToDoubleArray;

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
		BufferedImage image = new BufferedImage(relevantWidth, LEVEL_HEIGHT*BLOCK_SIZE, BufferedImage.TYPE_INT_RGB);
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


		File file = new File(name + ".jpg");
		ImageIO.write(image, "jpg", file);
		System.out.println("File saved: " + file);
	}

	public static void main(String[] args) throws IOException {
		//Settings.setPythonProgram();
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
			Settings.printInfoMsg("Passed vector: " + strLatentVector);
			double[] latentVector = JsonToDoubleArray(strLatentVector);
			level = eval.levelFromLatentVector(latentVector);
		} else {
                        double[] latentVector = {0.60804406, 1.97059734, -1.95852848, 0.93819554, 0.01075769, 0.32290900, -0.03688122, 0.49262360, 0.21605643, -0.01675038, 0.07440761, 1.36567880, -1.59015500, -0.11776762, 0.36646168, -0.48052153, -1.31933379, -0.24080577, 0.35069093, -0.07048727, -0.90313909, -2.17564029, 0.07239473, -0.05540180, -0.61889089, -0.93083144, 0.55789538, -0.33046400, 1.20411228, -1.00185340, 0.06364341, -0.66042938};
                        latentVector = MarioEvalFunction.mapArrayToOne(latentVector);
                        level = eval.levelFromLatentVector(latentVector);
		}

		saveLevel(level, "LevelClipped", true);
		saveLevel(level, "LevelFull", false);
		// Should probably terminate the GAN process in a cleaner fasion
		System.exit(0);
	}
}
