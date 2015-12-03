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

import java.util.Date;
import java.util.UUID;

import com.adobe.granite.workflow.exec.Status;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.model.WorkflowNode;

public final class SyntheticGraniteWorkItem implements WorkItem {

    private static String CURRENT_ASSIGNEE = "Synthetic Workflow";

    private Date timeStarted = null;

    private Date timeEnded = null;

    private UUID uuid = UUID.randomUUID();

    private Workflow workflow;

    private WorkflowData workflowData;

    private MetaDataMap metaDataMap = new SyntheticGraniteMetaDataMap();

    public SyntheticGraniteWorkItem(WorkflowData workflowData) {
        this.workflowData = workflowData;
        this.timeStarted = new Date();
    }

    public void setWorkflow(SyntheticGraniteWorkflow workflow) {
        workflow.setActiveWorkItem(this);
        this.workflow = workflow;
    }

    public void setTimeEnded(Date timeEnded) {
        if (timeEnded == null) {
            this.timeEnded = null;
        } else {
            this.timeEnded = (Date) timeEnded.clone();
        }
    }

    @Override
    public String getId() {
        return uuid.toString() + "_" + this.getWorkflowData().getPayload();
    }

    @Override
    public Date getTimeStarted() {
        return this.timeStarted == null ? null : (Date) this.timeStarted.clone();
    }

    @Override
    public Date getTimeEnded() {
        return this.timeEnded == null ? null : (Date) this.timeEnded.clone();
    }

    @Override
    public WorkflowData getWorkflowData() {
        return this.workflowData;
    }

    @Override
    public String getCurrentAssignee() {
        return CURRENT_ASSIGNEE;
    }

    /**
     * This metadata map is local to this Workflow Item. This Map will change with each
     * WorkflowProcess step.
     *
     * @return the WorkItem's MetaDataMap
     */
    @Override
    public MetaDataMap getMetaDataMap() {
        return this.metaDataMap;
    }

    @Override
    public Workflow getWorkflow() {
        return this.workflow;
    }

    @Override
    public Status getStatus() {
       return Status.ACTIVE;
    }

    /* Unimplemented Methods */

    @Override
    public WorkflowNode getNode() {
        return null;
    }

    @Override
    public String getItemType() {
        return null;
    }

    @Override
    public String getItemSubType() {
        return null;
    }

}
