package competition.cig.matthewerickson;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class GPAgent implements Evolvable, Agent {

	private int maxDepth = 3;
	private String name = "GPAgent";
	GPNode program = new FloatNode();
	public boolean[] actions = new boolean[Environment.numberOfButtons];
	
	public Evolvable copy() {
		GPAgent copy = new GPAgent();
		copy.program = program.copy();
		return copy;
	}

	public Evolvable getNewInstance() {
		GPAgent newAgent = new GPAgent();
		newAgent.program = GPNode.newProgram(maxDepth);
		return newAgent; 
	}

	public void mutate() {
		// TODO Auto-generated method stub

	}
	

	public void reset() {
		actions = new boolean[Environment.numberOfButtons];
	}

	public boolean[] getAction(Environment observation) {
		this.reset();
		float[] result = program.evaluate(this, observation);
		
		//First item in vector is direction
		if(result[0] > 0){actions[Mario.KEY_RIGHT] = true;}
		if(result[0] < 0){actions[Mario.KEY_LEFT] = true;}
		
		//Second is jump
		if(result[1] > 0){actions[Mario.KEY_JUMP] = true;}
		
		//Third is speed
		if(result[1] > 0){actions[Mario.KEY_SPEED] = true;}
		
		//Fourth is down
		if(result[2] > 0){actions[Mario.KEY_DOWN] = true;}
		
		return actions;
	}

	public String getName() {
		return name;
	}

	public AGENT_TYPE getType() {
		return AGENT_TYPE.AI;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GPAgent newCrossbreedWith(GPAgent parent2) {
		// Pick a random node on each parent and create a
		// new offspring via crossbreeding
		int crossPoint1 = (int)(Math.random() * (double)parent2.program.size()) + 1;
		int crossPoint2 = (int)(Math.random() * ((double)this.program.size() - 1)) + 2;//Cannot pick root node
		GPNode newSubtree;
		GPAgent newAgent = (GPAgent)this.copy();
		newSubtree = parent2.program.copySubtreeAt(crossPoint1);
		if(newAgent.program.size() == 1) 
		{
			//HACK
			newAgent.program = newSubtree;
			return newAgent;
		}
		newAgent.program.replaceTree(crossPoint2,newSubtree);
		return newAgent;
	}

	public GPAgent newMutant() {
		int point = (int)(Math.random() * (double)this.program.size()) + 1;
		GPAgent newAgent = (GPAgent)this.copy();
		GPNode newSubTree = GPNode.newProgram(5);
		newAgent.program.replaceTree(point, newSubTree);
		return newAgent;
	}

}
