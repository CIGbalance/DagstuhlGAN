package ch.idsia.mario.engine;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


public class Art
{
    public static Image[][] mario;
    public static Image[][] smallMario;
    public static Image[][] fireMario;
    public static Image[][] enemies;
    public static Image[][] items;
    public static Image[][] level;
    public static Image[][] particles;
    public static Image[][] font;
    public static Image[][] bg;
    public static Image[][] map;
    public static Image[][] endScene;
    public static Image[][] gameOver;
    public static Image logo;
    public static Image titleScreen;
    final static String curDir = System.getProperty("user.dir");
    final static String img = curDir + "/../img/";

    public static void init(GraphicsConfiguration gc)
    {
        try
        {
//            System.out.println("Image Directory: " + img);
//            System.out.println(curDir);
            mario = cutImage(gc, "mariosheet.png", 32, 32);
            smallMario = cutImage(gc, "smallmariosheet.png", 16, 16);
            fireMario = cutImage(gc, "firemariosheet.png", 32, 32);
            enemies = cutImage(gc, "enemysheet.png", 16, 32);
            items = cutImage(gc, "itemsheet.png", 16, 16);
            level = cutImage(gc, "mapsheet.png", 16, 16);
            map = cutImage(gc, "worldmap.png", 16, 16);
            particles = cutImage(gc, "particlesheet.png", 8, 8);
            bg = cutImage(gc, "bgsheet.png", 32, 32);
            logo = getImage(gc, "logo.gif");
            titleScreen = getImage(gc, "title.gif");
            font = cutImage(gc, "font.gif", 8, 8);
            endScene = cutImage(gc, "endscene.gif", 96, 96);
            gameOver = cutImage(gc, "gameovergost.gif", 96, 64);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static Image getImage(GraphicsConfiguration gc, String imageName) throws IOException
    {
        // System.out.println("trying to get " + imageName);
        BufferedImage source = null;
        try {
            source = ImageIO.read(Art.class.getResourceAsStream(imageName));
            // System.out.println("source: " + source);
        }
        catch (Exception e) {
            e.printStackTrace ();
        }

        if (source == null) {
            // System.out.println("Could not read image through getResourceAsStream");
            imageName = img + imageName;
            File file = new File(imageName);
            // System.out.println("File: " + file + ", exists " + file.exists() + ", length " + file.length ());
            source = ImageIO.read(file);
            // System.out.println("source: " + source);
        }
        if (source == null) { // still!!
            // System.out.println("Could not read image through ImageIO either!");
            File file = new File(imageName);
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            String suffix = imageName.substring(imageName.length() - 3, imageName.length());
            // System.out.println("suffix: " + suffix);
            ImageReader reader = ImageIO.getImageReadersBySuffix(suffix).next ();
            // System.out.println("Let's try this reader: " + reader);
            reader.setInput(iis, true);
            source = reader.read (0);
            // System.out.println("source: " + source);
        }
        //System.out.println("gc: " + gc);
        Image image = gc.createCompatibleImage(source.getWidth(), source.getHeight(), Transparency.BITMASK);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return image;
    }

    private static Image[][] cutImage(GraphicsConfiguration gc, String imageName, int xSize, int ySize) throws IOException
    {
        Image source = getImage(gc, imageName);
        Image[][] images = new Image[source.getWidth(null) / xSize][source.getHeight(null) / ySize];
        for (int x = 0; x < source.getWidth(null) / xSize; x++)
        {
            for (int y = 0; y < source.getHeight(null) / ySize; y++)
            {
                Image image = gc.createCompatibleImage(xSize, ySize, Transparency.BITMASK);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setComposite(AlphaComposite.Src);
                g.drawImage(source, -x * xSize, -y * ySize, null);
                g.dispose();
                images[x][y] = image;
            }
        }

        return images;
    }

}