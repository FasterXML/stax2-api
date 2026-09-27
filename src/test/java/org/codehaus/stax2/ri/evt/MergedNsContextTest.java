package org.codehaus.stax2.ri.evt;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.stream.events.Namespace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MergedNsContext}, mostly regarding
 * [stax2-api#47]: {@code getPrefixes()} returned null.
 */
public class MergedNsContextTest
{
    @Test
    public void testGetPrefixes()
    {
        List<Namespace> ns = new ArrayList<>();
        ns.add(NamespaceEventImpl.constructNamespace(null, "a", "urn:test"));
        ns.add(NamespaceEventImpl.constructNamespace(null, "b", "urn:test"));
        ns.add(NamespaceEventImpl.constructNamespace(null, "c", "urn:other"));
        MergedNsContext ctxt = MergedNsContext.construct(null, ns);

        assertEquals(Arrays.asList("a", "b"), toList(ctxt.getPrefixes("urn:test")));
        assertEquals(Arrays.asList("c"), toList(ctxt.getPrefixes("urn:other")));
        // No match: must get empty iterator, not null
        Iterator<String> it = ctxt.getPrefixes("urn:missing");
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetPrefixesNoDuplicates()
    {
        MergedNsContext parent = MergedNsContext.construct(null,
                Arrays.<Namespace>asList(NamespaceEventImpl.constructNamespace(null, "p", "urn:test")));
        // Same binding re-declared locally
        MergedNsContext ctxt = MergedNsContext.construct(parent,
                Arrays.<Namespace>asList(NamespaceEventImpl.constructNamespace(null, "p", "urn:test")));

        assertEquals(Arrays.asList("p"), toList(ctxt.getPrefixes("urn:test")));
        // Parent also reports pre-defined "xml" prefix
        assertEquals(Arrays.asList("xml"),
                toList(ctxt.getPrefixes(XMLConstants.XML_NS_URI)));
    }

    private static List<String> toList(Iterator<String> it)
    {
        assertNotNull(it);
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        return result;
    }
}
