package competition.cig.alexandrupaler;

public class Cell implements Comparable<Cell>{
	public float g;
	public float h;
	public Cell parent = null;
	
	public int[] coord = new int[2];
	
	public Cell(int[] pos, float g, float h)
	{
		this.coord[0] = pos[0];
		this.coord[1] = pos[1];
		
		this.g = g;
		this.h = h;
	}
	
	public float f()
	{
		return g+h;
	}
	
	public int compareTo(Cell o) {
		Float f1 = f();
		Float f2 = o.f();
		
		return f1.compareTo(f2);
	}
}
