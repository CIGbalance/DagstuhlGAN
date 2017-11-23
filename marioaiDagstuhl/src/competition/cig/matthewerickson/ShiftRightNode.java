package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class ShiftRightNode extends GPNode {

	public int arity() {
		return 1;
	}

	GPNode copy() {
		ShiftRightNode copy = new ShiftRightNode();
		copy.children[0] = children[0].copy();
		return copy;
	}

	public float[] evaluate(GPAgent agent, Environment observation) {
		float[] result = {0,0,0,0};
		float[] childValue = children[0].evaluate(agent, observation);
		result[0] = childValue[3];
		result[1] = childValue[0];
		result[2] = childValue[1];
		result[3] = childValue[2];
		
		return result;
	}
	
	public String toString()
	{
		return "SR(" + children[0].toString() + ")";
	}

}
