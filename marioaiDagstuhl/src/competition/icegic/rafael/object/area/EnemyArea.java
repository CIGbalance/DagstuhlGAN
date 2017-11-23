package competition.icegic.rafael.object.area;

public class EnemyArea implements IObjectArea
{
	private float[] observation;
	private static long oldX;
	private static long oldY;

	private EnemyType type;

	public EnemyType getType() {
		return type;
	}

	public void setType(EnemyType type) {
		this.type = type;
	}

	public EnemyArea(float[] observation)
	{
		this.observation = observation;

		if (observation != null && observation.length > 0)
		{
			int typeId = (int)observation[0];
			switch(typeId)
			{
				case 2:
					this.setType(EnemyType.MUSHROOM_BAD);
					break;

				case 3:
					this.setType(EnemyType.MUSHROOM_BAD_FLY);
					break;

				case 4:
					this.setType(EnemyType.TORTOISE_RED);
					break;

				case 5:
					this.setType(EnemyType.TORTOISE_RED_FLY);
					break;

				case 6:
					this.setType(EnemyType.TORTOISE_GREEN);
					break;

				case 7:
					this.setType(EnemyType.TORTOISE_GREEN_FLY);
					break;

				case 8:
					this.setType(EnemyType.MISSILE);
					break;

				case 9:
					this.setType(EnemyType.BARK);
					break;

				case 10:
					this.setType(EnemyType.BARK_FLY);
					break;

				case 12:
					this.setType(EnemyType.PLANT_CARNIVORE);
					break;
			}
		}
	}

	public long getX()
	{
		if (observation != null && observation.length > 0)
		{
			long enemyPosX = (long) observation[1];
			return enemyPosX;
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
			long enemyPosY = (long) observation[2];
			return enemyPosY;
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

	public boolean isPaused()
	{
		if ((this.getX() == oldX)
				&& (this.getY() == oldY))
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

	public void setEnvironment(float[] observation)
	{
		this.observation = observation;
	}

	public boolean isWalkToLeft()
	{
		boolean result = (this.getX() < oldX);
		oldX = this.getX();

		return result;
	}

	public boolean isWalkToRight()
	{
		boolean result = (this.getX() > oldX);
		oldX = this.getX();

		return result;
	}

	public boolean isFireBallSensitive()
	{
		if ((this.getType() != null) && ((this.getType().equals(EnemyType.MUSHROOM_BAD))
			|| (this.getType().equals(EnemyType.TORTOISE_GREEN))
			|| (this.getType().equals(EnemyType.TORTOISE_RED_FLY))
			|| (this.getType().equals(EnemyType.TORTOISE_GREEN_FLY))
			|| (this.getType().equals(EnemyType.MUSHROOM_BAD_FLY))
			|| (this.getType().equals(EnemyType.TORTOISE_RED))))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}
