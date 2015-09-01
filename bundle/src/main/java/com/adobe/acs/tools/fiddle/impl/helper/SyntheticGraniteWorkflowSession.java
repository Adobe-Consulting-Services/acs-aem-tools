/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2015 Adobe
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
package com.adobe.acs.tools.fiddle.impl.helper;

import java.security.AccessControlException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.collection.util.ResultSet;
import com.adobe.granite.workflow.exec.HistoryItem;
import com.adobe.granite.workflow.exec.InboxItem;
import com.adobe.granite.workflow.exec.Participant;
import com.adobe.granite.workflow.exec.Route;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.filter.InboxItemFilter;
import com.adobe.granite.workflow.exec.filter.WorkItemFilter;
import com.adobe.granite.workflow.model.VersionException;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.adobe.granite.workflow.model.WorkflowModelFilter;

public final class SyntheticGraniteWorkflowSession implements WorkflowSession {

    private final ResourceResolver resourceResolver;

    public SyntheticGraniteWorkflowSession(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (ResourceResolver.class == type) {
            return (AdapterType) resourceResolver;
        } else if (Session.class == type) {
            return (AdapterType) resourceResolver.adaptTo(Session.class);
        }

        return null;
    }

    @Override
    public void deployModel(WorkflowModel model) throws WorkflowException {
        // TODO Auto-generated method stub

    }

    @Override
    public WorkflowModel createNewModel(String title) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowModel createNewModel(String title, String id) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteModel(String id) throws WorkflowException {
        // TODO Auto-generated method stub

    }

    @Override
    public WorkflowModel[] getModels() throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowModel[] getModels(WorkflowModelFilter filter) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<WorkflowModel> getModels(long start, long limit) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<WorkflowModel> getModels(long start, long limit, WorkflowModelFilter filter)
            throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowModel getModel(String id) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowModel getModel(String id, String version) throws WorkflowException, VersionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Workflow startWorkflow(WorkflowModel model, WorkflowData data) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Workflow startWorkflow(WorkflowModel model, WorkflowData data, Map<String, Object> metaData)
            throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void terminateWorkflow(Workflow instance) throws WorkflowException {
        // TODO Auto-generated method stub

    }

    @Override
    public void resumeWorkflow(Workflow instance) throws WorkflowException {
        // TODO Auto-generated method stub

    }

    @Override
    public void suspendWorkflow(Workflow instance) throws WorkflowException {
        // TODO Auto-generated method stub

    }

    @Override
    public WorkItem[] getActiveWorkItems() throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<WorkItem> getActiveWorkItems(long start, long limit) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<WorkItem> getActiveWorkItems(long start, long limit, WorkItemFilter filter)
            throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<InboxItem> getActiveInboxItems(long start, long limit, InboxItemFilter filter)
            throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<InboxItem> getActiveInboxItems(long start, long limit, String itemSubType, InboxItemFilter filter)
            throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkItem[] getAllWorkItems() throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<WorkItem> getAllWorkItems(long start, long limit) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkItem getWorkItem(String id) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Workflow[] getWorkflows(String[] states) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet<Workflow> getWorkflows(String[] states, long start, long limit) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Workflow[] getAllWorkflows() throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Workflow getWorkflow(String id) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void complete(WorkItem item, Route route) throws WorkflowException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Route> getRoutes(WorkItem item, boolean expand) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Route> getBackRoutes(WorkItem item, boolean expand) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkflowData newWorkflowData(String payloadType, Object payload) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Participant> getDelegates(WorkItem item) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delegateWorkItem(WorkItem item, Participant participant)
            throws WorkflowException, AccessControlException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<HistoryItem> getHistory(Workflow instance) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateWorkflowData(Workflow instance, WorkflowData data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isSuperuser() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void restartWorkflow(Workflow workflow) throws WorkflowException {
        // TODO Auto-generated method stub

    }

}
