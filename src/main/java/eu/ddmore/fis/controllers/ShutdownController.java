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

import eu.ddmore.fis.service.integration.ShutdownHandler;

@Controller
@RequestMapping("/shutdown")
public class ShutdownController {
    
    private static final Logger LOG = Logger.getLogger(ShutdownController.class);
    
    @Autowired
    private ShutdownEndpoint shutdownEndpoint;
    
    private ShutdownHandler mifShutdown;

    private ShutdownHandler ctsShutdown;
    /**
     * Kills MIF, CTS and FIS itself. This method will not fail if MIF or CTS communication fails. 
     * @return 'OK' if the request was processed correctly. 
     */
    @RequestMapping(method=RequestMethod.POST, produces={MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody String shutdown() {
        try {
            mifShutdown.invoke();
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
    public void setMifShutdown(ShutdownHandler mifShutdown) {
        this.mifShutdown = mifShutdown;
    }
    
    public ShutdownHandler getMifShutdown() {
        return mifShutdown;
    }
    
    public void setShutdownEndpoint(ShutdownEndpoint shutdownEndpoint) {
        this.shutdownEndpoint = shutdownEndpoint;
    }

    @Required
    public void setCtsShutdown(ShutdownHandler ctsShutdown) {
        this.ctsShutdown = ctsShutdown;
    }
    
    public ShutdownHandler getCtsShutdown() {
        return ctsShutdown;
    }
}
