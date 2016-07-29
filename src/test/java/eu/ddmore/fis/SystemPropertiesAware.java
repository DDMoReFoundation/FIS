/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package eu.ddmore.fis;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;


public class SystemPropertiesAware {
    private static final Properties initialProperties = new Properties();
    @BeforeClass
    public static void setUpClass() throws IOException  {
        initialProperties.putAll(System.getProperties());
        Properties props = new Properties();
        props.load(SystemPropertiesAware.class.getResource("/tests.properties").openStream());
        
        for(Entry<Object, Object> en : props.entrySet()) {
            if(System.getProperty((String) en.getKey())==null) {
                System.setProperty((String)en.getKey(), (String)en.getValue());
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Properties p = new Properties();
        p.putAll(initialProperties);
        System.setProperties(p);
    }
}
