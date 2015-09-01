package com.adobe.acs.tools.fiddle.impl.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.filter.WorkItemFilter;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;

public final class SyntheticGraniteWorkflow implements Workflow {

    private String id;

    private Date timeStarted;

    private WorkflowData workflowData;

    private SyntheticGraniteWorkItem activeWorkItem;

    public SyntheticGraniteWorkflow(String id, WorkflowData workflowData) {
        this.id = id;
        this.workflowData = workflowData;
        this.timeStarted = new Date();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List<WorkItem> getWorkItems() {
        return Arrays.asList(new WorkItem[]{this.activeWorkItem});
    }

    @Override
    public List<WorkItem> getWorkItems(WorkItemFilter workItemFilter) {
        List<WorkItem> filtered = new ArrayList<WorkItem>();

        for (WorkItem workItem : this.getWorkItems()) {
            if (workItemFilter.doInclude(workItem)) {
                filtered.add(workItem);
            }
        }

        return filtered;
    }

    @Override
    public WorkflowModel getWorkflowModel() {
        return null;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getState() {
        return "Synthetic Running";
    }

    @Override
    public String getInitiator() {
        return "Synthetic Workflow";
    }

    @Override
    public Date getTimeStarted() {
        return (Date) this.timeStarted.clone();
    }

    @Override
    public Date getTimeEnded() {
        return null;
    }

    @Override
    public WorkflowData getWorkflowData() {
        return this.workflowData;
    }

    public void setWorkflowData(WorkflowData workflowData) {
        this.workflowData = workflowData;
    }

    @Override
    public MetaDataMap getMetaDataMap() {
        return this.getWorkflowData().getMetaDataMap();
    }

    public void setActiveWorkItem(SyntheticGraniteWorkItem workItem) {
        this.activeWorkItem = workItem;
    }

}
