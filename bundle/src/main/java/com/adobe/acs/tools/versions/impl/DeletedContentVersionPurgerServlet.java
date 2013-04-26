package com.adobe.acs.tools.versions.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

/**
 * Servlet which removes version history of nodes which have been deleted.
 */
@SuppressWarnings("serial")
@SlingServlet(paths = "/bin/versions/purge-deleted-content-history")
public class DeletedContentVersionPurgerServlet extends SlingAllMethodsServlet {

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        try {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            @SuppressWarnings("deprecation")
            Query query = queryManager.createQuery("//element(*, nt:versionHistory)", Query.XPATH);
            QueryResult result = query.execute();

            NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                String contentUuid = node.getProperty("jcr:versionableUuid").getString();

                try {
                    // would be nice if there was a session.nodeIdentifierExists(),
                    // but we'll live with catching this exception
                    session.getNodeByIdentifier(contentUuid);
                } catch (ItemNotFoundException e) {
                    writer.printf("Could not find content node for uuid %s from version history at %s. Thus deleting.\n", contentUuid,
                            node.getPath());

                    List<String> names = new ArrayList<String>();
                    VersionHistory history = (VersionHistory) node;
                    VersionIterator vit = history.getAllVersions();
                    while (vit.hasNext()) {
                        Version v = vit.nextVersion();
                        String name = v.getName();
                        if (!name.equals("jcr:rootVersion")) {
                            names.add(v.getName());
                        }
                    }
                    Collections.reverse(names);
                    for (String name : names) {
                        history.removeVersion(name);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
