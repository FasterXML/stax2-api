package org.codehaus.stax2.ri.typed;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ValueDecoderFactory.DecimalDecoder}: values with up to 18
 * digits and no exponent take a fast path, which must give the same results
 * (value and scale) as {@link BigDecimal#BigDecimal(String)}, like all other
 * values.
 */
class BigDecimalDecoderTest
{

    private static final String[] VALID = {
        "0", "-0", "+0", "0.0", "-0.00", "5", "+5", "-5", "007", "007.100",
        "1.", ".5", "-.5", "+.5", "12.50", "12345.67", "-98765432.123456",
        // longest values for fast path: 18 digits
        "999999999999999999", ".999999999999999999",
        "-99999999999999999.9", "+123456789.123456789",
        // 19 digits and more: BigDecimal only
        "1234567890123456789", "9.999999999999999999", "12345678901234567890",
        "-0.0000000000000000001", "12345678901234567890.123",
        // exponents: BigDecimal only
        "1e3", "1E-3", "1.5e+2", "-2.5E10",
        // non-ASCII digits (accepted by BigDecimal): U+0663 ARABIC-INDIC DIGIT THREE
        "\u0663", "1.\u0663"
    };

    private static final String[] INVALID = {
        "", "-", "+", ".", "-.", "--1", "+-1", "1..2", "1.2.3", "1a", "a1",
        "1 2", " 1", "1 ", "0x10", "1e", "e3", "1.5e", "INF", "NaN"
    };

    private final ValueDecoderFactory.DecimalDecoder decoder = new ValueDecoderFactory().getDecimalDecoder();

    @Test
    void decodeValidString()
    {
        for (String value : VALID) {
            decoder.decode(value);
            // BigDecimal.equals() also compares scale
            assertEquals(new BigDecimal(value), decoder.getValue(), value);
        }
    }

    @Test
    void decodeValidCharArray()
    {
        for (String value : VALID) {
            // Decoded range is not at the start or end of the array
            char[] buf = ("ab" + value + "cd").toCharArray();
            decoder.decode(buf, 2, buf.length - 2);
            assertEquals(new BigDecimal(value), decoder.getValue(), value);
        }
    }

    @Test
    void decodeInvalidString()
    {
        for (String value : INVALID) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> decoder.decode(value), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of decimal"),
                    e.getMessage());
        }
    }

    @Test
    void decodeInvalidCharArray()
    {
        for (String value : INVALID) {
            char[] buf = ("ab" + value + "cd").toCharArray();
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> decoder.decode(buf, 2, buf.length - 2), value);
            assertTrue(e.getMessage().contains("not a valid lexical representation of decimal"),
                    e.getMessage());
        }
    }
}
