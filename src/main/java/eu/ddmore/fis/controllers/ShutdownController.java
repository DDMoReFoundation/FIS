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
     * Kills MIF, CTS and FIS itself. This method will not fail if MIF or CTS communication fails. 
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
