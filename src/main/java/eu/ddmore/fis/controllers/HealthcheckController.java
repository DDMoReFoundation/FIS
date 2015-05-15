/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.ddmore.fis.service.HealthDetail;
import eu.ddmore.fis.service.mif.Healthcheck;

/**
 * This class exposes additional SpringBoot's healthcheck endpoint.
 * It provides basic "UP" or error message response.
 * 
 * This is a legacy class, clients should use '/health' endpoint instead and parse JSON returned by that endpoint.
 * @deprecated clients should use '/health' endpoint exposed at management port (default 9011) and parse JSON that is returned
 */
@Controller
@RequestMapping("/healthcheck")
@Deprecated
public class HealthcheckController {
    private static final Logger LOG = Logger.getLogger(HealthcheckController.class);
    private HealthEndpoint healthEndpoint;
    
    @Autowired(required=true)
    @Qualifier("mifHealth")
	private Healthcheck mifHealthcheck;

    @Autowired(required=true)
    @Qualifier("ctsHealth")
    private eu.ddmore.fis.service.cts.Healthcheck ctsHealthcheck;
	
	/**
	 * Checks the health of MIF, CTS and of FIS itself. 
	 * @return 'UP' if everything is ok, error message otherwise. 
	 */
    @RequestMapping(method=RequestMethod.GET, produces={MediaType.TEXT_HTML_VALUE})
    public @ResponseBody String healthcheck() {
        LOG.debug("Handling healthcheck request");
        Health mifHealth = mifHealthcheck.health();
        if(!mifHealth.getStatus().equals(Status.UP)) {
            Object error = mifHealth.getDetails().get(HealthDetail.ERROR);
            if(error!=null) {
                return error.toString();
            } else {
                return "MIF is not running";
            }
        }
        Health ctsHealth = ctsHealthcheck.health();
        if(!ctsHealth.getStatus().equals(Status.UP)) {
            Object error = ctsHealth.getDetails().get(HealthDetail.ERROR);
            if(error!=null) {
                return error.toString();
            } else {
                return "CTS is not running";
            }
        }
        Status fisStatus = healthEndpoint.invoke().getStatus();
        LOG.debug(String.format("System health is %s", fisStatus));
        return fisStatus.getCode();
    }

    @Required
    public void setHealthEndpoint(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }
}
