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
package eu.ddmore.fis.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ClientAvailableConnectorDetails;


/**
 * Implementation of the command registry that allows for either pre-defined {@link ClientAvailableConnectorDetails}
 * to be provided via Spring config within FIS, or for {@link ClientAvailableConnectorDetails} to be lazily
 * auto-discovered from TES (MIF) the first time an execution request is made.
 * <p>
 * Which constructor is used determines which of these modes is used.
 */
public class CommandRegistryImpl implements CommandRegistry {
    private static final Logger LOGGER = Logger.getLogger(CommandRegistryImpl.class);

	private MIFHttpRestClient mifClient;
    
    // Map of executionType to Client-Available Connector Details
    private Map<String, ClientAvailableConnectorDetails> connectorDetails = null;
    
    /**
     * Construct a Command Registry with an empty set of {@link ClientAvailableConnectorDetails}.
     * This is used when FIS is to auto-discover Client-Available Connector Details exposed by TES (MIF).
     */
    public CommandRegistryImpl() {
    	LOGGER.info("FIS is running in connector-auto-discovery mode: Initialising an empty Command Registry.");
    	this.connectorDetails = new HashMap<String, ClientAvailableConnectorDetails>();
    }
    
    /**
     * Construct a Command Registry with a predefined set of Client-Available Connector Details,
     * defined in FIS Spring configuration. This is the "old way" since this precludes being able
     * tp drop new Connector JARs in to the connectors directory in MIF and have them able to be
     * used straightaway.
     * <p>
     * @param connectorDetails - Map of executionType to {@link ClientAvailableConnectorDetails} to populate with
     */
    @Deprecated
    public CommandRegistryImpl(Map<String, ClientAvailableConnectorDetails> connectorDetails) {
        Preconditions.checkNotNull(connectorDetails, "Client-Available Connector Details can't be null");
        LOGGER.warn("FIS is NOT running in connector-auto-discovery mode: Populating the Command Registry with the pre-defined Client-Available Connector Details.");
        this.connectorDetails = connectorDetails;
    }
    
    @Override
    public ClientAvailableConnectorDetails resolveClientAvailableConnectorDetailsFor(final String executionType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(executionType), String.format("Execution Type must be non-empty string"));
        
        // Lazily discover the Client-Available Connector Details exposed by TES (MIF)
        if (MapUtils.isEmpty(this.connectorDetails)) {
        	discoverConnectorDetails();
        }

        if (!this.connectorDetails.containsKey(executionType)) {
        	throw new NoSuchElementException(String.format("Client-Available Connector Details for execution type %s was not found", executionType));
        }
        return this.connectorDetails.get(executionType);
    }
    
    /**
     * Discover {@link ClientAvailableConnectorDetails} from TES (MIF) and add each one to the internal Set.
     */
    private void discoverConnectorDetails() {
    	final Map<String, ClientAvailableConnectorDetails> clientAvailableConnectorDetails = this.mifClient.getClientAvailableConnectorDetails();
    	LOGGER.info("Discovered Client-Available Connector Details of TES Connectors for executionTypes: " + clientAvailableConnectorDetails.keySet());
    	this.connectorDetails.putAll(clientAvailableConnectorDetails);
    }
    
	public MIFHttpRestClient getMifClient() {
		return this.mifClient;
	}

	@Required
	public void setMifClient(MIFHttpRestClient mifClient) {
		this.mifClient = mifClient;
	}
    
}
