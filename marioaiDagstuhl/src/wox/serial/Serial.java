package wox.serial;

import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 05-Aug-2004
 * Time: 14:48:57
 * To change this template use Options | File Templates.
 */
public interface Serial {
    // use string constants to enforce consistency
    // between readers and writers
    public static final String OBJECT = "object";
    public static final String FIELD = "field";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String VALUE = "value";
    public static final String ARRAY = "array";
    public static final String LENGTH = "length";
    public static final String ID = "id";
    public static final String IDREF = "idref";

    // next is used to disambiguate shadowed fields
    public static final String DECLARED = "declaredClass";


    public static final Class[] primitiveArrays =
            new Class[]{
                int[].class,
                boolean[].class,
                byte[].class,
                short[].class,
                long[].class,
                char[].class,
                float[].class,
                double[].class
            };

    // now declare the wrapper classes for each primitive object type
    // note that this order must correspond to the order in primitiveArrays

    // there may be a better way of doing this that does not involve
    // wrapper objects (e.g. Integer is the wrapper of int), but I've
    // yet to find it
    // note that the creation of wrapper objects is a significant
    // overhead
    // example: reading an array of 1 million int (all zeros) takes
    // about 900ms using reflection, versus 350ms hard-coded
    public static final Class[] primitiveWrappers =
            new Class[]{
                Integer.class,
                Boolean.class,
                Byte.class,
                Short.class,
                Long.class,
                Character.class,
                Float.class,
                Double.class
            };

    public static final Class[] primitives =
            new Class[]{
                int.class,
                boolean.class,
                byte.class,
                short.class,
                long.class,
                char.class,
                float.class,
                double.class
            };
}
