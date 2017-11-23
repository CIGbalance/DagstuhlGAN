package competition.cig.peterlawford;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

import competition.cig.peterlawford.search_algs.AStarThreaded;
import competition.cig.peterlawford.search_algs.AbstractSearch;
import competition.cig.peterlawford.search_algs.BlockEvaluator;
import competition.cig.peterlawford.search_algs.MoveRightBumpBricksOption;
import competition.cig.peterlawford.search_algs.MoveToLocationOption;
import competition.cig.peterlawford.search_algs.MoveToTargetOption;
import competition.cig.peterlawford.search_algs.Option;
import competition.cig.peterlawford.simulator.ClonedEnvironment;
import competition.cig.peterlawford.simulator.Frame;
import competition.cig.peterlawford.simulator.TheoreticEnemies;
import competition.cig.peterlawford.simulator.TheoreticLevel;
import competition.cig.peterlawford.simulator.TheoreticMario;
import competition.cig.peterlawford.visualizer.Visualizer;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

public class PeterLawford_SlowAgent implements Agent {

	public static final Logger logger = Logger.getLogger("Global");
	static {
		//		Handler handler = new MemoryHandler();
		Formatter formatter = new Formatter() {
//			@Override
			public String format(LogRecord arg0) {
				return arg0.getMessage();
			}
		};
		Handler console_handler = new ConsoleHandler();
		console_handler.setFormatter(formatter);
		Handler handler = new MemoryHandler(console_handler, 700, Level.SEVERE);

		logger.addHandler(handler);
		logger.setLevel(Level.OFF);
		logger.setFilter(null);
		//		Logger.getLogger("PatternTree2");
		//		Logger.global.addHandler(myHandler);
		//		Logger.global.setLevel(Level.ALL);
		//		Logger.global.setFilter(null);
		logger.setUseParentHandlers(false);
		//		logger;
	}

	public static final boolean DEBUG = false;

	public static final int MAX_PATH_LENGTH = 0;

	private LinkedList<PathInfo> best_path = new LinkedList<PathInfo>();

	public static int mouse_x = -1;
	public static int mouse_y = -1;

	class PathInfo {
		final float nSpeedX;
		final float nSpeedY;
		boolean fOnGround;
		public PathInfo(float nSpeedX, float nSpeedY, boolean fOnGround) {
			super();
			this.nSpeedX = nSpeedX;
			this.nSpeedY = nSpeedY;
			this.fOnGround = fOnGround;
		}

		public String toString() {
			if ((nSpeedX > 0) && (nSpeedY > 0)) return "↗";
			if ((nSpeedX > 0) && (nSpeedY < 0)) return "↘";
			if ((nSpeedX < 0) && (nSpeedY < 0)) return "↙";
			if ((nSpeedX < 0) && (nSpeedY > 0)) return "↖";
			if ((nSpeedX > 0) && (nSpeedY == 0)) return "→";
			if ((nSpeedX < 0) && (nSpeedY == 0)) return "←";
			if ((nSpeedX == 0) && (nSpeedY > 0)) return "↑";
			if ((nSpeedX == 0) && (nSpeedY < 0)) return "↓";
			return "*";
		}
	}

    private boolean[] action;
    private String name;
	public PeterLawford_SlowAgent() {
		name="PeterLawford_SlowAgent";
		reset();
	}
    public Agent.AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }
    public String getName() {        return name;    }
    public void setName(String Name) { this.name = Name;    }

	class Observations {
		final byte[][] all;
		final byte[][] enemies;
		final byte[][] scene;
		public Observations(byte[][] all, byte[][] enemies, byte[][] scene) {
			super();
			this.all = all;
			this.enemies = enemies;
			this.scene = scene;
		}
	}

	//	private byte[][] obsAllPrev = null;
	//	private byte[][] obsAllPrevPrev = null;

	//	private byte[][] obsEnemyPrev = null;
	//	private byte[][] obsEnemyPrevPrev = null;

	private byte[][] obsScenePrev = null;
	//	private byte[][] obsScenePrevPrev = null;
	//	private byte[][] obsScenePrevPrevPrev = null;

	boolean fFlyingPrev = true;
	boolean fFlyingPrevPrev = true;
	//	boolean fFlyingPrevPrevPrev = true;

	int dir = -1;
	int dirPrev = -1;
	int dirPrevPrev = -1;

	int jump_count = 0;


	boolean fEmergencyHoleJump = false;

	int getXDir(int nCode) {
		if ((nCode == 0) || (nCode == 3) || (nCode == 6)) return -1;
		if ((nCode == 2) || (nCode == 5) || (nCode == 8)) return 1;
		return 0;
	}
	int getYDir(int nCode) {
		if ((nCode == 0) || (nCode == 1) || (nCode == 2)) return 1;
		if ((nCode == 6) || (nCode == 7) || (nCode == 8)) return -1;
		return 0;
	}

	private float calcSpeedY(float n1, float n2, float n3) {
		if ((n1 >= 0) && (n2 >= 0) && (n3 >= 0))
			return (n1+n2+n3)/3;
		if ((n1 <= 0) && (n2 <= 0) && (n3 <= 0))
			return (n1+n2+n3)/3;
		if ((n1 >= 0) && (n2 >= 0)) 
			return (n1+n2)/2;
		if ((n1 <= 0) && (n2 <= 0)) 
			return (n1+n2)/2;
		return n1;
	}
	private float calcSpeedX(float n1, float n2, float n3) {
		if ((n1 >= 0) && (n2 >= 0) && (n3 >= 0))
			return (n1+n2+n3)/3;
		if ((n1 <= 0) && (n2 <= 0) && (n3 <= 0))
			return (n1+n2+n3)/3;
		if ((n1 >= 0) && (n2 >= 0)) 
			return (n1+n2)/2;
		if ((n1 <= 0) && (n2 <= 0)) 
			return (n1+n2)/2;
		return n1;
	}

	public static BlockEvaluator block_eval = new BlockEvaluator();
	public static Visualizer viz = new Visualizer();

	AbstractSearch a_star = new AStarThreaded(block_eval);

	float old_mario_x = -1;
	float old_mario_y = -1;


	competition.cig.peterlawford.search_algs.Option.Factory move_right = 
		new MoveRightBumpBricksOption.Factory();
	MoveToLocationOption.Factory move_to_mouse = 
		new MoveToLocationOption.Factory();
	MoveToLocationOption.Factory get_fireflower = 
		new MoveToLocationOption.Factory();
	Option.Factory get_mushroom = 
		new MoveToTargetOption.Factory();

//	@Override
	public boolean[] getAction(Environment env) {

		logger.info("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv\n");
		//System.out.println(mouse_x+","+mouse_y);

//		MarioComponent env_orig = (MarioComponent)env;
		env = new ClonedEnvironment(env);

		float mario_x = env.getMarioFloatPos()[0];
		float mario_y = env.getMarioFloatPos()[1];

		if (f1 != null) {

			if (((mario_x != f1.mario.getX()) ||
					(mario_y != f1.mario.getY()))) {
				if ((mario_x == old_mario_x) && (mario_y == old_mario_y)) {
					f1.decrementTick();
					f1.mario.fPauseWorld = true;
					f1.mario.fNextPauseWorld = true;
					f1.mario.x = mario_x; f1.mario.y = mario_y;
//					System.out.println(" === WIN - PAUSE WORLD ===");
					PeterLawford_SlowAgent.logger.info(" === WIN - PAUSE WORLD ===");
					f1.mario.status = TheoreticMario.STATUS_WIN;
				} else {
					if (!f1.mario.fixup(mario_x, mario_y)) {

						//					System.out.println("guessed "+f1.mario.getX()+", "+f1.mario.getY()+" got "+
						//							mario_x+","+mario_y);
						logger.severe("guessed "+f1.mario.getX()+", "+f1.mario.getY()+" got "+
								mario_x+","+mario_y);
//						throw new java.lang.NullPointerException();
					}
				}
			}
			if (f1.mario.fire != (env.getMarioMode() == 2)) {
//				System.err.println("I thought Mario could(nt) shoot!");
				//				if (!(!f1.mario.fire && (env.getMarioMode() == 2))) {
//				viz.ansiViz(env, block_eval);
				logger.severe("I thought Mario could(nt) shoot!");
//				throw new java.lang.NullPointerException();
				//				}
			}
			if (f1.mario.large != (env.getMarioMode() != 0)) {
//				System.err.println("I thought Mario was(nt) big!");
			logger.severe("I thought Mario was(nt) big!");
//				throw new java.lang.NullPointerException();
			}
			if ( (f1.mario.carried != null) != env.isMarioCarrying()) {
				//				System.err.println("I thought Mario was(n't) carrying!");
				logger.severe("I thought Mario was(n't) carrying!");
//				throw new java.lang.NullPointerException();				
			}
/*			if (f1.getTick() != env_orig.levelScene.tick) {
				logger.severe("I am out of sync!"+
						f1.getTick()+" VS "+env_orig.levelScene.tick+
						", is world paused?"+env_orig.levelScene.paused+"\n");
				//				throw new java.lang.NullPointerException();				
			} */
		}
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		 */
		if ((mario_x == 32) && (mario_y == 0)) {
			reset();
		}		

		old_mario_x = mario_x;
		old_mario_y = mario_y;


		//		if ((f1 != null) && block_eval.fIsEndpoint) f1.mario.status = Mario.STATUS_WIN;

		//		byte[][] obs = env.getCompleteObservation();
//		byte[][] obsScene = env.getLevelSceneObservation();

		block_eval.evaluate(env, viz);

		if (viz != null) {
			//			viz.ansiViz(env, block_eval);
			viz.update(env);
		}

		//		dirPrevPrev = dirPrev;
		//		dirPrev = dir;
		//		if (obsScenePrev != null)
		//			dir = deltaScene2(obsScenePrev, obsScene);
		//		int nX1 = getXDir(dir);
		//		int nY1 = getYDir(dir);
		//		int nX2 = getXDir(dirPrev);
		//		int nY2 = getYDir(dirPrev);
		//		int nX3 = getXDir(dirPrevPrev);
		//		int nY3 = getYDir(dirPrevPrev);

		//		float nSpeedY = calcSpeedY(nY1,nY2,nY3);
		//		float nSpeedX = calcSpeedX(nX1,nX2,nX3);
		//		boolean fFalling = nSpeedY < 0;

		//		fFlyingPrevPrev = fFlyingPrev;
		//		fFlyingPrev = env.isMarioOnGround();


		if (env.isMarioOnGround()) {
			jump_count = 0;
			fEmergencyHoleJump = false;
		}

//		obsScenePrev = obsScene;




		TheoreticMario mario_t = (f1 != null) ? f1.mario : 
			TheoreticMario.getInitialMarioLevelStart(env, level);

		TheoreticLevel.updateImage(env);

		TheoreticEnemies enemies_t = (f1 != null) ? f1.enemies : new TheoreticEnemies(level);
		enemies_t.processEnemyInfo(env, mario_t);

		Frame f0;
		if (f1 == null) {
			f0 = new Frame(this, mario_t, enemies_t, level, 0, 1);
		} else {
			f0 = new Frame(
					//				((MarioComponent)env).mario.world,
					this,
					new TheoreticMario(level, 
							mario_x, mario_y,
							env.isMarioOnGround(), prevWasOnGround,
							env.mayMarioJump(),
							(env.getMarioMode() != 0),
							(env.getMarioMode() == 2),
							f1.mario, enemies_t),
							enemies_t, level,
							f1.fireballsOnScreen, f1.getTick());
		}

		move_to_mouse.setPoint(new Point2D.Float(mario_x+mouse_x-160, mouse_y));

		Option.Factory next_action = move_right;
//		if (env.getMarioMode() == 1) {
//			Point2D.Float flower = block_eval.getFireflower(
//					env.getEnemiesObservationZ(0));
//			if (flower != null) {
////				viz.ansiViz(env, block_eval);
////				System.out.println("Found flower at "+flower.x+","+flower.y);
//				//				throw new java.lang.NullPointerException();
//				next_action = get_fireflower;
//				get_fireflower.setPoint(flower);
//			}
//		}
//		if (env.getMarioMode() == 0) {
//			if (enemies_t.isMushroomPresent()) {
//				next_action = get_mushroom;
//			}
//		}

		int coded_action = 0;
		if ((f1 == null) || (f1.mario.status == TheoreticMario.STATUS_RUNNING)) {
			coded_action = a_star.a_star(env.getMarioFloatPos()[0],
					env.getMarioFloatPos()[1], f0, null, next_action, block_eval);
		}
//		System.out.print(Integer.toHexString(coded_action));
		action = AbstractSearch.coded_keys[coded_action];

		//				action = actor0.suggestAction(action, env, nSpeedX, nSpeedY);
		//				projectAction(action, env, nSpeedX, nSpeedY);



		f1 = new Frame(f0, PeterLawford_SlowAgent.DEBUG, true);
		f1.move(action);

		prevWasOnGround = env.isMarioOnGround();

		logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");

		return action;
	}
	Frame f1 = null;
	public final TheoreticLevel level = new TheoreticLevel();
	boolean prevWasOnGround = false;


	//	DisplayPathInfo guess;

	//	static final int CLEAR = 0;
	//	static final int COIN = 34;


	private int deltaScene2(byte[][] oldSc, byte[][] newSc) {
		boolean fMaxOneResult = true;
		boolean fMinOneResult = false;

		boolean[][] guess = deltaScene1(oldSc, newSc);
		int nResult = -1;

		for (int i=0; i<9; i++) {
			boolean t = guess[i/3][i%3];
			fMaxOneResult = fMaxOneResult && !(fMinOneResult && t);
			fMinOneResult = fMinOneResult || t;
			if (t) {
				nResult = i;
			}
		}
		if (!fMaxOneResult) return -1;
		return nResult;
	}

	private boolean[][] deltaScene1(byte[][] oldSc, byte[][] newSc) {
		boolean[][] possBits = new boolean[3][3];
		for (int k=-1; k<2; k++) {
			for (int l=-1; l<2; l++) {
				possBits[k+1][l+1] = true;
			}
		}

		for (int i=0; i<22; i++)
			for (int j=0; j<22; j++) {
				for (int k=-1; k<2; k++) {
					for (int l=-1; l<2; l++) {
						if ((i+k < 0) || (i+k > 21) ||
								(j+l < 0) || (j+l > 21))
							continue;
						if ( (oldSc[i][j] == 1) || (newSc[i+k][j+l] == 1))
							continue;	// ignore mario
						if (newSc[i+k][j+l] != oldSc[i][j]) {
							possBits[1-k][1-l] = false;
						}
					}
				}
			}
		return possBits;
	}


//	@Override
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
		action[TheoreticMario.KEY_RIGHT] = true;
		action[TheoreticMario.KEY_SPEED] = true;

		old_mario_x = -1;
		old_mario_y = -1;		
		f1 = null;
		prevWasOnGround = false;
		obsScenePrev = null;

		level.reset();
		a_star.reset();
		block_eval.reset();
	}
}
