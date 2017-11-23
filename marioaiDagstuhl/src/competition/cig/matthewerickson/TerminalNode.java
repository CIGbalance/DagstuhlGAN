package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public abstract class TerminalNode extends GPNode {

	public final boolean isTerminail(){ return true; }
	
	public final int arity() {
		return 0;
	}

	abstract public float[] evaluate(GPAgent agent, Environment observation);

}
