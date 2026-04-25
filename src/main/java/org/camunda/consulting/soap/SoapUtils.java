package org.camunda.consulting.soap;

import org.camunda.consulting.soap.model.SoapResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SoapUtils {

    private static final String SOAP_SUCCESS_TEMPLATE = """
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
              <SOAP-ENV:Header/>
              <SOAP-ENV:Body>
                <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
                  <ns2:success>true</ns2:success>
                  <ns2:result>%s</ns2:result>
                </ns2:evaluateDecisionResponse>
              </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            """;

    private static final String SOAP_ERROR_TEMPLATE = """
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
              <SOAP-ENV:Header/>
              <SOAP-ENV:Body>
                <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
                  <ns2:success>false</ns2:success>
                  <ns2:errorMessage>%s</ns2:errorMessage>
                </ns2:evaluateDecisionResponse>
              </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            """;

    private SoapUtils() {
    }

    public static String toJson(ObjectMapper objectMapper, Object value) throws JacksonException {
        return objectMapper.writeValueAsString(value);
    }

    public static String toSoap(ObjectMapper objectMapper, Object value) throws JacksonException {
        return toSoapSuccess(objectMapper, value);
    }

    public static String toSoapSuccess(ObjectMapper objectMapper, Object value) throws JacksonException {
        JsonNode rootNode = objectMapper.valueToTree(value);
        String xmlPayload = toXmlPayload(rootNode);
        return SOAP_SUCCESS_TEMPLATE.formatted(xmlPayload);
    }

    public static String toSoapError(String errorMessage) {
        return SOAP_ERROR_TEMPLATE.formatted(escapeXml(errorMessage));
    }

    public static SoapResult toSoapResult(ObjectMapper objectMapper, Object value) throws JacksonException {
        JsonNode rootNode = objectMapper.valueToTree(value);
        String xmlPayload = toXmlPayload(rootNode);

        SoapResult soapResult = new SoapResult();
        if (xmlPayload.isBlank()) {
            return soapResult;
        }

        soapResult.setAny(parseXmlElements(xmlPayload));
        return soapResult;
    }

    public static Object normalizeSoapValue(Object value) {
        if (value instanceof Node node) {
            // Preserve nested SOAP XML structures as map/list data for decision variables.
            return convertDomNodeToObject(node);
        }
        return value;
    }

    private static Object convertDomNodeToObject(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent().trim();
            return text.isEmpty() ? null : parseValue(text);
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Map<String, Object> map = new HashMap<>();
            var children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = child.getLocalName();
                    Object nodeValue = convertDomNodeToObject(child);

                    if (map.containsKey(nodeName)) {
                        Object existing = map.get(nodeName);
                        if (existing instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> list = (List<Object>) existing;
                            list.add(nodeValue);
                        } else {
                            List<Object> list = new ArrayList<>();
                            list.add(existing);
                            list.add(nodeValue);
                            map.put(nodeName, list);
                        }
                    } else {
                        map.put(nodeName, nodeValue);
                    }
                }
            }

            return map.isEmpty() ? parseValue(node.getTextContent().trim()) : map;
        }

        return null;
    }

    private static Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            if (!value.contains(".")) {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException ignored) {
            // Continue with next parser.
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            // Continue with next parser.
        }

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String toXmlPayload(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }

        if (node.isObject()) {
            StringBuilder xml = new StringBuilder();
            node.properties().forEach(entry -> appendXmlElement(xml, entry.getKey(), entry.getValue()));
            return xml.toString();
        }

        if (node.isArray()) {
            StringBuilder xml = new StringBuilder();
            for (JsonNode item : node) {
                appendXmlElement(xml, "item", item);
            }
            return xml.toString();
        }

        return escapeXml(scalarValue(node));
    }

    private static void appendXmlElement(StringBuilder xml, String name, JsonNode value) {
        xml.append('<').append(name).append('>');

        if (value == null || value.isNull()) {
            // Leave element empty for null values.
        } else if (value.isObject()) {
            value.properties().forEach(entry -> appendXmlElement(xml, entry.getKey(), entry.getValue()));
        } else if (value.isArray()) {
            for (JsonNode item : value) {
                appendXmlElement(xml, "item", item);
            }
        } else {
            xml.append(escapeXml(scalarValue(value)));
        }

        xml.append("</").append(name).append('>');
    }

    private static List<Element> parseXmlElements(String xmlPayload) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            String wrapped = "<root>" + xmlPayload + "</root>";
            var document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(wrapped)));
            var nodes = document.getDocumentElement().getChildNodes();

            List<Element> elements = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elements.add((Element) node.cloneNode(true));
                }
            }

            return elements;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert payload into SOAP XML result.", e);
        }
    }

    private static String scalarValue(JsonNode node) {
        if (node.isString()) {
            return node.stringValue();
        }
        if (node.isNumber()) {
            return String.valueOf(node.numberValue());
        }
        if (node.isBoolean()) {
            return String.valueOf(node.booleanValue());
        }
        return node.toString();
    }
}

