package competition.cig.robinbaumgarten.astar.sprites;

import competition.cig.robinbaumgarten.astar.LevelScene;
import competition.cig.robinbaumgarten.astar.level.SpriteTemplate;


public class Sprite implements Cloneable
{
	
    public static final int KIND_NONE = -1;
    public static final int KIND_MARIO = 1;
    public static final int KIND_GOOMBA = 2;
    public static final int KIND_GOOMBA_WINGED = 3;
    public static final int KIND_RED_KOOPA = 4;
    public static final int KIND_RED_KOOPA_WINGED = 5;
    public static final int KIND_GREEN_KOOPA = 6;
    public static final int KIND_GREEN_KOOPA_WINGED = 7;
    public static final int KIND_BULLET_BILL = 8;
    public static final int KIND_SPIKY = 9;
    public static final int KIND_SPIKY_WINGED = 10;
    public static final int KIND_ENEMY_FLOWER = 11;
    public static final int KIND_FLOWER_ENEMY = 12;
    public static final int KIND_SHELL = 13;
    public static final int KIND_MUSHROOM = 14;
    public static final int KIND_FIRE_FLOWER = 15;    
    public static final int KIND_PARTICLE = 21;
    public static final int KIND_SPARCLE = 22;
    public static final int KIND_COIN_ANIM = 20;
    public static final int KIND_FIREBALL = 25;

    public static final int KIND_UNDEF = -42;

	
    public static SpriteContext spriteContext;
    public byte kind = 120; //SK: undefined, if this is shown!
    
    public float xOld, yOld, x, y, xa, ya, lastAccurateX, lastAccurateY;
    public int mapX, mapY;
      
    public int layer = 1;
    public SpriteTemplate spriteTemplate;
    public LevelScene world;

    // if we've never seen the enemy jump/fall, we don't know how fast it'll fall. 
    public boolean unknownYA = true; 
    
    public void move()
    {
        x+=xa;
        y+=ya;
    }
    
    @Override
	public Object clone() throws CloneNotSupportedException
    {
    	Sprite s = (Sprite) super.clone();
    	if (spriteTemplate != null)
    		s.spriteTemplate = (SpriteTemplate) this.spriteTemplate.clone();
    	return s;
    }
    
    public final void tick()
    {
        xOld = x;
        yOld = y;
        mapX = (int)(xOld / 16);
        mapY = (int)(yOld / 16);
        move();
    }

    public final void tickNoMove()
    {
        xOld = x;
        yOld = y;
    }

    public void collideCheck()
    {
    }

    public void bumpCheck(int xTile, int yTile)
    {
    }

    public boolean shellCollideCheck(Shell shell)
    {
        return false;
    }

    public void release(Mario mario)
    {
    }

    public boolean fireballCollideCheck(Fireball fireball)
    {
        return false;
    }
}