
package wox.serial;

import org.jdom.Element;

public interface ObjectWriter extends Serial {
    public Element write(Object o);
}
