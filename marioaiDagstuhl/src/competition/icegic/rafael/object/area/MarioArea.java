package competition.icegic.rafael.object.area;

import java.util.List;


public class MarioArea implements IObjectArea
{
	private float[] observation;
	private static long oldX = 0;
	private static long oldY = 0;

	public MarioArea(float[] observation)
	{
		this.observation = observation;
	}

	public long getX()
	{
		if (observation != null && observation.length > 0)
		{
			long marioPosX = (long) observation[0];
			return marioPosX;
		}
		else
		{
			return 0;
		}
	}

	public long getY()
	{
		if (observation != null && observation.length > 0)
		{
			long marioPosY = (long) observation[1];
			return marioPosY;
		}
		else
		{
			return 0;
		}

	}

	public boolean isObjectComing(IObjectArea object)
	{
		if ((object == null) || (object.getX() == 0 && object.getY() == 0))
		{
			return false;
		}


		long enemyPosIni = (long) object.getX() - 40;
		long enemyPosFin = (long) object.getX() + 40;

		if ((this.getX() >= enemyPosIni)
				&& (this.getX() < enemyPosFin))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isObjectComing(IObjectArea object, int level)
	{
		if ((object == null) || (object.getX() == 0 && object.getY() == 0))
		{
			return false;
		}


		long enemyPosIni = (long) object.getX() - level;
		long enemyPosFin = (long) object.getX() + level;

		if ((this.getX() >= enemyPosIni)
				&& (this.getX() < enemyPosFin))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isObjectComing(List<EnemyArea> listObject)
	{
		if (listObject != null && listObject.size() > 0)
		{
			for (EnemyArea object : listObject)
			{
				if (this.isObjectComing(object))
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean isPaused()
	{
		if (this.getX() == oldX && this.getY() == oldY)
		{
			return true;
		}
		else
		{
			oldX = this.getX();
			oldY = this.getY();
			return false;
		}
	}

	public boolean isBlocked()
	{
		return isPaused();
	}

	public boolean isObstacleNext()
	{
		//TODO
		return false;
	}

	public void setEnvironment(float[] observation)
	{
		this.observation = observation;
	}

}
