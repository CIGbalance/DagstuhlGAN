package ch.idsia.scenarios.test;

import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.ea.ES;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.agents.ai.*;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.scenarios.Stats;
import wox.serial.Easy;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Jun 13, 2009
 * Time: 2:16:18 PM
 */
public class PaperEvolve {

    final static int generations = 100;
    final static int populationSize = 100;


    public static void main(String[] args) {
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setNumberOfTrials(1);
        Evolvable initial = new LargeSRNAgent();
        if (args.length > 0) {
            initial = (Evolvable) AgentsPool.load (args[0]);
        }
        AgentsPool.addAgent ((Agent) initial);
        options.setMaxFPS(true);
        options.setPauseWorld(false);
        options.setVisualization(false);
        ProgressTask task = new ProgressTask(options);
        int seed = (int) (Math.random () * Integer.MAX_VALUE);
        ES es = new ES (task, initial, populationSize);
        System.out.println("Evolving " + initial + " with task " + task);
        int difficulty = 0;
        final String fileName = "evolved" + (int) (Math.random () * Integer.MAX_VALUE) + ".xml";
        options.setLevelRandSeed(seed);
        for (int gen = 0; gen < generations; gen++) {
            es.nextGeneration();
            double bestResult = es.getBestFitnesses()[0];
            System.out.println("Generation " + gen + " diff " + difficulty + "  best " + bestResult);
            Easy.save (es.getBests()[0], fileName);
            if (bestResult > 4000) {
                difficulty++;
                options.setLevelDifficulty(difficulty);
                options.setLevelRandSeed(seed);
            }
        }
        Stats.main(new String[]{fileName, "0"});
        System.out.println("\n\n\n\n\n\n\n\n\n");
    }
}
