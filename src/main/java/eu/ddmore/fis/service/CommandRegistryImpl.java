package eu.ddmore.fis.service;

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
    
    private Set<ClientAvailableConnectorDetails> connectorDetails = null;
    
    /**
     * Construct a Command Registry with an empty set of {@link ClientAvailableConnectorDetails}.
     * This is used when FIS is to auto-discover Client-Available Connector Details exposed by TES (MIF).
     */
    public CommandRegistryImpl() {
    	LOGGER.info("FIS is running in connector-auto-discovery mode: Initialising an empty Command Registry.");
    	this.connectorDetails = new HashSet<ClientAvailableConnectorDetails>();
    }
    
    /**
     * Construct a Command Registry with a predefined set of Client-Available Connector Details,
     * defined in FIS Spring configuration. This is the "old way" since this precludes being able
     * tp drop new Connector JARs in to the connectors directory in MIF and have them able to be
     * used straightaway.
     * <p>
     * @param connectorDetails - set of {@link ClientAvailableConnectorDetails} to populate with
     */
    @Deprecated
    public CommandRegistryImpl(Set<ClientAvailableConnectorDetails> connectorDetails) {
        Preconditions.checkNotNull(connectorDetails, "Client-Available Connector Details can't be null");
        LOGGER.warn("FIS is NOT running in connector-auto-discovery mode: Populating the Command Registry with the pre-defined Client-Available Connector Details.");
        this.connectorDetails = connectorDetails;
    }
    
    @Override
    public ClientAvailableConnectorDetails resolveClientAvailableConnectorDetailsFor(final String command) {
        Preconditions.checkArgument(StringUtils.isNotBlank(command), String.format("Command must be non-empty string"));
        
        // Lazily discover the Client-Available Connector Details exposed by TES (MIF)
        if (CollectionUtils.isEmpty(this.connectorDetails)) {
        	discoverConnectorDetails();
        }
        
        Set<ClientAvailableConnectorDetails> matchingConnectorDetails = Sets.filter(this.connectorDetails, new Predicate<ClientAvailableConnectorDetails>() {
            @Override
            public boolean apply(final ClientAvailableConnectorDetails cacd) {
                return command.equals(cacd.getCommand());
            }
        });
        if (matchingConnectorDetails.size() != 1) {
            if (matchingConnectorDetails.size() == 0 ) {
                throw new NoSuchElementException(String.format("Client-Available Connector Details for command %s was not found", command));
            } else {
                throw new IllegalStateException(String.format("Found more than one Client-Available Connector Details for command %s", command));
            }
        }
        return Iterables.getOnlyElement(matchingConnectorDetails);
    }
    
    /**
     * Discover {@link ClientAvailableConnectorDetails} from TES (MIF) and add each one to the internal Set.
     */
    private void discoverConnectorDetails() {
    	for (final Map.Entry<String, String> entry : this.mifClient.getClientAvailableConnectorDetails().entrySet()) {
    		LOGGER.info("Discovered Client-Available Connector Details of a TES Connector: executionType=" + entry.getKey() + ",clientAvailableConnectorDetails=" + entry.getValue());
    		try {
    			// Re-create the Client-Available Connector Details object from the JSON
	            final ClientAvailableConnectorDetails connectorDetails = new ObjectMapper().readValue(entry.getValue(), ClientAvailableConnectorDetails.class);
	            // And add it to the registry
	            this.connectorDetails.add(connectorDetails);
	            LOGGER.info("Successfully added Client-Available Connector Details for executionType " + entry.getKey() + " to the CommandRegistry!");
            } catch (Exception e) {
	            LOGGER.error("Error deserialising JSON when discovering Client-Available Connector Details for " + entry.getValue(), e);
	            throw new RuntimeException("Error deserialising JSON when discovering Client-Available Connector Details for " + entry.getValue(), e);
            }
    	}
    }
    
	public MIFHttpRestClient getMifClient() {
		return this.mifClient;
	}

	@Required
	public void setMifClient(MIFHttpRestClient mifClient) {
		this.mifClient = mifClient;
	}
    
}
