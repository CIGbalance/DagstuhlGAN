package competition.icegic.rafael.object.ai.behaviour;

import competition.icegic.rafael.RjAgentUtils;
import competition.icegic.rafael.object.area.EnemiesArea;
import competition.icegic.rafael.object.area.EnemyArea;
import competition.icegic.rafael.object.area.EnemyType;
import competition.icegic.rafael.object.area.IObjectArea;
import competition.icegic.rafael.object.area.MarioArea;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class RjBehaviourJumpObstacle implements IObjectBehaviour
{

	private IObjectArea you;
	private IObjectArea enemy;
	private Environment observation;
	private EnemiesArea enemiesArea;
	private RjBehaviourJumpHole behaviourJumpHole;

	private boolean jumping = false;
	private int scenesJump = 0;
	private int scenesRight = 0;
	private boolean holeComming = false;

	public RjBehaviourJumpObstacle()
	{

	}

	public void behave(boolean[] Action)
	{
		if (isProlongJump())
		{
			Action[Mario.KEY_JUMP] = true;

			if (isWalkRight(holeComming))
			{
				Action[Mario.KEY_RIGHT] = true;
			}
			else
			{
				Action[Mario.KEY_RIGHT] = false;
			}
		}



		//if pass blocked, jump!
		String state = observation.getBitmapLevelObservation();
		byte[][] levelSceneFromBitmap = RjAgentUtils.decode(state);
		if(((MarioArea)you).isBlocked() || (levelSceneFromBitmap[11][13] != 0))
		//if((levelSceneFromBitmap[11][13] != 0))
		//if(((MarioArea)you).isBlocked())
		{
			if(observation.mayMarioJump())
			{
				//only jump if no contains enemy next
				if (((MarioArea)you).isObjectComing(enemy, 50)
						&& (((EnemyArea)enemy).getType().equals(EnemyType.PLANT_CARNIVORE)))
				{
					if (enemy.isPaused())
					{
						jumping = true;
						Action[Mario.KEY_JUMP] = true;
						holeComming = isHoleComming();
					}
				}
				else
				{
					jumping = true;
					Action[Mario.KEY_JUMP] = true;
					holeComming = isHoleComming();
				}
			}
		}

	}

	private void resetKeys(boolean action[])
	{
		action[Mario.KEY_LEFT] = false;
		action[Mario.KEY_SPEED] = false;
		action[Mario.KEY_JUMP] = false;
		action[Mario.KEY_RIGHT] = false;
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
				holeComming = false;
				return false;
			}
		}
		else
		{
			scenesJump = 0;
			jumping = false;
			holeComming = false;
			return false;
		}
	}

	private boolean isWalkRight(boolean holeComming)
	{
		if (jumping)
		{
			if (holeComming)
			{
				if (scenesJump == 5)
				{
					return true;
				}
			}
			else
			{
				return true;
			}
		}

		return false;
	}

	private boolean isHoleComming()
	{

		//if contains a next hole, can not prolong the right jump!
		String state = observation.getBitmapLevelObservation();
		byte[][] levelSceneFromBitmap = RjAgentUtils.decode(state);
		if (behaviourJumpHole.isHoleNext(levelSceneFromBitmap, 20))
		{
			return true;
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

	public RjBehaviourJumpHole getBehaviourJumpHole() {
		return behaviourJumpHole;
	}

	public void setBehaviourJumpHole(RjBehaviourJumpHole behaviourJumpHole) {
		this.behaviourJumpHole = behaviourJumpHole;
	}

	public EnemiesArea getEnemiesArea() {
		return enemiesArea;
	}

	public void setEnemiesArea(EnemiesArea enemiesArea) {
		this.enemiesArea = enemiesArea;
	}

}
