package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public class FloatNode extends TerminalNode {

	float[] value;

    FloatNode()
	{
    	value = new float[4];
    	for(int i=0;i<value.length;i++)
    	{
    		value[i] = (float)Math.random();
    	}
	}
	
	public float[] evaluate(GPAgent agent, Environment observation) 
	{
        return value;
	}
	
	public String toString()
	{
		return "" + value[0] + "," + value[1] + "," + value[2] + "," + value[3];
	}

	public GPNode copy() {
		FloatNode copy = new FloatNode();
		copy.value = value.clone();
		return copy;
	}
	
}
