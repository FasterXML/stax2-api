package org.codehaus.stax2.ri.evt;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.events.Namespace;

/**
 * Helper class used to combine an enclosing namespace context with
 * a list of namespace declarations contained, to result in a single
 * namespace context object.
 */
public class MergedNsContext
    implements NamespaceContext
{
    final NamespaceContext _parentCtxt;

    /**
     * List of {@link Namespace} instances.
     */
    final List<Namespace> _namespaces;

    protected MergedNsContext(NamespaceContext parentCtxt, List<Namespace> localNs)
    {
        _parentCtxt = parentCtxt;
        _namespaces = (localNs == null) ? Collections.<Namespace>emptyList() : localNs;
    }

    public static MergedNsContext construct(NamespaceContext parentCtxt, List<Namespace> localNs)
    {
        return new MergedNsContext(parentCtxt, localNs);
    }

    /*
    /**********************************************************************
    /* NamespaceContext API
    /**********************************************************************
     */

    public String getNamespaceURI(String prefix)
    {
        if (prefix == null) {
            throw new IllegalArgumentException("Illegal to pass null prefix");
        }
        for (int i = 0, len = _namespaces.size(); i < len; ++i) {
            Namespace ns = _namespaces.get(i);
            if (prefix.equals(ns.getPrefix())) {
                return ns.getNamespaceURI();
            }
        }
        // Not found; how about from parent?
        if (_parentCtxt != null) {
            String uri = _parentCtxt.getNamespaceURI(prefix);
            if (uri != null) {
                return uri;
            }
        }
        if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
            return XMLConstants.XML_NS_URI;
        }
        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        return null;
    }

    public String getPrefix(String nsURI)
    {
        if (nsURI == null || nsURI.length() == 0) {
            throw new IllegalArgumentException("Illegal to pass null/empty prefix as argument.");
        }
        /* Ok, first: if we can find it from within current namespaces,
         * we are golden:
         */
        for (int i = 0, len = _namespaces.size(); i < len; ++i) {
            Namespace ns = _namespaces.get(i);
            if (nsURI.equals(ns.getNamespaceURI())) {
                return ns.getPrefix();
            }
        }
        // If not, let's first try the easy way:
        if (_parentCtxt != null) {
            String prefix = _parentCtxt.getPrefix(nsURI);
            if (prefix != null) {
                // Must check for masking
                String uri2 = getNamespaceURI(prefix);
                if (uri2.equals(nsURI)) {
                    // No masking, we are good:
                    return prefix;
                }
            }

            // Otherwise, must check other candidates
            Iterator<?> it = _parentCtxt.getPrefixes(nsURI);
            while (it.hasNext()) {
                String p2 = (String) it.next();
                if (!p2.equals(prefix)) { // no point re-checking first prefix
                    // But is it masked?
                    String uri2 = getNamespaceURI(p2);
                    if (uri2.equals(nsURI)) {
                        // No masking, we are good:
                        return p2;
                    }
                }
            }
        }

        // Ok, but how about pre-defined ones (for xml, xmlns)?
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }

        // Nope, none found:
        return null;
    }

    public Iterator<String> getPrefixes(String nsURI)
    {
        if (nsURI == null || nsURI.length() == 0) {
            throw new IllegalArgumentException("Illegal to pass null/empty prefix as argument.");
        }

        // Any local bindings?
        ArrayList<String> l = null;
        for (int i = 0, len = _namespaces.size(); i < len; ++i) {
            Namespace ns = _namespaces.get(i);
            if (nsURI.equals(ns.getNamespaceURI())) {
                l = addToList(l, ns.getPrefix());
            }
        }

        // How about parent?
        if (_parentCtxt != null) {
            Iterator<?> it = _parentCtxt.getPrefixes(nsURI);
            while (it.hasNext()) {
                String p2 = (String) it.next();
                // But is it masked?
                String uri2 = getNamespaceURI(p2);
                if (uri2.equals(nsURI)) {
                    // No masking, we are good:
                    l = addToList(l, p2);
                }
            }
        }

        // Ok, but how about pre-defined ones (for xml, xmlns)?
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            l = addToList(l, XMLConstants.XML_NS_PREFIX);
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            l = addToList(l, XMLConstants.XMLNS_ATTRIBUTE);
        }

        return null;
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected <T> ArrayList<T> addToList(ArrayList<T> l, T value)
    {
        if (l == null) {
            l = new ArrayList<T>();
        }
        l.add(value);
        return l;
    }
}
