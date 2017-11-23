package competition.icegic.rafael.object.ai.behaviour;

import competition.icegic.rafael.object.area.IObjectArea;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class RjBehaviourThrowFireBall implements IObjectBehaviour
{

	private IObjectArea you;
	private IObjectArea enemy;
	private Environment observation;

	private int scenes = 0;
	private boolean jumping;

	public RjBehaviourThrowFireBall()
	{

	}

	public void behave(boolean[] Action)
	{
		if (jumping && observation.getMarioMode() == 2)
		{
			if (scenes >= 3)
			{
				Action[Mario.KEY_SPEED] = true;
				scenes++;
			}
			else
			{
				scenes++;
			}

			if(scenes >= 6)
			{
				scenes = 0;
			}
		}
		else
		{
			scenes = 0;
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
