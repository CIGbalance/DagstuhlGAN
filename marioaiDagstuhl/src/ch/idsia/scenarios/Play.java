package ch.idsia.scenarios;

import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */

/**
 * The <code>Play</code> class shows how simple is to run an iMario benchmark.
 * It shows how to set up some parameters, create a task,
 * use the CmdLineParameters class to set up options from command line if any.
 * Defaults are used otherwise.
 *
 * @author  Julian Togelius, Sergey Karakovskiy
 * @version 1.0, May 5, 2009
 * @since   JDK1.0
 */

public class Play {
    /**
     * <p>An entry point of the class.
     *
     * @param args input parameters for customization of the benchmark.
     *
     * @see ch.idsia.scenarios.MainRun
     * @see ch.idsia.tools.CmdLineOptions
     * @see ch.idsia.tools.EvaluationOptions
     *
     * @since   iMario1.0
     */

    public static void main(String[] args) {
        EvaluationOptions options = new CmdLineOptions(args);
        Task task = new ProgressTask(options);
//        options.setMaxFPS(false);
//        options.setVisualization(true);
//        options.setNumberOfTrials(1);
        options.setLevelFile("marioaiDagstuhl/data/mario/largeExamples.json");
        options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
        options.setLevelDifficulty(3);

        int levelIndex = 0;
        // Goes through all levels in the json file
        while(true) { // This will eventually crash
	        options.setLevelIndex(levelIndex += 500);
	        task.setOptions(options);
	        System.out.println ("Score: " + task.evaluate (options.getAgent())[0]);
	        System.out.println("Simulation/Play finished");       
        }
    }
}
