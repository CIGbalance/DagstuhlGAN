/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package competition.icegic.michal;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;


/**
 *
 * @author Michal Tuláček
 */
public class TutchekAgent extends BasicAIAgent implements Agent {
    int print;
    int jumpCounter;

    public enum STATE {
        WALK_FORWARD,
        WALK_BACKWARD,
        JUMP,
        JUMP_HOLE
    };

    boolean facing_left;
    int leftCounter;
    int shootCounter;
    STATE state;

    public TutchekAgent()
    {
        super("MichalTutchek_Agent");
        
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        state = STATE.WALK_FORWARD;
        facing_left = false;

        print = 0;
        jumpCounter = 0;
        leftCounter = 0;
        shootCounter = 0;
    }

    protected byte getLocation( int relX, int relY, byte[][] scene )
    {
        int realX = 11 + relX;
        int realY = 11 + relY;

        return scene[realY][realX];
    }

    protected boolean thereIsObstacle( byte[][] scene )
    {
        byte[] inFrontOf = new byte[]{
                getLocation( 1, 0, scene  ),
                getLocation( 2, 0, scene  ),
                getLocation( 2, -1, scene  )
        };

        for (int i = 0; i < inFrontOf.length; i++)
        {
            if (inFrontOf[i] < 0 || inFrontOf[i] == 20)
            {
                return true;
            }
        }

        return false;
    }

    protected boolean thereIsHole( byte[][] scene )
    {
        for (int i = 1; i < 3; i++)
        {
            for (int j = 1; j < 11; j++)
            {
                if (getLocation( i, j, scene ) != 0)
                {
                    return false;
                }
            }
        }

        return true;
    }

    protected void printState(STATE state)
    {
        switch ( state )
        {

            case JUMP:
                System.out.println("Jumping");
                break;

            case JUMP_HOLE:
                System.out.println("Jumping hole");
                break;

           case WALK_FORWARD:
                System.out.println("Walk forward");
                break;

            case WALK_BACKWARD:
                System.out.println("Walk backward");
                break;

            default:
                System.out.println("???");
                break;
        }
    }

    protected boolean enemyInFront( byte[][] enemies )
    {
        for (int i = 0; i > -2; i--)
        {
            for (int j = 1; j < 2; j++)
            {
                if (getLocation(j, i, enemies) > 1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean[] getAction(Environment observation)
    {
        byte[][] scene = observation.getLevelSceneObservation();
        byte[][] enemies = observation.getEnemiesObservation();

        //printState(state);

        if ( enemyInFront(enemies) )
        {
            if (shootCounter > 0)
            {
                action[Mario.KEY_SPEED] = false;
            } else {
                action[Mario.KEY_SPEED] = true;
                shootCounter++;
            }
            return action;
        } else if (shootCounter > 0) {
            shootCounter = 0;
        }

        switch (state)
        {
            case WALK_BACKWARD:
                if (leftCounter > 5)
                {
                    state = STATE.WALK_FORWARD;
                    facing_left = false;
                }

                leftCounter++;
                action[Mario.KEY_LEFT] = true;
                action[Mario.KEY_RIGHT] = false;

                break;

            case WALK_FORWARD:
                action[Mario.KEY_LEFT] = false;
                if (thereIsHole( scene ))
                {
                    //System.out.println("hole");
                    state = STATE.JUMP_HOLE;
                    action[Mario.KEY_JUMP] = true;
                    action[Mario.KEY_SPEED] = true;
                }
                else if (thereIsObstacle( scene ))
                {
                    //System.out.println("obstacle");
                    state = STATE.JUMP;
                    action[Mario.KEY_JUMP] = true;
                    action[Mario.KEY_RIGHT] = true;
                    action[Mario.KEY_SPEED] = false;
                } else {
                    action[Mario.KEY_RIGHT] = true;
                    action[Mario.KEY_SPEED] = false;
                }
                break;

            case JUMP:
                if (action[Mario.KEY_RIGHT] && thereIsHole(scene) )
                {
                    //System.out.println("jump hole");
                    action[Mario.KEY_RIGHT] = false;
                    action[Mario.KEY_LEFT] = true;

                    facing_left = true;
                }
                else if ( observation.isMarioOnGround() )
                {
                    //System.out.println("on ground");

                    if (facing_left)
                    {
                        state = STATE.WALK_BACKWARD;
                        leftCounter = 0;
                    } else {
                        state = STATE.WALK_FORWARD;
                    }

                    action[Mario.KEY_LEFT] = false;
                    action[Mario.KEY_RIGHT] = false;

                    action[Mario.KEY_JUMP] = false;
                    action[Mario.KEY_SPEED] = false;
                }
                break;

            case JUMP_HOLE:
                if ( observation.isMarioOnGround() )
                {
                    //System.out.println("on ground");
                    state = STATE.WALK_FORWARD;

                    action[Mario.KEY_JUMP] = false;
                    action[Mario.KEY_SPEED] = false;

                    action[Mario.KEY_LEFT] = false;
                    action[Mario.KEY_RIGHT] = false;
                }
                break;
        }

        return action;
    }
}
