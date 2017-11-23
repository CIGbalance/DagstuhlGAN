package competition.cig.peterlawford.search_algs;

import java.awt.Point;
import java.awt.geom.Point2D;

import competition.cig.peterlawford.simulator.Frame;
import competition.cig.peterlawford.simulator.TheoreticMario;
import competition.cig.peterlawford.visualizer.DisplayPathInfo;

import ch.idsia.mario.engine.sprites.Mario;

public class MoveToLocationOption extends Option<MoveToLocationOption> {

	 double raw_gScore;
	double calc_dist;
//	final double hScore;

	int frameCycleCnt;

	float height;

	int maximize = -1;	// 1 for minimize

	private final Point2D.Double optimal_dir;

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

	public boolean isFailure(PQSearch pq_search,
			BlockEvaluator block_eval) {
		if (nCycleCnt < 16) return false;
		if (AStarThreaded.block_eval.fIsStartpoint) return false;

		float current_x = f.mario.getX();
		float parent_x = parent.f.mario.getX();
		
//		if ((current_x == parent_x) && 
//				(f.mario.getY() == parent.f.mario.getY()))
//			return true;
		
		if (parent != null) {
			int nScnVal = AStarThreaded.block_eval.getTruePt(current_x, f.mario.getY());
			int nScnParentVal = AStarThreaded.block_eval.getTruePt(
					parent_x, parent.f.mario.getY());
			if ( ((nScnVal == 1) || (nScnVal == 2)) && 
					(nScnParentVal != 1) && (nScnParentVal != 2) )
				return true;
		}
		if (f.mario.fIsHurt) return true;	

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

	@Override
	public boolean isGoal(float nXStart) {
		double dX = pt.x - f.mario.x; double dY = pt.y - f.mario.y;
		return (Math.sqrt(dX*dX+dY*dY) < 4);
	}
	
//	// This is the version for the standard a-star
//	public MoveToLocationOption(MoveToLocationOption old, Frame f, int coded_action,
//			DisplayPathInfo line) {
//		super(old, f, coded_action);
//		pt = old.pt;
//
//		old.children[coded_action] = this;
//		this.line = line;
//		this.nCycleCnt = old.nCycleCnt+1;
//
//
//		//		if (Math.abs(old.f.mario.y+f.mario.ya-f.mario.y) > 5) {
//		//			System.err.println(old.f.mario.x+","+old.f.mario.y+","+
//		//					old.f.mario.xa+","+old.f.mario.ya+"\t"+
//		//					f.mario.x+","+f.mario.y+","+f.mario.xa+","+f.mario.ya);
//		//			throw new java.lang.NullPointerException();
//		//		}
////		this.coded_action = coded_action;
//
//		int nOldCnt = old.nCycleCnt - AStarThreaded.initial_cycle_cnt;
//		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
//
//		if (nNewCnt > 110) throw new java.lang.NullPointerException();
//
//		// First method
//		//		raw_gScore =  (old.raw_gScore*(nOldCnt) + distance_to(old.f)) / (nNewCnt);
//		calc_dist = distance_to(old.f);
//		// Second method
//		//		raw_gScore =  old.raw_gScore + calc_dist;
//		// Third method
//		double new_dist = 0;
//		MoveToLocationOption opt_t = this;
//		for (int i=0; i<32; i++) {
//			if (opt_t == null) break;
//			new_dist += this.calc_dist;
//			opt_t = opt_t.parent;
//		}
//		raw_gScore = new_dist;
//
//		int nBackCnt = 0;
//		/*		Option opt_t = this;
//		int nCodedAction = opt_t.coded_action;
//		if (nCodedAction != 0)
//			while ((opt_t.parent != null) && (opt_t.parent.coded_action == nCodedAction)) {
//				opt_t = opt_t.parent;
//				nBackCnt++;
//			} */
//		hScore = - ((f.mario.xa+(120-nOldCnt)*16) / (120-nNewCnt)) -nBackCnt;
//
//		//		raw_gScore = old.raw_gScore+distance_to(old.f);
//		//		hScore = AStar2.endpt_x - f.mario.x;
//	}

	// This is the version for the inverted a-star
	public MoveToLocationOption(MoveToLocationOption old, Frame f, int coded_action, BlockEvaluator block_eval) {
		super(old, f, coded_action);
		pt = old.pt;

		old.children[coded_action] = this;

		this.nCycleCnt = old.nCycleCnt+1;
		line = null;

		optimal_dir = normalize(f.mario.x-pt.x, f.mario.y-pt.y);
		
		boolean[] action = AStarThreaded.coded_keys[coded_action];
		calc_dist = estimate_distance_travelled(block_eval, f.mario, 
				action[Mario.KEY_LEFT], action[Mario.KEY_RIGHT],
				action[Mario.KEY_JUMP], action[Mario.KEY_DOWN],
				action[Mario.KEY_SPEED]);

//		int nCycles = 0;
//		double new_dist = 0;
//				MoveToLocationOption opt_t = this;
//		while ((opt_t.parent != null) && (nCycles < 32)) {
//			new_dist += opt_t.calc_dist; nCycles++;
//			opt_t = opt_t.parent;
//		}
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
//		double tScore = new_dist / nCycles;
//		raw_gScore = tScore;
//		raw_gScore = 0;
		raw_gScore = old.raw_gScore + calc_dist;
		
		//		if (f.mario.y == parent.f.mario.y) {
		//		hScore = f.mario.ya/10;	
		//		} else {
		int nOldCnt = old.nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
		//		hScore = - ((calc_dist+(120-nOldCnt)*16) / (120-nNewCnt));
//		hScore = -calc_dist;
		//		}
	}

	private final Point2D.Float pt;
	
	public MoveToLocationOption(Frame f, int nCycleCnt, Point2D.Float pt) {
		super(null, f, -1);
		this.pt = pt;

		optimal_dir = normalize(f.mario.x-pt.x, f.mario.y-pt.y);

		this.nCycleCnt = nCycleCnt;

		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nOldCnt = nNewCnt-1;

		line = null;

		raw_gScore = 0;
		calc_dist = 0;
		//		hScore = -(16 - (f.mario.xa+(120+1)*16) / (120+1+1)));
//		hScore = -((f.mario.xa+(120-nOldCnt*16) / (120-nNewCnt)));
		//		hScore = AStar2.endpt_x;
	}

	final DisplayPathInfo line;

	static long max_true_sim_time = 0;

//	public MoveToLocationOption clone_and_update(int i, List<DisplayPathInfo> lines) {
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
//		return new MoveToLocationOption(this, f_new, i, line);		
//	}

	public double getGScore() {
		return raw_gScore;
//		return 0;
//		float dX = pt.x - f.mario.x; float dY = pt.y - f.mario.y;
//		return dX*dX+dY*dY;
	}

	
	private static final double MAX_TRAVELABLE_DISTANCE =
		Math.sqrt(320*320+240*240);
	public double getHScore() {
		//		return (AStar2.endpt_x - f.mario.x) + (f.nCycleId*16);
		//		return (AStar2.endpt_x - f.mario.x) / 
		//		return 16;
//		return hScore;
//		System.out.println(f.mario.x+","+f.mario.y+"=>"+pt.x+","+pt.y+" ");
		double dX = pt.x - f.mario.x; double dY = pt.y - f.mario.y;
//		return 1/(MAX_TRAVELABLE_DISTANCE - Math.sqrt(dX*dX+dY*dY))
		return  Math.sqrt(dX*dX+dY*dY);
	}

	public int hashCode() {
		return (int)(1000*(nCycleCnt + f.mario.getX() + f.mario.getY() + f.mario.xa + f.mario.ya));
	}

//	public void dump() {
//		if (parent != null) parent.dump();
//		System.err.print(coded_action);
//	}

	public MoveToLocationOption clone_and_guess(int i,
			BlockEvaluator block_eval) {
		Frame f_new = new Frame(f, false);
		return new MoveToLocationOption(this, f_new, i, block_eval);		
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
		raw_gScore = parent.raw_gScore + calc_dist;
		
//		if (lines != null) {
////			DisplayPathInfo line = new DisplayPathInfo(Color.RED);
////			line.push(new float[]{old_x, old_y});
////			line.push(new float[]{f.mario.getX(), f.mario.getY()});
//			//		SlowAgent.lines.add(line);
////			lines.add(line);
//			
////			lines.set.add(new float[]{old_x, old_y, f.mario.getX(), f.mario.getY()});
//			lines.addToSet(old_x, old_y,
//					f.mario.getX(), f.mario.getY(), null);
//		}
	}

//	private double fudge_distance_travelled(BlockEvaluator block_eval,
//			double calc_dist, double old_x) {
////		if (block_eval.fGuessedEndpoint != -1)
////			System.out.print(block_eval.fGuessedEndpoint+":"+old_x+" ");
//		if ((block_eval.fGuessedEndpoint != -1) && 
//				(old_x <= block_eval.fGuessedEndpoint) &&
//				(old_x+calc_dist > block_eval.fGuessedEndpoint) ) {
//			double delta = old_x+calc_dist-block_eval.fGuessedEndpoint;
////			System.out.print(delta+" ");
//			calc_dist += delta*10;
//		}
//		return calc_dist;
//	}
//	private double fudge_distance_travelled2(BlockEvaluator block_eval,
//			double calc_dist, double old_x) {
//		if ((block_eval.fGuessedEndpoint != -1) && 
//				(old_x <= block_eval.fGuessedEndpoint) &&
//				(old_x+calc_dist > block_eval.fGuessedEndpoint) ) {
//			double delta = old_x+calc_dist-block_eval.fGuessedEndpoint;
//			calc_dist -= 10*(10-delta);
//		}
//		return calc_dist;
//	}
	
	private Point2D.Double normalize(float x, float y) {
		double len = Math.sqrt(x*x+y*y);
		return new Point2D.Double(x/len, y/len);
	}
	
	private double estimate_distance_travelled(
			BlockEvaluator block_eval,
			TheoreticMario mario, boolean l, boolean r,
			boolean j, boolean d, boolean s) {
		float xa = mario.xa;
		float ya = (j && mario.onGround) ? -1.9f : mario.ya;
		xa *= 0.89;
		float sideWaysSpeed = (s) ? 1.2f : 0.6f;
		if (l) xa -= sideWaysSpeed;
		if (r) xa += sideWaysSpeed;
		if ((mario.onGround) && j) xa -= 0.01;
		
//		return 1/(0.000001+Math.sqrt(xa*xa + ya*ya));
		return 20-Math.sqrt(xa*xa + ya*ya);
//		return fudge_distance_travelled(block_eval, xa, mario.x);
	}

	private double verify_distance_travelled(
			BlockEvaluator block_eval,
			TheoreticMario mario_new,
			TheoreticMario mario_old) {
//		return fudge_distance_travelled2(block_eval, mario_new.x - mario_old.x,
//				mario_old.x);
		float dX = mario_new.x - mario_old.x;
		float dY = mario_new.y - mario_old.y;
//		return 1/(0.000001+Math.sqrt(dX*dX + dY*dY));
		return 20-Math.sqrt(dX*dX + dY*dY);
	}

	public static class Factory extends Option.Factory {
		Point2D.Float pt = null;
		
		public void setPoint(Point2D.Float pt) { this.pt = pt; }
		
		@Override
		public Option create(Frame f, int nCycleCnt) {
			return new MoveToLocationOption(f, nCycleCnt, pt);
		}
		
	}
	
}
