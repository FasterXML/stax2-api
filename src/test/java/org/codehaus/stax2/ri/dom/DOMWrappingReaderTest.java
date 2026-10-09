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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link DOMWrappingReader#getTextCharacters(int, char[], int, int)},
 * which is meant to be called repeatedly with a growing source offset to
 * read text in segments.
 *<p>
 * Covers the requested length, which used to be limited to the full text
 * length instead of what is remaining after the source offset (so reading the
 * last segment failed unless all of it fit in the first).
 */
public class DOMWrappingReaderTest
{
    private final static String TEXT = "abcdefghij";

    // Shorter, dividing evenly or not, as long as and longer than TEXT
    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 5, 10, 16 })
    public void testReadInSegments(int chunk) throws Exception
    {
        XMLStreamReader2 sr = readerAt("<root>"+TEXT+"</root>", false,
                XMLStreamConstants.CHARACTERS);
        assertEquals(TEXT, readInSegments(sr, chunk));
    }

    // Text and CDATA merged into one event
    @ParameterizedTest
    @ValueSource(ints = { 1, 3, 5, 10, 16 })
    public void testReadCoalescedInSegments(int chunk) throws Exception
    {
        XMLStreamReader2 sr = readerAt("<root>abc<![CDATA[def]]>ghij</root>", true,
                XMLStreamConstants.CHARACTERS);
        assertEquals(TEXT, readInSegments(sr, chunk));
    }

    @Test
    public void testReadOtherTextualEventsInSegments() throws Exception
    {
        assertEquals(TEXT, readInSegments(readerAt("<root><![CDATA["+TEXT+"]]></root>", false,
                XMLStreamConstants.CDATA), 3));
        assertEquals(TEXT, readInSegments(readerAt("<root><!--"+TEXT+"--></root>", false,
                XMLStreamConstants.COMMENT), 3));
    }

    @Test
    public void testCopyToTargetOffset() throws Exception
    {
        XMLStreamReader2 sr = readerAtText();
        char[] buf = "..........".toCharArray();
        // only "hij" remaining of the 5 requested
        assertEquals(3, sr.getTextCharacters(7, buf, 2, 5));
        assertEquals("..hij.....", new String(buf));
    }

    @Test
    public void testSourceStartAtOrPastEnd() throws Exception
    {
        XMLStreamReader2 sr = readerAtText();
        char[] buf = new char[5];
        assertEquals(0, sr.getTextCharacters(10, buf, 0, 5));
        assertEquals(0, sr.getTextCharacters(11, buf, 0, 5));
        assertEquals(0, sr.getTextCharacters(12, buf, 0, 0));
        assertEquals(0, sr.getTextCharacters(Integer.MAX_VALUE, buf, 0, 5));
    }

    @Test
    public void testInvalidArguments() throws Exception
    {
        XMLStreamReader2 sr = readerAtText();
        char[] buf = new char[5];
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(-1, buf, 0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(Integer.MIN_VALUE, buf, 0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(0, buf, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(0, buf, 6, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(0, buf, 0, -1));
        // target too small for what remains to be copied
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(0, buf, 1, Integer.MAX_VALUE));
        assertThrows(IndexOutOfBoundsException.class, () -> sr.getTextCharacters(0, buf, 0, 6));
        // but filling target up to its end is fine
        assertEquals(5, sr.getTextCharacters(0, buf, 0, 5));
        assertEquals(0, sr.getTextCharacters(0, buf, 5, 0));
    }

    // DTD has no text accessible as characters (its node value is null)
    @Test
    public void testNoArgTextCharactersWrongState() throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        XMLStreamReader2 sr = new DOMReader(new DOMSource(dbf.newDocumentBuilder()
                .parse(new InputSource(new StringReader("<!DOCTYPE root><root>"+TEXT+"</root>")))),
                false);
        assertEquals(XMLStreamConstants.DTD, sr.next());
        assertThrows(IllegalStateException.class, () -> sr.getTextCharacters());

        assertEquals(TEXT, new String(readerAtText().getTextCharacters()));
    }

    // Like Woodstox and Aalto: requested length may exceed target, as long as
    // what remains of the text fits
    @Test
    public void testLengthLimitedToRemainingText() throws Exception
    {
        XMLStreamReader2 sr = readerAtText();
        char[] buf = new char[5];
        assertEquals(2, sr.getTextCharacters(8, buf, 0, 20));
        assertEquals("ij", new String(buf, 0, 2));
        assertEquals(3, sr.getTextCharacters(7, buf, 2, Integer.MAX_VALUE));
        assertEquals("ijhij", new String(buf));
    }

    /*
    ///////////////////////////////////////////
    // Helper methods
    ///////////////////////////////////////////
     */

    private static String readInSegments(XMLStreamReader2 sr, int chunk) throws Exception
    {
        char[] buf = new char[chunk];
        StringBuilder sb = new StringBuilder();
        // guard against a regression that never returns fewer than requested
        final int maxCalls = TEXT.length() + 2;
        int count;
        int calls = 0;
        // as per StAX javadocs: no more text once fewer than requested are copied
        do {
            if (++calls > maxCalls) {
                fail("No end of text after "+maxCalls+" calls; got: \""+sb+"\"");
            }
            count = sr.getTextCharacters(sb.length(), buf, 0, chunk);
            sb.append(buf, 0, count);
        } while (count == chunk);
        return sb.toString();
    }

    private static XMLStreamReader2 readerAtText() throws Exception
    {
        return readerAt("<root>"+TEXT+"</root>", false, XMLStreamConstants.CHARACTERS);
    }

    private static XMLStreamReader2 readerAt(String xml, boolean coalescing, int textEvent)
        throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        // keep CDATA sections as separate nodes so that coalescing has something to merge
        dbf.setCoalescing(false);
        XMLStreamReader2 sr = new DOMReader(new DOMSource(dbf.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)))), coalescing);
        assertEquals(XMLStreamConstants.START_ELEMENT, sr.next());
        assertEquals(textEvent, sr.next());
        return sr;
    }

    /**
     * Minimal concrete reader; the abstract methods are not needed here.
     */
    static class DOMReader extends DOMWrappingReader
    {
        DOMReader(DOMSource src, boolean coalescing) throws XMLStreamException {
            super(src, true, coalescing);
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
