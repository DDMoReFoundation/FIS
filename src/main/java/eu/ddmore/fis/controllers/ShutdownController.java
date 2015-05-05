/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mango.mif.MIFHttpRestClient;

import eu.ddmore.fis.service.cts.CTSShutdown;

@Controller
@RequestMapping("/shutdown")
public class ShutdownController {
    
    private static final Logger LOG = Logger.getLogger(ShutdownController.class);
    
    @Autowired
    private ShutdownEndpoint shutdownEndpoint;
    
	private MIFHttpRestClient mifClient;

    private CTSShutdown ctsShutdown;
    /**
     * Kills MIF and FIS itself. It will not fail if it can't communicate with MIF - MIF not running should not prevent FIS from shutting down. 
     * @return 'OK' if the request was processed correctly. 
     */
    @RequestMapping(method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String shutdown() {
        try {
            mifClient.killMif();
        } catch(Exception ex) {
            LOG.warn("Exception was thrown when tried to kill MIF", ex);
        }
        try {
            ctsShutdown.invoke();
        } catch(Exception ex) {
            LOG.warn("Exception was thrown when tried to kill CTS", ex);
        }
        shutdownEndpoint.invoke();
        return "OK";
    }

    @Required
    public void setMifClient(MIFHttpRestClient mifClient) {
        this.mifClient = mifClient;
    }
    
    public MIFHttpRestClient getMifClient() {
        return mifClient;
    }
    
    public void setShutdownEndpoint(ShutdownEndpoint shutdownEndpoint) {
        this.shutdownEndpoint = shutdownEndpoint;
    }

    @Required
    public void setCtsShutdown(CTSShutdown ctsShutdown) {
        this.ctsShutdown = ctsShutdown;
    }
    
    public CTSShutdown getCtsShutdown() {
        return ctsShutdown;
    }
}
