package org.codehaus.stax2.ri.typed;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ValueDecoderFactory.IntegerDecoder}: values that fit in a {@code long} take a fast path
 * ({@link ValueDecoderFactory.DecoderBase#tryParseLong}), which must give the same results as
 * {@link BigInteger#BigInteger(String)}, like all other values.
 */
class BigIntegerDecoderTest
{
    private static final long NOT_A_LONG = ValueDecoderFactory.DecoderBase.NOT_A_LONG;

    // Values handled by the fast path
    private static final String[] VALID_LONG = {
        "0", "-0", "+0", "5", "+5", "-5", "007", "-007", "42",
        "2147483648", "-2147483649",
        "999999999999999999", "-999999999999999999", "+100000000000000000",
        // 19 digits, up to Long.MAX_VALUE
        "1000000000000000000", "9223372036854775807", "-9223372036854775807",
        // leading zeroes do not count towards max. digits
        "0000000000000000000042", "-000000000000000000000000000001", "00000000000000000000000",
        "0009223372036854775807"
    };

    // Valid values left to BigInteger
    private static final String[] VALID_NOT_LONG = {
        // overflow on last digit
        "9223372036854775808", "-9223372036854775808", "9223372036854775810",
        "9999999999999999999", "10000000000000000000",
        "123456789012345678901234567890"
    };

    private static final String[] INVALID = {
        "", "-", "+", "--1", "+-1", "1a", "a1", "1.5", "1e3", "1 2", " 1", "1 ", "0x10", "00-1"
    };

    @Test
    void tryParseLong()
    {
        for (String value : VALID_LONG) {
            long expected = Long.parseLong(value);
            assertEquals(expected, ValueDecoderFactory.DecoderBase.tryParseLong(value, 0, value.length()), value);
            char[] buf = pad(value);
            assertEquals(expected, ValueDecoderFactory.DecoderBase.tryParseLong(buf, 2, buf.length - 2), value);
        }
        for (String[] values : new String[][] { VALID_NOT_LONG, INVALID }) {
            for (String value : values) {
                assertEquals(NOT_A_LONG, ValueDecoderFactory.DecoderBase.tryParseLong(value, 0, value.length()), value);
                char[] buf = pad(value);
                assertEquals(NOT_A_LONG, ValueDecoderFactory.DecoderBase.tryParseLong(buf, 2, buf.length - 2), value);
            }
        }
    }

    @Test
    void decodeValidString()
    {
        for (String[] values : new String[][] { VALID_LONG, VALID_NOT_LONG }) {
            for (String value : values) {
                ValueDecoderFactory.IntegerDecoder decoder = newDecoder();
                decoder.decode(value);
                assertEquals(new BigInteger(value), decoder.getValue(), value);
            }
        }
    }

    @Test
    void decodeValidCharArray()
    {
        for (String[] values : new String[][] { VALID_LONG, VALID_NOT_LONG }) {
            for (String value : values) {
                ValueDecoderFactory.IntegerDecoder decoder = newDecoder();
                char[] buf = pad(value);
                decoder.decode(buf, 2, buf.length - 2);
                assertEquals(new BigInteger(value), decoder.getValue(), value);
            }
        }
    }

    @Test
    void decodeInvalidString()
    {
        for (String value : INVALID) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> newDecoder().decode(value), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of integer"), e.getMessage());
        }
    }

    @Test
    void decodeInvalidCharArray()
    {
        for (String value : INVALID) {
            char[] buf = pad(value);
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> newDecoder().decode(buf, 2, buf.length - 2), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of integer"), e.getMessage());
        }
    }

    @Test
    void decodeInvertedRange()
    {
        // Must not be decoded as an (empty) zero value
        assertThrows(IndexOutOfBoundsException.class,
                () -> newDecoder().decode("12345".toCharArray(), 4, 2));
    }

    private static ValueDecoderFactory.IntegerDecoder newDecoder() {
        return new ValueDecoderFactory().getIntegerDecoder();
    }

    // Decoded range is not at the start or end of the array; padding with digits so reading outside
    // of the range gives a wrong value, instead of falling back to BigInteger
    private static char[] pad(String value) {
        return ("12" + value + "34").toCharArray();
    }
}
