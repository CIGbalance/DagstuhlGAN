package competition.cig.peterlawford.visualizer;

import java.util.List;

import competition.cig.peterlawford.search_algs.BlockEvaluator;

import ch.idsia.mario.environments.Environment;

public class Visualizer {
	public static final byte CLEAR = 0;

//	public static final byte MARIO = 1;
	public static final byte MARIO = -18;

	public static final byte WALL_CHECK = 3;
	public static final byte COIN = 34;
	public static final byte SHELL = 13;
	public static final byte MUSHROOM = 14;
	public static final byte FIREFLOWER = 15;
	public static final byte FIREBALL = 25;

	public static final byte ENEMY_GOOMBA = 2;
	public static final byte ENEMY_FLYING_GOOMBA = 3;
	public static final byte ENEMY_RED_KOOPA = 4;
	public static final byte ENEMY_FLYING_RED_KOOPA = 5;
	public static final byte ENEMY_GREEN_KOOPA = 6;
	public static final byte ENEMY_FLYING_GREEN_KOOPA = 7;
	public static final byte ENEMY_BULLET = 8;
	public static final byte ENEMY_SPINY = 9;
	public static final byte ENEMY_FLYING_SPINY = 10;
	public static final byte ENEMY_PIRANHA_PLANT = 12;

	
	private byte[][] obsAll1 = null;
	private byte[][] obsAll2 = null;
	private byte[][] obsAll3 = null;
	private byte[][] obsAll4 = null;

	private byte[][] obsEnemy1 = null;
	private byte[][] obsEnemy2 = null;
	private byte[][] obsEnemy3 = null;
	private byte[][] obsEnemy4 = null;

	private byte[][] obsScene1 = null;
	private byte[][] obsScene2 = null;
	private byte[][] obsScene3 = null;
	private byte[][] obsScene4 = null;

	
	public void update(Environment env) {
		obsAll4 = obsAll3;
		obsAll3 = obsAll2;
		obsAll2 = obsAll1;
//		obsAll1 = env.getCompleteObservation();
		obsAll1 = env.getMergedObservationZ(0,0);
		
		obsEnemy4 = obsEnemy3;
		obsEnemy3 = obsEnemy2;
		obsEnemy2 = obsEnemy1;
//		obsEnemy1 = env.getEnemiesObservation();
		obsEnemy1 = env.getEnemiesObservationZ(0);
		
		obsScene4 = obsScene3;
		obsScene3 = obsScene2;
		obsScene2 = obsScene1;
//		obsScene1 = env.getLevelSceneObservation();		
		obsScene1 = env.getLevelSceneObservationZ(0);		
	}
	
	private static char decodeLvlScene(byte lvl_scene) {
		switch (lvl_scene) {
		// These are ZLevel-1 results
		case -10: 
			return '~';	// ground, or bullet
		case -11: return '=';	// platform
		case -12: return '@';	// rock

		case CLEAR: return ' ';	// empty space
		case MARIO: return 'M';	// mario

		case 16: return '$';	// coinbox
		// Removed by Jacob. Char encoding issue
		//case 17: return 'â‚¬';	// coinbox
		//case 18: return 'Â¢';	// coinbox
		case 20: return 'P';	// pipe
		// (it is now impossible to tell pipe from cannon stand in ZLevel-1)
		case 21: return '?';	// mystery box
		//case 22: return 'Â§';	// coinbox

		case COIN: return 'C';	// coin
		//case 46: return 'Â¤';	// bullet shooter stand (old-school)

		// These are ZLevel-2 results
		case 1: return '#';	// ground
		
		// These are ZLevel-0 results (also include ZLevel-1)
//		case CLEAR: return ' ';	// empty space
//		case COIN: return 'C';	// coin
		
		case -127: return 'T';	// ground
		case -111: return '#';	// interior ground

		case -106: return ';';	// background underneath platform right
		case -107: return '.';	// background underneath platform
		case -108: return ',';	// background underneath platform left

		case -110: return '|';	// wall XXX|
		case -112: return '|';	// wall |XXX

		case -122: return '>';	// platform
		case -123: return '=';	// platform
		case -124: return '<';	// platform
		//case -76: return 'Â«';	// platform underneath another platform
		//case -74: return 'Â»';	// platform underneath another platform

		//case -125: return 'â–Ÿ';
		//case -126: return 'â–œ';
		//case -128: return 'â–›';
		//case -109: return 'â–™';	// interior ground

		//case 14: return 'Ãž';	// bullet shooter
		//case 30: return 'Â¤';	// bullet shooter stand
//		case 46: return 'Â¤';	// bullet shooter stand (old-school)

		case 4: return '!';	// punched coinbox
//		case 16: return '$';	// coinbox or brick
//		case 21: return '?';	// mystery box

		case 9: return '@';	// rock

		//case 10: return 'Æ¤';	// pipe - top-left corner
		//case 11: return 'Æ¤';	// pipe - top-right corner
		case 26: return 'P';	// pipe
		case 27: return 'P';	// pipe

		default: System.out.print(Byte.toString(lvl_scene));
		return '?';
		}		
	}

	public static char decodeEnemies(byte b) {
		switch (b) {
		case -1: return ' ';	// empty space
		case 0: return ' ';	// new-style empty space
		case MARIO: return 'M';	// mario
		case ENEMY_GOOMBA: return 'X';	// goomba
		case ENEMY_FLYING_GOOMBA: return 'x';	// flying goomba
		//case ENEMY_RED_KOOPA: return 'Ó�';	// red koopa (turtle)
		//case 5: return 'Ó‚';	// flying red koopa (turtle)
		case ENEMY_GREEN_KOOPA: return 'K';	// green koopa (turtle)
		case 7: return 'k';	// flying green koopa (turtle)
		case 8: return 'B';	// bullet
		case ENEMY_SPINY: return 'S';	// spiny
		case 10: return 's';	// flying spiny
		case ENEMY_PIRANHA_PLANT: return 'Q';	// piranha plant
		case SHELL: return 'D';	// shell
		
		case MUSHROOM: return 'H';	// mushroom (increases size)
		case FIREFLOWER: return 'h';	// fireflower (gives fireball)
		
//		case 22: return 'f';	// flower (gives fireball)
		case 25: return 'F';	// fireball
		default: System.out.print(Byte.toString(b));
		return '?';		
		}
	}

	static Ansi ansi_blue = new Ansi(Ansi.Attribute.BRIGHT, null, Ansi.Color.BLUE);
	static Ansi ansi_yellow = new Ansi(Ansi.Attribute.DIM, null, Ansi.Color.YELLOW);
	static Ansi ansi_white = new Ansi(Ansi.Attribute.DIM, null, Ansi.Color.WHITE);
//	ansi.out("hello ansi world");

	public static String getVisualization2(
			byte b, byte lvl_scene, byte enemies, int eval) {
		char ch = getVisualization(b, lvl_scene, enemies);
		if (eval < 0) {
			return ansi_blue.colorize(Character.toString(ch));
		} else {
			if (eval <=1) {
				return ansi_yellow.colorize(Character.toString(ch));
			} else {
				if (eval <= 3) {
					return ansi_white.colorize(Character.toString(ch));					
				} else {
			return Character.toString(ch);
				}
			}
		}
	}

	public static char getVisualization(byte b, byte lvl_scene, byte enemies) {
		if (true) {
//			char result = decodeLvlScene(lvl_scene);
//			if (result == ' ') 
			return decodeEnemies(enemies);
//			return result;
		}
		//		if (true) return decodeLvlScene(lvl_scene);
		switch (b) {
		case -127: return 'T';	// ground
		case -111: return '#';	// interior ground

		case -106: return ';';	// background underneath platform right
		case -107: return '.';	// background underneath platform
		case -108: return ',';	// background underneath platform left

		case -110: return '|';	// wall XXX|
		case -112: return '|';	// wall |XXX

		case -122: return '>';	// platform
		case -123: return '=';	// platform
		case -124: return '<';	// platform
		//case -76: return 'Â«';	// platform underneath another platform
		//case -74: return 'Â»';	// platform underneath another platform

		//case -125: return 'â–Ÿ';
		//case -126: return 'â–œ';
		//case -128: return 'â–›';
		//case -109: return 'â–™';	// interior ground


		// New-style scenery reporting (Aug. 13, 2009)
		//case -10: return 'â–’';	// Unknown type of solid ground
		case -11: return '=';	// Unknown type of platform
		case -12: return '@';	// rock
	
		
		
		case -1: return '*';

		case CLEAR: return ' ';	// empty space
		case MARIO: return 'M';	// mario
		case 2: return 'X';	// goomba
		case 3: return 'x';	// flying goomba
		//case 4: return 'Ó�';	// red koopa (turtle)
		//case 5: return 'Ó‚';	// flying red koopa (turtle)
		case 6: return 'K';	// green koopa (turtle)
		case 7: return 'k';	// flying green koopa (turtle)
		case ENEMY_BULLET: return 'B';	// (8) bullet
		case 9: {
			if (decodeEnemies(enemies) == 'S') return 'S';	// spiny
			return 'R';	// rock
		}

		case 10: {
			if (decodeEnemies(enemies) == 's') return 's';	// flying spiny
			return ' '; //'Æ¤';	// pipe
		}
		case 11: return ' '; //'Æ¤';	// pipe

		case 12: {
			if (decodeEnemies(enemies) == 'Q') return 'Q';	// pirahna plant
			return '$';	// coin box
		}
		case 13: return 'D';	// shell
		//case 14: return 'Ãž';	// bullet shooter

		case 16: return '@';	// brick
		//case 17: return 'â–ˆ';	// brick
		case 18: return '$';	// brick
		
		case 20: return 'c';	// popup coin from a brick ?
		
		case 21: return '$';	// brick
		case 22: return '$';	// brick

		case FIREBALL: return 'F';	// fireball
		case 26: return 'P';	// pipe
		case 27: return 'P';	// pipe
		case 30: return 'b';	// bullet
		case COIN: return 'C';	// coin
		//case 46: return 'ÃŸ';	// bullet
		default: System.out.print(Byte.toString(b));
		return '?';
		}
	}
	
	public void ansiViz(Environment env, BlockEvaluator block_eval) {
		for (int i=0; i<22; i++)
			visualizeLine(env, i, block_eval);		
	}
	
	private void visualizeLine(Environment e, int n, BlockEvaluator eval) {
		System.out.print(Integer.toString(n, 22)+":");
		for (int i=0; i<22; i++) {
		//	char ch =
			System.out.print(
				Visualizer.getVisualization2(
					e.getMergedObservationZ(0,0)[n][i],
					e.getLevelSceneObservationZ(0)[n][i],
					e.getEnemiesObservationZ(0)[n][i],
					eval.getPt(n, i)))
					;
//			if (ch == ' ') ch = ((i%4 == 0)?'"' : ((i%2==0)?'`' : ' '));
//		System.out.print(ch);
	}
		
		if (obsAll1 != null) {
			System.out.print("\t");
			for (int i=0; i<22; i++)
				System.out.print(Visualizer.getVisualization(
						obsAll1[n][i],
						obsScene1[n][i],
						obsEnemy1[n][i]));

		}
		if (obsAll2 != null) {
			System.out.print("\t");
			for (int i=0; i<22; i++)
				System.out.print(Visualizer.getVisualization(
						obsAll2[n][i],
						obsScene2[n][i],
						obsEnemy2[n][i]));
		}
		if (obsAll3 != null) {
			System.out.print("\t");
			for (int i=0; i<22; i++)
				System.out.print(Visualizer.getVisualization(
						obsAll3[n][i],
						obsScene3[n][i],
						obsEnemy3[n][i]));
		}
		if (obsAll4 != null) {
			System.out.print("\t");
			for (int i=0; i<22; i++)
				System.out.print(Visualizer.getVisualization(
						obsAll4[n][i],
						obsScene4[n][i],
						obsEnemy4[n][i]));
		} 
		System.out.println();
	}

	public static void showBestPath(List<PathInfo> best_path) {
		System.out.print("PATH: ");
		for (PathInfo info : best_path)
			System.out.print(info+"  ");
		System.out.println();

		System.out.print("      ");
		for (PathInfo info : best_path) {
			int n = Math.round(info.nSpeedX*10);
			if (n >= 0)
				System.out.print(Integer.toString(n, 11)+"  ");
			else
				System.out.print(Integer.toString(n, 11)+" ");
		}
		System.out.println();
		
		System.out.print("      ");
		for (PathInfo info : best_path) {
			int n = Math.round(info.nSpeedY*10);
			if (n >= 0)
				System.out.print(Integer.toString(n, 11)+"  ");
			else
				System.out.print(Integer.toString(n, 11)+" ");
		}
		System.out.println();
	}

	public class PathInfo {
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
			if ((nSpeedX > 0) && (nSpeedY > 0)) return "â†—";
			if ((nSpeedX > 0) && (nSpeedY < 0)) return "â†˜";
			if ((nSpeedX < 0) && (nSpeedY < 0)) return "â†™";
			if ((nSpeedX < 0) && (nSpeedY > 0)) return "â†–";
			if ((nSpeedX > 0) && (nSpeedY == 0)) return "â†’";
			if ((nSpeedX < 0) && (nSpeedY == 0)) return "â†�";
			if ((nSpeedX == 0) && (nSpeedY > 0)) return "â†‘";
			if ((nSpeedX == 0) && (nSpeedY < 0)) return "â†“";
			return "*";
		}
	}

}
