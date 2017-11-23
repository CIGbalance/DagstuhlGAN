package competition.cig.peterlawford.search_algs;

import java.util.concurrent.PriorityBlockingQueue;

public class PQSearch extends PriorityBlockingQueue<Option> {

	double nWorstScore = -Double.MAX_VALUE;
	
	double nBestEndScore = -1;
	
	public boolean add(Option o) {
		if (o.getFScore() > nWorstScore) nWorstScore = o.getFScore(); 
		return super.add(o);
	}
	public void clear() {
		nWorstScore = Double.MAX_VALUE;
		nBestEndScore = -1;
		super.clear();
	}
	public double getWorstScore() { return nWorstScore; }
	public double getBestFinalScore() { return nBestEndScore; }
	public void setBestFinalScore(double nBestEndScore) {
		this.nBestEndScore = nBestEndScore;
	}
}
