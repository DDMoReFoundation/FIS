package eu.ddmore.fis.service;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;


/**
 * Basic implementation of the command registry
 */
public class CommandRegistryImpl implements CommandRegistry{
    
    private Set<CommandExecutionTarget> executionTargets;
    
    public CommandRegistryImpl(Set<CommandExecutionTarget> executionTargets) {
        Preconditions.checkNotNull(executionTargets, "Command Execution Targets can't be null");
        this.executionTargets = executionTargets;
    }
    
    @Override
    public CommandExecutionTarget resolveExecutionTargetFor(final String command) {
        Preconditions.checkArgument(StringUtils.isNotBlank(command), String.format("Command must be non-empty string, but was %s",command));
        Set<CommandExecutionTarget> targets = Sets.filter(executionTargets, new Predicate<CommandExecutionTarget>() {
            @Override
            public boolean apply(@Nullable
            CommandExecutionTarget element) {
                return command.equals(element.getCommand());
            }
        });
        if(targets.size()!=1) {
            if(targets.size()==0) {
                throw new NoSuchElementException(String.format("Execution target for command %s was not found", command));
            } else {
                throw new IllegalStateException(String.format("Found more than one execution target for command %s", command));
            }
            
        }
        return Iterables.getOnlyElement(targets);
    }
}
