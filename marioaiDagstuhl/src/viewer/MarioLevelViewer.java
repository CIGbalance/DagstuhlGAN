package viewer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.mario.engine.LevelRenderer;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import cmatest.MarioEvalFunction;

public class MarioLevelViewer {

	public static final int BLOCK_SIZE = 16;
	public static final int LEVEL_HEIGHT = 12;
	
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

		int relevantWidth = excludeBufferRegion ?
				(level.width - (2*LevelParser.BUFFER_WIDTH)) * BLOCK_SIZE :
				level.width;
		BufferedImage image = new BufferedImage(relevantWidth, LEVEL_HEIGHT*BLOCK_SIZE, BufferedImage.TYPE_INT_ARGB);
		// Skips buffer zones at start and end of level
		LevelRenderer.renderArea((Graphics2D) image.getGraphics(), level, 0, 0, LevelParser.BUFFER_WIDTH*BLOCK_SIZE, 0, relevantWidth, LEVEL_HEIGHT*BLOCK_SIZE);
		return image;
	}
	
	public static void main(String[] args) throws IOException {
		// This is used because it contains code for communicating with the GAN
		MarioEvalFunction eval = new MarioEvalFunction();
		// Provide the vector: preferably from command line
		Level level = eval.levelFromLatentVector(new double[] {});
		
		BufferedImage image = getLevelImage(level, true);
		
		ImageIO.write(image, "jpg", new File("Level.jpg"));
	}
}
