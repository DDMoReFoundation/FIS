/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import eu.ddmore.fis.domain.UserInfo;


/**
 * Parent class for acceptance tests that ensures that sets up test properties.
 */
public class AcceptanceTestParent {
    private static final Logger LOG = Logger.getLogger(AcceptanceTestParent.class);
    private static final Properties initialProperties = new Properties();
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    private static final String CLIENT_USER_NAME = "fis.client.user.name";
    private static final String CLIENT_USER_PASSWORD = "fis.client.user.password";
    private static final String CLIENT_USER_IDENTITY_FILE = "fis.client.user.identityFile";
    private static final String CLIENT_USER_IDENTITY_FILE_PASS_PHRASE = "fis.client.user.identityFilePassphrase";
    private static final String CLIENT_USER_EXECUTE_AS_USER =  "fis.client.user.executeAsUser";
    
    private UserInfo userInfo;

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
    
    @Before
    public void prepareUserInfo() {
        if(!StringUtils.isBlank(System.getProperty(CLIENT_USER_NAME))) {
            userInfo = new UserInfo(System.getProperty(CLIENT_USER_NAME),System.getProperty(CLIENT_USER_PASSWORD),System.getProperty(CLIENT_USER_IDENTITY_FILE),System.getProperty(CLIENT_USER_IDENTITY_FILE_PASS_PHRASE), Boolean.parseBoolean(System.getProperty(CLIENT_USER_EXECUTE_AS_USER, "false")));
        }
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Properties p = new Properties();
        p.putAll(initialProperties);
        System.setProperties(p);
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
}
