package eu.ddmore.fis.service;

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

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


/**
 * Implementation of the command registry that allows for either pre-defined {@link CommandExecutionTarget}s
 * to be provided via Spring config within FIS, or for {@link CommandExecutionTarget}s to be lazily
 * auto-discovered from TES (MIF) the first time an execution request is made.
 * <p>
 * Which constructor is used determines which of these modes is used.
 */
public class CommandRegistryImpl implements CommandRegistry {
    private static final Logger LOGGER = Logger.getLogger(CommandRegistryImpl.class);

	private MIFHttpRestClient mifClient;
    
    private Set<CommandExecutionTarget> executionTargets = null;
    
    /**
     * Construct a Command Registry with an empty set of {@link CommandExecutionTarget}s.
     * This is used when FIS is to auto-discover Command Execution Targets exposed by TES (MIF).
     */
    public CommandRegistryImpl() {
    	LOGGER.info("FIS is running in connector-auto-discovery mode: Initialising an empty Command Registry.");
    	this.executionTargets = new HashSet<CommandExecutionTarget>();
    }
    
    /**
     * Construct a Command Registry with a predefined set of Command Execution Targets,
     * defined in FIS Spring configuration. This is the "old way" since such details
     * are specific to TES (MIF) Connectors and shouldn't be stored within FIS.
     * <p>
     * @param executionTargets - set of {@link CommandExecutionTarget}s to populate with
     */
    @Deprecated
    public CommandRegistryImpl(Set<CommandExecutionTarget> executionTargets) {
        Preconditions.checkNotNull(executionTargets, "Command Execution Targets can't be null");
        LOGGER.warn("FIS is NOT running in connector-auto-discovery mode: Populating the Command Registry with the pre-defined Command Execution Targets.");
        this.executionTargets = executionTargets;
    }
    
    @Override
    public CommandExecutionTarget resolveExecutionTargetFor(final String command) {
        Preconditions.checkArgument(StringUtils.isNotBlank(command), String.format("Command must be non-empty string"));
        
        // Lazily discover the Command Execution Targets exposed by TES (MIF)
        if (CollectionUtils.isEmpty(this.executionTargets)) {
        	discoverCommandExecutionTargets();
        }
        
        Set<CommandExecutionTarget> targets = Sets.filter(this.executionTargets, new Predicate<CommandExecutionTarget>() {
            @Override
            public boolean apply(@Nullable
            CommandExecutionTarget element) {
                return command.equals(element.getCommand());
            }
        });
        if (targets.size() != 1) {
            if (targets.size() == 0 ) {
                throw new NoSuchElementException(String.format("Execution target for command %s was not found", command));
            } else {
                throw new IllegalStateException(String.format("Found more than one execution target for command %s", command));
            }
        }
        return Iterables.getOnlyElement(targets);
    }
    
    /**
     * Discover {@link CommandExecutionTarget}s from TES (MIF) and add each one to the internal Set.
     */
    private void discoverCommandExecutionTargets() {
    	for (final Map.Entry<String, String> entry : this.mifClient.getCommandExecutionTargetDetails().entrySet()) {
    		LOGGER.info("Discovered CommandExecutionTarget of a TES Connector: executionType=" + entry.getKey() + ",commandExecutionTarget=" + entry.getValue());
    		try {
    			// Re-create the Command Execution Target object from the JSON
	            final CommandExecutionTarget executionTarget = new ObjectMapper().readValue(entry.getValue(), CommandExecutionTarget.class);
	            // And add it to the registry
	            this.executionTargets.add(executionTarget);
	            LOGGER.info("Successfully added Command Execution Target for executionType " + entry.getKey() + " to the CommandRegistry!");
            } catch (Exception e) {
	            LOGGER.error("Error deserialising JSON when discovering Command Execution Target for " + entry.getValue(), e);
	            throw new RuntimeException("Error deserialising JSON when discovering Command Execution Target for " + entry.getValue(), e);
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
