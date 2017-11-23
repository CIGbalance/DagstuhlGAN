package competition.cig.alexandrupaler;

import java.util.AbstractCollection;
import java.util.PriorityQueue;
import java.util.Vector;


public class Astar {
	protected int mapNrRows = 22;
	protected int mapNrCols = 22;
	
	protected byte[][] map = new byte[mapNrRows][mapNrCols];
	protected int[][] pos = new int[2][2];

	public Vector<int[]> findRoute(int[][] pos)
	{
		this.pos = (int[][])pos.clone();
		
		PriorityQueue<Cell> open = new PriorityQueue<Cell>();
		Vector<Cell> closed = new Vector<Cell>();
		
		/*adauga start la open*/
		Cell c = new Cell(pos[0], 0.0f/*g initial*/, h(pos[0], pos) /*h initial*/);
		open.add(c);
		
		Cell lr = null; //lowest ranked
		lr = open.peek();
		
		int[][] move = {{1,0},{0,1},{-1,0},{0,-1}};/*jos dreapta sus stanga*/
		
		while(lr!= null && isNotStop(lr))/*while stop not reached*/
		{
			open.remove(lr);
			closed.add(lr);
			
			for(int t=0; t<move.length; t++)
			{
				{
					int i = move[t][0];
					int j = move[t][1];
					if(possiblePosition(lr.coord, i, j))
					{
						/* costul existent + costul de mutare la vecin
						 * adica 1 deocamdata
						 * */
						int[] cellpos = {lr.coord[0] + i, lr.coord[1] + j};
						float cost = lr.g + map[cellpos[0]][cellpos[1]] + 1;
						
						Cell inOpen = contains(open, lr.coord[0] + i, lr.coord[1] + j);
						Cell inClosed = contains(closed, lr.coord[0] + i, lr.coord[1] + j);
						
						if(inOpen != null && cost < inOpen.g)
						{
							open.remove(inOpen);
						}
						
						if(inClosed != null && cost < inClosed.g)
						{
							closed.remove(inClosed);
						}
						
						if(inOpen == null && inClosed == null)
						{
							Cell outsider = new Cell(cellpos, cost, h(cellpos, pos));
							outsider.parent = lr;
							open.add(outsider);
						}
					}
				}
			}
			
			lr = open.peek();
		}
		
		/*reconstruct path*/
		Vector<int[]> ret = new Vector<int[]>();
		
		if(lr != null && !isNotStop(lr))
		{
			while(lr != null)
			{
				ret.insertElementAt(lr.coord, 0);
				lr = lr.parent;
			}
		}
		
		return ret;
	}
	
	protected boolean isNotStop(Cell c)
	{
		return h(c.coord, pos) != 0;
	}
	
	/*cautare dupa capul meu de idiot*/
	protected Cell contains(AbstractCollection<Cell> unde, int row, int col)
	{
		for(Cell c : unde)
		{
			if(c.coord[0] == row && c.coord[1] == col)
			{
				return c;
			}
		}
		return null;
	}
	
	protected boolean possiblePosition(int[] coord, int i, int j)
	{
		return coord[0] + i >= 0 && coord[0] + i < mapNrRows 
			&& coord[1] + j >=0 && coord[1] + j < mapNrCols
			&& map[coord[0] + i][coord[1] + j] < 99
			&& Math.abs(i) + Math.abs(j) != 2;
	}
	
	protected float h(int[] cellpos, int[][] pos)
	{
		/*manhattan distance - cel mai simplu deocamdata*/
		float heuristic = 0.0f;
		heuristic = Math.abs(cellpos[0] - pos[1][0]) + Math.abs(cellpos[1] - pos[1][1]);
		return heuristic;
	}
	
	public void setMap(byte[][] map)
	{
		for(int i=0; i<mapNrRows; i++)
		{
			for(int j=0; j<mapNrCols; j++)
			{
				this.map[i][j] = map[i][j];
			}
		}
	}
}