package competition.icegic.erek;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.tools.EvaluationInfo;

import java.util.List;
import java.util.Random;


/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 16, 2009
 * Time: 2:54:30 PM
 * My task which uses different seeds and difficulties and a custom fitness.
 */
public class ErekTask implements Task {

    private EvaluationOptions options = new EvaluationOptions();
    public int[] seeds = new int[1];
    private int[] difficulties = {0, 3, 5, 10};
    Random ran;

    public ErekTask()
    {
        ran = new Random();
        for(int i = 0; i < seeds.length; i++)
        {
            seeds[i] = ran.nextInt(50);
        }
        options.setNumberOfTrials(1);
        options.setVisualization(false);
        options.setMaxFPS(true);
    }

    public double[] evaluate(Agent controller) {
        double[] fitnesses = new double[1];
        options.setAgent(controller);
        
        for (int i : difficulties) {

            for(int j : seeds)
            {
                controller.reset();
                options.setLevelRandSeed(j);
                options.setLevelDifficulty(i);


                Evaluator evaluator = new Evaluator(options);
                List<EvaluationInfo> results = evaluator.evaluate();
                EvaluationInfo result = results.get(0);

                // This fitness is distance traveled plus winning and coins and time
                double fitness = result.computeBasicFitness();
                fitnesses[0] += fitness;
                //System.out.println("seed:" + j +" diff:" + i + " score:" + fitness);
            }
        }
        //TODO: AgentsPool has to be edited to allow removing the agent every iteration.
        //AgentsPool.remove("ErekSpeedAgent");
        fitnesses[0] = fitnesses[0]/((double)(difficulties.length*seeds.length));
        return fitnesses;
    }

    public EvaluationOptions getOptions() {
        return options;
    }

    public void setOptions(EvaluationOptions options) {
        this.options = options;
    }
}
