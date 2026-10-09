package org.codehaus.stax2.ri.typed;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ValueDecoderFactory.DecimalDecoder}: values with a {@code long} unscaled value and
 * no exponent take a fast path ({@link ValueDecoderFactory.DecimalDecoder#tryParseDecimal}), which must
 * give the same results (value and scale) as {@link BigDecimal#BigDecimal(String)}, like all other values.
 */
class BigDecimalDecoderTest
{
    // Values handled by the fast path
    private static final String[] VALID_FAST = {
        "0", "-0", "+0", "0.0", "-0.00", "5", "+5", "-5", "007", "007.100",
        "1.", ".5", "-.5", "+.5", "12.50", "12345.67", "-98765432.123456",
        "999999999999999999", ".999999999999999999",
        "-99999999999999999.9", "+123456789.123456789",
        // 19 digits, up to Long.MAX_VALUE
        "1234567890123456789", "9223372036854775807", "-922337203685477580.7",
        ".9223372036854775807",
        // leading zeroes do not count towards max. digits
        "-0.0000000000000000001", "0.000000000000000001", "000000000000000000.5",
        "0009223372036854775807.", "0000000000000000000000.000000000000000000000"
    };

    // Valid values left to BigDecimal
    private static final String[] VALID_SLOW = {
        // overflow of unscaled value
        "9223372036854775808", "-9223372036854775808", "9.999999999999999999",
        "12345678901234567890", "12345678901234567890.123",
        // exponents
        "1e3", "1E-3", "1.5e+2", "-2.5E10",
        // non-ASCII digits (accepted by BigDecimal): U+0663 ARABIC-INDIC DIGIT THREE
        "٣", "1.٣"
    };

    private static final String[] INVALID = {
        "", "-", "+", ".", "-.", "--1", "+-1", "1..2", "1.2.3", "1a", "a1",
        "1 2", " 1", "1 ", "0x10", "1e", "e3", "1.5e", "INF", "NaN"
    };

    @Test
    void tryParseDecimal()
    {
        for (String value : VALID_FAST) {
            // BigDecimal.equals() also compares scale
            BigDecimal expected = new BigDecimal(value);
            assertEquals(expected, ValueDecoderFactory.DecimalDecoder.tryParseDecimal(value, 0, value.length()), value);
            char[] buf = pad(value);
            assertEquals(expected, ValueDecoderFactory.DecimalDecoder.tryParseDecimal(buf, 2, buf.length - 2), value);
        }
        for (String[] values : new String[][] { VALID_SLOW, INVALID }) {
            for (String value : values) {
                assertNull(ValueDecoderFactory.DecimalDecoder.tryParseDecimal(value, 0, value.length()), value);
                char[] buf = pad(value);
                assertNull(ValueDecoderFactory.DecimalDecoder.tryParseDecimal(buf, 2, buf.length - 2), value);
            }
        }
    }

    @Test
    void decodeValidString()
    {
        for (String[] values : new String[][] { VALID_FAST, VALID_SLOW }) {
            for (String value : values) {
                ValueDecoderFactory.DecimalDecoder decoder = newDecoder();
                decoder.decode(value);
                assertEquals(new BigDecimal(value), decoder.getValue(), value);
            }
        }
    }

    @Test
    void decodeValidCharArray()
    {
        for (String[] values : new String[][] { VALID_FAST, VALID_SLOW }) {
            for (String value : values) {
                ValueDecoderFactory.DecimalDecoder decoder = newDecoder();
                char[] buf = pad(value);
                decoder.decode(buf, 2, buf.length - 2);
                assertEquals(new BigDecimal(value), decoder.getValue(), value);
            }
        }
    }

    @Test
    void decodeInvalidString()
    {
        for (String value : INVALID) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> newDecoder().decode(value), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of decimal"), e.getMessage());
        }
    }

    @Test
    void decodeInvalidCharArray()
    {
        for (String value : INVALID) {
            char[] buf = pad(value);
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> newDecoder().decode(buf, 2, buf.length - 2), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of decimal"), e.getMessage());
        }
    }

    @Test
    void decodeInvertedRange()
    {
        // Must not be decoded as an (empty) zero value
        assertThrows(IndexOutOfBoundsException.class,
                () -> newDecoder().decode("12345".toCharArray(), 4, 2));
    }

    private static ValueDecoderFactory.DecimalDecoder newDecoder() {
        return new ValueDecoderFactory().getDecimalDecoder();
    }

    // Decoded range is not at the start or end of the array; padding with digits so reading outside
    // of the range gives a wrong value, instead of falling back to BigDecimal
    private static char[] pad(String value) {
        return ("12" + value + "34").toCharArray();
    }
}
