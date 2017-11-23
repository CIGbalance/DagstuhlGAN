package wox.serial;

import java.util.ArrayList;
import java.util.AbstractList;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 08-Aug-2004
 * Time: 23:22:56
 * To change this template use File | Settings | File Templates.
 */
public class ArrayListTest {
    public static void main(String[] args) throws Exception {
        ArrayList al = new ArrayList();
        al.add("Hello");
        al.add("Hello");
        al.add("Hello");

        Field[] fields = SimpleWriter.getFields(ArrayList.class);
        for (int i=0; i<fields.length; i++) {
            System.out.println(i + " : " + fields[i]);
            try {
                fields[i].setAccessible(true);
                Object val = fields[i].get(al);
                fields[i].set(al,val);
                System.out.println("Set val: " + val);

            }   catch(Exception e) {
                System.out.println(e);
                // e.printStackTrace();
            }
        }
        Field field = java.util.AbstractList.class.getDeclaredField("modCount");
        field.setAccessible(true);
        System.out.println("Field: " + field);
        // field.set(al, new Integer(3));
        field = ArrayList.class.getField("size");
        field.setAccessible(true);
        System.out.println("Field: " + field);
        field.set(al, new Integer(10));
    }
}
