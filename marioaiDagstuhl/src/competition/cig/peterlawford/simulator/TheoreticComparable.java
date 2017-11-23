package competition.cig.peterlawford.simulator;

public class TheoreticComparable {
	public byte nType;
	public float x;
	public float y;
	
	public float getX() { return x; }
	public float getY() { return y; }
	
	public TheoreticComparable(byte nType, float x, float y) {
		this.nType = nType;
		this.x =x;
		this.y = y;
		
	}
}
