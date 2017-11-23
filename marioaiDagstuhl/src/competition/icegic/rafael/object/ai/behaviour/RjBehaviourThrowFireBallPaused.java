package competition.icegic.rafael.object.ai.behaviour;

import competition.icegic.rafael.object.area.EnemyArea;
import competition.icegic.rafael.object.area.IObjectArea;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class RjBehaviourThrowFireBallPaused implements IObjectBehaviour
{

	private IObjectArea you;
	private IObjectArea enemy;
	private Environment observation;

	private boolean toThrow = true;
	private boolean jumping;

	public RjBehaviourThrowFireBallPaused()
	{

	}

	public void behave(boolean[] Action)
	{
		EnemyArea enemyArea = (EnemyArea) enemy;

		//if enemy is fire ball sensitive and not contains enemy very next, throw fire ball!!
		//if enemy is coming, must jump!
		if (enemyArea != null && enemyArea.isFireBallSensitive() && observation.getMarioMode() == 2)
		{
			if  (!you.isObjectComing(enemy) 
					&& enemyArea.isWalkToLeft() 
					&& (you.getY() <= enemy.getY()+3)
					&& observation.isMarioOnGround())
			{
				if (toThrow)
				{
					Action[Mario.KEY_JUMP] = true;
					Action[Mario.KEY_RIGHT] = false;
					Action[Mario.KEY_LEFT] = false;
					Action[Mario.KEY_SPEED] = true;
					toThrow = false;
				}
				else
				{
					toThrow = true;
				}
			}
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
