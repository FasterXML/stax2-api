package org.codehaus.stax2.ri.typed;

import org.codehaus.stax2.typed.Base64Variant;

/**
 * Helper class used for serializing typed values to String. Mostly
 * used for "non-native" stream writers; things like DOM-backed
 * stream writer, or stax (v1) adapter.
 *
 * @author Tatu Saloranta
 */
public class SimpleValueEncoder
{
    /**
     * For efficient encoding, need a working buffer
     */
    protected final char[] mBuffer = new char[500];

    protected final ValueEncoderFactory mEncoderFactory;

    public SimpleValueEncoder()
    {
        mEncoderFactory = new ValueEncoderFactory();
    }

    public String encodeAsString(int[] value, int from, int length)
    {
        return encode(mEncoderFactory.getEncoder(value, from, length));
    }

    public String encodeAsString(long[] value, int from, int length)
    {
        return encode(mEncoderFactory.getEncoder(value, from, length));
    }

    public String encodeAsString(float[] value, int from, int length)
    {
        return encode(mEncoderFactory.getEncoder(value, from, length));
    }

    public String encodeAsString(double[] value, int from, int length)
    {
        return encode(mEncoderFactory.getEncoder(value, from, length));
    }

    public String encodeAsString(Base64Variant v, byte[] value, int from, int length)
    {
        return encode(mEncoderFactory.getEncoder(v, value, from, length));
    }

    /*
    ///////////////////////////////////////////////////
    // Internal methods
    ///////////////////////////////////////////////////
     */

    protected String encode(AsciiValueEncoder enc)
    {
        // note: nothing in buffer, can't flush (thus no need to call to check)
        int last = enc.encodeMore(mBuffer, 0, mBuffer.length);
        if (enc.isCompleted()) { // fitted in completely?
            return new String(mBuffer, 0, last);
        }
        StringBuilder sb = new StringBuilder(mBuffer.length << 1);
        sb.append(mBuffer, 0, last);
        do {
            last = enc.encodeMore(mBuffer, 0, mBuffer.length);
            sb.append(mBuffer, 0, last);
        } while (!enc.isCompleted());
        return sb.toString();
    }
}
