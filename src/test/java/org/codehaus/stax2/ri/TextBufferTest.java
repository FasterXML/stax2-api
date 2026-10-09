package org.codehaus.stax2.ri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Stax2Util.TextBuffer}, which the DOM-backed reader uses
 * to combine text of adjacent nodes: a single segment is kept as is, more
 * are combined in a builder.
 */
class TextBufferTest
{
    @Test
    void empty()
    {
        Stax2Util.TextBuffer tb = new Stax2Util.TextBuffer();
        assertTrue(tb.isEmpty());
        assertEquals("", tb.get());

        // Empty segments are ignored
        tb.append("");
        assertTrue(tb.isEmpty());
        assertEquals("", tb.get());
    }

    @Test
    void singleSegmentReturnedAsIs()
    {
        Stax2Util.TextBuffer tb = new Stax2Util.TextBuffer();
        String text = "abc";
        tb.append(text);
        tb.append("");
        assertFalse(tb.isEmpty());
        assertSame(text, tb.get());
    }

    @Test
    void combinesSegments()
    {
        Stax2Util.TextBuffer tb = new Stax2Util.TextBuffer();
        tb.append("ab");
        tb.append("");
        tb.append("cde");
        tb.append("f");
        assertFalse(tb.isEmpty());
        assertEquals("abcdef", tb.get());
        // get() does not consume
        assertEquals("abcdef", tb.get());
    }

    @Test
    void reusableAfterReset()
    {
        Stax2Util.TextBuffer tb = new Stax2Util.TextBuffer();
        tb.append("ab");
        tb.append("cd");
        tb.reset();
        assertTrue(tb.isEmpty());
        assertEquals("", tb.get());

        tb.append("x");
        tb.append("y");
        assertEquals("xy", tb.get());
    }
}
