package competition.cig.peterlawford.search_algs;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import competition.cig.peterlawford.simulator.Frame;
import competition.cig.peterlawford.simulator.TheoreticMario;
import competition.cig.peterlawford.visualizer.DisplayPathInfo;

import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.sprites.Mario;


class ProcessingThread extends Thread {

	private static final int BEST_N = 3;

	//	LinkedList<DisplayPathInfo> lines = null;
	DisplayPathInfo lines = null;

	final PQSearch open_set;
	//	final GoalEvaluator goal_eval;
	final BlockEvaluator block_eval;
	final AStarThreaded.BlockingGrid blocking_grid;

	LinkedList<Option> results = new LinkedList<Option>();

	final Thread parent_thread;


	public ProcessingThread(PQSearch set_in,
			AStarThreaded.BlockingGrid blocking_grid,
			//			GoalEvaluator goal_eval,
			Thread parent_thread,
			BlockEvaluator block_eval) {
		open_set = set_in;
		this.blocking_grid = blocking_grid;
		//		this.goal_eval = goal_eval;
		this.parent_thread = parent_thread;
		this.block_eval = block_eval;

		if (DisplayPathInfo.isEnabled())
			lines = new DisplayPathInfo(Color.RED);
	}

	boolean fShutdown = false;
	SynchronizedFlag fPaused = new SynchronizedFlag(true);
	volatile boolean fStopped;

	int cnt = 0;
	int max_choice_cnt = 0;
	long max_loop_time = 0;
	long max_inner_loop_time = 0;
	long max_sim_time = 0;
	long max_true_sim_time = 0;

	int nLongestPath = 0;

	int nBestSpotsLeft;

	private static final int INITIAL_DELAY = 12;

	public static void addFutureOptions(PriorityBlockingQueue<Option> q,
			Option x,
			BlockEvaluator block_eval) {
		int nCnt = 0;
		for (int i=0; i<AStarThreaded.ENCODED_CHOICEVAL_CNT; i++) {	
			if (i==5) continue;

			if ((x.nCycleCnt < INITIAL_DELAY) && (i != 7) && (i != 9))
				continue;

			TheoreticMario m_t = x.f.mario;

			// Don't bother jumping if it won't be registered
			if ((m_t.jumpTime == 0) && AStarThreaded.coded_keys[i][Mario.KEY_JUMP])
				if (!m_t.mayJump || (!m_t.onGround && !m_t.sliding)) {										
					continue;
				}

			// Don't bother jumping if we're wall-hacking
			if (m_t.jumpTime < 0 && !m_t.onGround && !m_t.sliding && 
					AStarThreaded.coded_keys[i][Mario.KEY_JUMP])
				continue;

			// Don't bother moving left/right if we're ducking
			//			if (m_t.onGround && AStarThreaded.coded_keys[i][Mario.KEY_DOWN] && m_t.large) || 
			if (!m_t.onGround && m_t.ducking)
				if (AStarThreaded.coded_keys[i][Mario.KEY_LEFT] || 
						AStarThreaded.coded_keys[i][Mario.KEY_RIGHT])
					continue;

			// Don't bother ducking if we're small or not-on-ground
			if ((!m_t.large || !m_t.onGround) && AStarThreaded.coded_keys[i][Mario.KEY_DOWN])
				continue;

			// Don't bother walking into objects that are blocking us on the right
			if ((x.nCycleCnt >= INITIAL_DELAY) && m_t.onGround && 
					(m_t.isBlocking(m_t.getX() + m_t.xa + m_t.width,
							m_t.getY() + m_t.ya - m_t.height / 2, m_t.xa, m_t.ya)))
				if (!AStarThreaded.coded_keys[i][Mario.KEY_JUMP] &&
						AStarThreaded.coded_keys[i][Mario.KEY_RIGHT]) {
					continue;
				}

			Option y = x.clone_and_guess(i, block_eval);
			nCnt++;
			q.add(y);
		}
	}


	@Override
	public void run() {
		while (!fShutdown) {
			fStopped = false;
			cnt = 0;
			max_choice_cnt = 0;
			nLongestPath = 0;

			nBestSpotsLeft = BEST_N-1;
			results.clear();

			if (lines != null) lines.set.clear();

			while (!fPaused.isTrue()) {		
				long loop_time = System.currentTimeMillis();
				Option x = null;
				do {
					try {
						x = open_set.poll(1, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						//						e.printStackTrace();
					}
				} while ((x == null) && !fPaused.isTrue());
				if (fPaused.isTrue()) {
					if (x != null) open_set.add(x);
					continue;
				}

				if (block_eval.nGuessedEndpoint != -1)
					if (x.f.mario.x > block_eval.nGuessedEndpoint) {
						if (x.fHasBeenUpdated) {
							results.add(x);
						} else {
							x.update(block_eval, lines);					
							open_set.add(x);
						}
						continue;
					}

				if (x.isGoal(AStarThreaded.getXStart())) {
					if (nBestSpotsLeft <= 0) {
						fPaused.set();
					} else {
						nBestSpotsLeft--;
					}
					results.add(x);
					//					System.out.print("G");
					continue;
				}

				int t_choice_cnt = 0;
				long inner_loop_time = System.currentTimeMillis();


				if (x.isPrefailure())
					continue;

				float x_old = x.f.mario.x; float y_old = x.f.mario.y;
				x.update(block_eval, lines);

				if (x.nCycleCnt-AStarThreaded.initial_cycle_cnt > nLongestPath)
					nLongestPath = x.nCycleCnt-AStarThreaded.initial_cycle_cnt;

				if (x.isFailure(open_set, block_eval)) {
					//				if (x.line != null) x.line.color = Color.BLACK;
					//				System.out.print("F");
					continue;
				}

				if ((open_set.size() > AStarThreaded.QUEUE_SIZE_SOFT_LIMIT) &&
						(x.getFScore() > open_set.getWorstScore())) {
					continue;
				}


				if ((blocking_grid != null) && 
						(x.nCycleCnt-AStarThreaded.initial_cycle_cnt > 3)) {
					synchronized(blocking_grid) {
						if (blocking_grid.blocked(
								x, AStarThreaded.block_eval.fIsStartpoint)) {
							continue;
						}
						blocking_grid.add(x);
					}
				}
				addFutureOptions(open_set, x, block_eval);








				inner_loop_time = System.currentTimeMillis() - inner_loop_time;
				if (inner_loop_time > max_inner_loop_time) max_inner_loop_time = inner_loop_time;				

				if (t_choice_cnt > max_choice_cnt) max_choice_cnt = t_choice_cnt;

				loop_time = System.currentTimeMillis() - loop_time;
				if (loop_time > max_loop_time) max_loop_time = loop_time;
			}
			fStopped = true;

			if (lines != null)
				DisplayPathInfo.addPath(lines);

			synchronized(this) {
				while (fPaused.isTrue()) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}


}

class SynchronizedCounter {
	private int c = 0;

	public synchronized void increment() { c++; }
	public synchronized void decrement() { c--; }
	public synchronized int value() { return c; }
	public synchronized void reset() { c = 0; }
}

class SynchronizedFlag {
	private boolean b = false;

	public SynchronizedFlag(boolean c) {
		b = c;
	}

	public synchronized boolean isTrue() {
		return b == true;
	}

	public synchronized void clear() {
		b = false;
	}

	public synchronized void set() {
		b = true;
	}
}

public class AStarThreaded extends AbstractSearch {

	private static final boolean FLAG_INVERTED = true;
	static final int QUEUE_SIZE_SOFT_LIMIT = 80;
	static final int PROCESSING_TIME = 35;	//1000;

	public BlockingGrid blocking_grid = new BlockingGrid();

	public static float endpt_x = 0;

	public static int initial_cycle_cnt = 0;

	public class DefaultGoalEvaluator implements GoalEvaluator {


		float x_start; float y_start;
		// TODO: 1.6
		//		@Override
		public boolean isGoal(Option o) {
			if (o.parent == null) {
				if (o.f.mario.getX() > x_start + 11*16) {
					throw new java.lang.NullPointerException();
				}
				if (o.nCycleCnt-AStarThreaded.initial_cycle_cnt > 105) {
					throw new java.lang.NullPointerException();					
				}
			}

			if (o.f.mario.getX() > x_start + 11*16) return true;
			if (o.nCycleCnt-AStarThreaded.initial_cycle_cnt > 105) return true;
			return false;
		}
		// TODO: 1.6
		//		@Override
		public void setMario(float x, float y) {
			x_start =x ; y_start = y;
		}

	}

	static final int ENCODED_CHOICEVAL_CNT = 12;
	// 0: --- 1: --L 2: --R 3: -JL 4: -JR 5: S-- 6: S-L 7: S-R 8: SJL 9: SJR
	// 10: -JD 11: --D
	/*
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
	 */
	PQSearch open_set = new PQSearch();
	Option prev_best_first_choicepoint = null;

	public static BlockEvaluator block_eval = null;

	private void restart(Frame f0, Option.Factory factory) {
		if (blocking_grid != null) {
			blocking_grid.clear();
		}

		open_set.clear();				
		Option start = factory.create(f0, initial_cycle_cnt);
		if (FLAG_INVERTED) {
			ProcessingThread.addFutureOptions(open_set, start, block_eval);
		} else {
			open_set.add(start);
		}		

	}

	@Override
	public void reset() {
		open_set.clear();				
		prev_best_first_choicepoint = null;
		endpt_x = 0;
		initial_cycle_cnt = 0;

		for (int i=0; i<ENCODED_CHOICEVAL_CNT; i++)
			choice_succ[i].reset();
	}

	LinkedList<ProcessingThread> threads = new LinkedList<ProcessingThread>();

	public AStarThreaded(BlockEvaluator block_eval) {
		this.block_eval = block_eval;
		Runtime runtime = Runtime.getRuntime();    
		int nrOfProcessors = 1;	//runtime.availableProcessors();	

		for (int i=0; i<nrOfProcessors; i++) {
			ProcessingThread thread = new ProcessingThread(
					open_set, blocking_grid, Thread.currentThread(),
					block_eval);
			threads.add(thread);
			thread.start();
		}

	}

	//	GoalEvaluator goal_eval = new DefaultGoalEvaluator();
	private static volatile float x_start;
	public static float getXStart() { return x_start; }

	Option.Factory old_factory = null;

	@Override
	public int a_star(float mario_x, float mario_y,
			Frame f0, GoalEvaluator goal_eval_in,
			Option.Factory factory,
			BlockEvaluator block_eval
	) {

		//		AStarThreaded.block_eval = block_eval;
		if (blocking_grid != null) blocking_grid.setMario(mario_x, mario_y);

		{
			boolean fAllStopped = true;
			for (ProcessingThread t : threads) {
				fAllStopped = fAllStopped && t.fStopped;
			}
			if (!fAllStopped) throw new java.lang.NullPointerException();
		}

		x_start = mario_x;
		//goal_eval.setMario(mario_x, mario_y);

		endpt_x = mario_x+11*16;

		if (false) {
			if ((factory == old_factory) && !open_set.isEmpty()) {
				if (prev_best_first_choicepoint.f.equals(f0)) {
					if (prev_best_first_choicepoint.parent == null)
						throw new java.lang.NullPointerException();
					prune_open_set(prev_best_first_choicepoint);
				} else  {
					restart(f0, factory);
				}
			} else {
				restart(f0, factory);			
			}
		} else {
			restart(f0, factory);
		}

		old_factory = factory;

		long start_time = System.currentTimeMillis();

		for (ProcessingThread t : threads) {
			synchronized (t) {
				t.fPaused.clear();
				t.notify();
			}
		}


		int processed_time = PROCESSING_TIME;

		boolean fAllStopped;
		do {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processed_time-=1;

			if (processed_time <= 0)
				for (ProcessingThread t : threads) {
					t.fPaused.set();
				}

			fAllStopped = true;
			for (ProcessingThread t : threads) {
				fAllStopped = fAllStopped && t.fStopped && t.fPaused.isTrue();
			}
		} while (!fAllStopped);

		initial_cycle_cnt++;


		int nTotalCnt = 0;
		Option opt_t = null;

		boolean fFirst = true;
		for (ProcessingThread t : threads) {
			nTotalCnt += t.results.size();
			if (!t.results.isEmpty()) opt_t = t.results.getFirst();
			fFirst = false;
		}

		Option best_result_t = null;
		for (ProcessingThread t : threads) {
			for (Option o : t.results) {
				//				 System.out.println("g="+o.getGScore());
				if ((best_result_t == null) || (o.getGScore() < best_result_t.getGScore()))
					best_result_t = o;
			}
		}
		if (best_result_t != null) {
			perfect_path = best_result_t;
//			//System.out.print("+");
			nPathFailures = 0;
		} else {
			nPathFailures++;
			if (false) {	//(nPathFailures >= 3) {
				perfect_path.dump();
				throw new java.lang.NullPointerException();
			}
		}

		if (best_result_t == null) {
			best_result_t = open_set.peek();
//			MarioComponent.fCaptureScreenshot = true;
			//System.out.print("P"+processed_time+","+open_set.size());
			for (ProcessingThread t : threads) {
				//System.out.print(":"+t.nLongestPath);
			}

			for (Option o : open_set) {
				DisplayPathInfo path = new DisplayPathInfo(Color.GREEN);
				o.highlight_path(path);
				DisplayPathInfo.addPath(path);
			}
		}

		for (ProcessingThread t : threads) {
			open_set.addAll(t.results);
			t.results.clear();
		}

		if (!open_set.isEmpty()) {
			prev_best_first_choicepoint = best_result_t;	//open_set.peek();
			Option opt_result = prev_best_first_choicepoint.get_first_in_path();
			int result = opt_result.coded_action;

			//			if ((best_result_t == perfect_path) && (result == 0) &&
			//					(opt_result.f.mario.xa == 0)) {
			//				//System.out.println();
			//				//System.out.println(block_eval.nGuessedEndpoint);
			//				best_result_t.dumpAction();
			//				//System.out.println();
			//				best_result_t.dump();
			//				throw new java.lang.NullPointerException();
			//			}

			if (prev_best_first_choicepoint.parent == null)
				return 0;

			while (prev_best_first_choicepoint.parent.parent != null)
				prev_best_first_choicepoint =
					prev_best_first_choicepoint.parent;
			if (result != prev_best_first_choicepoint.coded_action)
				throw new java.lang.NullPointerException();
			if (result == -1) throw new java.lang.NullPointerException();
			return prev_best_first_choicepoint.coded_action;
		}

		return 0;
	}

	int nPathFailures = 0;
	Option perfect_path = null;

	private void prune_i(Option o) {
		if (o == null) return;

	}

	private void prune_open_set(Option o_first) {
		if (o_first.parent == null) throw new java.lang.NullPointerException();

		boolean fHasChildren = false;
		for (int i=0; i<10; i++)
			if (o_first.children[i] != null)
				fHasChildren = true;
		if(!fHasChildren) {
			return;
		}

		Iterator<Option> iterSet = open_set.iterator();
		while (iterSet.hasNext()) {
			Option o = iterSet.next();
			Option option_true_second = o;
			while ((option_true_second != null) && (option_true_second.parent != o_first)) {
				option_true_second = option_true_second.parent;
			}
			if (option_true_second == null) {
				iterSet.remove();
			}
		} 
		o_first.parent = null;
	}

	class BlockingGrid {
		private int WINDOW_SIZE = 400;
		private int WINDOW_HEIGHT = 240;
		private int LOOKAHEAD_SIZE = 240;
		private int LOOKBEHIND_SIZE = 80;
		private float SCALE_FACTOR = 1;

		private Option[][] grid = new Option[WINDOW_SIZE][WINDOW_HEIGHT];

		private float mario_x = 0;
		private float mario_y = 0;

		private int ptr_x = 0;
		private int ptr_y = 0;

		public void setMario(float x, float y) {
			float old_x = mario_x;
			mario_x = x; mario_y = y;

			if (x <= old_x) return;

			int old_ptr_x = ptr_x;
			ptr_x = ((int)(x/2))%WINDOW_SIZE;

			int old_erase_column = (old_ptr_x+LOOKAHEAD_SIZE)%WINDOW_SIZE;
			int erase_column = (ptr_x+LOOKAHEAD_SIZE)%WINDOW_SIZE;
			for (int i=old_erase_column; i<erase_column; i=(i+1)%WINDOW_SIZE) {
				for (int yt=0; yt<WINDOW_HEIGHT; yt++)
					grid[i][yt] = null;
			}

		}

		public void clear() {
			grid = new Option[WINDOW_SIZE][WINDOW_HEIGHT];
		}


		public void add(Option o) {
			int x = ((int)((o.f.mario.x-mario_x)/SCALE_FACTOR) + ptr_x)%WINDOW_SIZE;
			int y = (int)(o.f.mario.y/SCALE_FACTOR);

			if ((x>=0) && (y >=0) && (x<WINDOW_SIZE) && (y<WINDOW_HEIGHT))
				grid[x][y] = o;		
		}


		public boolean blocked(Option o, boolean fIsStart) {
			int x_t = (int)((o.f.mario.x-mario_x)/SCALE_FACTOR);
			if (x_t > LOOKAHEAD_SIZE) return true;
			if (x_t < -LOOKBEHIND_SIZE) return true;

			int x = (x_t + ptr_x)%WINDOW_SIZE;
			int y = (int)(o.f.mario.y/SCALE_FACTOR);
			if (y >= WINDOW_HEIGHT) return !fIsStart;
			if ((x>=0) && (y >=0) && (x<WINDOW_SIZE) && (y<WINDOW_HEIGHT)) {
				if (grid[x][y] != null) {
					if (o.nCycleCnt - grid[x][y].nCycleCnt > 5) return false;
					return true;
				}
			}
			return false;
		}
	}
}
