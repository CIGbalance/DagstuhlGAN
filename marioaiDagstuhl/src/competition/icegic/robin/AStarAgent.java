package competition.icegic.robin;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import competition.icegic.robin.astar.AStarSimulator;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class AStarAgent extends BasicAIAgent implements Agent
{
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "AStarAgent";
    private AStarSimulator sim;
    private int tickCounter = 0;
    private float lastX = 0;
    private float lastY = 0;

    public AStarAgent() {
        super("AStarAgent");
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];// Empty action
        sim = new AStarSimulator();
    }
    
    public void printLevel(byte[][] levelScene)
    {
    	for (int i = 0; i < levelScene.length; i++)
    	{
    		for (int j = 0; j < levelScene[i].length; j++)
    		{
    			//if ((levelScene[i][j] > 1 && levelScene[i][j] <= 15) || levelScene[i][j] == 20 
    			//		|| levelScene[i][j] == 21 || levelScene[i][j] == 22 || levelScene[i][j] == 25) 
    			//	System.out.print(">");
    			System.out.print(levelScene[i][j]+"\t");
    		}
    		System.out.println("");
    	}
    }

    public boolean[] getAction(Environment observation)
    {
    	tickCounter++;
    	String s = "Fire";
    	if (!sim.levelScene.mario.fire)
    		s = "Large";
    	if (!sim.levelScene.mario.large)
    		s = "Small";
    	if (sim.levelScene.verbose > 0) System.out.println("Next action! Tick " + tickCounter + " Simulated Mariosize: " + s);

    	boolean[] ac = new boolean[5];
    	ac[Mario.KEY_RIGHT] = true;
    	ac[Mario.KEY_SPEED] = true;
    	
    	//byte[][] scene = observation.getCompleteObservation();//observation.getLevelSceneObservation(0);
    	byte[][] scene = observation.getLevelSceneObservation();
    	float[] enemies = observation.getEnemiesFloatPos();
    	
    	//observation.getCompleteObservation();
    	//System.out.println("Clean scene:");
    	//printLevel(scene);
    	
    	//System.out.println("Complete Obs:");
    	//printLevel(observation.getCompleteObservation());
    	
    	if (sim.levelScene.verbose > 2) System.out.println("Simulating using action: " + sim.printAction(action));
        sim.advanceStep(action);   
    	
        if (sim.levelScene.verbose > 5) System.out.println("Simulated sprites: ");
        if (sim.levelScene.verbose > 5) sim.levelScene.dumpSprites();
        
    	//System.out.println("Internal scene after sim:");
        //printLevel(sim.levelScene.levelSceneObservation(0));
        
        sim.setLevelPart(scene, enemies);
        //printLevel(sim.levelScene.levelSceneObservation(0));
		float[] f = observation.getMarioFloatPos();
		if (sim.levelScene.verbose > 5)
			System.out.println("Sim Mario Pos: " 
					+ sim.levelScene.mario.x + " " + sim.levelScene.mario.y + " " +
					" a: " + sim.levelScene.mario.xa + " " + sim.levelScene.mario.ya );
		if (sim.levelScene.mario.x != f[0] || sim.levelScene.mario.y != f[1])
		{
			if (f[0] == lastX && f[1] == lastY)
				return ac;
			//System.out.print("i");
			if (sim.levelScene.verbose > 0) System.out.println("INACURATEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE!");
			if (sim.levelScene.verbose > 0) System.out.println("Real: "+f[0]+" "+f[1]
			      + " Est: "+ sim.levelScene.mario.x + " " + sim.levelScene.mario.y +
			      " Diff: " + (f[0]- sim.levelScene.mario.x) + " " + (f[1]-sim.levelScene.mario.y));
			sim.levelScene.mario.x = f[0];
			sim.levelScene.mario.y = f[1];
			sim.levelScene.mario.xa = (f[0] - lastX) *0.89f;
			sim.levelScene.mario.ya = (f[1] - lastY) * 0.85f + 3f;
			//errCount++;
			//if (errCount > 1)
			//	errAgent.lastX++;
		}
		lastX = f[0];
		lastY = f[1];

        action = sim.optimise();
        
        if (sim.levelScene.verbose > 1) System.out.println("Returning action: " + sim.printAction(action));
        return action;
    }

    public AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() 
    {        
    	return name;    
    }

    public void setName(String Name) 
    { 
    	this.name = Name;    
    }
}
