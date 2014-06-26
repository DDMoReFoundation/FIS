/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mango.mif.MIFHttpRestClient;

@Controller
@RequestMapping("/healthcheck")
public class HealthcheckController {
    
    private HealthEndpoint<Map<String,Object>> healthEndpoint;
    
	private MIFHttpRestClient mifClient;
	
	/**
	 * Checks the health of mif and of FIS itself. 
	 * @return 'ok' if everything is ok, 'MIF is not running' if MIF is down. 
	 */
    @RequestMapping(method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String healthcheck() {
        if(!mifClient.healthcheck()) {
            return "MIF is not running";
        }
        return healthEndpoint.invoke().get("status").toString();
    }

    @Required
    public void setMifClient(MIFHttpRestClient mifClient) {
        this.mifClient = mifClient;
    }
    
    public MIFHttpRestClient getMifClient() {
        return mifClient;
    }

    @Required
    public void setHealthEndpoint(HealthEndpoint<Map<String,Object>> healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }
}
