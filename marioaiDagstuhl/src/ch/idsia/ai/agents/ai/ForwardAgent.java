package ch.idsia.ai.agents.ai;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.utils.MathX;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 8, 2009
 * Time: 4:03:46 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class ForwardAgent extends BasicAIAgent implements Agent
{
    int trueJumpCounter = 0;
    int trueSpeedCounter = 0;

    public ForwardAgent()
    {
        super("ForwardAgent");
        reset();
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
        trueJumpCounter = 0;
        trueSpeedCounter = 0;
    }

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

    private byte[][] decode(String estate)
    {
        byte[][] dstate = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        for (int i = 0; i < dstate.length; ++i)
            for (int j = 0; j < dstate[0].length; ++j)
                dstate[i][j] = 2;
        int row = 0;
        int col = 0;
        int totalBitsDecoded = 0;

        for (int i = 0; i < estate.length(); ++i)
        {
            char cur_char = estate.charAt(i);
            if (cur_char != 0)
            {
                //MathX.show(cur_char);
            }
            for (int j = 0; j < 16; ++j)
            {
                totalBitsDecoded++;
                if (col > Environment.HalfObsHeight*2 - 1)
                {
                    ++row;
                    col = 0;
                }

//                if ((MathX.pow(2,j) & cur_char) != 0)
                if ((MathX.powsof2[j] & cur_char) != 0)
                {

                    try{
                        dstate[row][col] = 1;
//                        show(cur_char);
                    }
                    catch (Exception e)
                    {
                        //System.out.println("row = " + row);
                        //System.out.println("col = " + col);
                    }
                }
                else
                {
                    dstate[row][col] = 0; //TODO: Simplify in one line of code.
                }
                ++col;
                if (totalBitsDecoded == 484)
                    break;
            }
        }

        //System.out.println("totalBitsDecoded = " + totalBitsDecoded);
        return dstate;
    }


    public boolean[] getAction(Environment observation)
    {
        //TODO: Discuss increasing diffuculty for handling the gaps.
        // this Agent requires observation.

        assert(observation != null);
        byte[][] levelScene = observation.getCompleteObservation(/*1, 0*/);
        float[] marioPos = observation.getMarioFloatPos();
        float[] enemiesPos = observation.getEnemiesFloatPos();
//        String encodedState = observation.getBitmapLevelObservation();
//        byte[][] levelSceneFromBitmap = decode(encodedState);
//        encodedState = observation.getBitmapEnemiesObservation();
//        byte[][] enemiesFromBitmap = decode(encodedState);

//        System.out.println("\nEnemies BIMAP:");
//        for (int i = 0; i < enemiesFromBitmap.length; ++i)
//        {
//            for (int j = 0; j < enemiesFromBitmap[0].length; ++j)
//            {
//                if (enemiesFromBitmap[i][j] != 0)
////                    System.out.print( "1 ");
//                    System.out.print(enemiesFromBitmap[i][j] + " ");
//                else
//                    System.out.print( "  ");
//            }
//            System.out.println("");
//        }

//        System.out.println("\nBItmaP:");
//        for (int i = 0; i < levelSceneFromBitmap.length; ++i)
//        {
//            for (int j = 0; j < levelSceneFromBitmap[0].length; ++j)
//            {
//                if (levelSceneFromBitmap[i][j] != 0)
////                    System.out.print( "1 ");
//                    System.out.print(levelSceneFromBitmap[i][j] + " ");
//                else
//                    System.out.print( "  ");
//            }
//            System.out.println("");
//        }
//
//        System.out.println("\nLEVELScene:");
//        for (int i = 0; i < levelScene.length; ++i)
//        {
//            for (int j = 0; j < levelScene[0].length; ++j)
//            {
//                if (levelScene[i][j] != 0)
//                    System.out.print( "1 ");
//                else
//                    System.out.print( "  ");
//            }
//            System.out.println("");
//        }

        
//        if (levelSceneFromBitmap[11][13] != 0 || levelSceneFromBitmap[11][12] != 0 ||  DangerOfGap(levelSceneFromBitmap))
//        {
//            if (observation.mayMarioJump() || ( !observation.isMarioOnGround() && action[Mario.KEY_JUMP]))
//            {
//                action[Mario.KEY_JUMP] = true;
//            }
//            ++trueJumpCounter;
//        }
//        else
//        {
//            action[Mario.KEY_JUMP] = false;
//            trueJumpCounter = 0;
//        }
//
//        if (trueJumpCounter > 16)
//        {
//            trueJumpCounter = 0;
//            action[Mario.KEY_JUMP] = false;
//        }
//
//        action[Mario.KEY_SPEED] = DangerOfGap(levelSceneFromBitmap);
//        return action;
//
//
        if (levelScene[11][13] != 0 || levelScene[11][12] != 0 ||  DangerOfGap(levelScene))
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

        if (trueJumpCounter > 16)
        {
            trueJumpCounter = 0;
            action[Mario.KEY_JUMP] = false;
        }

        action[Mario.KEY_SPEED] = DangerOfGap(levelScene);
        return action;
    }
}
