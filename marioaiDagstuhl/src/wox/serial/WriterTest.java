package wox.serial;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 05-Aug-2004
 * Time: 11:32:33
 * To change this template use Options | File Templates.
 */
public class WriterTest {
    public static void main(String[] args) throws Exception {
        ObjectWriter writer = new SimpleWriter();
        TestObject ob = new TestObject(10);
        ob.to = new TestObject(99);
        ob.to.alist.add("Size FOUR!");
        ob.to.to = ob;
        Element el = writer.write( ob );
        XMLOutputter out = new XMLOutputter();
        out.output( el, System.out );
        System.out.println("");
    }
}
