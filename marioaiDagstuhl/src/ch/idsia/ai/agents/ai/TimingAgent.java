package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.Evaluator;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 10, 2009
 * Time: 6:41:42 PM
 */
public class TimingAgent implements Agent {

    private Agent agent;
    private long timeTaken = 0;
    private int evaluations = 0;

    public TimingAgent (Agent agent) {
        this.agent = agent;
    }
    
    public void reset() {
        agent.reset ();
    }

    public boolean[] getAction(Environment observation) {
        long start = System.currentTimeMillis();
        boolean[] action = agent.getAction (observation);
        timeTaken += (System.currentTimeMillis() - start);
        evaluations++;
        //compute all metrics
        return action;
    }

    public AGENT_TYPE getType() {
        return agent.getType ();
    }

    public String getName() {
        return agent.getName ();
    }

    public void setName(String name) {
        agent.setName (name);
    }

    public double averageTimeTaken () {
        double average = ((double) timeTaken) / evaluations;
        timeTaken = 0;
        evaluations = 0;
        return average;
    }
}
