package wox.serial;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 07-Aug-2004
 * Time: 00:13:07
 * To change this template use File | Settings | File Templates.
 */
public class ShadowTest {
    public static class X {
        int x = 10;
    }

    public static class Y extends X {
        int x = 20;
        String s = "Hello";
        Integer i = new Integer(7);
    }

    public static void main(String[] args) throws IOException {
        Y ob = new Y();
        ob.x = 55;
        ob.x = 66;

        ObjectWriter writer = new SimpleWriter();
        Element el = writer.write( ob );
        XMLOutputter out = new XMLOutputter();
        out.output( el , System.out );
        Object obj = new SimpleReader().read( el );
        el = new SimpleWriter().write( obj );
        System.out.println("");
        System.out.println("Should be the same as before...");
        out.output( el , System.out );
    }
}
