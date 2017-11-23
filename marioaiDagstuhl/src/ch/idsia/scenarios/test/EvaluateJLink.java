package ch.idsia.scenarios.test;

import ch.idsia.ai.SRN;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.LargeSRNAgent;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.tools.EvaluationInfo;

/**
 * Created by IntelliJ IDEA.
 * User: koutnij
 * Date: Jul 27, 2009
 * Time: 4:34:37 PM
 */
public class EvaluateJLink {


    /** returns {in, rec, out} array. Just to make math and java codes fully independent. */
    public static int[] getDimension() {
        return new int[]{getInputSize()*getInputSize()*2+3, 6, 6};
    }

    /** returns length of an edge of the input window square*/
    public static int getInputSize() {
        return 7;
    }

    public double evaluateLargeSRN (double[][] inputs, double[][] recurrent, double[][] output, int level, int seed) {
        // System.out.println(inputs.length+" "+inputs[0].length);
        // System.out.println(recurrent.length+" "+recurrent[0].length);
        // System.out.println(output.length+" "+output[0].length);
        SRN srn = new SRN (inputs, recurrent, output, recurrent.length, output[0].length);
        Agent agent = new LargeSRNAgent(srn);
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        final int startingSeed = 0;
        options.setLevelRandSeed(seed);
        options.setNumberOfTrials(1);
        options.setVisualization(false);
        options.setMaxFPS(true);
        options.setLevelDifficulty(level);
        options.setPauseWorld(false);
        agent.reset();
        options.setAgent(agent);
        Evaluator evaluator = new Evaluator (options);
        EvaluationInfo result = evaluator.evaluate().get(0);
       // System.out.print(".");
        double score = result.computeDistancePassed();
         System.out.println("score: " +score);
        return score;
    }

}
