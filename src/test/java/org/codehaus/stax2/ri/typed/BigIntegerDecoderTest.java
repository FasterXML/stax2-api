package org.codehaus.stax2.ri.typed;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ValueDecoderFactory.IntegerDecoder}: values with up to 18 digits take a fast path, which must give
 * the same results as {@link BigInteger#BigInteger(String)}, like all other values.
 */
class BigIntegerDecoderTest
{

    private static final String[] VALID = {
        "0", "-0", "+0", "5", "+5", "-5", "007", "-007", "42",
        "2147483648", "-2147483649", "9223372036854775807",
        // longest values for fast path: 18 digits
        "999999999999999999", "-999999999999999999", "+100000000000000000",
        // 19 digits and more: BigInteger only
        "1000000000000000000", "-9223372036854775808", "9999999999999999999",
        "123456789012345678901234567890", "-000000000000000000000000000001",
        // non-ASCII digits (accepted by BigInteger): U+0663 ARABIC-INDIC DIGIT THREE
        "\u0663", "1\u0663"
    };

    private static final String[] INVALID = {
        "", "-", "+", "--1", "+-1", "1a", "a1", "1.5", "1e3", "1 2", " 1", "1 ", "0x10"
    };

    private final ValueDecoderFactory.IntegerDecoder decoder = new ValueDecoderFactory().getIntegerDecoder();

    @Test
    void decodeValidString()
    {
        for (String value : VALID) {
            decoder.decode(value);
            assertEquals(new BigInteger(value), decoder.getValue(), value);
        }
    }

    @Test
    void decodeValidCharArray()
    {
        for (String value : VALID) {
            // Decoded range is not at the start or end of the array
            char[] buf = ("ab" + value + "cd").toCharArray();
            decoder.decode(buf, 2, buf.length - 2);
            assertEquals(new BigInteger(value), decoder.getValue(), value);
        }
    }

    @Test
    void decodeInvalidString()
    {
        for (String value : INVALID) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> decoder.decode(value), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of integer"), e.getMessage());
        }
    }

    @Test
    void decodeInvalidCharArray()
    {
        for (String value : INVALID) {
            char[] buf = ("ab" + value + "cd").toCharArray();
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> decoder.decode(buf, 2, buf.length - 2), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of integer"), e.getMessage());
        }
    }
}
