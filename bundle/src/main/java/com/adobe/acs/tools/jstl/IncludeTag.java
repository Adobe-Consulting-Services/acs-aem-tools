package com.adobe.acs.tools.jstl;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.SimpleTag;
import javax.servlet.jsp.tagext.Tag;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.input.ReaderInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Extends the CQ include tag to offer support for defining default component.
 */
public class IncludeTag implements SimpleTag {

    /**
     * Proxy object to the original tag, called in tandem. It would be more
     * ideal to extend the Include tag, but it is not possible in this case
     * because the methods needed to access the JSP Body were marked protected.
     */
    com.day.cq.wcm.tags.IncludeTag includeTag = new com.day.cq.wcm.tags.IncludeTag();

    @Override
    public void doTag() throws JspException, IOException {
        Node currentNode = (Node) context.findAttribute("currentNode");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            StringWriter writer = new StringWriter();
            body.invoke(writer);
            InputStream is = new ReaderInputStream(new StringReader(writer.toString()));
            Document dom = db.parse(is);
            Element rootElement = dom.getDocumentElement();
            processDefaults(currentNode, rootElement, "nt:unstructured", resourceType);
            currentNode.getSession().save();
        }
        catch (ParserConfigurationException pce) {
            throw new JspException("ParserConfigurationException trying to parse Include tag body: " + pce.getMessage(), pce);
        }
        catch (SAXException se) {
            throw new JspException("SaxException trying to parse Include tag body: " + se.getMessage(), se);
        }
        catch (IOException ioe) {
            throw new JspException("IOException trying to parse Include tag body: " + ioe.getMessage(), ioe);
        }
        catch (RepositoryException ex) {
            throw new JspException("RepositoryException trying to process Include tag body: " + ex.getMessage(), ex);
        }
        includeTag.doEndTag();
        includeTag.release();
    }

    private void processDefaults(Node currentNode, Element element, String jcrType, String slingResourceType) throws RepositoryException {
        Node mergeNode = null;
        // Create node if it does not exist or merge with existing (if allowed)
        if (!currentNode.hasNode(element.getTagName())) {
            // Fist determine the jcr type
            String type = element.hasAttribute("jcr:primaryType") ? element.getAttribute("jcr:primaryType") : jcrType;
            mergeNode = currentNode.addNode(element.getTagName(), type);
            String elementType = element.hasAttribute("sling:resourceType") ? element.getAttribute("sling:resourceType") : slingResourceType;
            if (elementType != null) {
                mergeNode.setProperty("sling:resourceType", elementType);
            }
        } else {
            if (!mergeWithExistingData) {
                return;
            }
            mergeNode = currentNode.getNode(element.getTagName());
        }

        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            org.w3c.dom.Node attr = element.getAttributes().item(i);
            String nodeNameRaw = attr.getNodeName();
            String nodeName = getNodeName(nodeNameRaw);
            String nodeValue = attr.getNodeValue();
            if (!mergeNode.hasProperty(nodeName)) {
                setNodeProperty(mergeNode, nodeNameRaw, nodeValue);
                mergeNode.setProperty(nodeName, nodeValue);
            }
        }

        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            org.w3c.dom.Node child = element.getChildNodes().item(i);
            if (!(child instanceof Element)) {
                continue;
            }
            Element childElement = (Element) child;
            processDefaults(mergeNode, childElement, jcrType, null);
        }
    }

    private String getNodeName(String nodeNameRaw) {
        return nodeNameRaw;
    }

    @Override
    public void setParent(JspTag jsptag) {
        includeTag.setParent((Tag) jsptag);
    }

    @Override
    public JspTag getParent() {
        return includeTag.getParent();
    }

    JspContext context;

    @Override
    public void setJspContext(JspContext jc) {
        context = jc;
        includeTag.setPageContext((PageContext) jc);
    }

    JspFragment body;

    @Override
    public void setJspBody(JspFragment jf) {
        body = jf;
    }

    //-------- Include parameters accepted by CQ Include tag
    // These are provided here so that the values can pass through.
    // Only the path tag is used by this tag.
    public void setFlush(boolean flush) {
        includeTag.setFlush(flush);
    }

    public void setIgnoreComponentHierarchy(boolean ignoreComponentHierarchy) {
        includeTag.setIgnoreComponentHierarchy(ignoreComponentHierarchy);
    }

    public void setPath(String path) {
        includeTag.setPath(path);
    }

    String resourceType;

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
        includeTag.setResourceType(resourceType);
    }

    public void setScript(String script) {
        includeTag.setScript(script);
    }

    boolean mergeWithExistingData;

    public void setMerge(boolean merge) {
        mergeWithExistingData = merge;
    }

    private void setNodeProperty(Node node, String propertyName, String propertyValue) throws RepositoryException {
        // TODO: Would be more useful if different attribute types could be supported
        // Right now everything is treated as string            
        // Class nodeType = getNodeType(nodeNameRaw);
        node.setProperty(propertyName, propertyValue);
    }
}
