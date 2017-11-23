package competition.icegic.rafael.object.area;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.mario.environments.Environment;

public class EnemiesArea
{
	private List<EnemyArea> listEnemies;
	private Environment observation;

	private float[] enemyPos;

	public EnemiesArea(Environment observation)
	{
		this.observation = observation;

		fillObject();

	}

	private void fillObject()
	{
		if ((observation.getEnemiesFloatPos() != null)
    			&& (observation.getEnemiesFloatPos().length >= 0))
    	{
			listEnemies = new ArrayList<EnemyArea>();
			float[] enemiesPos = observation.getEnemiesFloatPos();

			for(int i=0; i < enemiesPos.length; i=i+3)
			{
//				enemyPos = Arrays.copyOfRange(enemiesPos, i, i+3);
				EnemyArea enemyArea = new EnemyArea(enemyPos);
				listEnemies.add(enemyArea);
			}

    	}

	}

	public List<EnemyArea> getListEnemies() {
		return listEnemies;
	}

	public void setListEnemies(List<EnemyArea> listEnemies) {
		this.listEnemies = listEnemies;
	}

	public EnemyArea getNextEnemy(MarioArea marioArea)
	{
		EnemyArea enemyComming = null;
		long commingPosition = 1000;
		if (listEnemies != null && listEnemies.size() > 0)
		{
			enemyComming = listEnemies.get(0);
			//commingPosition = enemyComming.getX();
			for (EnemyArea enemy : listEnemies)
			{
				if (enemy.getX() > marioArea.getX())
				{
					long pos = enemy.getX() - marioArea.getX();
					if (pos < commingPosition)
					{
						enemyComming = enemy;
					}
				}
			}

		}

		return enemyComming;
	}

	public int getEnemiesFireBallSensitive()
	{
		int total = 0;
		if (listEnemies != null)
		{
			for(EnemyArea enemy : listEnemies)
			{
				if (enemy.isFireBallSensitive())
				{
					total++;
				}
			}
		}

		return total;
	}



}
