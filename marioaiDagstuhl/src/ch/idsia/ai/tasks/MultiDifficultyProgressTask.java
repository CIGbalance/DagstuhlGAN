package ch.idsia.ai.tasks;

import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.ai.agents.Agent;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Jun 13, 2009
 * Time: 2:44:59 PM
 */
public class MultiDifficultyProgressTask implements Task {

    private EvaluationOptions options;
    private int startingSeed = 0;
    private int[] difficulties = {0, 3, 5, 10};

    public MultiDifficultyProgressTask(EvaluationOptions evaluationOptions) {
        setOptions(evaluationOptions);
    }

    public double[] evaluate(final Agent controller) {
        double distanceTravelled = 0;
        double[] fitnesses = new double[difficulties.length + 1];
        for (int i = 0; i < difficulties.length; i++) {
            controller.reset();
            options.setLevelRandSeed(startingSeed);
            options.setLevelDifficulty(difficulties[i]);
            options.setAgent(controller);
            Evaluator evaluator = new Evaluator(options);
            List<EvaluationInfo> results = evaluator.evaluate();
            EvaluationInfo result = results.get(0);
            double thisDistance = result.computeDistancePassed();
            fitnesses[i + 1] = thisDistance;
            distanceTravelled += thisDistance;
        }
        distanceTravelled = distanceTravelled / difficulties.length;
        fitnesses[0] = distanceTravelled;
        return fitnesses;
        //return new double[]{distanceTravelled};
    }

    public void setStartingSeed (int seed) {
        startingSeed = seed;
    }

    public void setOptions(EvaluationOptions options) {
        this.options = options;
    }

    public EvaluationOptions getOptions() {
        return options;
    }
}
