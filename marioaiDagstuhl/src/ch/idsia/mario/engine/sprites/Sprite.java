package ch.idsia.mario.engine.sprites;

import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.level.SpriteTemplate;
import ch.idsia.mario.engine.LevelScene;

import java.awt.*;

public class Sprite implements Cloneable
{
    public static final int KIND_NONE = 0;
    public static final int KIND_MARIO = -31;
    public static final int KIND_GOOMBA = 2;
    public static final int KIND_GOOMBA_WINGED = 3;
    public static final int KIND_RED_KOOPA = 4;
    public static final int KIND_RED_KOOPA_WINGED = 5;
    public static final int KIND_GREEN_KOOPA = 6;
    public static final int KIND_GREEN_KOOPA_WINGED = 7;
    public static final int KIND_BULLET_BILL = 8;
    public static final int KIND_SPIKY = 9;
    public static final int KIND_SPIKY_WINGED = 10;
//    public static final int KIND_ENEMY_FLOWER = 11;
    public static final int KIND_ENEMY_FLOWER = 12;
    public static final int KIND_SHELL = 13;
    public static final int KIND_MUSHROOM = 14;
    public static final int KIND_FIRE_FLOWER = 15;    
    public static final int KIND_PARTICLE = 21;
    public static final int KIND_SPARCLE = 22;
    public static final int KIND_COIN_ANIM = 20;
    public static final int KIND_FIREBALL = 25;

    public static final int KIND_UNDEF = -42;

    //public SpriteContext spriteContext;
    public byte kind = KIND_UNDEF;
    
    public float xOld, yOld, x, y, xa, ya;
    public int mapX, mapY;
    
    public int xPic, yPic;
    public int wPic = 32;
    public int hPic = 32;
    public int xPicO, yPicO;
    public boolean xFlipPic = false;
    public boolean yFlipPic = false;
    public Image[][] sheet;
    public boolean visible = true;
    
    public int layer = 1;

    public SpriteTemplate spriteTemplate;
    public LevelScene world;

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
        
    public void render(Graphics og, float alpha)
    {
        if (!visible) return;
        
//        int xPixel = (int)(xOld+(x-xOld)*alpha)-xPicO;
//        int yPixel = (int)(yOld+(y-yOld)*alpha)-yPicO;

        int xPixel = (int)x-xPicO;
        int yPixel = (int)y-yPicO;


        og.drawImage(sheet[xPic][yPic], xPixel+(xFlipPic?wPic:0), yPixel+(yFlipPic?hPic:0), xFlipPic?-wPic:wPic, yFlipPic?-hPic:hPic, null);
        if (GlobalOptions.Labels)
            og.drawString("" + xPixel + "," + yPixel, xPixel, yPixel);
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

//    public float getX(float alpha)
//    {
//        return (xOld+(x-xOld)*alpha)-xPicO;
//    }
//
//    public float getY(float alpha)
//    {
//        return (yOld+(y-yOld)*alpha)-yPicO;
//    }

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