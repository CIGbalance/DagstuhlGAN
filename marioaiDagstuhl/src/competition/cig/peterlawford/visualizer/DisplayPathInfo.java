package competition.cig.peterlawford.visualizer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;

class ShortLine {
	float x1, y1, x2, y2;
	Color color;
	public ShortLine(float x1, float y1, float x2, float y2, Color color) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = color;
	}
}

public class DisplayPathInfo {
	public final LinkedList<float[]> path = new LinkedList<float[]>();
//	public final LinkedList<float[]> set = new LinkedList<float[]>();
	public final LinkedList<ShortLine> set = new LinkedList<ShortLine>();
	public Color color;
	public DisplayPathInfo(Color color) {
		this.color = color;
		if (color == null) throw new java.lang.NullPointerException();
	}
	public void push(float[] x) {
		path.add(x);
		//			path.push(x);
	}	
	
	private static ArrayList<DisplayPathInfo> lines =  null;
	// new ArrayList<DisplayPathInfo>();

	public static void addPath(DisplayPathInfo p) {
		if (lines != null)
		synchronized(lines) {
		lines.add(p);
		}
	}
	
	public void addToSet(float x1, float y1, float x2, float y2, Color color) {
		set.add(new ShortLine(x1,y1,x2,y2,color));
	}
	
	public static boolean isEnabled() { return lines != null; }
	
	public static void renderLines(Graphics g, float xCam, float yCam) {
		if (lines == null) return;

		synchronized(lines) {
		for (DisplayPathInfo l : lines) {

			if (g == null) System.out.println("g is null");
			if (l == null) System.out.println("l is null");
			if (l.color == null) System.out.println("color is null");
			g.setColor(l.color);

			float[] prev = null;
			for (float[] f : l.path)
			{
				if (prev != null) {
					g.drawLine(
							(int)(prev[0] - xCam),
							(int)(prev[1] - yCam),
							(int)(f[0] - xCam),
							(int)(f[1] - yCam));
					if ((f[0] - xCam < 0) &&
							(prev[0] - xCam < 0)) break;
				}
				prev = f;
			} 
			
//			for (float[] f : l.set) {
//				g.drawLine(
//						(int)(f[0] - xCam),
//						(int)(f[1] - yCam),
//						(int)(f[2] - xCam),
//						(int)(f[3] - yCam));				
//			}
			for (ShortLine f : l.set) {
				if (f.color != null)
					g.setColor(f.color);
				g.drawLine(
						(int)(f.x1 - xCam),
						(int)(f.y1 - yCam),
						(int)(f.x2 - xCam),
						(int)(f.y2 - yCam));
				if (f.color != null)
					g.setColor(l.color);
			}
		}
		}
	}

}

