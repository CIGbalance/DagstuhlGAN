package wox.serial;

import org.jdom.Element;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

// import crjaim.EncodeBase64;

/**
 * Created by Simon M. Lucas
 * Base 64 encoding of byte arrays by Carlos R. Jaimez Gonzalez
 * User: sml
 * Date: 06-Aug-2004
 * Time: 09:24:16
 *
 */
public class SimpleReader implements ObjectReader {

    HashMap map;

    public SimpleReader() {
        map = new HashMap();
    }

    public Object read(Element xob) {
        // there are several possibilities - see how we handle them
        if (empty(xob)) {
            return null;
        } else if (reference(xob)) {
            return map.get(xob.getAttributeValue(IDREF));
        }
        // at this point we must be reading an actual Object
        // so we need to store it in
        // there are two ways we can handle objects referred to
        // by idrefs
        // the  simplest is to put all objects in an ArrayList or
        // HashMap, and then get retrieve the objects from the collection
        Object ob = null;
        String id = xob.getAttributeValue(ID);
        if (primitiveArray(xob)) {
            ob = readPrimitiveArray(xob, id);
        } else if (array(xob)) {
            ob = readObjectArray(xob, id);
        } else if (Util.stringable(xob.getAttributeValue(TYPE))) {
            ob = readStringObject(xob, id);
        } else { // assume we have a normal object with some fields to set
            ob = readObject(xob, id);
        }
        // now place the object in a collection for later reference
        return ob;
    }

    public boolean empty(Element xob) {
        // empty only if it has no attributes and no content
        // System.out.println("Empty test on: " + xob);
        return !xob.getAttributes().iterator().hasNext() &&
                !xob.getContent().iterator().hasNext();
    }

    public boolean reference(Element xob) {
        boolean ret = xob.getAttribute(IDREF) != null;
        // System.out.println("Reference? : " + ret);
        return ret;
    }

    public boolean primitiveArray(Element xob) {
        if (!xob.getName().equals(ARRAY)) {
            return false;
        }
        // at this point we must have an array - but is it
        // primitive?  - iterate through all the primitive array types to see
        String arrayType = xob.getAttributeValue(TYPE);
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(arrayType)) {
                return true;
            }
        }
        return false;
    }

    public boolean array(Element xob) {
        // this actually returns true for any array
        return xob.getName().equals(ARRAY);
    }

    // now on to the reading methods
    public Object readPrimitiveArray(Element xob, Object id) {
        try {
            Class type = getPrimitiveType(xob.getAttributeValue(TYPE));
            Class wrapperType = getWrapperType(type);
            // get the constructor for the wrapper class - this will
            // always take a String argument
            // System.out.println(type + " : " + wrapperType);
            Constructor cons = wrapperType.getDeclaredConstructor(new Class[]{String.class});
            Object[] args = new Object[1];
            int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
            Object array = Array.newInstance(type, len);
            map.put(id, array);

            // Array.   //why int primitive array must be standalone
            if (type.equals(int.class)) {
                Object intArray = readIntArray((int[]) array, xob);
                return intArray;
            }

            //code added by Carlos Jaimez (29th April 2005)
            if (type.equals(byte.class)) {
                Object byteArray = readByteArray((byte[]) array, xob);
                return byteArray;
            }
            //----------------------------------


            StringTokenizer st = new StringTokenizer(xob.getText());
            int index = 0;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                // will this be tedious?  need to get the right
                // type from this now
                args[0] = s;
                Object value = cons.newInstance(args);
                // System.out.println(index + " : " + value);
                Array.set(array, index++, value);
                // Array.set
            }
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    //-----------------------------------------------------------------------------
    public Class getPrimitiveType(String name) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(name)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitives[i];
            }
        }
        return null;
    }


    public Class getWrapperType(Class type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitiveWrappers[i];
            }
        }
        return null;
    }


    public Class getWrapperType(String type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitiveWrappers[i];
            }
        }
        return null;
    }


    public Object readIntArray(int[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            a[index++] = Integer.parseInt(s);
            // Array.set
        }
        // System.out.println("Read int array: " + index);
        return a;
    }


    //-----------------------------------------------------------------------------
    /**
     * Purpose: To constuct the byte array based on the a and xob
     * Befor constructs back the byte array, it has to be decoded
     * Carlos Jaimez (29 april 2005)
     * @param a
     * @param xob
     * @return : int Array
     */
    public Object readByteArray(byte[] a, Element xob) {
        String strByte = xob.getText();
        //System.out.println("a.length: " + a.length);
        a = strByte.getBytes();
        //System.out.println("a.length after getting the real bytes: " + a.length);
        //decode the source byte[] array
        byte[] decodedArray = EncodeBase64.decode(a);
        //return the real array
        return decodedArray;
    }


    //-----------------------------------------------------------------------------
    public Object readObjectArray(Element xob, Object id) {
        // to read an object array we first determine the
        // class of the array - leave this to a separate method
        // since there seems to be no automatic way to get the
        // type of the array

        try {
            String arrayTypeName = xob.getAttributeValue(TYPE);
            int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
            Class componentType = getObjectArrayComponentType(arrayTypeName);
            Object array = Array.newInstance(componentType, len);
            map.put(id, array);
            // now fill in the array
            List children = xob.getChildren();
            int index = 0;
            for (Iterator i = children.iterator(); i.hasNext();) {
                Object childArray = read((Element) i.next());
                // System.out.println(index + " child: " + childArray);
                Array.set(array, index++, childArray);
            }
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Class getObjectArrayComponentType(String arrayTypeName) throws Exception {
        // System.out.println("Getting class for: " + arrayTypeName);
        return Class.forName(arrayTypeName);
//        String componentTypeName = arrayTypeName.substring(1);
//        System.out.println("Component type name: " + componentTypeName);
//        Class componentType = Class.forName(componentTypeName);
//        System.out.println("Component type: " + componentType);
//        return componentType;
    }

    public Object readStringObject(Element xob, Object id) {
        try {
            Class type = Class.forName(xob.getAttributeValue(TYPE));
            // System.out.println("Declared: ");
            // print(type.getDeclaredConstructors());
            // System.out.println("All?: ");
            // print(type.getConstructors());
            // System.out.println("Type: " + type);
            // System.out.println("Text: " + xob.getText());
            // AccessController.doPrivileged(null);
            // PrivilegedAction action
            if (type.equals(Class.class)) {
                // handle class objects differently
                return Class.forName(xob.getText());
            } else {
                Class[] st = {String.class};
                Constructor cons = type.getDeclaredConstructor(st);
                // System.out.println("String Constructor: " + cons);
                Object ob = makeObject(cons, new String[]{xob.getText()}, id);
                return ob;
            }
        } catch (Exception e) {

            e.printStackTrace();
            // System.out.println("While trying: " type );
            return null;
            // throw new RuntimeException(e);
        }

    }

    public Object readObject(Element xob, Object id) {
        // to read in an object we iterate over all the field elements
        // setting the corresponding field in the Object
        // first we construct an object of the correct class
        // this class may not have a public default constructor,
        // but will have a private default constructor - so we get
        // this back
        try {
            // System.out.println("Type: " + xob.getAttributeValue(TYPE));
            // System.out.println("Element: " + xob.getName());
            Class type = Class.forName(xob.getAttributeValue(TYPE));
            // System.out.println("Declared: ");
            // print(type.getDeclaredConstructors());
            // System.out.println("All?: ");
            // print(type.getConstructors());
            // AccessController.doPrivileged(null);
            // PrivilegedAction action

            // put the forced call in here!!!
            // Constructor cons = type.getDeclaredConstructor(new Class[0]);
            Constructor cons = Util.forceDefaultConstructor(type);
            cons.setAccessible(true);
            // System.out.println("Default Constructor: " + cons);
            Object ob = makeObject(cons, new Object[0], id);

            // now go through setting all the fields
            setFields(ob, xob);
            return ob;
        } catch (Exception e) {
            System.out.println(e);
            // e.printStackTrace();
            return null;
            // throw new RuntimeException(e);
        }
    }

    public void setFields(Object ob, Element xob) {
        // iterate over the set of fields
        Class type = ob.getClass();
        for (Iterator i = xob.getChildren().iterator(); i.hasNext();) {
            Element fe = (Element) i.next();
            String name = fe.getAttributeValue(NAME);
            // ignore shadowing for now...
            String declaredType = fe.getAttributeValue(DECLARED);
            try {
                Class declaringType;
                if (declaredType != null) {
                    declaringType = Class.forName(declaredType);
                } else {
                    declaringType = type;
                }
                // System.out.println("Field name: " + name + " belonging to: " + declaringType);
                Field field = getField(declaringType, name);
                field.setAccessible(true);
                Object value = null;
                if (Util.primitive(field.getType())) {
                    // System.out.println("Primitive");
                    value = makeWrapper(field.getType(), fe.getAttributeValue(VALUE));
                } else {
                    // must be an object with only one child
                    // System.out.println("Object");
                    Element child = (Element) fe.getChildren().iterator().next();
                    value = read(child);
                }
                // System.out.println("  Setting: " + field);
                // System.out.println("  of: " + ob);
                // System.out.println("  to: " + value );
                field.set(ob, value);
                // still need to retrieve the value of this object!!!
                // how to do that?
                // well - either the Object is stringable (e.g. String or
                // so at this stagw we either determine the value of the
                // field directly, or otherwise
            } catch (Exception e) {
                // e.printStackTrace();
                // throw new RuntimeException(e);
                System.out.println(name + " : " + e);

            }
        }

    }

    // this method not only makes the object, but also places
    // it in the HashMap of object references
    public Object makeObject(Constructor cons, Object[] args, Object key) throws Exception {
        cons.setAccessible(true);
        Object value = cons.newInstance(args);
        map.put(key, value);
        return value;
    }

    public Object makeWrapper(Class type, String value) throws Exception {
        Class wrapperType = getWrapperType(type);
        // System.out.println("wrapperType: " + wrapperType + " : " + type + " : " + (type == int.class));
        Constructor cons = wrapperType.getDeclaredConstructor(new Class[]{String.class});
        return cons.newInstance(new Object[]{value});
    }

    public Field getField(Class type, String name) throws Exception {
        // System.out.println(type + " :::::: " + name);
        if (type == null) {
            return null;
        }
        try {
            // throws an exception if there's no such field
            return type.getDeclaredField(name);
        } catch (Exception e) {
            // try the superclass instead
            return getField(type.getSuperclass(), name);
        }
    }

//    public Constructor getConstructor(Class type) {
//        Constructor[] cons = type.getDeclaredConstructors();
//        return null;
//    }

    public void print(Constructor[] cons) {
        for (int i = 0; i < cons.length; i++) {
            System.out.println(i + " : " + cons[i]);
        }
    }


    public Class getComponentType(String type) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (primitiveArrays[i].getName().equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitives[i];
            }
        }
        return null;
    }

    public Class getArrayType(String type) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (primitiveArrays[i].getName().equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitiveArrays[i];
            }
        }
        return null;
    }
}
