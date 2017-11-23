package competition.cig.sergeypolikarpov;

import java.util.Random;

import competition.cig.sergeypolikarpov.CyberNeuron;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Apr 28, 2009
 * Time: 2:09:42 PM
 * 
 * 
 * Modified by Sergey V. Polikarpov
 * Date: Aug 29, 2009
 * Time: 16:09:00
 */
public class SergeyPolikarpov_SimpleCyberNeuronAgent implements Agent, Evolvable {

	private CyberNeuron cbrn;
    private String name = "SergeyPolikarpov_SimpleCyberNeuronAgent";
    final int numberOfOutputs = 10;
    final int block_size = 340;
    final int numberOfInputs = 2*block_size+numberOfOutputs;//100;//340;

    final double LearningRate = 0.01;
    boolean temporary_disable_cbrn = false;
    boolean action_in_progress = false;			
    int action_in_progress_type;				
    int count_of_action_in_progress=0;			
    final int action_forward = 1;						
    final int action_forward_jump = 2;			
    final int action_backward = 3;					
    final int action_backward_jump = 4;			
    final int action_forward_with_prone = 5;			
    final int action_forward_with_jump_far = 6;
    final int action_none_action = 7;
    int [][] progressed_action = new int [][] {{3,3,3,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6},{2,2,2,2,2,2,2,2,2,2,2,2},{7,7,7,7,7,7,7,7,7,7,7},{4,4,4,4,4,4,4,4,4,4,4,4,4},{1,1,1,1,1,1,1,1},{3,3,3,6,6,6,6,6,6,6,6},{2,2,2,2,2,2},{3,3,3,3,3},{4,4,4,4,4,4},{1,1,1,1,1}};

    int deep_of_buffer = 35;//16;
    int num_pushes_to_buffer = 0;
    boolean buffer_is_full = false;
    double [][] buffer_of_inputs = new double [deep_of_buffer][numberOfInputs];
    double [] buffer_of_actions = new double [deep_of_buffer];
    int is_positive;
    
    float Mario_pos_X_old;
    float Mario_pos_Y_old;
    float max_allowed_Y = 260;
    int deep_of_buffer_of_Mario_X = 46;//16
    int waiting_counter = 0;
    int num_pushes_to_buffer_of_Mario_X = 0;
    boolean buffer_is_full_of_Mario_X = false;
    float [] buffer_of_Mario_X = new float [deep_of_buffer_of_Mario_X];
    int prev_mario_mode = 2;

    
    int count_active_agent = 0;
    int count_inactive_agent = 0;
    int hurting_count = 0;

    private CyberNeuron detector_of_holes;
    final int detector_of_holes_numberOfInputs = 22*10;
    final int detector_of_holes_numberOfOutputs = 1;
    final int detector_of_holes_deep_of_buffer = 17;//15;
    boolean detector_of_holes_buffer_is_full = false;
    int detector_of_holes_num_pushes_to_buffer = 0;
    double [][] detector_of_holes_buffer_of_inputs = new double [detector_of_holes_deep_of_buffer][detector_of_holes_numberOfInputs];
    boolean force_long_jump_forward = false;
    int detector_of_holes_wait_counter = 0;
    boolean is_first_action_when_hole_is_detected = true;
    
    public static final Random random = new Random();               /**********/
    
    public SergeyPolikarpov_SimpleCyberNeuronAgent() {
        cbrn = new CyberNeuron (numberOfInputs, numberOfOutputs);
        detector_of_holes = new CyberNeuron (detector_of_holes_numberOfInputs, detector_of_holes_numberOfOutputs);
    }

    private SergeyPolikarpov_SimpleCyberNeuronAgent(CyberNeuron cbrn, CyberNeuron cbrn_2) {
        this.cbrn = cbrn;
        this.detector_of_holes = cbrn_2;
        this.reset();
    }

    public Evolvable getNewInstance() {
        return new SergeyPolikarpov_SimpleCyberNeuronAgent(cbrn.getNewInstance(), detector_of_holes.getNewInstance());
    }

    public Evolvable copy() {
        return new SergeyPolikarpov_SimpleCyberNeuronAgent(cbrn.copy (), detector_of_holes.copy());
    }

    public void reset() {
    	cbrn.reset ();
    	detector_of_holes.reset();
        	action_in_progress = false;			
        	count_of_action_in_progress = 0;			
        	buffer_is_full = false;
        	is_positive = 0;
            num_pushes_to_buffer = 0;
            count_active_agent = 0;
            count_inactive_agent = 0;
            waiting_counter = 0;
            num_pushes_to_buffer_of_Mario_X = 0;
            buffer_is_full_of_Mario_X = false;
            prev_mario_mode = 2;
            detector_of_holes_buffer_is_full = false;
            detector_of_holes_num_pushes_to_buffer = 0;
            hurting_count = 0;
            detector_of_holes_wait_counter = 0;
            is_first_action_when_hole_is_detected = true;
        	for (int i = 0; i < buffer_of_inputs.length; i++) {
        		for (int j = 0; j < buffer_of_inputs[i].length; j++) {
        			buffer_of_inputs[i][j] = 0;
        		}
        	}
        	for (int i = 0; i < buffer_of_actions.length; i++) {
        			buffer_of_actions[i] = 0;
        	}
    }

    public void mutate() {
    	cbrn.mutate ();
    }

    public boolean[] getAction(Environment observation) {
        boolean[] action = new boolean[Environment.numberOfButtons]; 
      	byte[][] scene = observation.getLevelSceneObservation();
        byte[][] enemies = observation.getEnemiesObservation();
        float [] Mario_pos = observation.getMarioFloatPos();
        double[] inputs = new double[numberOfInputs];
        int[] outs_of_cbrn = new int [numberOfOutputs];
                
        int which = 0;
        
		/*for (int i = block_size+numberOfOutputs; i < 2*block_size+numberOfOutputs; i++) {
			//inputs[which++] = buffer_of_inputs[buffer_of_inputs.length-1][i];
			inputs[which++] = 0;
		}*/
		for(int f = 0; f < block_size/numberOfOutputs+1; f++) {
			for(int i = 0; i < numberOfOutputs; i++) {
				if(i == buffer_of_actions[buffer_of_actions.length-1-f]) {outs_of_cbrn[i] = 1;}
				else											 		 {outs_of_cbrn[i] = 0;}
			}
			for(int i = 0; i < numberOfOutputs; i++) {
				inputs[which++] = outs_of_cbrn[i];
			}
		}

        for (int i = -6; i < 7; i++) {
            for (int j = -6; j < 7; j++) {
                inputs[which++] = probe(i, j, scene);
            }
        }
        for (int i = -6; i < 7; i++) {
            for (int j = -6; j < 7; j++) {
                inputs[which++] = probe_enemies(i, j, enemies);
            }
        }    
        inputs[which++] = observation.isMarioOnGround() ? 1 : 0;
        inputs[which++] = observation.mayMarioJump() ? 1 : 0;
        
        /*********start detector_of_holes*******/
        int which2 = 0;
        double[] detector_of_holes_inputs = new double[detector_of_holes_numberOfInputs];
        for (int i = 0; i < 10; i++) {
        	for (int j = -11; j < 11; j++) {
            	detector_of_holes_inputs[which2++] = probe(i, j, scene);
            }
        }
        double[] detector_of_holes_result = detector_of_holes.propagate(detector_of_holes_inputs);
        int hole_is_close = 0;
        if(detector_of_holes_result[0] > 0.75) {
         	if(Mario_pos[0] < 70) { 
         		hole_is_close = 0;
        	}
        	else {
        		hole_is_close = 1;
        		detector_of_holes_wait_counter = 34;
        	}
        }
        if(hole_is_close == 1) {
        	action_in_progress = false; 
        	force_long_jump_forward = true;
        	is_first_action_when_hole_is_detected = true;
        }
        /*********end detector_of_holes*******/
        
        if((action_in_progress_type == 0)||(action_in_progress_type == 1)||(action_in_progress_type == 3))
        	if((observation.isMarioOnGround() == true)/*&&(observation.mayMarioJump() == true)*/) {
        		if(count_of_action_in_progress>3) {action_in_progress = false;}
        	}
       
        /******Select sequence of actions ***********************/
       if(force_long_jump_forward == false) {
        if((action_in_progress == false)&&(observation.isMarioOnGround() == true)/*&&(observation.mayMarioJump() == true))*/) {
        	double[] cbrn_action_in_progress = cbrn.propagate (inputs);
        		boolean agent_is_active = false; int count_tmp=0;
        		for(int r = 0; r < cbrn_action_in_progress.length; r++) {
        			if(cbrn_action_in_progress[r] > 0.65) {count_tmp++;}
        		}
        		if(count_tmp == 1) {agent_is_active = true; count_active_agent++;}
        		else			   {agent_is_active = false; count_inactive_agent++;}
        		if(temporary_disable_cbrn == true) {
        			agent_is_active = false;
        		}
        		//agent_is_active = false;
        		if(agent_is_active == true) {
        			for(int r = 0; r < cbrn_action_in_progress.length; r++) {
            			if(cbrn_action_in_progress[r] > 0.65) {action_in_progress_type = r;}
            		}
        		}	
            	if(agent_is_active == false) { /************ if cyberneurons are inactive *****************/
            		int rvalue = random.nextInt(100);
            		if(rvalue < 30) {
            			action_in_progress_type = 0; 
            		}
            		else if(rvalue >= 30 && rvalue < 40) {
            			action_in_progress_type = 5;
            		}
            		else if(rvalue >= 40 && rvalue < 50) {
            			action_in_progress_type = 1;
            		}
            		else if(rvalue >= 50 && rvalue < 60) {
            			action_in_progress_type = 2;
            		}
            		else if(rvalue >= 60 && rvalue < 70) {
            			action_in_progress_type = 3;
            		}
            		else if(rvalue >= 70 && rvalue < 80) {
            			action_in_progress_type = 4;
            		}
            		else if(rvalue >= 80 && rvalue < 85) {
            			action_in_progress_type = 6;
            		}
            		else if(rvalue >= 85 && rvalue < 90) {
            			action_in_progress_type = 7;
            		}
            		else if(rvalue >= 90 && rvalue < 95) {
            			action_in_progress_type = 8;
            		}
            		else {
            			action_in_progress_type = 9;
            		}
            	}
    			count_of_action_in_progress = 0;
    			action_in_progress = true;
    			temporary_disable_cbrn = false;
    	        this.push_inputs_and_actions_to_buffer(inputs, action_in_progress_type); 
        }
        if(action_in_progress == true) { /************ do actions from selected sequence *****************/
    		switch(progressed_action[action_in_progress_type][count_of_action_in_progress])
    		{
    		case(action_forward): 				{action[0] = false; action[1] = true; action[2] = false; action[3] = false; action[4] = false; break;}
    		case(action_forward_jump):			{action[0] = false; action[1] = true; action[2] = false; action[3] = true; action[4] = false;  break;}
    		case(action_backward): 				{action[0] = true; action[1] = false; action[2] = false; action[3] = false; action[4] = false;  break;}
    		case(action_backward_jump): 		{action[0] = true; action[1] = false; action[2] = false; action[3] = true; action[4] = false;  break;}
    		case(action_forward_with_prone): 	{action[0] = false; action[1] = true; action[2] = true; action[3] = false; action[4] = false;  break;}
    		case(action_forward_with_jump_far): {action[0] = false; action[1] = true; action[2] = false; action[3] = true; action[4] = true;  break;}
    		}
    		count_of_action_in_progress++;
    		if(count_of_action_in_progress >= progressed_action[action_in_progress_type].length) {
    			action_in_progress = false;
    		}
        }
       }
       else { /************ selecting predefined sequence of actions when hole is near *****************/
    	   if(is_first_action_when_hole_is_detected == true) {
    		   action_in_progress_type = 2;
    		   count_of_action_in_progress = 0;
    		   is_first_action_when_hole_is_detected = false;
    		   this.push_inputs_and_actions_to_buffer(inputs, action_in_progress_type); 
    	   }
    	   
       		switch(progressed_action[action_in_progress_type][count_of_action_in_progress])
    		{
    		case(action_forward): 				{action[0] = false; action[1] = true; action[2] = false; action[3] = false; action[4] = false; break;}
    		case(action_forward_jump):			{action[0] = false; action[1] = true; action[2] = false; action[3] = true; action[4] = false;  break;}
    		case(action_backward): 				{action[0] = true; action[1] = false; action[2] = false; action[3] = false; action[4] = false;  break;}
    		case(action_backward_jump): 		{action[0] = true; action[1] = false; action[2] = false; action[3] = true; action[4] = false;  break;}
    		case(action_forward_with_prone): 	{action[0] = false; action[1] = true; action[2] = true; action[3] = false; action[4] = false;  break;}
    		case(action_forward_with_jump_far): {action[0] = false; action[1] = true; action[2] = false; action[3] = true; action[4] = true;  break;}
    		case(action_none_action): 			{action[0] = false; action[1] = false; action[2] = false; action[3] = false; action[4] = false;  break;}
    		}
    		count_of_action_in_progress++;
    		if(action_in_progress_type == 2)
    			if(count_of_action_in_progress >= progressed_action[action_in_progress_type].length) {
    				count_of_action_in_progress = 0; 
    				action_in_progress_type = 0;
    				this.push_inputs_and_actions_to_buffer(inputs, action_in_progress_type);
    			}
    		
    		if(action_in_progress_type == 0)
    			if(count_of_action_in_progress >= progressed_action[action_in_progress_type].length) {
    				action_in_progress = false; 
    				force_long_jump_forward = false;
    				is_first_action_when_hole_is_detected = true;
    			}

       }
        /************ end selecting sequence of action *****************/
        
       
        Mario_pos_X_old = Mario_pos[0];
        Mario_pos_Y_old = Mario_pos[1];
        push_Mario_X_pos_to_buffer(Mario_pos[0]);
 	        /****Calculate mean for forwarding Mario***/
        float forward_mean=0;
        forward_mean = buffer_of_Mario_X[buffer_of_Mario_X.length-1] - buffer_of_Mario_X[0];

        /*********if Mario long stay at one position***********/
        if(forward_mean <= 0)  
            if(forward_mean <= 0)  {
            	if(waiting_counter == 0) {
            		waiting_counter = 12;
            		is_positive = 0; 
                	action_in_progress = false;
                	temporary_disable_cbrn = true;
            	}
            	else {
            		waiting_counter--;
            	}
        }
        /*********end if Mario long stay at one position***********/
        
        /*********if Mario progressed well***********/
        if(forward_mean > 0) {
        	is_positive = 1;
        	//***double LearningRate = forward_mean*0.004;
        	//***if(LearningRate > 1) {LearningRate = 1;}
        	cbrn.ssetLearningRate(LearningRate);
        		if((buffer_is_full_of_Mario_X == true)&&(buffer_is_full == true)) {
          		  for(int t = 0; t < deep_of_buffer-1; t++)	{
          			double[] targeted_outs_of_cbrn = new double [numberOfOutputs];
            		for(int i = 0; i < numberOfOutputs; i++) {
            			if(i == buffer_of_actions[t]) {targeted_outs_of_cbrn[i] = 1;}
            			else							 {targeted_outs_of_cbrn[i] = 0;}
            		}
        			cbrn.propagate(buffer_of_inputs[t]);
            		cbrn.backPropagate(targeted_outs_of_cbrn);
          		  }
       		}
        }
        /*********end if Mario progressed well***********/
        
        /*********if Mario fallen to hole***********/
        if(Mario_pos[1] > 251) {
        	is_positive = 0;
            	/*********start detector_of_holes*******/
    		if(detector_of_holes_buffer_is_full == true) {
        		double [] detector_of_holes_targeted_outs = new double [detector_of_holes_numberOfOutputs];
        		//detector_of_holes.ssetLearningRate(0.1);
        		detector_of_holes.ssetLearningRate(0.15);
        		detector_of_holes_targeted_outs[0] = 1;
        		for (int i = 0; i < 2/*2/*detector_of_holes_buffer_of_inputs.length-1*/; i++) {
            		detector_of_holes.propagate(detector_of_holes_buffer_of_inputs[i]);
            		detector_of_holes.backPropagate(detector_of_holes_targeted_outs);
            	}
    		}
             	/*********end detector_of_holes*******/
        }
        /*********end if Mario fall to hole***********/
        
        /************ if Mario don't fallen to a hole *****************/        
        if((observation.isMarioOnGround() == true)&&/*(detector_of_holes_result[0] > 0.75)&&*/(detector_of_holes_buffer_is_full == true)) {
        		/*********start detector_of_holes*******/
    		
        	double [] detector_of_holes_targeted_outs = new double [detector_of_holes_numberOfOutputs];
    		detector_of_holes.ssetLearningRate(0.001);
    		detector_of_holes_targeted_outs[0] = 0;
    		for (int i = 0; i < 2/*detector_of_holes_buffer_of_inputs.length-1*/; i++) {
        		detector_of_holes.propagate(detector_of_holes_buffer_of_inputs[i]);
        		detector_of_holes.backPropagate(detector_of_holes_targeted_outs);
        	}
             	/*********end detector_of_holes*******/
        }
        this.detector_of_holes_push_inputs_to_buffer(detector_of_holes_inputs);

        /*********if enemies hurt Mario***********/
        if(prev_mario_mode - observation.getMarioMode() > 0) {
        			is_positive = 0;
        			cbrn.ssetLearningRate(LearningRate);
            		if(buffer_is_full_of_Mario_X == true) {
            			double[] targeted_outs_of_cbrn = new double [numberOfOutputs];
                		for(int i = 0; i < numberOfOutputs; i++) {
                			 targeted_outs_of_cbrn[i] = 0;
                		}
            			cbrn.propagate(buffer_of_inputs[buffer_of_inputs.length-2]);
                		cbrn.backPropagate(targeted_outs_of_cbrn);
            		}
        }
        /*********end if enemies hurt Mario***********/

        
        prev_mario_mode = observation.getMarioMode();
        return action;
    }


    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private double probe (int x, int y, byte[][] scene) {
        int realX = x + 11;
        int realY = y + 11;
        return (scene[realX][realY] != 0) ? 1 : 0;
    }

    private double probe_enemies (int x, int y, byte[][] scene) {
        int realX = x + 11;
        int realY = y + 11;
        double result = 0;
        if((scene[realX][realY] != -1)&&(scene[realX][realY] != 1))
                  {result = 1;}
        else 
                  {result = 0;}
        return result;
    }

    
    private void push_Mario_X_pos_to_buffer(float Mario_X_pos) {
    	for (int i = 0; i < buffer_of_Mario_X.length-1; i++) {
    		buffer_of_Mario_X[i] = buffer_of_Mario_X[i+1];
    	}
    	buffer_of_Mario_X[buffer_of_Mario_X.length-1] = Mario_X_pos;
    	num_pushes_to_buffer_of_Mario_X++;
    	if(num_pushes_to_buffer_of_Mario_X >= deep_of_buffer_of_Mario_X) {buffer_is_full_of_Mario_X = true;}
    }
    
    private void push_inputs_and_actions_to_buffer(double[] inputs, int action_in_progress_type) {
    	for (int i = 0; i < buffer_of_inputs.length-1; i++) {
    		for (int j = 0; j < buffer_of_inputs[i].length; j++) {
    			buffer_of_inputs[i][j] = buffer_of_inputs[i+1][j];
    		}
    	}
    	for (int i = 0; i < inputs.length; i++) {
    		buffer_of_inputs[buffer_of_inputs.length-1][i] = inputs[i];
    	}
    	for (int i = 0; i < buffer_of_actions.length-1; i++) {
    			buffer_of_actions[i] = buffer_of_actions[i+1];
    	}
    	buffer_of_actions[buffer_of_actions.length-1] = action_in_progress_type;
    	
    	num_pushes_to_buffer++;
    	if(num_pushes_to_buffer >= deep_of_buffer) {buffer_is_full = true;}
    }
    
    
    private void detector_of_holes_push_inputs_to_buffer(double[] inputs) {
    	for (int i = 0; i < detector_of_holes_buffer_of_inputs.length-1; i++) {
    		for (int j = 0; j < detector_of_holes_buffer_of_inputs[i].length; j++) {
    			detector_of_holes_buffer_of_inputs[i][j] = detector_of_holes_buffer_of_inputs[i+1][j];
    		}
    	}
    	for (int i = 0; i < inputs.length; i++) {
    		detector_of_holes_buffer_of_inputs[detector_of_holes_buffer_of_inputs.length-1][i] = inputs[i];
    	}
    	
    	detector_of_holes_num_pushes_to_buffer++;
    	if(detector_of_holes_num_pushes_to_buffer >= detector_of_holes_deep_of_buffer) {detector_of_holes_buffer_is_full = true;}
    }
   
    
}

