package ch.idsia.ai.agents;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey@idsia.ch
 * Date: May 19, 2010
 * Time: 3:45:19 PM
 */

public interface LearningAgent extends Agent
{
    public void learn();
    public void giveReward(float reward);

    public void newEpisode();
}
