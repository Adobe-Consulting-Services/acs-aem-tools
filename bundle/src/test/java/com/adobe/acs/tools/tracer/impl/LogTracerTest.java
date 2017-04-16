/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2014 Adobe
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
package com.adobe.acs.tools.tracer.impl;

import java.util.Arrays;

import ch.qos.logback.classic.Level;
import com.adobe.acs.tools.tracer.impl.LogTracer.TracerConfig;
import com.adobe.acs.tools.tracer.impl.LogTracer.TracerContext;
import com.adobe.acs.tools.tracer.impl.LogTracer.TracerSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LogTracerTest {

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidConfig() throws Exception {
        new TracerSet("foo");
    }

    @Test
    public void parseTracerSet() throws Exception {
        TracerSet a = new TracerSet("foo : com.foo, com.bar;level=INFO");
        assertEquals("foo", a.name);
        TracerConfig tcfoo = a.getConfig("com.foo");
        assertNotNull(tcfoo);
        assertEquals(Level.DEBUG, tcfoo.level);

        assertNotNull("Config for parent should match for child", a.getConfig("com.foo.bar"));


        TracerConfig tcbar = a.getConfig("com.bar");
        assertNotNull(tcbar);
        assertEquals(Level.INFO, tcbar.level);
    }

    @Test
    public void childLoggerLevelDiff() throws Exception{
        TracerSet ts = new TracerSet("foo : a.b;level=trace, a.b.c;level=info");
        TracerContext tc = getContext(ts);

        assertTrue(tc.shouldLog("a.b", Level.TRACE));
        assertTrue(tc.shouldLog("a.b.d", Level.TRACE));
        assertFalse(tc.shouldLog("a.b.c", Level.TRACE));
    }

    @Test
    public void tracerConfigTest() throws Exception{
        TracerConfig tc = new TracerConfig("a.b.c", Level.DEBUG);
        assertEquals(3, tc.depth);
        assertEquals(LogTracer.MatchResult.MATCH_LOG, tc.match("a.b.c.d", Level.DEBUG));
        assertEquals(LogTracer.MatchResult.MATCH_NO_LOG, tc.match("a.b.c.d", Level.TRACE));
        assertEquals(LogTracer.MatchResult.NO_MATCH, tc.match("a.b.d", Level.TRACE));
    }

    @Test
    public void tracerConfigSort() throws Exception{
        TracerConfig[] configs = new TracerConfig[] {
          new TracerConfig("a.b.c.d", Level.DEBUG),
          new TracerConfig("a", Level.DEBUG),
          new TracerConfig("a.b.e", Level.DEBUG),
        };

        Arrays.sort(configs);
        assertEquals("a.b.c.d", configs[0].loggerName);
        assertEquals("a.b.e", configs[1].loggerName);
        assertEquals("a", configs[2].loggerName);

    }

    private static TracerContext getContext(TracerSet ts){
        return new TracerContext(ts.configs.toArray(new TracerConfig[ts.configs.size()]));
    }

}