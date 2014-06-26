package com.adobe.acs.tools.find_and_replace.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;

import com.adobe.acs.commons.packaging.PackageHelper;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.jcr.vault.packaging.JcrPackage;
import com.day.jcr.vault.packaging.JcrPackageDefinition;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "acs-tools/components/find-and-replace",
extensions = "json", methods = "POST", selectors = "findreplace")
public class FindReplaceServlet extends SlingAllMethodsServlet {
@Reference
private PackageHelper packageHelper;

@Reference
private QueryBuilder builder;

private Session session;

private static final int SAVE_THRUSHOLD = 1000;

@Override
protected final void doPost(SlingHttpServletRequest request,
SlingHttpServletResponse response) throws ServletException {
try {
 JSONObject result = new JSONObject();
 String pathToSearch = request.getParameter("search_path");
 String searchString = request.getParameter("search_string");
 String replaceString = request.getParameter("replace_string");
 String searchComponent = request.getParameter("search_component");
 String searchElement = request.getParameter("search_element");
 String updateReferences = request.getParameter("update_references");
 boolean updateRefs = false;
 int save = SAVE_THRUSHOLD;
 int updateCounter = 0;
 int updateTracker = 0;
 int totalprocessed = 0;
 final Set<Resource> packageResources = new HashSet<Resource>();
  StringBuffer finalMessage = new StringBuffer();
  String errorMessage = "";

  if (updateReferences.equalsIgnoreCase("replace")) {
   updateRefs = true;
  }

  try {

   final ResourceResolver resourceResolver = request
     .getResourceResolver();
   session = resourceResolver.adaptTo(Session.class);
   // create query description as hash map (simplest way, same as
   // form post)
   Map<String, String> map = new HashMap<String, String>();

   // create query description as hash map (simplest way, same as
   // form post)

   map.put("path", pathToSearch);
   map.put("type", searchElement);
   map.put("property", "sling:resourceType");
   map.put("property.value", searchComponent);
   map.put("p.limit", "-1");

   Query query = builder.createQuery(PredicateGroup.create(map),
     session);
   SearchResult queryResult = query.getResult();
   for (Hit hit : queryResult.getHits()) {
    Resource curRes = resourceResolver.resolve(hit.getPath());
    Node n = curRes.adaptTo(Node.class);
    try {
     if (!n.isNodeType("cq:Page")) {
      boolean valueChanged = false;
      ModifiableValueMap valueMap = curRes
        .adaptTo(ModifiableValueMap.class);
      for (Map.Entry<String, Object> pairs : valueMap
        .entrySet()) {
       Object objectValue = pairs.getValue();
       if (objectValue instanceof String) {
        String actualValue = (String) objectValue;
        String replaceValue = actualValue
          .replaceAll(searchString,
            replaceString);

        if (!actualValue.equals(replaceValue)) {
         valueChanged = true;
         if (updateRefs) {
          valueMap.put(pairs.getKey(),
            replaceValue);
         }
        }
       } else if (objectValue instanceof String[]) {
        // Start
        String actualValue = null;
        String replaceValue = null;
        String[] actualArrayValues = (String[]) objectValue;
        for (int i = 0; i < actualArrayValues.length; i++) {
         actualValue = actualArrayValues[i];
         replaceValue = actualValue.replaceAll(
           searchString, replaceString);
         actualArrayValues[i] = replaceValue;

        if (!actualValue
          .matches(actualArrayValues[i])) {
         valueChanged = true;
         if (updateRefs) {
          valueMap.put(pairs.getKey(),
            actualArrayValues);
         }
        }
        }

        // End

       }

      }

      ++totalprocessed;
      if (valueChanged) {
       ++updateCounter;
       packageResources.add(curRes);
      }

      if (updateCounter == save) {
       updateTracker = updateCounter + updateTracker;
       updateCounter = 0;
       session.save();
      }
     }
    } catch (Exception e) {
     errorMessage = " Error updating " + n.getPath() + " : "
       + e.getMessage() + "\n";
    }
   }
   updateTracker = updateCounter + updateTracker;
   if (updateRefs && updateCounter > 0) {
    session.save();
   }

   finalMessage.append("Completed the action " + updateReferences
     + ". \n");
   finalMessage.append("Found " + updateTracker
     + " occurrance of nodes out of " + totalprocessed
     + " nodes." + "\n");

   if (updateReferences.equalsIgnoreCase("package")
     && !packageResources.isEmpty()) {
    final Map<String, String> packageDefinitionProperties = new HashMap<String, String>();
    // Package Description
    packageDefinitionProperties.put(
      JcrPackageDefinition.PN_DESCRIPTION,
      "Backup content package");
    final JcrPackage jcrPackage = packageHelper.createPackage(
      packageResources, request.getResourceResolver()
        .adaptTo(Session.class), "backup",
      "content replace", "1",
      PackageHelper.ConflictResolution.IncrementVersion,
      packageDefinitionProperties);
    finalMessage.append("A package has been created at: "
      + jcrPackage.getNode()
      + ". Go to the CRX Package manager to build "
      + "and download this package before doing "
      + "the replace." + "\n");
    finalMessage.append("Json is "
      + packageHelper.getSuccessJSON(jcrPackage));
   }
   result.put("success", true);

  } catch (Exception e) {
   errorMessage = e.getMessage() + "\n";
   result.put("success", false);
   result.put("error", errorMessage);
  }
  finalMessage.append("The affected nodes are \n");
  for (Resource i : packageResources) {
   finalMessage.append(i.getPath() + "\n");
  }
  result.put("successMessage", finalMessage.toString());

  result.put("error", errorMessage);

  response.setContentType("application/json");
  result.write(response.getWriter());

 } catch (Exception e) {
  throw new ServletException(e);
 }
 }

}
