package ch.idsia.scenarios;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.ea.ES;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import wox.serial.Easy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 4, 2009
 * Time: 4:33:25 PM
 */
public class Evolve {

    final static int generations = 100;
    final static int populationSize = 100;


    public static void main(String[] args) {
        EvaluationOptions options = new CmdLineOptions(args);
        options.setNumberOfTrials(1);
        options.setPauseWorld(true);
        List<Agent> bestAgents = new ArrayList<Agent>();
        DecimalFormat df = new DecimalFormat("0000");
        for (int difficulty = 0; difficulty < 11; difficulty++)
        {
            System.out.println("New Evolve phase with difficulty = " + difficulty + " started.");
            Evolvable initial = new SimpleMLPAgent();

            options.setLevelDifficulty(difficulty);
            options.setAgent((Agent)initial);

            options.setMaxFPS(true);
            options.setVisualization(false);

            Task task = new ProgressTask(options);
            ES es = new ES (task, initial, populationSize);

            for (int gen = 0; gen < generations; gen++) {
                es.nextGeneration();
                double bestResult = es.getBestFitnesses()[0];
//                LOGGER.println("Generation " + gen + " best " + bestResult, LOGGER.VERBOSE_MODE.INFO);
                System.out.println("Generation " + gen + " best " + bestResult);
                options.setVisualization(gen % 5 == 0 || bestResult > 4000);
                options.setMaxFPS(true);
                Agent a = (Agent) es.getBests()[0];
                a.setName(((Agent)initial).getName() + df.format(gen));
//                AgentsPool.setCurrentAgent(a);
                bestAgents.add(a);
                double result = task.evaluate(a)[0];
//                LOGGER.println("trying: " + result, LOGGER.VERBOSE_MODE.INFO);
                options.setVisualization(false);
                options.setMaxFPS(true);
                Easy.save (es.getBests()[0], "evolved.xml");
                if (result > 4000)
                    break; // Go to next difficulty.
            }
        }
        /*// TODO: log dir / log dump dir option
        // TODO: reduce number of different
        // TODO: -fq 30, -ld 1:15, 8 
        //LOGGER.println("Saving bests... ", LOGGER.VERBOSE_MODE.INFO);

        options.setVisualization(true);
        int i = 0;
        for (Agent bestAgent : bestAgents) {
            Easy.save(bestAgent, "bestAgent" +  df.format(i++) + ".xml");
        }

        LOGGER.println("Saved! Press return key to continue...", LOGGER.VERBOSE_MODE.INFO);
        try {System.in.read();        } catch (IOException e) {            e.printStackTrace();        }

//        for (Agent bestAgent : bestAgents) {
//            task.evaluate(bestAgent);
//        }


        LOGGER.save("log.txt");*/
        System.exit(0);
    }
}
