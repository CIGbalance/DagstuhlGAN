package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class RandomNode extends TerminalNode {


	//return random value between -1 and 1
	public float[] evaluate(GPAgent agent, Environment observation) {
		float[] result = new float[4];
		for(int i=0;i<result.length;i++)
		{
		    if(Math.random() > 0.5)
		    {
	           result[i] = (float)Math.random();
		    }
		    else
		    {
			    result[i] = -1*(float)Math.random();
		    }
		}
		return result;
	}

	GPNode copy() {
		return new RandomNode();
	}
	
	public String toString()
	{
		return "RND";
	}

}
