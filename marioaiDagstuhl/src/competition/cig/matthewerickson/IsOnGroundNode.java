package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class IsOnGroundNode extends TerminalNode {

	public float[] evaluate(GPAgent agent, Environment observation) {
		if(observation.isMarioOnGround())
		{
			float[] result = {1,1,1,1};
			return result;
		}
		else 
		{
			float[] result = {-1,-1,-1,-1};
			return result;
		}
	}

	GPNode copy() {
		return new IsOnGroundNode();
	}

	public String toString()
	{
		return "IsOnGround";
	}
	
}
