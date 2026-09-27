package org.codehaus.stax2.ri.typed;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ValueEncoderFactory}, mostly regarding
 * [stax2-api#41]: long values failing with {@code getScalarEncoder()}.
 */
public class ValueEncoderFactoryTest
{
    // Smallest buffer allowed by AsciiValueEncoder contract; forces
    // long values to be encoded in multiple segments
    private final static int BUFFER_SIZE = AsciiValueEncoder.MIN_CHARS_WITHOUT_FLUSH;

    private final ValueEncoderFactory _factory = new ValueEncoderFactory();

    @Test
    public void testShortValueUsesTokenEncoder()
    {
        assertInstanceOf(ValueEncoderFactory.TokenEncoder.class,
                _factory.getScalarEncoder(""));
        assertInstanceOf(ValueEncoderFactory.TokenEncoder.class,
                _factory.getScalarEncoder(value(AsciiValueEncoder.MIN_CHARS_WITHOUT_FLUSH)));
    }

    @Test
    public void testLongValueUsesStringEncoder()
    {
        assertInstanceOf(ValueEncoderFactory.StringEncoder.class,
                _factory.getScalarEncoder(value(AsciiValueEncoder.MIN_CHARS_WITHOUT_FLUSH + 1)));
    }

    @Test
    public void testScalarEncodingAsChars()
    {
        for (int len : lengthsToTest()) {
            String value = value(len);
            assertEquals(value, encodeAsChars(_factory.getScalarEncoder(value)),
                    "Failed for length "+len);
        }
    }

    @Test
    public void testScalarEncodingAsBytes()
    {
        for (int len : lengthsToTest()) {
            String value = value(len);
            assertEquals(value, encodeAsBytes(_factory.getScalarEncoder(value)),
                    "Failed for length "+len);
        }
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    private static int[] lengthsToTest() {
        final int min = AsciiValueEncoder.MIN_CHARS_WITHOUT_FLUSH;
        return new int[] { 0, 1, min - 1, min, min + 1,
                2 * min - 1, 2 * min, 2 * min + 1, 1000 };
    }

    // Distinct characters at each position, to catch wrong offsets
    private static String value(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ++i) {
            sb.append((char) ('a' + (i % 26)));
        }
        return sb.toString();
    }

    // Emulates stream writer: flush as per AsciiValueEncoder contract.
    // Buffer starts partially filled so that output does not start at 0.
    private static String encodeAsChars(AsciiValueEncoder enc)
    {
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE + 10];
        int ptr = 10;
        final int end = buffer.length;
        final int start = ptr;

        if (enc.bufferNeedsFlush(end - ptr)) {
            fail("Should not need flush with "+(end - ptr)+" free chars");
        }
        while (true) {
            ptr = enc.encodeMore(buffer, ptr, end);
            out.append(buffer, start, ptr - start);
            if (enc.isCompleted()) {
                break;
            }
            ptr = start; // "flush"
        }
        return out.toString();
    }

    private static String encodeAsBytes(AsciiValueEncoder enc)
    {
        StringBuilder out = new StringBuilder();
        byte[] buffer = new byte[BUFFER_SIZE + 10];
        int ptr = 10;
        final int end = buffer.length;
        final int start = ptr;

        if (enc.bufferNeedsFlush(end - ptr)) {
            fail("Should not need flush with "+(end - ptr)+" free bytes");
        }
        while (true) {
            ptr = enc.encodeMore(buffer, ptr, end);
            out.append(new String(buffer, start, ptr - start, StandardCharsets.US_ASCII));
            if (enc.isCompleted()) {
                break;
            }
            ptr = start; // "flush"
        }
        return out.toString();
    }
}
