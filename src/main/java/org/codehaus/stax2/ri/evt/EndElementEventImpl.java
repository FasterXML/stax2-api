package org.codehaus.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.EmptyIterator;
import org.codehaus.stax2.ri.evt.BaseEventImpl;

public class EndElementEventImpl
    extends BaseEventImpl
    implements EndElement
{
    final protected QName mName;
    final protected ArrayList<Namespace> mNamespaces;

    /**
     * Constructor usually used when reading events from a stream reader.
     */
    public EndElementEventImpl(Location loc, XMLStreamReader r)
    {
        super(loc);
        mName = r.getName();

        // Let's figure out if there are any namespace declarations...
        int nsCount = r.getNamespaceCount();
        if (nsCount == 0) {
            mNamespaces = null;
        } else {
            ArrayList<Namespace> l = new ArrayList<Namespace>(nsCount);
            for (int i = 0; i < nsCount; ++i) {
                l.add(NamespaceEventImpl.constructNamespace
                      (loc, r.getNamespacePrefix(i), r.getNamespaceURI(i)));
            }
            mNamespaces = l;
        }
    }

    /**
     * Constructor used by the event factory.
     */
    public EndElementEventImpl(Location loc, QName name, Iterator<Namespace> namespaces)
    {
        super(loc);
        mName = name;
        if (namespaces == null || !namespaces.hasNext()) {
            mNamespaces = null;
        } else {
            ArrayList<Namespace> l = new ArrayList<Namespace>();
            while (namespaces.hasNext()) {
                /* Let's do typecast here, to catch any cast errors early;
                 * not strictly required, but helps in preventing later
                 * problems
                 */
                l.add((Namespace) namespaces.next());
            }
            mNamespaces = l;
        }
    }

    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    public QName getName() {
        return mName;
    }

    public Iterator<Namespace> getNamespaces() 
    {
        if (mNamespaces == null) {
            return EmptyIterator.getInstance();
        }
        return mNamespaces.iterator();
    }

    /*
    /**********************************************************************
    /* Implementation of abstract base methods, overrides
    /**********************************************************************
     */

    @Override
    public EndElement asEndElement() { // overriden to save a cast
        return this;
    }

    @Override
    public int getEventType() {
        return END_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w)
        throws XMLStreamException
    {
        try {
            w.write("</");
            String prefix = mName.getPrefix();
            if (prefix != null && prefix.length() > 0) {
                w.write(prefix);
                w.write(':');
            }
            w.write(mName.getLocalPart());
            w.write('>');
        } catch (IOException ie) {
            throwFromIOE(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 w) throws XMLStreamException {
        w.writeEndElement();
    }

    /*
    /**********************************************************************
    /* Standard method impl
    /**********************************************************************
     */

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;

        if (!(o instanceof EndElement)) return false;

        EndElement other = (EndElement) o;
        // First of all, names must match obviously
        if (getName().equals(other.getName())) {
            /* But then, what about namespaces etc? For now,
             * let's actually not consider namespaces: chances
             * are corresponding START_ELEMENT must have matched
             * well enough.
             */
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }
}
