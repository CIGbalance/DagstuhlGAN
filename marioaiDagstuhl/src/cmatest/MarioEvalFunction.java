package cmatest;

import static reader.JsonReader.JsonToDoubleArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelParser;
import ch.idsia.tools.EvaluationInfo;
import communication.GANProcess;
import communication.MarioProcess;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;
import reader.JsonReader;
import static reader.JsonReader.JsonToDoubleArray;

public class MarioEvalFunction implements IObjectiveFunction {

	// This is the distance that Mario traverses when he beats the short levels
	// that we are generating. It would need to be changed if we train on larger
	// levels or in any way change the level length.
	public static final int LEVEL_LENGTH = 704;

	private GANProcess ganProcess;
	private MarioProcess marioProcess;
        private int fitnessFun;

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
        
        public MarioEvalFunction(String GANPath, String GANDim, int fitnessFun, Agent agent) throws IOException {
		// set up process for GAN
        this.fitnessFun = fitnessFun;                
		ganProcess = new GANProcess(GANPath, GANDim);
		ganProcess.start();
		// set up mario game
		//marioProcess = new MarioProcess(new MarioAgent());
		marioProcess = new MarioProcess(agent);
		marioProcess.start();        
		// consume all start-up messages that are not data responses
		String response = "";
		while(!response.equals("READY")) {
			response = ganProcess.commRecv();
		}
	}

	/**
	 * Takes a json String representing several levels 
	 * and returns an array of all of those Mario levels.
	 * In order to convert a single level, it needs to be put into
	 * a json array by adding extra square brackets [ ] around it.
	 * @param json Json String representation of multiple Mario levels
	 * @return Array of those levels
	 */
	public static Level[] marioLevelsFromJson(String json) {
		List<List<List<Integer>>> allLevels = JsonReader.JsonToInt(json);
		Level[] result = new Level[allLevels.size()];
		int index = 0;
		for(List<List<Integer>> listRepresentation : allLevels) {
			result[index++] = LevelParser.createLevelJson(listRepresentation);
		}
		return result;
	}
        
        public static Level marioLevelFromJson(String json) {
            List<List<List<Integer>>> allLevels = JsonReader.JsonToInt(json);
            List<List<Integer>> mergedLevel = new ArrayList<List<Integer>>() {};
            for(int i=0; i<allLevels.get(0).size(); i++){
                mergedLevel.add(new ArrayList<Integer>());
            }
            for(List<List<Integer>> singleLevel : allLevels) {
                for(int i=0; i<singleLevel.size(); i++){
                    mergedLevel.get(i).addAll(singleLevel.get(i));
                }
            }
            Level level = LevelParser.createLevelJson(mergedLevel);
            return level;
	}
        
        public void exit() throws IOException{
            ganProcess.commSend("0");
        }

	/**
	 * Helper method to get the Mario Level from the latent vector
	 * @param x Latent vector
	 * @return Mario Level
	 * @throws IOException Problems communicating with Python GAN process
	 */
	public Level levelFromLatentVector(double[] x) throws IOException {
		// Interpret x to a level
                int chunk_length = Integer.valueOf(ganProcess.GANDim);
                String levelString = "";
                for(int i =0; i<x.length; i+=chunk_length){
                    // Brackets required since generator.py expects of list of multiple levels, though only one is being sent here
                    double[] chunk = Arrays.copyOfRange(x, i, i+chunk_length);
                    ganProcess.commSend("[" + Arrays.toString(chunk) + "]");
                    levelString = levelString + ", " + ganProcess.commRecv(); // Response to command just sent
                }
                levelString = levelString.replaceFirst(",", "");
                levelString = levelString.replaceFirst(" ", "");
		Level level = marioLevelFromJson("[" +levelString + "]"); // Really only one level in this array
		return level;
	}
        
	
	/**
	 * Directly send a string to the GAN (Should be array of arrays of doubles in Json format).
	 * 
	 * Note: A bit redundant: This could be called from the method above.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public String stringToFromGAN(String input) throws IOException {
                double[] x = JsonToDoubleArray(input);
                x = mapArrayToOne(x);
		ganProcess.commSend(Arrays.toString(x));
		String levelString = ganProcess.commRecv(); // Response to command just sent
		return levelString;
	}
	
        
        public double evaluate(EvaluationInfo info){
            if(this.fitnessFun==0) { //Progression / Playability
                return (double) -info.computeDistancePassed()/LEVEL_LENGTH;   			
            }else if(info.computeDistancePassed() < LEVEL_LENGTH){
                return 2000;
            }else if(this.fitnessFun==1){
                return (double) info.computeBasicFitness();
            }else if(this.fitnessFun==2){
                return (double) -info.computeJumpFraction();
            }else if(this.fitnessFun==3){
                return (double) -info.totalActionsPerfomed;
            }
            return Double.NaN;

        }
        
	/**
	 * Gets objective score for single latent vector.
	 */
	@Override
	public double valueOf(double[] x) {
            EvaluationInfo info;
            int simulations = 1;
            double val = 0;
            Level level = null;
            try {
                level = levelFromLatentVector(x);
            } catch (IOException ex) {
                Logger.getLogger(MarioEvalFunction.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            for(int i=0; i<simulations; i++){
                // Do a simulation
                info = this.marioProcess.simulateOneLevel(level);
                val += evaluate(info);
                System.out.println(evaluate(info));
            }
            return val/simulations;

            
            // playability
/*
			// Fitness is negative since CMA-ES tries to minimize
                        //System.out.println("done");
                        //System.out.println(info.jumpActionsPerformed);
                        //System.out.println(info.computeJumpFraction());
			if(info.computeDistancePassed() < LEVEL_LENGTH) { // Did not beat level
				// Only optimize distance passed in this case
				return (double) -info.computeDistancePassed()/LEVEL_LENGTH;//+20;    			
			} else{ // Did beat level
				//System.out.println("Beat level!");
                                //System.out.println(info.computeJumpFraction());
				// Also maximize time, since this would imply the level is more challenging/interesting
				//return -info.computeDistancePassed() - info.timeSpentOnLevel; 
                return (double) -info.computeDistancePassed()/LEVEL_LENGTH - info.jumpActionsPerformed;
			}

		} catch (IOException e) {
			// Error occurred
			e.printStackTrace();
			System.exit(1);
			return Double.NaN;
		}*/
	}

	@Override
	public boolean isFeasible(double[] x) {
		return true;
	}

	/**
	 * Map the value in R to (-1, 1)
	 * @param valueInR
	 * @return
	 */
	public static double mapToOne(double valueInR) {
		return ( valueInR / Math.sqrt(1+valueInR*valueInR) );
	}

	public static double[] mapArrayToOne(double[] arrayInR) {
		double[] newArray = new double[arrayInR.length];
		for(int i=0; i<newArray.length; i++) {
			double valueInR = arrayInR[i];
			newArray[i] = mapToOne(valueInR);
                        //System.out.println(valueInR);
		}
		return newArray;
	}
}
