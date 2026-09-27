package org.codehaus.stax2.ri.evt;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.Stax2WriterAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AttributeEventImpl}, mostly regarding
 * [stax2-api#45]: swapped arguments in {@code writeUsing()}.
 */
public class AttributeEventImplTest
{
    @Test
    public void testWriteUsingWithNamespace() throws Exception
    {
        AttributeEventImpl attr = new AttributeEventImpl(null,
                new QName("urn:test", "attr", "p"), "value", true);
        assertEquals("<root xmlns:p=\"urn:test\" p:attr=\"value\"></root>",
                writeWithAttribute("p", "urn:test", attr));
    }

    @Test
    public void testWriteUsingWithoutNamespace() throws Exception
    {
        AttributeEventImpl attr = new AttributeEventImpl(null,
                new QName("attr"), "value", true);
        assertEquals("<root attr=\"value\"></root>",
                writeWithAttribute(null, null, attr));
    }

    // Writes attribute via writeUsing() on a Stax2-wrapped JDK stream writer
    private static String writeWithAttribute(String nsPrefix, String nsURI,
            AttributeEventImpl attr)
        throws XMLStreamException
    {
        StringWriter out = new StringWriter();
        XMLStreamWriter2 w = Stax2WriterAdapter.wrapIfNecessary(
                XMLOutputFactory.newInstance().createXMLStreamWriter(out));
        w.writeStartElement("root");
        if (nsPrefix != null) {
            w.writeNamespace(nsPrefix, nsURI);
        }
        attr.writeUsing(w);
        w.writeEndElement();
        w.close();
        return out.toString();
    }
}
