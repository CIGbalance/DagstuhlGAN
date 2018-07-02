package ch.idsia.maibe.experiments;

import ch.idsia.ai.agents.Agent;
import ch.idsia.maibe.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Feb 24, 2010
 * Time: 1:46:36 PM
 * Package: ch.idsia.maibe.experiments
 */
public class EpisodicExperiment extends Experiment
{
    public EpisodicExperiment(Task task, Agent agent)
    {
        super(task, agent);
    }

    // returns the rewards of each step as an array of doubles
    public List<List<Double>> doEpisodes(int amount)
    {
        List<List<Double>> allRewards = new ArrayList<List<Double>>();
        for (int i = 0; i < amount; ++i)
        {
            List<Double> rewards = new ArrayList<Double>();
            this.stepNumber = 0;
            // the agent is informed of the start of the episode
//            this.agent.newEpisode();
            this.agent.reset();
            this.task.reset();
            while (!this.task.isFinished())
            {
                Double r = this.oneInteraction();
                rewards.add(r);
            }
            allRewards.add(rewards);
        }
        return allRewards;
    }

}
