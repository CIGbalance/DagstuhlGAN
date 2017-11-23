package competition.icegic.rafael;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import competition.icegic.rafael.object.ai.ObjectAI;
import competition.icegic.rafael.object.area.EnemiesArea;
import competition.icegic.rafael.object.area.EnemyArea;
import competition.icegic.rafael.object.area.IObjectArea;
import competition.icegic.rafael.object.area.MarioArea;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class RjAgent extends BasicAIAgent implements Agent
{

	int timeInitialize = 0;
    protected boolean Action[] = new boolean[Environment.numberOfButtons];
    protected String Name = "Rafael_RjAgent";

    private IObjectArea marioArea = new MarioArea(null);
    private IObjectArea enemyArea = new EnemyArea(null);
    ObjectAI ai = new ObjectAI(null, null, null);

    public RjAgent() {
        super("RjAgent");
    }

    public void reset()
    {
        Action = new boolean[Environment.numberOfButtons];
        Action[Mario.KEY_RIGHT] = true;
        Action[Mario.KEY_SPEED] = true;
    }



    public boolean[] getAction(Environment observation)
    {


    	byte[][] scene = observation.getLevelSceneObservation();



    	marioArea.setEnvironment(observation.getMarioFloatPos());
    	EnemiesArea enemiesArea = new EnemiesArea(observation);

    	ai.setYou(marioArea);
    	ai.setEnemiesArea(enemiesArea);
    	ai.setObservation(observation);

    	Action = ai.getAction();

    	try {
    		timeInitialize++;

    		if (timeInitialize > 20)
    			Thread.sleep(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        return Action;
    }



    public AGENT_TYPE getType()
    {
        return AGENT_TYPE.AI;
    }

    public String getName()
    {
    	return Name;
    }

    public void setName(String Name)
    {
    	this.Name = Name;
    }

}
