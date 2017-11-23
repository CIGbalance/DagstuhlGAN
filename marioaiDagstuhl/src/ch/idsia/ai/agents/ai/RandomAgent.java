package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 28, 2009
 * Time: 10:37:18 PM
 * Package: ch.idsia.ai.agents.ai;
 */
public class RandomAgent extends BasicAIAgent implements Agent
{
    public RandomAgent()
    {
        super("RandomAgent");
        reset();
    }

    private Random R = null;
    public void reset()
    {
        // Dummy reset, of course, but meet formalities!
        R = new Random();
    }

    public boolean[] getAction(Environment observation)
    {
        boolean[] ret = new boolean[Environment.numberOfButtons];

        for (int i = 0; i < Environment.numberOfButtons; ++i)
        {
            // Here the RandomAgent is encouraged to move more often to the Right and make long Jumps.
            boolean toggleParticularAction = R.nextBoolean();
            toggleParticularAction = (i == 0 && toggleParticularAction && R.nextBoolean()) ? R.nextBoolean() :  toggleParticularAction;
            toggleParticularAction = (i == 1 || i > 3 && !toggleParticularAction ) ? R.nextBoolean() :  toggleParticularAction;
            toggleParticularAction = (i > 3 && !toggleParticularAction ) ? R.nextBoolean() :  toggleParticularAction;
//            toggleParticularAction = (i == 4 && !toggleParticularAction ) ? R.nextBoolean() :  toggleParticularAction;
            ret[i] = toggleParticularAction;
        }
        if (ret[1])
            ret[0] = false;
        return ret;
    }
}
