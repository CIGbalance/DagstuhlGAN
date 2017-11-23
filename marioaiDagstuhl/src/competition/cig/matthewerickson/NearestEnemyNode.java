package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class NearestEnemyNode extends TerminalNode {

	public float[] evaluate(GPAgent agent, Environment observation) {
		//Marios x and y cordinates
		float[] marioPos = observation.getMarioFloatPos();
		//List of type, x, y
		float[] enemies = observation.getEnemiesFloatPos();
		
		float[] result = {0,0,0,0}; 
		float closestDist = 1000;
		
		
		for(int i=0; i<enemies.length; i+=3)
		{
			float dx = enemies[i+1] - marioPos[0];
			float dy = enemies[i+2] - marioPos[1];
			float dist = (float)Math.sqrt((dx*dx) + (dy*dy));
			
			if(dist < closestDist) 
			{
				closestDist = dist;
				result[0] = dx / 11;
				result[1] = dy / 11;
				result[2] = enemies[0] / 20;
				result[3] = dist / 200;
			} 
		}
		
		return result;
	}
	
	public String toString()
	{
		return "NearestEnemey";
	}

	GPNode copy() {
		return new NearestEnemyNode();
	}

}
