package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: May 9, 2009
 * Time: 1:42:03 PM
 * Package: ch.idsia.ai.agents
 */

public class ScaredSpeedyAgent extends BasicAIAgent implements Agent {
    public ScaredSpeedyAgent() {
        super("ScaredSpeedyAgent");
    }

    int trueJumpCounter = 0;
    int trueSpeedCounter = 0;

    private boolean DangerOfGap(byte[][] levelScene)
    {
        for (int x = 9; x < 13; ++x)
        {
            boolean f = true;
            for(int y = 12; y < 22; ++y)
            {
                if  (levelScene[y][x] != 0)
                    f = false;
            }
            if (f && levelScene[12][11] != 0)
                return true;
        }
        return false;
    }


    public void reset() {
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation) {
        byte[][] levelScene = observation.getLevelSceneObservation(/*1*/);
        if (/*levelScene[11][13] != 0 ||*/ levelScene[11][12] != 0 ||
           /* levelScene[12][13] == 0 ||*/ levelScene[12][12] == 0 )
        {
            if (observation.mayMarioJump() || ( !observation.isMarioOnGround() && action[Mario.KEY_JUMP]))
            {
                action[Mario.KEY_JUMP] = true;
            }
            ++trueJumpCounter;
        }
        else
        {
            action[Mario.KEY_JUMP] = false;
            trueJumpCounter = 0;
        }

        if (trueJumpCounter > 46)
        {
            trueJumpCounter = 0;
            action[Mario.KEY_JUMP] = false;
        }

        return action;
    }
}
