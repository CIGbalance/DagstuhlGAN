package competition.icegic.rafael.object.ai;

import competition.icegic.rafael.object.area.IObjectArea;
import ch.idsia.mario.environments.Environment;

public class FireBallAI
{
	private IObjectArea you;
	private IObjectArea emeny;
	private Environment observation;

	public FireBallAI(IObjectArea you, IObjectArea emeny, Environment observation)
	{
		this.you = you;
		this.emeny = emeny;
		this.observation = observation;
	}

	public boolean isThrowFireBall(boolean isJumping, int scenesJump)
	{
		//if mode 2, Mario fire!, only send fire when be jumping
		if (observation.getMarioMode() == 2)
		{
			if (isJumping && scenesJump > 3)
				return true;
			else
				return false;
		}
		else
		{
			return false;
		}
	}


	public boolean isThrowFireBallPaused()
	{
		if (you.isObjectComing(emeny) && observation.getMarioMode() == 2)
		{
			//only throw if the enemy are 4, 6, 2, 12
			//TODO
			return true;
		}
		else
		{
			return false;
		}
	}
}
