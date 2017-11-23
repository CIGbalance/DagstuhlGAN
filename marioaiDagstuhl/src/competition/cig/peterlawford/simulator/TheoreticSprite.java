package competition.cig.peterlawford.simulator;

public class TheoreticSprite extends TheoreticComparable {
    protected static float GROUND_INERTIA = 0.89f;
    protected static float AIR_INERTIA = 0.89f;
//    public float xOld, yOld;
//    public float x, y;
    public float xa, ya;
    
    float nXP = 0;
    float nYP = 0;
    
    float xOld = -1;
    float yOld = -1;
    
    public TheoreticSprite(byte nType, float x, float y, float xa, float ya) {
    	super(nType, x, y);
    	this.xa =xa;
    	this.ya = ya;

    }
/*
    public void move() {
 //   	xOld = x;
 //   	yOld = y;
    } */
}
