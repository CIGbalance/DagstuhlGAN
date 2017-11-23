package ch.idsia.scenarios.test;

import ch.idsia.ai.SRN;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.LargeSRNAgent;
import ch.idsia.scenarios.Stats;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Jul 30, 2009
 * Time: 5:26:01 PM
 */
public class StatsJLink {

    public void evaluateLargeSRN (double[][] inputs, double[][] recurrent, double[][] output, int level) {
        // 98 * 6
        // 6*6
        // 6*6
        SRN srn = new SRN (inputs, recurrent, output, recurrent.length, output[0].length);
        Agent agent = new LargeSRNAgent(srn);
        Stats.doStats(agent, 0);
    }

}
