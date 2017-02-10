/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2017 Dan Klco
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
package com.adobe.acs.tools.audit_log_search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.granite.security.user.UserProperties;
import com.adobe.granite.security.user.UserPropertiesManager;
import com.adobe.granite.security.user.UserPropertiesService;

/**
 * Simple POJO for audit log requests. Handles some of the crufty code around
 * loading and generating the query.
 */
public class AuditLogSearchRequest {

	private static final SimpleDateFormat HTML5_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
	private static final SimpleDateFormat QUERY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	private static String getJCRSQLDate(Date date) {
		return QUERY_DATE_FORMAT.format(date) + ".000Z";
	}

	{
		QUERY_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		HTML5_DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private final String contentRoot;
	private final boolean children;
	private final String type;
	private final String user;
	private final Date startDate;
	private final Date endDate;
	private final String order;
	private Map<String, String> userNames = new HashMap<String, String>();

	private Map<String, String> userPaths = new HashMap<String, String>();

	/**
	 * Constructs a new AuditLogSearchRequest from the SlingHttpServletRequest
	 * 
	 * @param request
	 *            yep, that's a request... guess what it does
	 * @throws ParseException
	 *             an exception occurred parsing the start / end date
	 */
	public AuditLogSearchRequest(SlingHttpServletRequest request) throws ParseException {
		contentRoot = request.getParameter("contentRoot");
		children = "true".equals(request.getParameter("children"));
		type = request.getParameter("type");
		user = request.getParameter("user");
		startDate = loadDate(request.getParameter("startDate"));
		endDate = loadDate(request.getParameter("endDate"));
		order = request.getParameter("order");
	}

	public String getContentRoot() {
		return contentRoot;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getOrder() {
		return order;
	}

	public String getQueryParameters() {
		List<String> expressions = new ArrayList<String>();

		if (!StringUtils.isEmpty(type)) {
			expressions.add("[cq:type]='" + StringEscapeUtils.escapeSql(type) + "'");
		}
		if (!StringUtils.isEmpty(user)) {
			expressions.add("[cq:userid]='" + StringEscapeUtils.escapeSql(user) + "'");
		}
		if (children) {
			expressions.add("[cq:path] LIKE '" + StringEscapeUtils.escapeSql(contentRoot) + "%'");
		} else {
			expressions.add("[cq:path]='" + StringEscapeUtils.escapeSql(contentRoot) + "'");
		}
		if (startDate != null) {
			expressions.add("[cq:time] > CAST('" + getJCRSQLDate(startDate) + "' AS DATE)");
		}
		if (endDate != null) {
			expressions.add("[cq:time] < CAST('" + getJCRSQLDate(endDate) + "' AS DATE)");
		}
		String q = StringUtils.join(expressions, " AND ");
		if (!StringUtils.isEmpty(order)) {
			q += " ORDER BY " + order;
		}
		return q;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getType() {
		return type;
	}

	public String getUser() {
		return user;
	}

	public String getUserName(ResourceResolver resolver, String userId) throws RepositoryException {
		if (!userNames.containsKey(userId)) {
			final UserPropertiesManager upm = resolver.adaptTo(UserPropertiesManager.class);
			UserProperties userProperties = upm.getUserProperties(userId, UserPropertiesService.PROFILE_PATH);
			String name = userId;
			if (userProperties != null && !StringUtils.isEmpty(userProperties.getDisplayName())) {
				name = userProperties.getDisplayName();
			}
			userNames.put(userId, name);
		}
		return userNames.get(userId);
	}

	public String getUserPath(ResourceResolver resolver, String userId)
			throws UnsupportedRepositoryOperationException, RepositoryException {
		if (!userPaths.containsKey(userId)) {
			final UserManager userManager = resolver.adaptTo(UserManager.class);
			final Authorizable usr = userManager.getAuthorizable(userId);
			if (usr != null) {
				userPaths.put(userId, usr.getPath());
			}
		}
		return userPaths.get(userId);
	}

	public boolean isChildren() {
		return children;
	}

	private Date loadDate(String dateStr) throws ParseException {
		Date date = null;
		if (!StringUtils.isEmpty(dateStr)) {
			date = HTML5_DATETIME_FORMAT.parse(dateStr);
		}
		return date;
	}

	@Override
	public String toString() {
		return "AuditLogSearchRequest [contentRoot=" + contentRoot + ", children=" + children + ", type=" + type
				+ ", user=" + user + ", startDate=" + startDate + ", endDate=" + endDate + ", order=" + order
				+ ", userNames=" + userNames + ", userPaths=" + userPaths + "]";
	}

}