package local.jarios.helpers;

import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Predicate;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import local.jarios.entity.atom.Entry;
import local.jarios.entity.atom.Feed;
import local.jarios.mappers.atom.MapperFeed;
import org.w3._2005.atom.FeedType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/** Conserva el elemento fuente completo, incluidas las extensiones ajenas al modelo Java. */
record AtomEntryXml(Feed feed, Element root) {
  private static final String ATOM = "http://www.w3.org/2005/Atom";
  private static final List<String> XML_ATTRIBUTES = List.of("base", "lang", "space");

  static AtomEntryXml read(Reader input, Unmarshaller unmarshaller) throws Exception {
    var factory = DocumentBuilderFactory.newDefaultNSInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    var document = factory.newDocumentBuilder().parse(new InputSource(input));
    var feedType = (FeedType) ((JAXBElement<?>) unmarshaller.unmarshal(document)).getValue();
    return new AtomEntryXml(MapperFeed.getFeed(feedType), document.getDocumentElement());
  }

  void capture(Predicate<Entry> archivar) throws Exception {
    var transformerFactory = TransformerFactory.newDefaultInstance();
    transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    var transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    List<Entry> entries = feed.getEntryList();
    int index = 0;
    for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (!(node instanceof Element source)
          || !ATOM.equals(source.getNamespaceURI())
          || !"entry".equals(source.getLocalName())) {
        continue;
      }
      if (index >= entries.size()) {
        throw new IllegalStateException("No coinciden las entradas XML y las entradas mapeadas");
      }
      Entry entry = entries.get(index++);
      if (archivar.test(entry)) {
        entry.setXml(serialize(source, root, transformer));
      }
    }
    if (index != entries.size()) {
      throw new IllegalStateException("No coinciden las entradas XML y las entradas mapeadas");
    }
  }

  private static String serialize(Element source, Element root, Transformer transformer)
      throws Exception {
    Element copy = (Element) source.cloneNode(true);
    var attributes = root.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI())
          && !copy.hasAttributeNS(attribute.getNamespaceURI(), attribute.getLocalName())) {
        copy.setAttributeNS(
            attribute.getNamespaceURI(), attribute.getNodeName(), attribute.getNodeValue());
      }
    }
    for (String attribute : XML_ATTRIBUTES) {
      if (root.hasAttributeNS(XMLConstants.XML_NS_URI, attribute)
          && !copy.hasAttributeNS(XMLConstants.XML_NS_URI, attribute)) {
        copy.setAttributeNS(
            XMLConstants.XML_NS_URI,
            "xml:" + attribute,
            root.getAttributeNS(XMLConstants.XML_NS_URI, attribute));
      }
    }
    var xml = new StringWriter();
    transformer.transform(new DOMSource(copy), new StreamResult(xml));
    return xml.toString();
  }
}
