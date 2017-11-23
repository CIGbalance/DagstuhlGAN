package competition.icegic.rafael.object.ai.behaviour;

import competition.icegic.rafael.RjAgentUtils;
import competition.icegic.rafael.object.area.IObjectArea;
import competition.icegic.rafael.object.ai.behaviour.IObjectBehaviour;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class RjBehaviourJumpHole implements IObjectBehaviour
{

	private IObjectArea you;
	private IObjectArea enemy;
	private Environment observation;

	private boolean jumping = false;
	private int scenesJump = 0;

	public RjBehaviourJumpHole()
	{

	}

	public void behave(boolean[] Action)
	{
		if (isProlongJump())
		{
			Action[Mario.KEY_JUMP] = true;
			Action[Mario.KEY_RIGHT] = true;
			//key speed to do the object to jump more long
			Action[Mario.KEY_SPEED] = true;
		}

		String state = observation.getBitmapLevelObservation();
		byte[][] levelSceneFromBitmap = RjAgentUtils.decode(state);
    	if (isHoleNext(levelSceneFromBitmap, 13) && observation.mayMarioJump())
        {
    		jumping = true;
            Action[Mario.KEY_JUMP] = true;
			//key speed to do the object to jump more long
			Action[Mario.KEY_SPEED] = true;

        }


	}


	public boolean isHoleNext(byte[][] levelScene, int levelX)
    {
        //for (int x = 9; x < 13; ++x)
		for (int x = 9; x < levelX; ++x)
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


	private boolean isProlongJump()
	{
		if (jumping  && !observation.isMarioOnGround())
		{
			if (scenesJump < 7)
			{
				scenesJump++;
				return true;
			}
			else
			{
				scenesJump = 0;
				jumping = false;
				return false;
			}
		}
		else
		{
			scenesJump = 0;
			jumping = false;
			return false;
		}
	}

	public IObjectArea getYou() {
		return you;
	}

	public void setYou(IObjectArea you) {
		this.you = you;
	}

	public IObjectArea getEnemy() {
		return enemy;
	}

	public void setEnemy(IObjectArea enemy) {
		this.enemy = enemy;
	}

	public Environment getObservation() {
		return observation;
	}

	public void setObservation(Environment observation) {
		this.observation = observation;
	}

	public boolean isJumping() {
		return jumping;
	}

	public void setJumping(boolean jumping) {
		this.jumping = jumping;
	}

}
