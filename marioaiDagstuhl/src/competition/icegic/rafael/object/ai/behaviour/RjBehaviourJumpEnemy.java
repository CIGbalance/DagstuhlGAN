package competition.icegic.rafael.object.ai.behaviour;

import competition.icegic.rafael.RjAgentUtils;
import competition.icegic.rafael.object.area.EnemiesArea;
import competition.icegic.rafael.object.area.IObjectArea;
import competition.icegic.rafael.object.area.MarioArea;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class RjBehaviourJumpEnemy implements IObjectBehaviour
{

	private MarioArea you;
	//private IObjectArea enemy;
	private Environment observation;
	private int scenesRight = 0;
	private EnemiesArea enemiesArea;
	private RjBehaviourJumpHole behaviourJumpHole;


	private boolean jumping = false;
	private int scenesJump = 0;

	public RjBehaviourJumpEnemy()
	{

	}

	public void behave(boolean[] Action)
	{
		if (isProlongJump())
		{
			Action[Mario.KEY_JUMP] = true;
			if (!isProlongRight())
			{
				Action[Mario.KEY_RIGHT] = false;
			}
			else
			{
				Action[Mario.KEY_RIGHT] = true;
			}
		}

		else if (you.isObjectComing(enemiesArea.getListEnemies())
				&& (you.getY() <= enemiesArea.getNextEnemy((MarioArea)you).getY()+1))

		{
			if (observation.isMarioOnGround())
			{
				jumping = true;
				Action[Mario.KEY_JUMP] = true;
				//Action[Mario.KEY_RIGHT] = true;
			}
		}

	}


	private boolean isProlongJump()
	{
		if (jumping  && !observation.isMarioOnGround())
		{
			if (scenesJump < 6)
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

	private boolean isProlongRight()
	{
		int level = 6;

		//if contains a next hole, can not prolong the right jump!
		String state = observation.getBitmapLevelObservation();
		byte[][] levelSceneFromBitmap = RjAgentUtils.decode(state);
		if (behaviourJumpHole.isHoleNext(levelSceneFromBitmap, 15))
		{
			level = 0;
		}
		else
		{

		}


		if (jumping)
		{
			if (scenesRight < level)
			{
				scenesRight++;
				return true;
			}
			else
			{
				scenesRight = 0;
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	public IObjectArea getYou() {
		return you;
	}

	public void setYou(IObjectArea you) {
		this.you = (MarioArea)you;
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

	public EnemiesArea getEnemiesArea() {
		return enemiesArea;
	}

	public void setEnemiesArea(EnemiesArea enemiesArea) {
		this.enemiesArea = enemiesArea;
	}

	public RjBehaviourJumpHole getBehaviourJumpHole() {
		return behaviourJumpHole;
	}

	public void setBehaviourJumpHole(RjBehaviourJumpHole behaviourJumpHole) {
		this.behaviourJumpHole = behaviourJumpHole;
	}

}
