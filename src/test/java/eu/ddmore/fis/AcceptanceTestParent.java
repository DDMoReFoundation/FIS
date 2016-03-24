/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
 * Parent class for acceptance tests that ensures that sets up test properties.
 */
public class AcceptanceTestParent {
    private static final Properties initialProperties = new Properties();
    @BeforeClass
    public static void setUpClass() throws IOException  {
        initialProperties.putAll(System.getProperties());
        Properties props = new Properties();
        props.load(AcceptanceTestParent.class.getResource("/acceptance-tests.properties").openStream());
        
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
