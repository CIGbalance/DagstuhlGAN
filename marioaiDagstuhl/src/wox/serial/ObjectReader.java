package wox.serial;

import org.jdom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: sml
 * Date: 06-Aug-2004
 * Time: 09:22:41
 * To change this template use Options | File Templates.
 */
public interface ObjectReader extends Serial {
    public Object read(Element xob);
}
