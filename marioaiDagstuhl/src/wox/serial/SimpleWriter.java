package wox.serial;

import org.jdom.Element;
import org.jdom.Comment;

import java.lang.reflect.*;
import java.util.*;


// import crjaim.EncodeBase64;

/**
 *  A simple but useful Object to XML serialiser.
 *  By Simon M. Lucas, August 2004
 *  Base 64 modifications by Carlos R. Jaimez Gonzalez
 */

public class SimpleWriter implements ObjectWriter {

    HashMap map;

    int count;
    boolean writePrimitiveTypes = true;
    boolean doStatic = true;

    // not much point writing out final values - at least yet -
    // the reader is not able to set them (though there's probably
    // a hidden way of doing this
    boolean doFinal = false;


    public SimpleWriter() {
        //System.out.println("inside SimpleWriter Constructor...");
        map = new HashMap();
        count = 0;

    }

    public Element write(Object ob) {
        Element el;
        if (ob == null) {
            // a null object is represented by an empty Object tag with no attributes
            return new Element(OBJECT);
        }
        if (map.get(ob) != null) {
            el = new Element(OBJECT);
            el.setAttribute(IDREF, map.get(ob).toString());
            return el;
        }
        // a previously unseen object...
        map.put(ob, new Integer(count++));
        if (Util.stringable(ob)) {
            el = new Element(OBJECT);
            el.setAttribute(TYPE, ob.getClass().getName());
            el.setText(stringify(ob));
        } else if (ob.getClass().isArray()) {
            el = writeArray(ob);
        } else {
            el = new Element(OBJECT);
            el.setAttribute(TYPE, ob.getClass().getName());
            writeFields(ob, el);
        }
        el.setAttribute(ID, map.get(ob).toString());
        return el;
    }

    public Element writeArray(Object ob) {
        if (isPrimitiveArray(ob.getClass())) {
            return writePrimitiveArray(ob);
        } else {
            return writeObjectArray(ob);
        }
    }

    public Element writeObjectArray(Object ob) {
        Element el = new Element(ARRAY);
        // el.setAttribute
        // int[].class.
        // Array.
        el.setAttribute(TYPE, ob.getClass().getComponentType().getName());
        int len = Array.getLength(ob);
        el.setAttribute(LENGTH, "" + len);
        for (int i = 0; i < len; i++) {
            el.addContent(write(Array.get(ob, i)));
        }
        return el;
    }

    public Element writePrimitiveArray(Object ob) {
        Element el = new Element(ARRAY);
        el.setAttribute(TYPE, ob.getClass().getComponentType().getName());
        int len = Array.getLength(ob);
        //CJ this should not be here beacsue the lenght for the byte[] can be different
        //el.setAttribute(LENGTH, "" + len);
        if (ob instanceof byte[]) {
            el.setText(byteArrayString((byte[]) ob, el));
        } else {
            el.setAttribute(LENGTH, "" + len);
            el.setText(arrayString(ob, len));
        }
        return el;
    }

    //method modified to include base64 encoding
    public String byteArrayString(byte[] a, Element e) {
        byte[] target = EncodeBase64.encode(a);
        //set the lenght fro the new encoded array
        e.setAttribute(LENGTH, "" + target.length);
        String strTarget = new String(target);
        return strTarget;
    }

    public String arrayString(Object ob, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(Array.get(ob, i).toString());
        }
        return sb.toString();
    }


    public void writeFields(Object o, Element parent) {
        // get the class of the object
        // get its fields
        // then get the value of each one
        // and call write to put the value in the Element

        Class cl = o.getClass();
        Field[] fields = getFields(cl);
        String name = null;
        for (int i = 0; i < fields.length; i++) {
            if ((doStatic || !Modifier.isStatic(fields[i].getModifiers())) &&
                    (doFinal || !Modifier.isFinal(fields[i].getModifiers())))
                try {
                    fields[i].setAccessible(true);
                    name = fields[i].getName();
                    // need to handle shadowed fields in some way...
                    // one way is to add info about the declaring class
                    // but this will bloat the XML file if we di it for
                    // every field - might be better to just do it for
                    // the shadowed fields
                    // name += "." + fields[i].getDeclaringClass().getName();
                    // fields[i].
                    Object value = fields[i].get(o);
                    Element field = new Element(FIELD);
                    field.setAttribute(NAME, name);
                    if (shadowed(fields, name)) {
                        field.setAttribute(DECLARED, fields[i].getDeclaringClass().getName());
                    }
                    if (fields[i].getType().isPrimitive()) {
                        // this is not always necessary - so it's optional
                        if (writePrimitiveTypes) {
                            field.setAttribute(TYPE, fields[i].getType().getName());
                        }
                        field.setAttribute(VALUE, value.toString());

                    } else {
                        field.addContent(write(value));
                    }
                    parent.addContent(field);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                    // at least comment on what went wrong
                    parent.addContent(new Comment(e.toString()));
                }
        }
    }

    private boolean shadowed(Field[] fields, String fieldName) {
        // count the number of fields with the name fieldName
        // return true if greater than 1
        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            if (fieldName.equals(fields[i].getName())) {
                count++;
            }
        }
        return count > 1;
    }

    public static String stringify(Object ob) {
        if (ob instanceof Class) {
            return ((Class) ob).getName();
        } else {
            return ob.toString();
        }
    }

    public static Field[] getFields(Class c) {
        Vector v = new Vector();
        while (!(c == null)) { // c.equals( Object.class ) ) {
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                // System.out.println(fields[i]);
                v.addElement(fields[i]);
            }
            c = c.getSuperclass();
        }
        Field[] f = new Field[v.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = (Field) v.get(i);
        }
        return f;
    }

    public static Object[] getValues(Object o, Field[] fields) {
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                fields[i].setAccessible(true);
                values[i] = fields[i].get(o);
                System.out.println(fields[i].getName() + "\t " + values[i]);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return values;
    }

    public boolean isPrimitiveArray(Class c) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (c.equals(primitiveArrays[i])) {
                return true;
            }
        }
        return false;
    }


}
