package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class NextPitNode extends TerminalNode {

	public float[] evaluate(GPAgent agent, Environment observation) {
		byte[][] levelScene = observation.getLevelSceneObservation();
		
		float[] result = {-1,-1,-1,-1};
		boolean foundPit = true;
		
		for(int x=12; x<levelScene[0].length; x++)
		{
			foundPit = true;
			for(int y=0; y<levelScene[x].length; y++)
			{
				if(levelScene[y][x] != 0)
				{
					foundPit = false;
				}
			}
			if(foundPit){
				result[0] = (float)x / 12;
				result[1] = (float)x / 12;
				result[2] = (float)x / 12;
				result[3] = (float)x / 12;
			}
		}
				
		return result;
	}

	GPNode copy() {
		return new NextPitNode();
	}
	
	public String toString()
	{
		return "NextPitNode";
	}

}
