package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class NotNode extends GPNode {

	public int arity() {
		return 1;
	}


	GPNode copy() {
		NotNode copy = new NotNode();
		copy.children[0] = children[0].copy();
		return copy;
	}

	@Override
	public float[] evaluate(GPAgent agent, Environment observation) {
		float[] result = children[0].evaluate(agent, observation);
		for(int i=0;i<result.length;i++)
		{
			result[i] *= -1;
		}
		return result;
	}
	
	public String toString()
	{
		return "NOT(" + children[0].toString() + ")"; 
	}

}
