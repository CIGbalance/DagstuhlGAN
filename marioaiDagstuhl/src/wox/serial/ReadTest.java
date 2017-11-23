package wox.serial;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 06-Aug-2004
 * Time: 10:00:32
 * To change this template use Options | File Templates.
 */
public class ReadTest {
    public static void main(String[] args) throws Exception {
        ObjectReader reader = new SimpleReader();
        ObjectWriter writer = new SimpleWriter();

        int n = 1000000;
        int[] a1 = new int[n];
        // int[][][] a2 = new int[3][3][3];
        double[][][] a2 = new double[3][3][3];
        TestObject to = new TestObject(2);
        to.inner = new TestObject.Inner(1024);

//        ObjectOutputStream oos = new ObjectOutputStream(System.out);
//        oos.writeObject( to );
//        ObjectInputStream ois = new ObjectInputStream(null);
//        ois.readObject();

        Element xob = writer.write( to );
        XMLOutputter out = new XMLOutputter();
        out.output( xob, System.out );

        System.out.println("");
        Object ob = reader.read( xob );

        System.out.println("Read: " + ob.getClass() );

        xob = new SimpleWriter().write(ob);
        out.output( xob, System.out );
//        for (int i=0; i<a.length; i++) {
//            System.out.println(a[i]);
//        }

        System.out.println("Check ref: " + to.alist.get(0) + " = " + to);

    }
}
