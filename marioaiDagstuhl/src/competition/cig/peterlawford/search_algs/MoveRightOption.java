package competition.cig.peterlawford.search_algs;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import competition.cig.peterlawford.simulator.Frame;
import competition.cig.peterlawford.simulator.TheoreticMario;
import competition.cig.peterlawford.visualizer.DisplayPathInfo;

import ch.idsia.mario.engine.sprites.Mario;

public class MoveRightOption extends Option<MoveRightOption> {
//	final Frame f;

	
//	MoveRightOption parent;
//	MoveRightOption[] children = new MoveRightOption[10];
	final double raw_gScore;
	double calc_dist;
	final double hScore;
//	final int coded_action;
//	int nCycleCnt;
	int frameCycleCnt;

	float height;

	int maximize = -1;	// 1 for minimize

	private double distance_to(Frame f_old) {
		int nScnVal = AStarThreaded.block_eval.getTruePt(f.mario.getX(), f.mario.getY());
		//		if (nScnVal == 1)
		//			return maximize * Float.MAX_VALUE;
		if ((nScnVal == 1) || (nScnVal == 2)) {
			//			System.err.println("$"+nScnVal+"$");
			return maximize * Float.MAX_VALUE;
		}
		if (nScnVal == 3) return
		(f.mario.getX() - f_old.mario.getX())/10;

		if (f.mario.fIsHurt) return maximize * Float.MAX_VALUE;
		if (f.mario.getY() > 256) return maximize * Float.MAX_VALUE;

		// Option 1
		//		return (f.mario.getX() - f_old.mario.getX());
		// Option 2 & 3
		//		float height = TheoreticLevel.getHeight(f.mario.x, f.mario.y);
		//Option 2
		//		return (f.mario.getX() - f_old.mario.getX()) *
		//			((320-height)/320);//+(f.mario.xa - f.mario.xa);		
		// Option 3
		//		return (f.mario.getX() - f_old.mario.getX()) -
		//		(height/100);//+(f.mario.xa - f.mario.xa);
		// Option 4
		return (f.mario.getX() - f_old.mario.getX()) -
		Math.abs(f.mario.getY() - f_old.mario.getY())/10;
	}

	@Override
	public boolean isFailure(PQSearch pq_search,
			BlockEvaluator block_eval) {
		if (nCycleCnt < 16) return false;
		if (AStarThreaded.block_eval.fIsStartpoint) return false;

		float current_x = f.mario.getX();
		float parent_x = parent.f.mario.getX();
		
		if (parent != null) {
			int nScnVal = AStarThreaded.block_eval.getTruePt(current_x, f.mario.getY());
			int nScnParentVal = AStarThreaded.block_eval.getTruePt(
					parent_x, parent.f.mario.getY());
			if ( ((nScnVal == 1) || (nScnVal == 2)) && 
					(nScnParentVal != 1) && (nScnParentVal != 2) )
				return true;
		}
		if (f.mario.fIsHurt) return true;	
		if (f.mario.fBumpsCannonOrPipeOnRight) return true;

		float parent_diff = parent_x - block_eval.nGuessedEndpoint;
		float current_diff = current_x - block_eval.nGuessedEndpoint;
		// Don't just stand there, cross the line!
		if ((block_eval.nGuessedEndpoint != -1) && 
				(parent_diff > -5) && (parent_diff < 0) && (current_diff < 0))
			return true;
		// Don't go backwards when heading toward the finish line!
		if ((block_eval.nGuessedEndpoint != -1) && 
				(parent_diff < 0) && (current_diff < 0) && (current_x < parent_x))
			return true;

		if ((block_eval.nGuessedEndpoint != -1) && (f.mario.xa < 3))
			return true;

		if ((block_eval.nGuessedEndpoint != -1) && 
				(parent_x <= block_eval.nGuessedEndpoint) &&
				(current_x > block_eval.nGuessedEndpoint) ) {
			double delta = current_x-block_eval.nGuessedEndpoint;
			boolean fResult = false;
			synchronized(pq_search) {
				if (delta < pq_search.getBestFinalScore()) {
					fResult = true;
				} else {
					pq_search.setBestFinalScore(delta);
				}
			}
			if (fResult == true) return true;
		}

		return false;
	}


	MoveRightOption(MoveRightOption old, Frame f, int coded_action) {
		super(old, f, coded_action);
		this.raw_gScore = 0;
		this.hScore = 0;
		this.line = null;
	}
	
	
	// This is the version for the standard a-star
	public MoveRightOption(MoveRightOption old, Frame f, int coded_action,
			DisplayPathInfo line) {
		super(old, f, coded_action);
//		this.f = f;
//		this.parent = old;
		old.children[coded_action] = this;
		this.line = line;
		this.nCycleCnt = old.nCycleCnt+1;


		//		if (Math.abs(old.f.mario.y+f.mario.ya-f.mario.y) > 5) {
		//			System.err.println(old.f.mario.x+","+old.f.mario.y+","+
		//					old.f.mario.xa+","+old.f.mario.ya+"\t"+
		//					f.mario.x+","+f.mario.y+","+f.mario.xa+","+f.mario.ya);
		//			throw new java.lang.NullPointerException();
		//		}
//		this.coded_action = coded_action;

		int nOldCnt = old.nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;

		if (nNewCnt > 110) throw new java.lang.NullPointerException();

		// First method
		//		raw_gScore =  (old.raw_gScore*(nOldCnt) + distance_to(old.f)) / (nNewCnt);
		calc_dist = distance_to(old.f);
		// Second method
		//		raw_gScore =  old.raw_gScore + calc_dist;
		// Third method
		double new_dist = 0;
		MoveRightOption opt_t = this;
		for (int i=0; i<32; i++) {
			if (opt_t == null) break;
			new_dist += this.calc_dist;
			opt_t = opt_t.parent;
		}
		raw_gScore = new_dist;

		int nBackCnt = 0;
		/*		Option opt_t = this;
		int nCodedAction = opt_t.coded_action;
		if (nCodedAction != 0)
			while ((opt_t.parent != null) && (opt_t.parent.coded_action == nCodedAction)) {
				opt_t = opt_t.parent;
				nBackCnt++;
			} */
		hScore = - ((f.mario.xa+(120-nOldCnt)*16) / (120-nNewCnt)) -nBackCnt;

		//		raw_gScore = old.raw_gScore+distance_to(old.f);
		//		hScore = AStar2.endpt_x - f.mario.x;
	}

	// This is the version for the inverted a-star
	public MoveRightOption(MoveRightOption old, Frame f, int coded_action, BlockEvaluator block_eval) {
		super(old, f, coded_action);
//		this.f = f;
//		this.parent = old;
		old.children[coded_action] = this;
//		this.coded_action = coded_action;
		this.nCycleCnt = old.nCycleCnt+1;
		line = null;

		boolean[] action = AStarThreaded.coded_keys[coded_action];
		calc_dist = estimate_distance_travelled(block_eval, f.mario, 
				action[Mario.KEY_LEFT], action[Mario.KEY_RIGHT],
				action[Mario.KEY_JUMP], action[Mario.KEY_DOWN],
				action[Mario.KEY_SPEED]);

		int nCycles = 0;
		double new_dist = 0;
				MoveRightOption opt_t = this;
		while ((opt_t.parent != null) && (nCycles < 32)) {
			new_dist += opt_t.calc_dist; nCycles++;
			opt_t = opt_t.parent;
		}
		 		// alternate processing
		//		int nCycles = 0;
/*		{
			Option opt_t = this.parent;
			while ((opt_t.parent != null) && (nCycles < 31)) {
				nCycles++;
				opt_t = opt_t.parent;
			}
			new_dist = calc_dist+(this.parent.f.mario.x - opt_t.f.mario.x);
		} */
		/*		if (new_dist != new_dist2) {
			System.err.println(new_dist+" VS "+new_dist2);
			nCycles = 0;
			while ((opt_t.parent != null) && (nCycles < 32)) {
				new_dist += opt_t.calc_dist; nCycles++;
				opt_t = opt_t.parent;
			}
			throw new java.lang.NullPointerException();
		}
		 */	
		double tScore = new_dist / nCycles;
		raw_gScore = tScore;
		
		//		if (f.mario.y == parent.f.mario.y) {
		//		hScore = f.mario.ya/10;	
		//		} else {
		int nOldCnt = old.nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
		//		hScore = - ((calc_dist+(120-nOldCnt)*16) / (120-nNewCnt));
		hScore = -calc_dist;
		//		}
	}

	public MoveRightOption(Frame f, int nCycleCnt) {
		super(null, f, -1);
//		this.f = f;
		this.nCycleCnt = nCycleCnt;

		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nOldCnt = nNewCnt-1;

//		parent = null;
//		coded_action = -1;
		line = null;

		raw_gScore = 0;
		calc_dist = 0;
		//		hScore = -(16 - (f.mario.xa+(120+1)*16) / (120+1+1)));
		hScore = -((f.mario.xa+(120-nOldCnt*16) / (120-nNewCnt)));
		//		hScore = AStar2.endpt_x;
	}

	final DisplayPathInfo line;


//	public MoveRightOption clone_and_update(int i, List<DisplayPathInfo> lines) {
//		// TODO Auto-generated method stub
//		long true_sim_time = System.currentTimeMillis();
//		Frame f_new = new Frame(f, false);
//		f_new.move(AStarThreaded.coded_keys[i]);
//		true_sim_time = System.currentTimeMillis() - true_sim_time;
//		if (true_sim_time > max_true_sim_time) max_true_sim_time = true_sim_time;
//
//		if (lines != null) {
//			DisplayPathInfo line = new DisplayPathInfo(Color.RED);
//			line.push(new float[]{f.mario.getX(), f.mario.getY()});
//			line.push(new float[]{f_new.mario.getX(), f_new.mario.getY()});
//			//		SlowAgent.lines.add(line);
//			lines.add(line);
//		}
//
//		return new MoveRightOption(this, f_new, i, line);		
//	}

	public double getGScore() {
		// First method
		//		return maximize * raw_gScore;
		// Second method
		//		return maximize * (raw_gScore/nCycleCnt);
		// Third method
		return (maximize * raw_gScore);
	}

	public double getHScore() {
		//		return (AStar2.endpt_x - f.mario.x) + (f.nCycleId*16);
		//		return (AStar2.endpt_x - f.mario.x) / 
		//		return 16;
		return hScore;
	}

//	public double getFScore() {
//		return getGScore() + getHScore();
//	}
//
//	public int reconstruct_path() {		
//		if (parent == null) {
//			return -1;
//		} 
//		if (line != null) {
//			//			System.err.println("CHG COLOR");
//			line.color = Color.WHITE;
//			float[] pt1 = line.path.getFirst();
//			float[] pt2 = line.path.get(1);
//			//			System.err.println(line.path.size()+":"+
//			//					pt1[0]+","+pt1[1]+"\t"+
//			//					pt2[0]+","+pt2[1]
//			///			);
//			//		AStar2.lines.add(line);
//		}
//		int result = parent.reconstruct_path();
//		if (result == -1) result = coded_action;
//		//		result.addFirst(new Integer(coded_action));
//		return result;
//	}
//
//	// TODO: 1.6
//	//	@Override
//	public int compareTo(Option o) {
//		double my_f_score = getFScore();
//		double other_f_score = o.getFScore();
//		// TODO Auto-generated method stub
//		if (my_f_score < other_f_score) return -1;
//		if (my_f_score > other_f_score) return 1;
//		return 0;
//	}
//
//	@Override
//	public boolean equals(Object o) {		
//		MoveRightOption opt = (MoveRightOption)o;
//		if ((opt.nCycleCnt == nCycleCnt) &&
//				(opt.f.mario.getX() == f.mario.getX()) && 
//				(opt.f.mario.getY() == f.mario.getY()) &&
//				(opt.f.mario.xa == f.mario.xa) &&
//				(opt.f.mario.ya == f.mario.ya))
//			return true;
//		return false;
//	}

	public int hashCode() {
		return (int)(1000*(nCycleCnt + f.mario.getX() + f.mario.getY() + f.mario.xa + f.mario.ya));
	}

//	public void dump() {
//		if (parent != null) parent.dump();
//		System.err.print(coded_action);
//	}

	public MoveRightOption clone_and_guess(int i,
			BlockEvaluator block_eval) {
		Frame f_new = new Frame(f, false);
		return new MoveRightOption(this, f_new, i, block_eval);		
	}

	public void update(BlockEvaluator block_eval,
			DisplayPathInfo lines) {
//		if (fHasBeenUpdated) return;
//		fHasBeenUpdated = true;
//		float old_x = f.mario.getX(); float old_y = f.mario.getY();
//
//		long true_sim_time = System.currentTimeMillis();
//		//		Frame f_new = new Frame(f, false);
//		f.move(AStarThreaded.coded_keys[coded_action]);
//		true_sim_time = System.currentTimeMillis() - true_sim_time;
//		if (true_sim_time > max_true_sim_time) max_true_sim_time = true_sim_time;
		super.update(block_eval, lines);
		
		calc_dist = verify_distance_travelled(block_eval, f.mario, parent.f.mario);

//		if (lines != null) {
////			DisplayPathInfo line = new DisplayPathInfo(Color.RED);
////			line.push(new float[]{old_x, old_y});
////			line.push(new float[]{f.mario.getX(), f.mario.getY()});
//			//		SlowAgent.lines.add(line);
////			lines.add(line);
//			
////			lines.set.add(new float[]{old_x, old_y, f.mario.getX(), f.mario.getY()});
//			lines.addToSet(old_x, old_y,
//					f.mario.getX(), f.mario.getY(),
//					(path_info == null)?null:Color.BLUE);
//		}
	}

	boolean fQualifiedForEndLevelBonus = false;

	private double fudge_distance_travelled(BlockEvaluator block_eval,
			double calc_dist, double old_x) {
//		if (block_eval.fGuessedEndpoint != -1)
//			System.out.print(block_eval.fGuessedEndpoint+":"+old_x+" ");
		fQualifiedForEndLevelBonus = 
			isQualifiedForLevelEndBonus(block_eval, calc_dist, old_x);

		if (fQualifiedForEndLevelBonus) {
			double delta = old_x+calc_dist-block_eval.nGuessedEndpoint;
//			System.out.print(delta+" ");
			calc_dist += delta*10;
		}
		return calc_dist;
	}
	private double fudge_distance_travelled2(BlockEvaluator block_eval,
			double calc_dist, double old_x) {
		fQualifiedForEndLevelBonus = 
			isQualifiedForLevelEndBonus(block_eval, calc_dist, old_x);

		if (fQualifiedForEndLevelBonus) {
			double delta = old_x+calc_dist-block_eval.nGuessedEndpoint;
			calc_dist -= 10*(10-delta);
		}
		return calc_dist;
	}
	
	private static boolean isQualifiedForLevelEndBonus(BlockEvaluator block_eval,
			double calc_dist, double old_x) {
		return (block_eval.nGuessedEndpoint != -1) && 
				(old_x <= block_eval.nGuessedEndpoint) &&
				(old_x+calc_dist > block_eval.nGuessedEndpoint);
	}
	

	private double estimate_distance_travelled(
			BlockEvaluator block_eval,
			TheoreticMario mario, boolean l, boolean r,
			boolean j, boolean d, boolean s) {
		float xa = mario.xa;
		xa *= 0.89;
		float sideWaysSpeed = (s) ? 1.2f : 0.6f;
		if (l) xa -= sideWaysSpeed;
		if (r) xa += sideWaysSpeed;
		if ((mario.onGround) && j) xa -= 0.01;
		return fudge_distance_travelled(block_eval, xa, mario.x);
	}

	private double verify_distance_travelled(
			BlockEvaluator block_eval,
			TheoreticMario mario_new,
			TheoreticMario mario_old) {
		return fudge_distance_travelled2(block_eval, mario_new.x - mario_old.x,
				mario_old.x);
	}

	@Override
	public boolean isGoal(float x_start) {
		if (parent == null) {
			if (f.mario.getX() > x_start + 11*16) {
				throw new java.lang.NullPointerException();
			}
			if (nCycleCnt-AStarThreaded.initial_cycle_cnt > 105) {
				throw new java.lang.NullPointerException();					
			}
		}

		if (f.mario.getX() > x_start + 11*16) return true;
		if (nCycleCnt-AStarThreaded.initial_cycle_cnt > 105) return true;
		return false;
	}
	
	
	
	public static class Factory extends Option.Factory {

		@Override
		public Option create(Frame f, int nCycleCnt) {
			return new MoveRightOption(f, nCycleCnt);
		}
		
	}

	
}
