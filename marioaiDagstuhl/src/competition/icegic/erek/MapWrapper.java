package competition.icegic.erek;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 16, 2009
 * Time: 2:26:06 AM
 * We wrap the arrays so that I can use them in the hash map.
 */
public class MapWrapper {



    private byte[][] map;

    public MapWrapper()
    {
        map = new byte[22][22];
    }

    public byte[][] getMap() {
        return map;
    }

    public void setMap(byte[][] map) {
        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j<map[i].length; j++)
            {
                switch(map[i][j])
                {
                    case(25):
                    case(14):
                    case(15):
                        this.map[i][j] = 0;
                        break;
                    case(-11):
                    case(20):
                    case(16):
                    case(21):
                        this.map[i][j] = -10;
                        break;
                    case(3):
                    case(4):
                    case(5):
                    case(6):
                    case(7):
                    case(8):
                        this.map[i][j] = 2;
                        break;
                    case(10):
                    case(12):
                        this.map[i][j] = 9;
                   default:
                       this.map[i][j] = map[i][j];

                }
            }
        }
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapWrapper other = (MapWrapper) o;
        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j < map[i].length; j++)
            {
                if(map[i][j] != other.getMap()[i][j])
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;

        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j < map[i].length; j++)
            {
                hash += map[i][j]*i+j;
            }
        }
        return hash;
    }
}
