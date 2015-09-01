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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.adobe.granite.workflow.metadata.MetaDataMap;

public final class SyntheticGraniteMetaDataMap implements MetaDataMap {

    private ValueMap metaDataMap;

    public SyntheticGraniteMetaDataMap() {
        this.metaDataMap = new ValueMapDecorator(new HashMap<String, Object>());
    }

    public SyntheticGraniteMetaDataMap(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<String, Object>();
        }

        this.metaDataMap = new ValueMapDecorator(map);
    }

    @Override
    public <T> T get(String s, Class<T> tClass) {
        return this.metaDataMap.get(s, tClass);
    }

    @Override
    public <T> T get(String s, T t) {
        return this.metaDataMap.get(s, t);
    }

    @Override
    public int size() {
        return this.metaDataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.metaDataMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.metaDataMap.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.metaDataMap.containsValue(o);
    }

    @Override
    public Object get(Object o) {
        return this.metaDataMap.get(o);
    }

    @Override
    public Object put(String s, Object o) {
        return this.metaDataMap.put(s, o);
    }

    @Override
    public Object remove(Object o) {
        return this.metaDataMap.remove(o);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        this.metaDataMap.putAll(map);
    }

    @Override
    public void clear() {
        this.metaDataMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.metaDataMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return this.metaDataMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.metaDataMap.entrySet();
    }

}
