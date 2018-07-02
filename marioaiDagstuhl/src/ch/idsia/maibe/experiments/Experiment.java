package ch.idsia.maibe.experiments;

import ch.idsia.ai.agents.Agent;
import ch.idsia.maibe.tasks.Task;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Feb 23, 2010
 * Time: 10:13:39 PM
 * Package: ch.idsia.maibe.experiments
 */
public class Experiment
{
    public Task task;
    public Agent agent;
    public int stepNumber;
    // An experiment matches up a task with an agent and handles their interactions.


    public Experiment(Task task, Agent agent)
    {
        this.task = task;
        this.agent = agent;
        this.stepNumber = 0;
    }

    public void doInteractions(int number)
    {
        // The default implementation directly maps the methods of the agent and the task.
//        Returns the number of interactions done.
        for (int i = 0; i < number; ++i)
        {
            this.oneInteraction();
        }
    }

    public double oneInteraction()
    {
        ++this.stepNumber;
//        self.agent.integrateObservation(self.task.getObservation())
//        self.task.performAction(self.agent.getAction())
//        reward = self.task.getReward()
//        self.agent.giveReward(reward)
        double reward = 0;
        return reward;
    }

    public List<List<Double>> doEpisodes(int amount)
    {
        for (int i = 0; i < amount; ++i)
        {
            this.oneInteraction();
        }
        return null;
    }
}
