package competition.cig.peterlawford.simulator;

import ch.idsia.mario.environments.Environment;

public class ClonedEnvironment implements Environment {
	
//	final private byte[][] enemies;
	final private byte[][] scenery;
//	final private byte[][] everything;

	final private byte[][] enemies0;
	final private byte[][] scenery0;
	final private byte[][] everything0;
	
	final private float[] mario;
	final private int mario_mode;
	final private boolean mario_on_ground;
	final private boolean mario_may_jump;
	final private boolean mario_carrying;
	final private float[] enemies_list;
	final private Environment base_env;
	
	public ClonedEnvironment(Environment env) {
//		enemies = env.getEnemiesObservation();
		scenery = env.getLevelSceneObservation();
//		everything = env.getCompleteObservation();

		enemies0 = env.getEnemiesObservationZ(0);
		scenery0 = env.getLevelSceneObservationZ(0);
		everything0 = env.getMergedObservationZ(0, 0);

		mario = env.getMarioFloatPos();
		mario_mode = env.getMarioMode();
		mario_on_ground = env.isMarioOnGround();
		mario_may_jump = env.mayMarioJump();
		mario_carrying = env.isMarioCarrying();
		enemies_list = env.getEnemiesFloatPos();
		
		base_env = env;
	}

	// TODO: 1.6
//	@Override
	public String getBitmapEnemiesObservation() {
		throw new java.lang.NullPointerException();
	}

	// TODO: 1.6
//	@Override
	public String getBitmapLevelObservation() {
		throw new java.lang.NullPointerException();
	}

	// TODO: 1.6
//	@Override
	public float[] getEnemiesFloatPos() {
		return enemies_list;
	}

	// TODO: 1.6
//	@Override
	public byte[][] getEnemiesObservation() {
		throw new java.lang.NullPointerException();
	}

	// TODO: 1.6
//	@Override
	public byte[][] getLevelSceneObservation() {
		return scenery;
	}

	// TODO: 1.6
//	@Override
	public float[] getMarioFloatPos() {
		return mario;
	}

	// TODO: 1.6
//	@Override
	public int getMarioMode() {
		return mario_mode;
	}

	// TODO: 1.6
//	@Override
	public byte[][] getCompleteObservation() {
		throw new java.lang.NullPointerException();
//		return everything;
	}

	// TODO: 1.6
//	@Override
	public boolean isMarioCarrying() {
		return mario_carrying;
	}

	// TODO: 1.6
//	@Override
	public boolean isMarioOnGround() {
		return mario_on_ground;
	}

	// TODO: 1.6
//	@Override
	public boolean mayMarioJump() {
		return mario_may_jump;
	}

	public byte[][] getMergedObservationZ(int sc_z, int e_z) {
		if (sc_z != 0) throw new java.lang.NullPointerException();
		if (e_z != 0) throw new java.lang.NullPointerException();
		return everything0;
	}

	public byte[][] getEnemiesObservationZ(int z) {
		if (z != 0) throw new java.lang.NullPointerException();
		return enemies0;
	}

	public byte[][] getLevelSceneObservationZ(int z) {
		if (z != 0) throw new java.lang.NullPointerException();
		return scenery0;
	}

//	@Override
	public int getKillsByFire() {
		return base_env.getKillsByFire();
	}

//	@Override
	public int getKillsByShell() {
		return base_env.getKillsByShell();
	}

    public boolean canShoot() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //	@Override
	public int getKillsByStomp() {
		return base_env.getKillsByStomp();
	}

//	@Override
	public int getKillsTotal() {
		return base_env.getKillsTotal();
	}
}
