package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class AddNode extends GPNode {

	public int arity() {
		return 2;
	}

	public float[] evaluate(GPAgent agent, Environment observation) {
		float[] result = new float[4];
		float[] result1 =  children[0].evaluate(agent, observation);
		float[] result2 =  children[1].evaluate(agent, observation);
		for(int i=0;i<result1.length;i++)
		{
			result[i] = result1[i] + result2[i];
		}
		
		return result;
	}

	public String toString()
	{
		return "Add(" + children[0].toString() + " , " + children[1].toString() + ")";
	}

	public GPNode copy() {
		AddNode copy = new AddNode();
		copy.children[0] = children[0].copy();
		copy.children[1] = children[1].copy();
		return copy;
	}
	
}
