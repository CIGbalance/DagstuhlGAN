package competition.cig.alexandrupaler;

import ch.idsia.ai.MLP;
import ch.idsia.ai.agents.Agent;
//import ch.idsia.ai.agents.RegisterableAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 29, 2009
 * Time: 12:19:49 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class HumanKeyboardAgent extends KeyAdapter implements Agent
{
    List<boolean[]> history = new ArrayList<boolean[]>();
    private boolean[] Action = null;
    private String Name = "HumanKeyboardAgent";

    private MLP mlp = new MLP(4, 50, 4); /*in coord start coord stop, out intensitatea apasarii tastelor*/
    private double[] lastGround = new double[2];
    
    protected int[] maxKp = {5, 15, 15, 30};
    protected int[] curKp = {0, 0, 0, 0};
    protected int[] press = {0, 0, 0, 0};
    protected boolean otherdir = false;
    protected boolean trainFinished = false;
    
    protected double[] inputIn = new double[4];
    protected double[] outputIn = new double[4];
    
    public HumanKeyboardAgent()
    {
        this.reset ();
        mlp.ssetLearningRate(0.01);
//        RegisterableAgent.registerAgent(this);
    }

    public void reset()
    {
        // Just check you keyboard. Especially arrow buttons and 'A' and 'S'!
        Action = new boolean[Environment.numberOfButtons];
    }

    public boolean[] getAction(Environment observation)
    {
    	reset();
    	
    	if(trainFinished)
    		return Action;
    	
    	boolean moveFinish = true;
    	for(int i=0; i<4; i++)
    	{
    		if(press[i] > 0)
    		{
    			press[i]--;
    			switch(i)
    			{
    			case 3:
   					Action[Mario.KEY_RIGHT] = true;
    				break;
    			case 2:
    				Action[Mario.KEY_JUMP] = true;
    				break;
    			case 1:
    				Action[Mario.KEY_LEFT] = true;
    				break;
    			case 0:
    				Action[Mario.KEY_SPEED] = true;
    				break;
    			default:
    				break;
    			}
    			
    			moveFinish = false;
    		}
    	}
    	
    	if(!moveFinish)
    	{
    		return Action;
    	}
    	else
    	{
    		inputIn[0] = (inputIn[0] - observation.getMarioFloatPos()[1]) / (6 * 32);/*h*/
    		inputIn[1] = (observation.getMarioFloatPos()[0] - inputIn[1]) / (6 * 32);/*w*/
    		inputIn[2] = 0.0;
    		inputIn[3] = 0.0;
    		
    		if(inputIn[0] < 0)
    		{
    			inputIn[2] = Math.abs(inputIn[0]);
    			inputIn[0] = 0.0;
    		}
    		
    		if(inputIn[1] < 0)
    		{
    			inputIn[3] = Math.abs(inputIn[1]);
    			inputIn[1] = 0.0;
    		}
    		
    		
    		mlp.propagate(inputIn);
    		
    		double eR = mlp.backPropagate(outputIn);
    		
//    		for(int i=0; i<4; i++)
//    		{
//    			System.out.print(inputIn[i] + "="  + outputIn[i] + "  ");
//    		}
//    		System.out.println();
//    		System.out.println(eR);
//    		System.out.println("-------------------------");
    	}
    	
    	if(!observation.isMarioOnGround())
    		return Action;
        
        if(curKp[0] < maxKp[0])//epoch
        {
        	if(curKp[1] < maxKp[1])//speed
        	{
        		if(curKp[2] < maxKp[2])//jump
        		{
        			if(curKp[3] < maxKp[3])//left + right
        			{
        				curKp[3]++;
        				otherdir = curKp[3] > 15;
        			}
        			else
        			{
        				curKp[2]++;
        				curKp[3] = 0;
        				otherdir = false;
        			}
        		}
        		else
        		{
        			curKp[1]++;
        			curKp[2] = 0;
        		}
        	}
        	else
        	{
        		curKp[0]++;
        		curKp[1] = 0;
        	}		
        }
        else
        {
        	double[] w = mlp.getWeightsArray();
            for(int i=0; i<w.length; i++)
            	System.out.print(w[i] + ", ");
//            System.out.println("^" + curKp[0]+ " -------------");
            
            trainFinished = true;
        }
        
        
        outputIn[0] = press[0] = curKp[1];
        outputIn[1] = press[1] = otherdir ? curKp[3] - 15 : 0;
        outputIn[2] = press[2] = curKp[2];
        outputIn[3] = press[3] = !otherdir ? curKp[3]: 0;
        
        for(int i=0; i<4; i++)
        	outputIn[i] /= 15;
        
        inputIn[0] = observation.getMarioFloatPos()[1];
        inputIn[1] = observation.getMarioFloatPos()[0];
        
        
//        System.out.println(curKp[0] + " " + curKp[1] + " " + curKp[2] + " " + curKp[3]);
        
        return Action;
        
//        int randsize = (int)(Math.random() * 20);
//        
//        if(observation.isMarioOnGround() || history.size() >  randsize)
//        {
//        	double c = lastGround[0];
//        	double r = lastGround[1];
//        	
//        	lastGround[0] = observation.getMarioFloatPos()[0];
//        	lastGround[1] = observation.getMarioFloatPos()[1];
//        	inputIn[0] = r - lastGround[1];
//        	inputIn[1] = lastGround[0] - c;
//        	
//        	//long start = System.currentTimeMillis(); // start timing
//        	outputIn = mlp.propagate(inputIn);
//        	//long stop = System.currentTimeMillis(); // stop timing
//            //System.out.println(r + " " + c); // print execution time
//
//        	double[] target = new double[4];
//        	while(!history.isEmpty())
//        	{
//        		boolean[] a = history.remove(0);
//        		//for(int i=0; i < Action.length; i++)
//        		{
//        			if(a[Mario.KEY_JUMP])
//        				target[0] += 0.01;
//        			if(a[Mario.KEY_LEFT])
//        				target[1] += 0.01;
//        			if(a[Mario.KEY_RIGHT])
//        				target[2] += 0.01;
//        			if(a[Mario.KEY_SPEED])
//        				target[3] += 0.01;
//        		}
//        	}
//        	if(Math.abs(target[0])+Math.abs(target[1])+Math.abs(target[2])+Math.abs(target[3]) != 0)
//        	{
//        		System.out.println("\nERROR: " + mlp.backPropagate(target));
//	        	
////	            for(int i=0; i<4; i++)
////	            	System.out.print(inputIn[i]+ " ");
////	            System.out.println();
////	            for(int i=0; i<4; i++)
////	            	System.out.print(target[i] + " ");
////	            System.out.println();
////	            for(int i=0; i<4; i++)
////	            	System.out.print(outputIn[i] + " ");
//	            System.out.println("\n----------------");
//	            
//	            double[] w = mlp.getWeightsArray();
//	            for(int i=0; i<w.length; i++)
//	            	System.out.print(w[i] + ", ");
//        	}
//        }
//        
//        history.add(Action);
//        
//        return Action;
    }

    public AGENT_TYPE getType() {        return AGENT_TYPE.HUMAN;    }

    public String getName() {   return Name; }

    public void setName(String name) {        Name = name;    }


    public void keyPressed (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
    }

    public void keyReleased (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }


    private void toggleKey(int keyCode, boolean isPressed)
    {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                Action[Mario.KEY_DOWN] = isPressed;
                break;

            case KeyEvent.VK_S:
                Action[Mario.KEY_JUMP] = isPressed;
                break;
            case KeyEvent.VK_A:
                Action[Mario.KEY_SPEED] = isPressed;
                break;
        }
    }

   public List<boolean[]> getHistory () {
       return history;
   }
   
   public double scaleTo05(double v)
   {
	   return v < 0 ? Math.abs(v)/2 : v/2 + 0.5;
   }
   
}
