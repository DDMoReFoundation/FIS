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
