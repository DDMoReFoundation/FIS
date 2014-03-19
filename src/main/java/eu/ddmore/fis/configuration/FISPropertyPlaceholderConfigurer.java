package eu.ddmore.fis.configuration;

import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;


public class FISPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties properties) throws BeansException {
        Properties sysProperties = System.getProperties();
        for(Entry<Object,Object> en: sysProperties.entrySet()) {
            if(properties.containsKey(en.getKey()) && en.getValue()!=null) {
                properties.setProperty(en.getKey().toString(), en.getValue().toString());
            }
        }
        super.processProperties(beanFactory, properties);
    }
}
