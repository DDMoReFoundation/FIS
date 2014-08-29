package eu.ddmore.fis.service;

import org.springframework.beans.factory.annotation.Required;


/**
 * Represents a execution target (TPT and Environment) runtime parameters associated with a given command.
 */
public class CommandExecutionTarget {

	private String executionType; // Keyed on this
	private String converterToolboxPath;
	private String environmentSetupScript;
	private String toolExecutablePath;
	private String command;
	private String outputFilenamesRegex;

	/**
	 * TODO: This should be renamed to connectorId since this is what it actually represents,
	 *       and also there is already an executionType property on ConnectorDescriptor.
	 *       The same change will need to be made to the corresponding CommandExecutionTarget
	 *       class in MIFServer too.
	 * <p>
	 * @return the connectorId key, which must be unique across all Connectors
	 */
	public String getExecutionType() {
		return executionType;
	}
	
	/**
	 * @param executionType - the connectorId key to set
	 */
	@Required
	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}
	
	/**
	 * @return the unique command string as passed to TEL-R submit.job() function
	 * 		   and resolved against in {@link eu.ddmore.fis.service.CommandRegistry#resolveExecutionTargetFor(String)}
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command - the unique command string as passed to TEL-R submit.job() function
	 * 		  and resolved against in {@link eu.ddmore.fis.service.CommandRegistry#resolveExecutionTargetFor(String)}
	 */
	@Required
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the executable to run
	 */
	public String getToolExecutablePath() {
		return toolExecutablePath;
	}
	
	/**
	 * @param toolExecutablePath - the executable to run
	 */
	public void setToolExecutablePath(String toolExecutablePath) {
		this.toolExecutablePath = toolExecutablePath;
	}
	
	public String getConverterToolboxPath() {
		return converterToolboxPath;
	}

	public void setConverterToolboxPath(String converterToolboxPath) {
		this.converterToolboxPath = converterToolboxPath;
	}

	/**
	 * @return a setup script to be run prior to each execution of a command
	 */
	public String getEnvironmentSetupScript() {
		return environmentSetupScript;
	}

	/**
	 * @param environmentSetupScript - a setup script to be run prior to each execution of a command
	 */
	public void setEnvironmentSetupScript(String environmentSetupScript) {
		this.environmentSetupScript = environmentSetupScript;
	}

	/**
	 * @param outputFilenamesRegex - the regular expression that would pick up the output files produced
	 * 								 by executing the command; will be used by FIS when copying files back
	 */
	public void setOutputFilenamesRegex(final String outputFilenamesRegex) {
		this.outputFilenamesRegex = outputFilenamesRegex;
	}

	/**
	 * @return the regular expression that would pick up the output files produced by executing the command;
	 * 		   will be used by FIS when copying files back
	 */
	public String getOutputFilenamesRegex() {
		return this.outputFilenamesRegex;
	}

}
