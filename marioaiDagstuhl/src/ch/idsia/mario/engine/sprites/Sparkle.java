package ch.idsia.mario.engine.sprites;

import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.LevelScene;

public class Sparkle extends Sprite
{
    public int life;
    public int xPicStart;
    
    public Sparkle(LevelScene world, int x, int y, float xa, float ya)
    {
        this(world, x, y, xa, ya, (int)(Math.random()*2), 0, 5);
    }

    public Sparkle(LevelScene world, int x, int y, float xa, float ya, int xPic, int yPic, int timeSpan)
    {
        kind = KIND_SPARCLE;
        sheet = Art.particles;
        this.x = x;
        this.y = y;
        this.xa = xa;
        this.ya = ya;
        this.xPic = xPic;
        xPicStart = xPic;
        this.yPic = yPic;
        this.xPicO = 4;
        this.yPicO = 4;
        
        wPic = 8;
        hPic = 8;
        life = 10+(int)(Math.random()*timeSpan);
        this.world = world;
    }

    public void move()
    {
        if (life>10)
            xPic = 7;
        else
            xPic = xPicStart+(10-life)*4/10;
        
        if (life--<0) this.world.removeSprite(this);
        
        x+=xa;
        y+=ya;
    }
}