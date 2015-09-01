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

import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;

public class SyntheticGraniteWorkflowData implements WorkflowData {
    private String payloadType;

    private Object payload;

    private MetaDataMap metaDataMap = new SyntheticGraniteMetaDataMap();

    public SyntheticGraniteWorkflowData(String payloadType, Object payload) {
        this.payloadType = payloadType;
        this.payload = payload;
    }

    @Override
    public Object getPayload() {
        return this.payload;
    }

    @Override
    public String getPayloadType() {
        return this.payloadType;
    }

    @Override
    public MetaDataMap getMetaDataMap() {
        return this.metaDataMap;
    }

}
