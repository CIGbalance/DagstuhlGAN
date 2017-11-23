package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class ShiftLeftNode extends GPNode {

	public int arity() {
		return 1;
	}

	GPNode copy() {
		ShiftLeftNode copy = new ShiftLeftNode();
		copy.children[0] = children[0].copy();
		return copy;
	}

	public float[] evaluate(GPAgent agent, Environment observation) {
		float[] result = {0,0,0,0};
		float[] childValue = children[0].evaluate(agent, observation);
		result[0] = childValue[1];
		result[1] = childValue[2];
		result[2] = childValue[3];
		result[3] = childValue[0];
		
		return result;
	}
	
	public String toString()
	{
		return "SL(" + children[0].toString() + ")";
	}

}
