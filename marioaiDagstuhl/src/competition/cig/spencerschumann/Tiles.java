package competition.cig.spencerschumann;

import ch.idsia.mario.environments.Environment;
import java.util.ArrayList;

/**
 *
 * @author Spencer Schumann
 */
public class Tiles {

    public static final byte EMPTY = 0;
    public static final byte WALL = 1;
    public static final byte LEDGE = 2;
    public static final byte PIPE = 3;
    public static final byte CANNON = 4;
    public static final byte BRICK = 5;
    public static final byte QUESTION = 6;
    public static final byte COIN = 7;
    public static final byte SECRET_COIN = 10;
    public static final byte SECRET_POWERUP = 20;
    public static final byte UNKNOWN = -1;

    public static boolean isWall(byte tile) {
        switch (tile) {
            case WALL:
            case BRICK:
            case BRICK + SECRET_COIN:
            case BRICK + SECRET_POWERUP:
            case PIPE:
            case CANNON:
            case QUESTION:
            case QUESTION + SECRET_COIN:
            case QUESTION + SECRET_POWERUP:
                return true;
            default:
                return false;
        }
    }

    private class Column {

        public int startRow = 0;
        byte[] tiles = null;

        public void setTile(int y, byte tile) {
            if (tiles == null) {
                tiles = new byte[1];
                tiles[0] = tile;
                startRow = y;
            } else {
                if (startRow > y) {
                    int expansion = startRow - y;
                    byte[] newTiles = new byte[tiles.length + expansion];
                    System.arraycopy(tiles, 0, newTiles, expansion, tiles.length);
                    int i;
                    for (i = 0; i < startRow; i++) {
                        newTiles[i] = UNKNOWN;
                    }
                    tiles = newTiles;
                    startRow = y;
                } else if (y >= startRow + tiles.length) {
                    int expansion = y - startRow - tiles.length + 1;
                    byte[] newTiles = new byte[tiles.length + expansion];
                    System.arraycopy(tiles, 0, newTiles, 0, tiles.length);
                    int i;
                    for (i = tiles.length; i < newTiles.length; i++) {
                        newTiles[i] = UNKNOWN;
                    }
                    tiles = newTiles;
                }
                tiles[y - startRow] = tile;
            }
        }

        public byte getTile(int y) {
            if (y < startRow || y >= startRow + tiles.length) {
                return UNKNOWN;
            } else {
                return tiles[y - startRow];
            }
        }
    }
    ArrayList<Column> columns;

    public Tiles() {
        columns = new ArrayList<Column>();
    }

    private void setTile(int x, int y, byte tile) {
        if (x < 0) {
            return;
        }
        while (x >= columns.size()) {
            columns.add(null);
        }
        Column c = columns.get(x);
        if (c == null) {
            c = new Column();
            columns.set(x, c);
        }
        c.setTile(y, tile);
    }

    public byte getTile(int x, int y) {
        if (x < 0) {
            return EMPTY;
        } else if (x >= columns.size()) {
            return UNKNOWN;
        }
        Column c = columns.get(x);
        if (c == null) {
            return UNKNOWN;
        } else {
            return c.getTile(y);
        }
    }

    public byte[][] getScene(int x, int y, int width, int height) {
        byte[][] scene = new byte[height][width];
        int row, col;
        for (row = 0; row < height; row++) {
            for (col = 0; col < width; col++) {
                scene[row][col] = getTile(col + x, row + y);
            }
        }
        return scene;
    }

    public void addObservation(Environment observation) {
        byte[][] scene = observation.getLevelSceneObservation();
        //byte[][] complete = observation.getCompleteObservation();

        float[] marioPos = observation.getMarioFloatPos();
        int offsetX = (int) (marioPos[0] / 16.0f);
        int offsetY = (int) (marioPos[1] / 16.0f);
        offsetX -= observation.HalfObsWidth;
        offsetY -= observation.HalfObsHeight;

        int x, y;
        for (y = 0; y < scene.length; y++) {
            for (x = 0; x < scene[y].length; x++) {
                byte tile = scene[y][x];
                switch (tile) {
                    case 0: // nothing
                            /*if (complete[y][x] == 14) { // Gun turret
                        tile = CANNON;
                        break;
                        }*/
                        tile = EMPTY;
                        break;
                    case 1: // mario
                        tile = UNKNOWN;
                        break;
                    case -10: // Wall
                    case -12: // Stone
                    case 46:  // Top of cannon support
                        tile = WALL;
                        break;
                    case 14:
                        tile = CANNON;
                        break;
                    case -11: // Ledge
                        tile = LEDGE;
                        break;
                    case 20: // Pipe
                        tile = PIPE;
                        break;
                    case 16: // Brick
                        tile = BRICK;
                        break;
                    case 17: // Brick with coin
                        tile = BRICK + SECRET_COIN;
                        break;
                    case 18: // Brick with power up
                        tile = BRICK + SECRET_POWERUP;
                        break;
                    case 21: // Question with coin
                        tile = QUESTION + SECRET_COIN;
                        break;
                    case 22: // Question with power up
                        tile = QUESTION + SECRET_POWERUP;
                        break;
                    case 34: // coin
                        tile = COIN;
                        break;
                    default:
                        tile = UNKNOWN;
                        break;
                }
                if (tile != UNKNOWN) {
                    setTile(x + offsetX, y + offsetY, tile);
                }
            }
        }
    }
}
