package ch.idsia.mario.engine.sprites;

import ch.idsia.mario.engine.Art;
import ch.idsia.mario.engine.LevelScene;


public class CoinAnim extends Sprite implements Cloneable
{
    private int life = 16;

    public CoinAnim(LevelScene world, int xTile, int yTile)
    {
        kind = KIND_COIN_ANIM;
        sheet = Art.level;
        wPic = hPic = 16;

        x = xTile * 16;
        y = yTile * 16 - 16;
        xa = 0;
        ya = -6f;
        xPic = 0;
        yPic = 2;
        this.world = world;
    }

    public void move()
    {
        if (life-- < 0)
        {
            this.world.removeSprite(this);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    this.world.addSprite(new Sparkle(this.world, (int)x + xx * 8 + (int) (Math.random() * 8), (int)y + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
        }

        xPic = life & 3;

        x += xa;
        y += ya;
        ya += 1;
    }
}