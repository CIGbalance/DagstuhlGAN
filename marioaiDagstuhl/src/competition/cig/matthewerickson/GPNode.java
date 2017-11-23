package competition.cig.matthewerickson;

import ch.idsia.mario.environments.Environment;

public abstract class GPNode {
	
	private static int numNodeTypes = 10;
	private static int numTerminalTypes = 5;
	private static double terminalChance = 0.05;
	GPNode children[];
	
	public GPNode() {
		this.children = new GPNode[arity()];
	}; 
	
	public void addNode(GPNode newNode, int position) 
	{
	   if (position >= children.length)
	   {
		   //Should throw an error here...
		   return;
	   }
	   children[position] = newNode;
	}
	
    public boolean isTerminail(){ return false; }
	abstract public int arity();
	abstract public float[] evaluate(GPAgent agent, Environment observation);

	public static GPNode newProgram(int maxDepth) {
		GPNode newNode;
		if(maxDepth <= 1){
		   newNode = randomTerminal();
		}
		else{
		   newNode = randomNode();
		   for(int i=0; i<newNode.arity(); i++)
		   {
			   newNode.addNode(GPNode.newProgram(maxDepth - 1), i);
		   }
		}
		return newNode;
	}

	private static GPNode randomTerminal() {
		switch((int)(Math.random()*(double)numTerminalTypes) + 1)
		{
		  case 1: return new FloatNode();
		  case 2: return new RandomNode();
		  case 3: return new NearestEnemyNode();
		  case 4: return new NextPitNode();
		  case 5: return new IsOnGroundNode();
		}
		return null;
	}

	private static GPNode randomNode() {
		int selection = (int)(Math.random()*(double)(numNodeTypes) + 1);
		selection += (int)(Math.random()*(terminalChance + 1));
		switch(selection)
		{
		  case 1: return new AddNode();
		  case 2: return new SubNode();
		  case 3: return new IfNode();
		  case 4: return new NotNode();
		  case 5: return new ShiftLeftNode();
		  case 6: return new ShiftRightNode();
		  case 7: return new MaskWNode();
		  case 8: return new MaskXNode();
		  case 9: return new MaskYNode();
		  case 10: return new MaskZNode();
		  
		  default: return randomTerminal();
		}
	}
	
	abstract GPNode copy();

	public int size() {
		int size = 1;
		for(int i=0;i<children.length;i++)
		{
			size += children[i].size();
		}
		
		return size;
	}

	public GPNode copySubtreeAt(int crossPoint) {
		
		if(crossPoint == 1) {return this.copy();}
		
		crossPoint -= 1;
		GPNode result = null;
		
		for(int i=0;i<children.length;i++)
		{
			result = children[i].copySubtreeAt(crossPoint);
			if(result != null)
			{
				return result;
			}
			crossPoint -= children[i].size();
		}
		
		return null;
	}

	//Kludge
	public GPNode replaceTree(int crossPoint, GPNode newSubtree) {
		crossPoint -= 1;
		for(int i=0;i<children.length;i++)
		{
			GPNode result;
			if(crossPoint <= 1)
			{
				children[i] = newSubtree;
				return this;
			}
			result = children[i].replaceTree(crossPoint, newSubtree);
			if(result != null){return this;}
			crossPoint -= children[i].size();
		}
		
		return null;
	}
	
}
