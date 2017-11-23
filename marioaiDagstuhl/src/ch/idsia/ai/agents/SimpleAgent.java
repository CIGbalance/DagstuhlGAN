package ch.idsia.ai.agents;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: May 12, 2009
 * Time: 7:28:57 PM
 * Package: ch.idsia.ai.agents
 */
public class SimpleAgent implements Agent
{
    protected boolean Action[] = new boolean[Environment.numberOfButtons];
    protected String Name = "SimpleAgent";

    public void reset()
    {
        Action = new boolean[Environment.numberOfButtons];
        Action[Mario.KEY_RIGHT] = true;
        Action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation)
    {
        Action[Mario.KEY_SPEED] = Action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();
        return Action;
    }

    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {        return Name;    }

    public void setName(String Name) { this.Name = Name;    }
}
