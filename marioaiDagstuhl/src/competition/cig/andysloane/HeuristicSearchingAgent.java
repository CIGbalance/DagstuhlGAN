package competition.cig.andysloane;

import java.io.IOException;
import java.util.Comparator;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public abstract class HeuristicSearchingAgent implements Agent
{
	public static final Comparator<MarioState> msComparator = new MarioStateComparator();

	protected final boolean[] action = new boolean[Environment.numberOfButtons];
	protected int[] marioPosition = null;
	protected Sensors sensors = new Sensors();
	
	public static final boolean verbose1 = false;
	public static final boolean verbose2 = false;
	protected static final boolean drawPath = true;
	// enable to single-step with the enter key on stdin
	protected static final boolean stdinSingleStep = false;

	MarioState ms = null, ms_prev = null;
	WorldState ws = null;
	float pred_x, pred_y;
	boolean won = false;
    private String name;

	public HeuristicSearchingAgent(String name) {
		setName(name);
		reset();
	}

    public Agent.AGENT_TYPE getType() { return Agent.AGENT_TYPE.AI; }
	public String getName() { return name; }
	public void setName(String Name) { this.name = Name; }


//	@Override
	public void reset() {
		ms = null;
		marioPosition = null;
		won = false;
	}

	private static final float lookaheadDist = 9*16;
	protected final float cost(MarioState s, MarioState initial) {
		float damage = Tunables.HurtCost * s.hurt;
		if(s.dead)
			return Tunables.DeadCost;
		int MarioX = (int)s.x/16 - s.ws.MapX;
		int MarioY = (int)s.y/16 - s.ws.MapY;
		int goal = 21;
		float fgoalX = (goal+s.ws.MapX)*16+8;
		// we're there!
		if(s.x > fgoalX) {
			// linearly interpolate at the max run speed to estimate how much
			// farther past the goal we went
			return (fgoalX - s.x)/9.71f;
		}
		float xsteps = MarioMath.stepsToRun(fgoalX - s.x, s.xa);
		if(MarioX < 0 || MarioX >= 22) // mario ran off the screen; we're done
			return xsteps + damage;
 
		// We need to determine how many steps, at a minimum, it will take to
		// jump over whatever obstacles are in front of us.
		//
		// We also need to know whether we're going to fall into an abyss with
		// nothing we can do about it, as soon as it is possible to know this,
		// so as to terminate the search here.
		//
		// So: if Mario is on the ground, we just figure in however long it
		// takes to jump above obstacles of various heights; otherwise, we need
		// to figure out the best place he could land and whether he needs to
		// make a further jump from there
		//
		// First, what is the biggest obstacle in front of us?
 
		float ysteps = 0;
		int ledgeY = 22;
		int ledgeX = MarioX;
		for(int i=MarioX;i<=goal;i++) {
			if(s.ws.heightmap[i] < ledgeY) {
				ledgeY = s.ws.heightmap[i];
				ledgeX = i;
			}
		}

		// fLedgeX,Y is mario's leftmost location atop highest ledge in front of us
		float fLedgeY = (ledgeY+s.ws.MapY)*16 - 1;
		float fLedgeX = (ledgeX+s.ws.MapX)*16 - 4;

		if(s.onGround) {
			if(s.y <= fLedgeY)
				return damage + xsteps;
			else {
				float sj = MarioMath.stepsToJump(s.y - fLedgeY);
				float sr1 = MarioMath.stepsToRun(fLedgeX - s.x, s.xa);
				float sr2 = MarioMath.stepsToRun(fgoalX - fLedgeX, s.xa);
				// (this assumes we can reach the ledge from our current location..)
				return damage + Math.max(sj,sr1) + sr2;
			}
		} else {
			// we're in the air.  okay, how far left and right can we possibly land?
			MarioState l = s.clone(), r = s.clone();
			// save x and y at apogee
			float apogeey = l.y;
			int apogeesteps=0;
			int stepsL=0, stepsR=0;
			while(!l.dead && !l.onGround) {
				l.move(MarioState.ACT_SPEED | MarioState.ACT_LEFT | MarioState.ACT_JUMP);
				stepsL++;
			}
			while(!r.dead && !r.onGround) {
				r.move(MarioState.ACT_SPEED | MarioState.ACT_RIGHT | MarioState.ACT_JUMP);
				stepsR++;
				if(r.y <= apogeey) {
					// if we have an unobstructed path to the right then this
					// should happen exactly once
					apogeey = r.y;
					apogeesteps = stepsR;
				}
			}
			if(r.dead && l.dead) // we're dead no matter what!  forget it!
				return Tunables.DeadCost;

			// okay, now, can we surmount the highest obstacle in our current jump?
			if(MarioMath.canReachLedge(s.x, s.xa, apogeesteps, apogeey, fLedgeX, fLedgeY)) {
				// if so, then we're golden; just jump and run right.
				return damage + xsteps;
			} else { // if not, we have to land, then jump over it.
				// search the heightmap between the left and right landings for
				// the highest perch we can land on
				int perchX=0, perchY=22;
				for(float i=l.x;i<=r.x;i+=16) {
					int idx = (int)i/16 - s.ws.MapX;
					if(idx<0 || idx>=22)
						continue;
					if(s.ws.heightmap[idx] < perchY && s.ws.heightmap[idx] >= MarioY) { // look for leftmost edge
						perchX = idx;
						perchY = s.ws.heightmap[idx];
					}
				}
				// leftmost position to land
				float landy = (perchY+s.ws.MapY)*16 - 1;
				float landx = (perchX+s.ws.MapX)*16 - 4;
				// are we already above the landing?
				//if(s.y <= landy) { this should always be true
				// if so, then figure out how much time it takes to land,
				// jump to the next ledge, and then run right
				float sf = 0; //MarioMath.stepsToFall(landy - s.y, s.ya);
				float sj = MarioMath.stepsToJump(fLedgeY - landy);
				float sr1 = MarioMath.stepsToRun(fLedgeX - s.x, s.xa);
				float sr2 = MarioMath.stepsToRun(fgoalX - fLedgeX, s.xa);
				return damage + Math.max(sf+sj, sr1) + sr2;
			}
		}
 
		// unreachable
	}
	
	static final public boolean useless_action(int a, MarioState s) {
		// speed without left or right: useless
		if((a&MarioState.ACT_SPEED)>0 && 
		   !((a&MarioState.ACT_LEFT)>0 || (a&MarioState.ACT_RIGHT)>0))
			return true;
		// left and right at the same time: useless
		if((a&MarioState.ACT_LEFT)>0 && (a&MarioState.ACT_RIGHT)>0) return true;
		// jumping when the jump button doesn't do anything: useless
		if((a&MarioState.ACT_JUMP)>0) {
			if(s.jumpTime == 0 && !s.mayJump) return true;
			if(s.jumpTime <= 0 && !s.onGround && !s.sliding) return true;
		}
		// standing next to something that we're going to collide with by
		// moving right: useless (except for walljumps actually)
		int ix = (int) s.x;
		if((a&MarioState.ACT_RIGHT)>0 && s.xa == 0 && (s.x - ix) == 0 && (ix&15) == (16-5)) {
			// ok, we are exactly at a brick boundary.  is there something there?
			if(s.ws.isBlocking((ix+5)/16, (int)(s.y/16), 1,s.ya) ||
			   s.ws.isBlocking((ix+5)/16, (int)(s.y/16)-1, 1,s.ya) ||
			   s.ws.isBlocking((ix+5)/16, (int)(s.y/16)-2, 1,s.ya))
				return true;
		}
		return false;
	}

	protected abstract int searchForAction(MarioState initialState, WorldState ws);
	
	public static MarioState marioMin(MarioState a, MarioState b) {
		if(a == null) return b;
		if(b == null) return a;
		// compare heuristic cost only
		if((a.cost - a.g) <= (b.cost - b.g)) return a;
		return b;
	}

//	@Override
	public boolean[] getAction(Environment observation)
	{
		if(won) // we won!  we can't do anything!
			return action;

		sensors.updateReadings(observation);
		marioPosition = sensors.getMarioPosition();
		float[] mpos = observation.getMarioFloatPos();
		if(ms == null) {
			// assume one frame of falling before we get an observation :(
			ms = new MarioState(mpos[0], mpos[1], 0.0f, 3.0f);
			ws = new WorldState(sensors.levelScene, ms, observation.getEnemiesFloatPos());
		} else {
			//System.out.println(String.format("mario x,y=(%5.1f,%5.1f)", mpos[0], mpos[1]));
			if(mpos[0] != pred_x || mpos[1] != pred_y) {
				if (!epsilon(mpos[0],pred_x)||!epsilon(mpos[1], pred_y)) {
					// generally this shouldn't happen, unless we mispredict
					// something.  currently if we stomp an enemy then we don't
					// predict that and get confused.
	
					// but it will happen when we win, cuz we have no idea we won
					// and it won't let us move.  well, let's guess whether we won:
					if(mpos[0] > 4000 && mpos[0] == ms_prev.x && mpos[1] == ms_prev.y) {
						//System.out.println("ack, can't move.  assuming we just won");
						won = true;
						return action;
					}
					if(verbose1) {
						float diff = Math.abs(ms.x-mpos[0]) + Math.abs(ms.y-mpos[1]);
						System.out.printf("mario state mismatch (%f,%f) -> (%f,%f); attempting resync\n",
								ms.x,ms.y, mpos[0], mpos[1]);
						//if(!stdinSingleStep && diff > 0.01f) try {
						//	System.in.read();
						//} catch(IOException e) {}
					}
				}
			}
			resync(observation, !epsilon(mpos[0],pred_x), !epsilon(mpos[1],pred_y));
			ms.ws.sync(ws, sensors.levelScene, ms, observation.getEnemiesFloatPos());
			ws = ms.ws;
		}
		// resync these things all the time
		ms.mayJump = observation.mayMarioJump();
		ms.onGround = observation.isMarioOnGround();
		ms.big = observation.getMarioMode() > 0;

		if(verbose2) {
			float[] e = observation.getEnemiesFloatPos();
			for(int i=0;i<e.length;i+=3) {
				System.out.printf(" e %d %f,%f\n", (int)e[i], e[i+1], e[i+2]);
			}
		}

		int next_action = searchForAction(ms, ws);
		ms_prev = ms;
		ms = ms.next(next_action, ws);
		pred_x = ms.x;
		pred_y = ms.y;
		if(verbose2) {
			System.out.printf("MarioState (%f,%f,%f,%f) -> action %d -> (%f,%f,%f,%f)\n",
				ms_prev.x, ms_prev.y, ms_prev.xa, ms_prev.ya,
				next_action,
				ms.x, ms.y, ms.xa, ms.ya);
		}

		action[Mario.KEY_SPEED] = (next_action&MarioState.ACT_SPEED)!=0;
		action[Mario.KEY_RIGHT] = (next_action&MarioState.ACT_RIGHT)!=0;
		action[Mario.KEY_LEFT] = (next_action&MarioState.ACT_LEFT)!=0;
		action[Mario.KEY_JUMP] = (next_action&MarioState.ACT_JUMP)!=0;

		if(stdinSingleStep) {
			try {
				System.in.read();
			} catch(IOException e) {}
		}

		return action;
	}
	
	private static boolean epsilon(float a, float b)
	{
		return Math.abs(a-b) < 0.01;
	}

	private void resync(Environment observation, boolean x, boolean y) {
		float[] mpos = observation.getMarioFloatPos();
		ms.x = mpos[0];
		ms.y = mpos[1];

		// lastmove_s was guessed wrong, or we wouldn't be out of sync.  we can
		// directly get the new xa and ya, as long as no collisions occurred.
		// if there *was* a collision and xa,ya are wrong, they probably will
		// be corrected by each call next()
		if(ms_prev != null) {
			// we may have stepped on a turtle or something though, in which
			// case the following just fucks us up
			if (x)
				ms.xa = (ms.x - ms_prev.x) * 0.89f;
			if (y)
				ms.ya = (ms.y - ms_prev.y) * 0.85f;
		}
	}
}
