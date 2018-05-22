package viewer;

import static reader.JsonReader.JsonToDoubleArray;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
import communication.MarioProcess;

/**
 * This file allows you to generate a level and play it for any latent vector
 * or your choice. The vector must have a length of 32 numbers separated
 * by commas enclosed in square brackets [ ]. For example,
 * [0.9881835842209917, -0.9986077315374948, 0.9995512051242508, 0.9998643432807639, -0.9976165917284504, -0.9995247114230822, -0.9997001909358728, 0.9995694511739592, -0.9431036754879115, 0.9998155541290887, 0.9997863689962382, -0.8761392912669269, -0.999843833016589, 0.9993230720045649, 0.9995470247917402, -0.9998847606084427, -0.9998322053148382, 0.9997707200294411, -0.9998905141832997, -0.9999512510490688, -0.9533512808031753, 0.9997703088007039, -0.9992229823819915, 0.9953917828622341, 0.9973473366437476, 0.9943030781608361, 0.9995290290713732, -0.9994945079679955, 0.9997109900652238, -0.9988379572928884, 0.9995070647543864, 0.9994132207570211]
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
			Settings.printInfoMsg("Passed vector: " + strLatentVector);
			double[] latentVector = JsonToDoubleArray(strLatentVector);
			level = eval.levelFromLatentVector(latentVector);
		} else {
			System.out.println("Generating level with default vector");
			level = eval.levelFromLatentVector(new double[] {0.9881835842209917, -0.9986077315374948, 0.9995512051242508, 0.9998643432807639, -0.9976165917284504, -0.9995247114230822, -0.9997001909358728, 0.9995694511739592, -0.9431036754879115, 0.9998155541290887, 0.9997863689962382, -0.8761392912669269, -0.999843833016589, 0.9993230720045649, 0.9995470247917402, -0.9998847606084427, -0.9998322053148382, 0.9997707200294411, -0.9998905141832997, -0.9999512510490688, -0.9533512808031753, 0.9997703088007039, -0.9992229823819915, 0.9953917828622341, 0.9973473366437476, 0.9943030781608361, 0.9995290290713732, -0.9994945079679955, 0.9997109900652238, -0.9988379572928884, 0.9995070647543864, 0.9994132207570211});
		}

		MarioProcess marioProcess = new MarioProcess();
		marioProcess.launchMario(new String[0], true); // true means there is a human player       
		marioProcess.simulateOneLevel(level);
		
		// Should probably terminate the GAN process in a cleaner fashion
		System.exit(0);
	}
}
