package org.codehaus.stax2.ri.dom;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.InputSource;

import org.codehaus.stax2.XMLStreamReader2;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link DOMWrappingReader#getTextCharacters(int, char[], int, int)},
 * which is meant to be called repeatedly with a growing source offset to
 * read text in segments.
 *<p>
 * Covers the requested length, which used to be limited to the full text
 * length instead of what is remaining after the source offset (so reading the
 * last segment failed unless all of it fit in the first).
 */
class DOMWrappingReaderTest
{
    private final static String TEXT = "abcdefghij";

    // Shorter, dividing evenly or not, as long as and longer than TEXT
    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 5, 10, 16 })
    void readInSegments(int chunk) throws Exception
    {
        XMLStreamReader2 sr = readerAtText("<root>"+TEXT+"</root>");
        char[] buf = new char[chunk];
        StringBuilder sb = new StringBuilder();
        int count;
        // as per StAX javadocs: no more text once fewer than requested are copied
        do {
            count = sr.getTextCharacters(sb.length(), buf, 0, chunk);
            sb.append(buf, 0, count);
        } while (count == chunk);
        assertEquals(TEXT, sb.toString());
    }

    @Test
    void copyToTargetOffset() throws Exception
    {
        XMLStreamReader2 sr = readerAtText("<root>"+TEXT+"</root>");
        char[] buf = "..........".toCharArray();
        // only "hij" remaining of the 5 requested
        assertEquals(3, sr.getTextCharacters(7, buf, 2, 5));
        assertEquals("..hij.....", new String(buf));
    }

    /*
    ///////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////
     */

    private static XMLStreamReader2 readerAtText(String xml) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        XMLStreamReader2 sr = new DOMReader(new DOMSource(dbf.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))));
        assertEquals(XMLStreamConstants.START_ELEMENT, sr.next());
        assertEquals(XMLStreamConstants.CHARACTERS, sr.next());
        return sr;
    }

    /**
     * Minimal concrete reader; the abstract methods are not needed here.
     */
    static class DOMReader extends DOMWrappingReader
    {
        DOMReader(DOMSource src) throws XMLStreamException {
            super(src, true, false);
        }

        @Override
        protected void throwStreamException(String msg, Location loc) throws XMLStreamException {
            throw new XMLStreamException(msg, loc);
        }

        @Override
        public Object getProperty(String name) { return null; }

        @Override
        public boolean isPropertySupported(String name) { return false; }

        @Override
        public boolean setProperty(String name, Object value) { return false; }
    }
}
