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
package eu.ddmore.fis.configuration;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Handles FIS properties and makes sure that the System properties override the default properties and properties from the config files.
 */
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
        printProperties(properties);
        
        validateProperties(properties);
    }

    private void printProperties(Properties properties) {
        System.out.println(StringUtils.repeat("*", 60));
        System.out.println("Properties:");
        for(Entry<Object,Object> en: properties.entrySet()) {
            System.out.println(String.format("%s = %s", en.getKey(), en.getValue()));
        }
        System.out.println(StringUtils.repeat("*", 60));
    }

    private void validateProperties(Properties properties) {
        final String[] requiredProperties = new String[] {
            "fis.retrieveOutputs", "mif.url", "fis.cts.url", "execution.host.fileshare.local", "execution.host.fileshare", "execution.host.fileshare.remote"
        };
        StringBuilder builder = new StringBuilder();
        for(String prop : requiredProperties) {
            String value = properties.getProperty(prop);
            
            if(StringUtils.isBlank(value)) {
                builder.append(String.format("Property %s was not set!\n", prop));
            }
        }
        String errorMsg = builder.toString();
        
        if(!StringUtils.isBlank(errorMsg)) {
            System.err.println(StringUtils.repeat("*", 60));
            System.err.println("ERROR!!!");
            System.err.println(StringUtils.repeat("*", 60));
            System.err.println(errorMsg);
            throw new IllegalStateException("Error when initialising service:\n" + errorMsg);
        }
    }
}
