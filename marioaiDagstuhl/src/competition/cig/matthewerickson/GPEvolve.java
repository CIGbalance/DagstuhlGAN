package competition.cig.matthewerickson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import wox.serial.Easy;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

public class GPEvolve {
    final static int generations = 10000;
    final static int populationSize = 500;


    public static void main(String[] args) {
        EvaluationOptions options = new CmdLineOptions(args);
        options.setNumberOfTrials(1);
        options.setPauseWorld(false);
        List<Agent> bestAgents = new ArrayList<Agent>();
        DecimalFormat df = new DecimalFormat("0000");
       for (int difficulty = 7; difficulty < 11; difficulty++)
        {
            System.out.println("New Evolve phase with difficulty = " + difficulty + " started.");
            GPAgent initial = new GPAgent();     
            options.setLevelDifficulty(difficulty);
            options.setAgent((Agent)initial);           
            options.setMaxFPS(true);
            options.setVisualization(false);           
            Task task = new ProgressTask(options);
            GPES gpes = new GPES (task, initial, populationSize, difficulty);
            
            for (int gen = 0; gen < generations; gen++) {
            	gpes.nextGeneration();                
            	double bestResult = gpes.getBestFitnesses()[0];//                LOGGER.println("Generation " + gen + " best " + bestResult, LOGGER.VERBOSE_MODE.INFO);
                System.out.println("Generation " + gen + " best " + bestResult);
                options.setVisualization(gen % 5 == 0 || bestResult > 4000);
                options.setMaxFPS(true);
                Agent a = (Agent) gpes.getBests()[0];
                System.out.println("Best agent: " + ((GPAgent)a).program);
                a.setName("GPAgent" + df.format(gen));
                bestAgents.add(a);
                double result = task.evaluate(a)[0];
                options.setVisualization(false);
                options.setMaxFPS(true);
                Easy.save (gpes.getBests()[0], "GPAgent" + difficulty +".xml");
                if (result > 4000)
                    break; // Go to next difficulty.
            }
        }
        
        System.exit(0);
    }
}
