package ch.idsia.ai.tasks;

import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.ai.agents.Agent;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 23, 2009
 * Time: 11:37:47 PM
 */
public class MultiSeedProgressTask implements Task {

    private EvaluationOptions options;
    private int startingSeed = 0;
    private int numberOfSeeds = 3;

    public MultiSeedProgressTask(EvaluationOptions evaluationOptions) {
        setOptions(evaluationOptions);
    }

    public double[] evaluate(Agent controller) {
        double distanceTravelled = 0;

        options.setAgent(controller);
        for (int i = 0; i < numberOfSeeds; i++) {
            controller.reset();
            options.setLevelRandSeed(startingSeed + i);
            Evaluator evaluator = new Evaluator(options);
            List<EvaluationInfo> results = evaluator.evaluate();     
            EvaluationInfo result = results.get(0);
            distanceTravelled += result.computeDistancePassed();
        }
        distanceTravelled = distanceTravelled / numberOfSeeds;
        return new double[]{distanceTravelled};
    }

    public void setStartingSeed (int seed) {
        startingSeed = seed;
    }

    public void setNumberOfSeeds (int number) {
        numberOfSeeds = number;
    }

    public void setOptions(EvaluationOptions options) {
        this.options = options;
    }

    public EvaluationOptions getOptions() {
        return options;
    }
}
