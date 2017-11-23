package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class IfNode extends GPNode {

	public int arity() {
		return 2;
	}

	GPNode copy() {
		IfNode copy = new IfNode();
		copy.children[0] = children[0].copy();
		copy.children[1] = children[1].copy();
		return copy;
	}

	public float[] evaluate(GPAgent agent, Environment observation) {
		if(children[0] == null) {System.out.println("Shit!");}
		if(children[0].evaluate(agent, observation)[0] > 0)
		{ 
			if(children[1] == null) {System.out.println("Shit2!");}
			return children[1].evaluate(agent, observation);
		}
		float[] result = {0,0,0,0}; 
		return result;
	}
	
	public String toString()
	{
		return "IF(" + children[0].toString() + " , " + children[1].toString() + ")";
	}

}
