package eu.ddmore.fis.service;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;

import eu.ddmore.fis.controllers.JobResourceProcessor;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;

/**
 * Default implementation of {@link JobDispatcher}
 */
public class JobDispatcherImpl implements JobDispatcher {

	private MIFHttpRestClient mifClient;
	private String mifUserName = "tel-user";
	private JobResourceProcessor jobResourcePublisher;
	private CommandRegistry commandRegistry;

	public LocalJob dispatch(LocalJob job) {

		// Invoke the publishInputs Groovy script
		final LocalJob publishedJob = this.jobResourcePublisher.process(job);

		if (publishedJob.getStatus() != LocalJobStatus.FAILED) {
			// Only continue if the pre-processing was successful

			final CommandExecutionTarget commandTarget = this.commandRegistry.resolveExecutionTargetFor(publishedJob.getCommand());
			final ExecutionRequest executionRequest = buildExecutionRequest(publishedJob, commandTarget);

			// The retrieveOutputs Groovy script needs to know the (MIF-connector-specific) file patterns it needs to copy back
			// from the MIF working directory to the FIS working directory. Ideally there would be a cleaner way than bunging
			// these in the LocalJob object, but this is the easiest solution since the LocalJob gets passed all the way through
			// to the Groovy script itself (as a bound variable).
			// Also, this would allow TEL-R (or another FIS client) to access the output filenames regex if it needed to for
			// whatever reason, such as copying files back from the FIS working directory to a user source directory.
			publishedJob.setOutputFilenamesRegex(commandTarget.getOutputFilenamesRegex());

			this.mifClient.executeJob(executionRequest);
		}

		return publishedJob;
	}

	private ExecutionRequest buildExecutionRequest(LocalJob job, CommandExecutionTarget commandTarget) {
		Preconditions.checkNotNull(job, "Job can't be null");
		Preconditions.checkNotNull(commandTarget, "Command Target can't be null");
		ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder().setRequestId(job.getId()).setName("FIS Service Job")
		        .setExecutionType(commandTarget.getExecutionType()).setExecutionFile(job.getControlFile())
		        .setCommand(commandTarget.getToolExecutablePath()).setSubmitAsUserMode(false).setUserName(mifUserName);
		requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE", job.getWorkingDirectory());
		requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE_REMOTE", job.getWorkingDirectory());
		if (commandTarget.getConverterToolboxPath() != null) {
			requestBuilder.addAttribute("CONVERTER_TOOLBOX_PATH", commandTarget.getConverterToolboxPath());
		}
		requestBuilder.setSubmitHostPreamble(commandTarget.getEnvironmentSetupScript());
		requestBuilder.setExecutionParameters(job.getCommandParameters());
		return requestBuilder.getExecutionRequest();
	}

	public MIFHttpRestClient getMifClient() {
		return mifClient;
	}

	@Required
	public void setMifClient(MIFHttpRestClient mifClient) {
		this.mifClient = mifClient;
	}

	public JobResourceProcessor getJobResourcePublisher() {
		return jobResourcePublisher;
	}

	@Required
	public void setJobResourcePublisher(JobResourceProcessor jobResourcePublisher) {
		this.jobResourcePublisher = jobResourcePublisher;
	}

	@Required
	public void setCommandRegistry(CommandRegistry commandRegistry) {
		this.commandRegistry = commandRegistry;
	}

	public CommandRegistry getCommandRegistry() {
		return this.commandRegistry;
	}
}
