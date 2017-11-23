package competition.cig.peterlawford.search_algs;

import competition.cig.peterlawford.simulator.Frame;

import ch.idsia.mario.engine.sprites.Mario;

public abstract class AbstractSearch {
	public abstract int a_star(float mario_x, float mario_y,
			Frame f0, GoalEvaluator goal_eval_in,
			Option.Factory factory,
			BlockEvaluator block_eval);

	private static final int ENCODED_CHOICEVAL_CNT = 12;
	// 0: --- 1: --L 2: --R 3: -JL 4: -JR
	// 5: S-- 6: S-L 7: S-R 8: SJL 9: SJR
	// 10: -JD 11:D  (SD, SJD?)
	public static SynchronizedCounter[] choice_succ = 
		new SynchronizedCounter[ENCODED_CHOICEVAL_CNT];
	static {
		for (int i=0; i<ENCODED_CHOICEVAL_CNT; i++)
			choice_succ[i] = new SynchronizedCounter();

	}

	public static boolean[][] coded_keys = new boolean[ENCODED_CHOICEVAL_CNT][16];
	static {
		for (int i=0; i<10; i++) {
			int i_t = i%5;
			if (i>=5) coded_keys[i][Mario.KEY_SPEED] = true;
			if (i_t>=3) coded_keys[i][Mario.KEY_JUMP] = true;
			if ((i_t == 1) || (i_t == 3))
				coded_keys[i][Mario.KEY_LEFT] = true;
			if ((i_t == 2) || (i_t == 4))
				coded_keys[i][Mario.KEY_RIGHT] = true;
		}
		coded_keys[10][Mario.KEY_JUMP] = true;
		coded_keys[10][Mario.KEY_DOWN] = true;
		coded_keys[11][Mario.KEY_DOWN] = true;
	}

	public abstract void reset();
}
