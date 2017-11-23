/*

-----BEGIN PGP SIGNED MESSAGE-----
Hash: SHA256

*/
package competition.icegic.glenn;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.utils.MathX;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:30:41 AM
 * Package: ch.idsia.ai.agents.ai;
 *
 *
 * Modified/Strategized by Glenn Hartmann
 * Date: August 11, 2009
 * Time: 9:02:34 PM
 */
public class AIwesome extends BasicAIAgent implements Agent
{
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "Glenn_AIwesome";
    protected int jumpCount = 0; // counter to determine if you've done a 'full' jump yet
    protected int speedCount = 0; // counter to determine if you should shoot again

    public AIwesome() {
        super("AIwesome");
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
        action[Mario.KEY_JUMP] = false;
    }
    
    // determines if jumping will cause you to hit an enemy or not
    private boolean safeToJumpFromEnemies(byte[][] enemiesFromBitmap)
    {
        for(int y = 8; y <= 12; y++)
        {
            for(int x = 14; x <= 17; x++)
            {
                if(!(x == 11 && y == 11) && enemiesFromBitmap[y][x] == 1)
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // determines if jumping will land you in a gap
    private boolean safeToJumpFromGaps(byte[][] levelSceneFromBitmap)
    {
        for(int y = 12; y <= 12; y++)
        {
            boolean b = false;
            
            for(int x = 14; x <= 17; x++)
            {
                if(levelSceneFromBitmap[y][x] == 1)
                {
                    b = true;
                    break;
                }
            }
            
            if(!b)
            {
                return false;
            }
        }
        
        return true;
    }
    
    // determines if there are enemies close enough to pose a danger to you - implies you should jump
    private boolean dangerFromEnemies(byte[][] enemiesFromBitmap)
    {
        for(int y = 10; y <= 12; y++)
        {
            for(int x = 11; x <= 15; x++)
            {
                if(!(x == 11 && y == 11) && enemiesFromBitmap[y][x] == 1)
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // determines if there is a gap close enough to pose a danger to you - implies you should jump
    private boolean dangerFromGaps(byte[][] levelSceneFromBitmap)
    {
        for(int y = 12; y <= 13; y++)
        {
            for(int x = 12; x <= 15; x++)
            {
                if(levelSceneFromBitmap[y][x] == 0)
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // determines if it's safe to jump
    private boolean safeToJump(byte[][] levelSceneFromBitmap, byte[][] enemiesFromBitmap)
    {
        return safeToJumpFromGaps(levelSceneFromBitmap) && safeToJumpFromEnemies(enemiesFromBitmap);
    }
    
    // determines if you're in danger (and should jump)
    private boolean danger(byte[][] levelSceneFromBitmap, byte[][] enemiesFromBitmap)
    {
        return dangerFromGaps(levelSceneFromBitmap) || dangerFromEnemies(enemiesFromBitmap);
    }
    
    // determines if there is something blocking your path that you need to jump over
    private boolean block(byte[][] levelSceneFromBitmap)
    {
        for(int y = 11; y <= 11; y++)
        {
            for(int x = 12; x <= 15; x++)
            {
                if(levelSceneFromBitmap[y][x] == 1)
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // function from ForwardAgent.java - I did not write this
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
            for (int j = 0; j < 8; ++j)
            {
                totalBitsDecoded++;
                if (col > Environment.HalfObsHeight*2 - 1)
                {
                    ++row;
                    col = 0;
                }

                if ((MathX.powsof2[j] & cur_char) != 0)
                {
                    try{
                        dstate[row][col] = 1;
                    }
                    catch (Exception e)
                    {
                     //   System.out.println("row = " + row);
                     //   System.out.println("col = " + col);
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

        //System.out.println("\ntotalBitsDecoded = " + totalBitsDecoded);
        return dstate;
    }
    
    // main strategy, determines what action to perform
    public boolean[] getAction(Environment observation)
    {
        
        assert(observation != null);
        byte[][] levelSceneFromBitmap = decode(observation.getBitmapLevelObservation()); // map of the scene
        //float[] marioPos = observation.getMarioFloatPos();
        //float[] enemiesPos = observation.getEnemiesFloatPos();
        //byte[][] levelScene = observation.getCompleteObservation();
        byte[][] enemiesFromBitmap = decode(observation.getBitmapEnemiesObservation()); // map of enemies
        
        // stuff to print out maps... taken from ForwardAgent.java
        /*System.out.println("\nEnemies BIMAP:");
        for (int i = 0; i < enemiesFromBitmap.length; ++i)
        {
            for (int j = 0; j < enemiesFromBitmap[0].length; ++j)
            {
                if (enemiesFromBitmap[i][j] != 0)
//                    System.out.print( "1 ");
                    System.out.print(enemiesFromBitmap[i][j] + " ");
                else
                    System.out.print( "  ");
            }
            System.out.println("");
        }

        System.out.println("\nBItmaP:");
        for (int i = 0; i < levelSceneFromBitmap.length; ++i)
        {
            for (int j = 0; j < levelSceneFromBitmap[0].length; ++j)
            {
                if (levelSceneFromBitmap[i][j] != 0)
//                    System.out.print( "1 ");
                    System.out.print(levelSceneFromBitmap[i][j] + " ");
                else
                    System.out.print( "  ");
            }
            System.out.println("");
        }

        System.out.println("\nlevelSceneFromBitmap:");
        for (int i = 0; i < levelSceneFromBitmap.length; ++i)
        {
            for (int j = 0; j < levelSceneFromBitmap[0].length; ++j)
            {
                if (levelSceneFromBitmap[i][j] != 0)
                    System.out.print( "1 ");
                else
                    System.out.print( "  ");
            }
            System.out.println("");
        }*/
        
        // if jump is active and jumpCount is too big, deactivate - jump is over and you'll need to get ready for next one
        if(action[Mario.KEY_JUMP] && jumpCount >= 8)
        {
            action[Mario.KEY_JUMP] = false;
            jumpCount = 0;
        }
        // otherwise you're in the middle of jump, increment jumpCount
        else if(action[Mario.KEY_JUMP])
        {
            jumpCount++;
        }
        // now, if you're in danger from enemies, or blocked by landscape, jump if it's safe to. If there's danger of falling, jump no matter what
        else if((((dangerFromEnemies(enemiesFromBitmap) || block(levelSceneFromBitmap)) && safeToJump(levelSceneFromBitmap, enemiesFromBitmap)) || dangerFromGaps(levelSceneFromBitmap)) && observation.mayMarioJump())
        {
            action[Mario.KEY_JUMP] = true;
        }
        
        // keep shooting
        if(action[Mario.KEY_SPEED] && speedCount >= 10)
        {
            action[Mario.KEY_SPEED] = false;
            speedCount = 0;
        }
        else if(action[Mario.KEY_SPEED])
        {
            speedCount++;
        }
        else
        {
            action[Mario.KEY_SPEED] = true;
        }
        
        return action;
    }

    public AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() {        return name;    }

    public void setName(String Name) { this.name = Name;    }
}
/*
-----BEGIN PGP SIGNATURE-----
Version: GnuPG/MacGPG2 v2.0.12 (Darwin)

iF4EAREIAAYFAkqI8B0ACgkQf7sAhIwsAlAjLAD/WOaC1t55ISz+Scpn5Q2chjfr
VNKx0xUsDJ3w6ttsYw4BAIx8OKSLXgukr9tG9t1sPNqKVu6CYPqQ25Z79jCm2fUv
=bZcm
-----END PGP SIGNATURE-----
*/
