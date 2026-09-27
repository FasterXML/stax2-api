package org.codehaus.stax2.typed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Base64Variant}, mostly regarding
 * [stax2-api#49]: {@code decodeBase64Byte()} crashed on non-ASCII bytes.
 */
public class Base64VariantTest
{
    private final Base64Variant MIME = Base64Variants.MIME;

    @Test
    public void testDecodeBase64ByteValid()
    {
        assertEquals(0, MIME.decodeBase64Byte((byte) 'A'));
        assertEquals(25, MIME.decodeBase64Byte((byte) 'Z'));
        assertEquals(26, MIME.decodeBase64Byte((byte) 'a'));
        assertEquals(52, MIME.decodeBase64Byte((byte) '0'));
        assertEquals(62, MIME.decodeBase64Byte((byte) '+'));
        assertEquals(63, MIME.decodeBase64Byte((byte) '/'));
    }

    @Test
    public void testDecodeBase64ByteInvalidAscii()
    {
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Byte((byte) 0));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Byte((byte) '!'));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Byte((byte) 0x7F));
    }

    // [stax2-api#49]: bytes 0x80 - 0xFF used to throw ArrayIndexOutOfBoundsException
    @Test
    public void testDecodeBase64ByteNonAscii()
    {
        for (int i = 0x80; i <= 0xFF; ++i) {
            assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Byte((byte) i),
                    "Byte 0x"+Integer.toHexString(i));
        }
    }

    @Test
    public void testDecodeBase64CharNonAscii()
    {
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Char((char) 0x80));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Char((char) 0xFF));
        assertEquals(Base64Variant.BASE64_VALUE_INVALID, MIME.decodeBase64Char('￿'));
    }
}
