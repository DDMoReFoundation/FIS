/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.converters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.core.convert.converter.Converter;

import com.mango.mif.core.exec.invoker.InvokerParameters;
import com.mango.mif.core.exec.jsch.JschInvokerParameters;

import eu.ddmore.fis.domain.UserInfo;


/**
 * Converts {@link UserInfo} to {@link InvokerParameters}
 */
public class UserInfoToInvokerParametersConverter implements Converter<UserInfo, InvokerParameters> {
    private final static String NULL_HOST = null;
    
    @NotBlank
    private String jschAuthenticationProtocols;
    
    @Override
    public InvokerParameters convert(UserInfo userInfo) {
        Map<String,String> parameters = new HashMap<String,String>();
        putIfNotBlank(parameters, JschInvokerParameters.IDENTITY_FILE_PROP, userInfo.getIdentityFilePath());
        putIfNotBlank(parameters, JschInvokerParameters.IDENTITY_FILE_PASSPHRASE_PROP, userInfo.getIdentityFilePassphrase());
        if(parameters.containsKey(JschInvokerParameters.IDENTITY_FILE_PROP)) {
            parameters.put(JschInvokerParameters.AUTH_PROTOCOLS_PROP, jschAuthenticationProtocols);
        }
        InvokerParameters result = new JschInvokerParameters(userInfo.getUserName(), userInfo.getPassword(), NULL_HOST, InvokerParameters.NULL_PORT, parameters);
        return result;
    }
    
    public void setJschAuthenticationProtocols(String jschAuthenticationProtocols) {
        this.jschAuthenticationProtocols = jschAuthenticationProtocols;
    }
    
    public String getJschAuthenticationProtocols() {
        return jschAuthenticationProtocols;
    }

    private void putIfNotBlank(Map<String, String> parameters, String property, String value) {
        if(StringUtils.isNotBlank(value)) {
            parameters.put(property, value);
        }
    }

}
