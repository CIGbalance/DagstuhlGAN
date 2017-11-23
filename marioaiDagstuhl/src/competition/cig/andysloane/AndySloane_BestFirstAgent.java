package competition.cig.andysloane;

import java.awt.Color;
import ch.idsia.ai.agents.Agent;

public final class AndySloane_BestFirstAgent extends HeuristicSearchingAgent
{
	private PrioQ pq;
	private static final int maxSteps = 800;

	public AndySloane_BestFirstAgent() {
		super("AndySloane_BestFirstAgent");
	}

	@Override
	public void reset() {
		super.reset();
		pq = new PrioQ(Tunables.MaxBreadth);
	}

	@Override
	protected int searchForAction(MarioState initialState, WorldState ws) {
		pq.clear();

		initialState.ws = ws;
		initialState.g = 0;
		initialState.dead = false;
		
		float threshold = 1e10f;

		initialState.cost = cost(initialState, initialState);

		int a,n;
		// add initial set
		for(a=0;a<16;a++) {
			if(useless_action(a, initialState))
				continue;
			MarioState ms = initialState.next(a, ws);
			ms.root_action = a;
			ms.cost = Tunables.FactorC + cost(ms, initialState);
			pq.offer(ms);
			if(verbose2)
				System.out.printf("BestFirst: root action %d initial cost=%f\n", a, ms.cost);
		}

		MarioState bestfound = pq.peek();

		// FIXME: instead of using a hardcoded number of iterations,
		// periodically grab the system millisecond clock and terminate the
		// search after ~40ms
		boolean goalFound = false;
		int pq_siz=0;
		for(n=0;n<maxSteps && !pq.isEmpty();n++) {
			MarioState next = pq.poll();

			for(a=0;a<16;a++) {
				if(useless_action(a, next))
					continue;
				MarioState ms = next.next(a, next.ws);
				ms.pred = next;

				if(ms.dead) continue;

				float h = cost(ms, initialState);
				ms.g = next.g + Tunables.GIncrement;
				ms.cost = ms.g + h + ((a&MarioState.ACT_JUMP)>0?Tunables.FeetOnTheGroundBonus:0);
				bestfound = marioMin(ms,bestfound);

				if (h < 0.1f)
					return ms.root_action;

				if (ms.cost < threshold)
					pq.offer(ms);
				pq_siz++;
			}
		}

		if (!pq.isEmpty())
			bestfound = marioMin(pq.poll(), bestfound);
		if(verbose2) {
			System.out.printf("BestFirst: giving up on search; best root_action=%d cost=%f lookahead=%f\n",
					bestfound.root_action, bestfound.cost, bestfound.g);
		}
		// return best so far
		pq.clear();
		return bestfound.root_action;
	}
}
