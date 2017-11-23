package competition.icegic.rafael.object.ai;

import competition.icegic.rafael.object.ai.behaviour.RjBehaviourJumpEnemy;
import competition.icegic.rafael.object.ai.behaviour.RjBehaviourJumpHole;
import competition.icegic.rafael.object.ai.behaviour.RjBehaviourJumpObstacle;
import competition.icegic.rafael.object.ai.behaviour.RjBehaviourThrowFireBall;
import competition.icegic.rafael.object.ai.behaviour.RjBehaviourThrowFireBallPaused;
import competition.icegic.rafael.object.area.EnemiesArea;
import competition.icegic.rafael.object.area.IObjectArea;
import competition.icegic.rafael.object.area.MarioArea;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class ObjectAI
{

	private IObjectArea you;
	private IObjectArea enemy;
	private EnemiesArea enemiesArea;
	private Environment observation;

	private RjBehaviourJumpEnemy behaviourJumpEnemy;
	private RjBehaviourJumpObstacle behaviourJumpObstacle;
	private RjBehaviourJumpHole behaviourJumpHole;
	private RjBehaviourThrowFireBall behaviourThrowFireBall;
	private RjBehaviourThrowFireBallPaused behaviourThrowFireBallPaused;

	public ObjectAI(IObjectArea you, EnemiesArea enemiesArea, Environment observation)
	{
		this.you = you;
		if (enemiesArea != null)
		{
			this.enemy = enemiesArea.getNextEnemy((MarioArea)you);
		}
		this.enemiesArea = enemiesArea;

		this.observation = observation;

		behaviourJumpEnemy = new RjBehaviourJumpEnemy();
		behaviourJumpObstacle = new RjBehaviourJumpObstacle();
		behaviourJumpHole = new RjBehaviourJumpHole();
		behaviourThrowFireBall = new RjBehaviourThrowFireBall();
		behaviourThrowFireBallPaused = new RjBehaviourThrowFireBallPaused();
	}

	public boolean[] getAction()
	{
		boolean action[] = new boolean[Environment.numberOfButtons];

		action[Mario.KEY_LEFT] = false;
		action[Mario.KEY_SPEED] = false;
		action[Mario.KEY_JUMP] = false;
		action[Mario.KEY_RIGHT] = false;


		behaviourThrowFireBallPaused.behave(action);
		behaviourJumpHole.behave(action);
		behaviourJumpObstacle.setBehaviourJumpHole(behaviourJumpHole);
		behaviourJumpEnemy.setBehaviourJumpHole(behaviourJumpHole);

		behaviourJumpObstacle.behave(action);

		behaviourJumpEnemy.behave(action);

		boolean jumping = (behaviourJumpEnemy.isJumping() ||
				behaviourJumpObstacle.isJumping() ||
				behaviourJumpHole.isJumping()) ? true : false;


		behaviourThrowFireBall.setJumping(jumping);
		behaviourThrowFireBall.behave(action);



		if (observation.isMarioOnGround())
		{
			action[Mario.KEY_RIGHT] = true;
		}



		return action;

	}

	

	public IObjectArea getYou() {
		return you;
	}

	public void setYou(IObjectArea you) {
		this.you = you;
		this.behaviourJumpEnemy.setYou(you);
		this.behaviourJumpObstacle.setYou(you);
		this.behaviourJumpHole.setYou(you);
		this.behaviourThrowFireBall.setYou(you);
		this.behaviourThrowFireBallPaused.setYou(you);
	}

	public IObjectArea getEnemy() {
		return enemy;
	}

	public void setEnemiesArea(EnemiesArea enemiesArea) {
		this.enemy = enemiesArea.getNextEnemy((MarioArea)you);
		this.enemiesArea = enemiesArea;
		this.behaviourJumpEnemy.setEnemiesArea(enemiesArea);
		this.behaviourJumpObstacle.setEnemiesArea(enemiesArea);
		this.behaviourJumpObstacle.setEnemy(this.enemy);
		this.behaviourJumpHole.setEnemy(this.enemy);
		this.behaviourThrowFireBall.setEnemy(this.enemy);
		this.behaviourThrowFireBallPaused.setEnemy(this.enemy);
	}

	public Environment getObservation() {
		return observation;
	}

	public void setObservation(Environment observation) {
		this.observation = observation;
		this.behaviourJumpEnemy.setObservation(observation);
		this.behaviourJumpObstacle.setObservation(observation);
		this.behaviourJumpHole.setObservation(observation);
		this.behaviourThrowFireBall.setObservation(observation);
		this.behaviourThrowFireBallPaused.setObservation(observation);
	}

	/*public static enum Action {JUMP, LEFT, RIGHT, SPEED};



	public boolean is(Action action)
	{


		switch(action)
		{
		case JUMP:
			return isJump();

		case LEFT:
			return isLeft();

		case RIGHT:
			return isRight();

		case SPEED:
			return isSpeed();

		default:
			return false;
		}
	}


	private boolean isSpeed()
	{
		//return isThrowFireBall();
		return false;
	}
	*/



	/*private static int scenesJump = 0;
	private static boolean isJumping = false;
	private boolean isJump()
	{

		//prolong the pressing button jump by five scenes, to can jump more high
		if (isProlongJump())
		{
			return true;
		}



		if (you.isObjectComing(emeny)
				&& (you.getY() <= emeny.getY()))

		{
			if (observation.isMarioOnGround())
			{
				isJumping = true;
				return true;
			}
		}



		//if pass blocked, jump!
		if(((MarioArea)you).isBlocked() && (observation.mayMarioJump()))
		{
			//only jump if no contains enemy next
			if (you.isObjectComing(emeny))
			{
				if (emeny.isPaused())
				{
					isJumping = true;
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				isJumping = true;
				return true;
			}
		}


		return false;
	}

	private boolean isProlongJump()
	{
		if (isJumping)
		{
			if (scenesJump < 6)
			{
				scenesJump++;
				return true;
			}
			else
			{
				scenesJump = 0;
				isJumping = false;
				return false;
			}
		}
		else
		{
			return false;
		}
	}



	private boolean isRight()
	{
		return !isPause();
	}

	private boolean isLeft()
	{
		return false;
	}*/


}
