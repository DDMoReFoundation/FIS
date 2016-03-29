/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
 * Parent class for acceptance tests that ensures that sets up test properties.
 */
public class AcceptanceTestParent {
    private static final Logger LOG = Logger.getLogger(AcceptanceTestParent.class);
    private static final Properties initialProperties = new Properties();
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    
    @BeforeClass
    public static void setUpClass() throws IOException  {
        initialProperties.putAll(System.getProperties());
        Properties props = new Properties();
        props.load(AcceptanceTestParent.class.getResource("/acceptance-tests.properties").openStream());
        
        for(Entry<Object, Object> en : props.entrySet()) {
            String prop = (String) en.getKey();
            String value = (String)en.getValue();
            if(System.getProperty(prop)==null) {
                if(TRUST_STORE_PROPERTY.equals(en.getKey())) {
                    // The path must be absolute 
                    value = Paths.get(value).normalize().toAbsolutePath().toString();
                }
                System.setProperty(prop, value);
            }
        }
        for(Entry<Object, Object> en: System.getProperties().entrySet()) {
            LOG.info(String.format("Property %s=%s", en.getKey(), en.getValue()));
        }
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Properties p = new Properties();
        p.putAll(initialProperties);
        System.setProperties(p);
    }
}
