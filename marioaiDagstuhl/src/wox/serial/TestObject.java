package wox.serial;

import java.util.ArrayList;

/**
 * User: sml
 * Date: 05-Aug-2004
 * Time: 11:33:26
 */

public class TestObject  {

    public static class Inner {
        int inx;

        public Inner(int inx) {
            this.inx = inx;
        }
    }

    private int x;
    int[] xa = {0, 1};
    int[] xb = xa;
    byte[] ba = {99, 12, (byte) 0xFF};
    TestObject to;
    int[][] xxx = {{0,1},{2}};
    int[] ia = {1, 2, 3};
    double[] dd = {4, 5, 6};
    Object[] objects;
    ArrayList alist;
    // check we can handle null objects
    Object myNull = null;
    Inner inner;

    // check we can handle Class variables
    // Class myClass = TestObject.class;

    public TestObject(int x) {
        this.x = x;
        objects = new Object[]{ this };
        alist = new ArrayList();
        alist.add(this);
        alist.add("Hello");
        alist.add(new Integer(23));
        // alist.set(12, "Twelve");
        inner = new Inner(99);
    }

    public synchronized int inc() {
        return x++;
    }

    // no longer needs any default constructor...
    // private TestObject() {}
}
