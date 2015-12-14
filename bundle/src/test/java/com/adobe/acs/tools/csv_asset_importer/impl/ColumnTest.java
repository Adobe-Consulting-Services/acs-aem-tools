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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.tools.csv_asset_importer.impl;


import org.junit.Assert;
import org.junit.Test;

public class ColumnTest {

    @Test
    public void testGetData() throws Exception {
        Column col;

        // Default (String)
        col = new Column("title", 0);
        Assert.assertEquals("test", col.getData("test"));

        // String
        col = new Column("title {{ String }}", 0);
        Assert.assertEquals("test", col.getData("test"));

        // String (true value)
        col = new Column("title {{ String }}", 0);
        Assert.assertEquals("true", col.getData("true"));

        // Long
        col = new Column("title {{ Long }}", 0);
        Assert.assertEquals(100L, col.getData("100"));

        // Int
        col = new Column("title {{ Int }}", 0);
        Assert.assertEquals(100L, col.getData("100"));

        // Integer
        col = new Column("title {{ Integer }}", 0);
        Assert.assertEquals(100L, col.getData("100"));

        // Double
        col = new Column("title {{ Double }}", 0);
        Assert.assertEquals(100.001D, col.getData("100.001"));

        // Boolean
        col = new Column("title {{ Boolean }}", 0);
        Assert.assertEquals(true, col.getData("true"));

        col = new Column("title {{ Boolean }}", 0);
        Assert.assertEquals(false, col.getData("FALSE"));
    }

    @Test
    public void testToObjectType() throws Exception {
        Column col;

        // Default (String)
        col = new Column("title", 0);
        Assert.assertEquals("test", col.toObjectType("test", String.class));

        // String (true value)
        col = new Column("title {{ String }}", 0);
        Assert.assertEquals("true", col.toObjectType("true", String.class));
    }

    @Test
    public void testGetPropertyName() throws Exception {
        Column col;

        col = new Column("title", 0);
        Assert.assertEquals("title", col.getPropertyName());

        col = new Column("title {{ String }}", 0);
        Assert.assertEquals("title", col.getPropertyName());

        col = new Column("title {{ String : multi }}", 0);
        Assert.assertEquals("title", col.getPropertyName());

        col = new Column("title {{String}}", 0);
        Assert.assertEquals("title", col.getPropertyName());

        col = new Column("title {{String:multi}}", 0);
        Assert.assertEquals("title", col.getPropertyName());
    }
}