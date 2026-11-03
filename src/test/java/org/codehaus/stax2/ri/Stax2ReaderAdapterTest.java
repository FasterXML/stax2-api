package org.codehaus.stax2.ri;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import org.codehaus.stax2.XMLStreamReader2;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for state {@link Stax2ReaderAdapter} keeps on top of the wrapped reader, which has to be updated by every
 * method that advances it.
 *<p>
 * Covers {@code nextTag()}, and leaving a chunked typed array read early, where the wrapped reader is already at
 * END_ELEMENT and the depth must not be decremented a second time.
 */
class Stax2ReaderAdapterTest
{
    // White space, comment and PI between tags, for nextTag() to skip
    private static final String XML = "<root> <a><!-- c --><b/></a>\n<?pi?><c></c> </root>";

    // "<name>" for START_ELEMENT, "</name>" for END_ELEMENT, each with getDepth()
    private static final List<String> EXPECTED = Arrays.asList(
        "<root> 1", "<a> 2", "<b> 3", "</b> 3", "</a> 2", "<c> 2", "</c> 2", "</root> 1");

    // Element "a" is only partially read as an int array before moving on
    private static final String XML_ABANDONED = "<root><a>1 2 3 4 5</a> <b/></root>";

    private static final List<String> EXPECTED_ABANDONED = Arrays.asList(
        "</a> 2", "<b> 2", "</b> 2", "</root> 1");

    @Test
    void depthWithNext() throws Exception
    {
        assertEquals(EXPECTED, walk(false));
    }

    @Test
    void depthWithNextTag() throws Exception
    {
        assertEquals(EXPECTED, walk(true));
    }

    @Test
    void depthAfterAbandonedChunkedReadWithNext() throws Exception
    {
        assertEquals(EXPECTED_ABANDONED, walkAbandoned(false));
    }

    @Test
    void depthAfterAbandonedChunkedReadWithNextTag() throws Exception
    {
        assertEquals(EXPECTED_ABANDONED, walkAbandoned(true));
    }

    /*
    ///////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////
     */

    private static List<String> walk(boolean useNextTag) throws XMLStreamException
    {
        // not wrapIfNecessary(): must be the adapter even if a Stax2 impl is on classpath
        XMLStreamReader2 sr = new Stax2ReaderAdapter(XMLInputFactory.newInstance()
                .createXMLStreamReader(new StringReader(XML)));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < EXPECTED.size(); ) {
            int type = useNextTag ? sr.nextTag() : sr.next();
            if (type == XMLStreamConstants.START_ELEMENT) {
                result.add("<"+sr.getLocalName()+"> "+sr.getDepth());
                ++i;
            } else if (type == XMLStreamConstants.END_ELEMENT) {
                result.add("</"+sr.getLocalName()+"> "+sr.getDepth());
                ++i;
            }
        }
        return result;
    }

    private static List<String> walkAbandoned(boolean useNextTag) throws XMLStreamException
    {
        XMLStreamReader2 sr = new Stax2ReaderAdapter(XMLInputFactory.newInstance()
                .createXMLStreamReader(new StringReader(XML_ABANDONED)));
        sr.nextTag();
        sr.nextTag();
        assertEquals(2, sr.readElementAsIntArray(new int[2], 0, 2));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < EXPECTED_ABANDONED.size(); ) {
            int type = useNextTag ? sr.nextTag() : sr.next();
            if (type == XMLStreamConstants.START_ELEMENT) {
                result.add("<"+sr.getLocalName()+"> "+sr.getDepth());
                ++i;
            } else if (type == XMLStreamConstants.END_ELEMENT) {
                result.add("</"+sr.getLocalName()+"> "+sr.getDepth());
                ++i;
            }
        }
        return result;
    }
}
