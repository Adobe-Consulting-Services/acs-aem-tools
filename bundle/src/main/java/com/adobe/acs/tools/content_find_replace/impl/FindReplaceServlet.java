/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2013 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.tools.content_find_replace.impl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import javax.jcr.Node;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import com.adobe.acs.commons.packaging.PackageHelper;
import org.apache.felix.scr.annotations.Reference;

import com.day.jcr.vault.packaging.JcrPackage;
import com.day.jcr.vault.packaging.JcrPackageDefinition;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "acs-tools/components/content-find-replace", extensions = "json", methods = "POST",
        selectors = "findreplace")
public class FindReplaceServlet extends SlingAllMethodsServlet {


	@Reference
    private PackageHelper packageHelper;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        try {
            JSONObject result = new JSONObject();

	String pathToSearch = request.getParameter("search_path");
	String searchString = request.getParameter("search_string");
	String replaceString = request.getParameter("replace_string");
	String searchComponent= request.getParameter("search_component");
	String searchType= request.getParameter("search_type");
	String searchElement= request.getParameter("search_element");
	String updateReferences = request.getParameter("update_references") ;
	String query = "/jcr:root" + pathToSearch + "//element(*, " + searchElement +")[@sling:resourceType = '" + searchComponent + "'] ";
	//StringBuffer sb = new StringBuffer();
	boolean updateRefs = false;
	int save = 1000;
	int updateCounter = 0;
	int updateTracker = 0;
	int totalprocessed = 0;
	final Set<Resource> packageResources = new HashSet<Resource>();
	StringBuffer finalMessage = new StringBuffer();
	String errorMessage = "";

	if(updateReferences.equalsIgnoreCase("replace")){
		updateRefs = true;
	}

  		try
  		{

	final ResourceResolver resourceResolver = request.getResourceResolver();
	Iterator<Resource> iter = resourceResolver.findResources(query,"xpath");
	while(iter.hasNext()) {
  		Resource curRes = iter.next();
  		Node n = curRes.adaptTo(Node.class);
  		try
  		{
   			if(!n.isNodeType("cq:Page")) {
				//sb.append ("Debugging "+((updateRefs)?"Updating ":"") + n.getPath() +"\n");
     			PropertyIterator propIter = n.getProperties();
     			Pattern pat = Pattern.compile(searchString);
     			boolean valueChanged = false;
     			while(propIter.hasNext()) {
       				Property prop = propIter.nextProperty();
       				if(prop.getType() == PropertyType.STRING) {
         				String propStr = null;
         				if(prop.getDefinition().isMultiple()) {
           					Value[] values = prop.getValues();
           					for(int i = 0; i < values.length; i++) {
             					propStr = values[i].getString();
             					final ValueFactory vf = ValueFactoryImpl.getInstance();
             					if(searchType.equals("dynamic")){
                 					Matcher m = pat.matcher(propStr);
                 					final Value propertyValue = vf.createValue(m.replaceAll(replaceString));
                 					values[i] = propertyValue;
             					}else{
                 					final Value propertyValue = vf.createValue(propStr.replaceAll(searchString, replaceString));
                 					values[i] = propertyValue;
             					}
             					if(!propStr.matches(values[i].getString())) {
             						//sb.append ("Real Path@property = newvalue => "+ n.getPath() +"@"+prop.getName() + " => " + values[i].getString());
             						valueChanged = true;
	             					if(updateRefs) {
    	           						prop.setValue(values);
        	     					}
             					}
           					}
         				} else {
            				propStr = prop.getString();
           					String value = propStr.replaceAll(searchString, replaceString);

             				if(!propStr.equals(value)) {
             						//sb.append ("Real Path@property = newvalue => "+ n.getPath() +"@"+prop.getName() + " => " + value );
             					valueChanged = true;
 					           if(updateRefs) {
    	         					prop.setValue(value);
	           				    }
           					}
         				}
       				}
     			}
		     	++totalprocessed;
		     	if(valueChanged){
					++updateCounter;
					packageResources.add(curRes);
				}

     			if(updateCounter == save) {
					updateTracker = updateCounter + updateTracker;
      				updateCounter = 0;
      			    ((Session)resourceResolver.adaptTo(Session.class)).save();
     			}
  			}
  		} catch (Exception e) {
			errorMessage = " Error updating "+ n.getPath()  + " : " + e.getMessage() +"\n";
  		}
	}//while
	updateTracker = updateCounter + updateTracker;
	if(updateRefs && updateCounter>0) ((Session)resourceResolver.adaptTo(Session.class)).save();

	finalMessage.append("Completed the action "+updateReferences+". \n") ;
	finalMessage.append( "Found "+ updateTracker+ " occurrance of nodes out of "+totalprocessed+" nodes." + "\n");

	if(updateReferences.equalsIgnoreCase("package") && !packageResources.isEmpty()){
		final Map<String, String> packageDefinitionProperties = new HashMap<String, String>();
    	// Package Description
		packageDefinitionProperties.put(JcrPackageDefinition.PN_DESCRIPTION,"Backup content package");
		final JcrPackage jcrPackage = packageHelper.createPackage(packageResources,request.getResourceResolver().adaptTo(Session.class),"backup","content replace","1",PackageHelper.ConflictResolution.IncrementVersion,packageDefinitionProperties);
		finalMessage.append( "A package has been created at: "+ jcrPackage.getNode() +". Go to the CRX Package manager to build and download this package before doing the replace." +"\n") ;		
		finalMessage.append( "Json is "+ packageHelper.getSuccessJSON(jcrPackage)) ;
	}
	    result.put("success", true);

	} catch (Exception e) {
		//sb.append (" Error Saving " + e.getMessage() );
		errorMessage = e.getMessage() +"\n";
		result.put("success", false);
    	result.put("error", errorMessage);
	}
    finalMessage.append( "The affected nodes are \n");
    for(Resource i : packageResources)
    	finalMessage.append(i.getPath()+"\n");


    result.put("successMessage",finalMessage.toString() );//updateReferences+sb.toString()

    result.put("error", errorMessage);

    response.setContentType("application/json");
    result.write(response.getWriter());

} catch (JSONException e) {
	throw new ServletException(e);
}
    }
}
