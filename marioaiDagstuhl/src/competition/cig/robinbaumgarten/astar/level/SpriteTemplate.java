package competition.cig.robinbaumgarten.astar.level;

import competition.cig.robinbaumgarten.astar.sprites.Sprite;

public class SpriteTemplate implements Cloneable
{
    public int lastVisibleTick = -1;
    public Sprite sprite;
    public boolean isDead = false;

    public int getType() {
        return type;
    }

    private int type;
    
    @Override
	public Object clone() throws CloneNotSupportedException
    {
    	return super.clone();
    	
    }
    public SpriteTemplate(int type, boolean winged)
    {
        this.type = type;
    }

}