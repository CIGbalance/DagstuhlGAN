package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class MaskZNode extends GPNode {

	public int arity() {
		return 1;
	}

	GPNode copy() {
		MaskZNode copy = new MaskZNode();
		copy.children[0] = children[0].copy();
		return copy;
	}

	public float[] evaluate(GPAgent agent, Environment observation) {
		float[] result = children[0].evaluate(agent, observation);
		result[3] = 0;
		return result;
	}
	
	public String toString()
	{
		return "MaskZ(" + children[0].toString() + ")";
	}

}
