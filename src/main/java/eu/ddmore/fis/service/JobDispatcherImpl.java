/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;

/**
 * Default implementation of {@link JobDispatcher}
 */
public class JobDispatcherImpl implements JobDispatcher {

    private final static Logger LOGGER = Logger.getLogger(JobDispatcherImpl.class);

    final static String DEFAULT_MIF_USERNAME = "tel-user";

	private MIFHttpRestClient mifClient;
	private String mifUserName;
	private String mifUserPassword;
	private JobResourceProcessor jobResourcePublisher;
	private CommandRegistry commandRegistry;
	private String executionHostFileshare;
	private String executionHostFileshareRemote;

	public LocalJob dispatch(LocalJob job) {
	    Preconditions.checkNotNull(job, "Job can't be null.");
        final ClientAvailableConnectorDetails clientAvailableConnectorDetails =
                this.commandRegistry.resolveClientAvailableConnectorDetailsFor(job.getExecutionType());
        
        Preconditions.checkNotNull(clientAvailableConnectorDetails, String.format("Could not retrieve connector details for execution type %s.", job.getExecutionType()));
        
		// Invoke the publishInputs Groovy script
		LOGGER.info(String.format("About to publish inputs for job:\n  Execution File = %1$s\n  Working Directory = %2$s\n  Extra Input Files = %3$s",
		    job.getExecutionFile(), job.getWorkingDirectory(), job.getExtraInputFiles()
		));
		final LocalJob publishedJob = this.jobResourcePublisher.process(job);

		if (publishedJob.getStatus() != LocalJobStatus.FAILED) {
			final ExecutionRequest executionRequest = buildExecutionRequest(publishedJob);

			// The retrieveOutputs Groovy script needs to know the (MIF-connector-specific) file patterns it needs to copy back
			// from the MIF working directory to the FIS working directory. Ideally there would be a cleaner way than bunging
			// these in the LocalJob object, but this is the easiest solution since the LocalJob gets passed all the way through
			// to the Groovy script itself (as a bound variable).
			// Also, this would allow TEL-R (or another FIS client) to access the output filenames regex if it needed to for
			// whatever reason, such as copying files back from the FIS working directory to a user source directory.
			publishedJob.setResultsIncludeRegex(clientAvailableConnectorDetails.getResultsIncludeRegex());
			publishedJob.setResultsExcludeRegex(clientAvailableConnectorDetails.getResultsExcludeRegex());

	        LOGGER.info(
	            String.format("About to submit job execution request:\n  Type = %1$s\n  Execution File = %2$s\n  Execution Parameters = %3$s\n  Submit As User Mode = %4$s\n  User Name = %5$s\n  User Password (Encrypted) = %6$s\n  Request ID = %7$s",
	            executionRequest.getType(), executionRequest.getExecutionFile(), executionRequest.getExecutionParameters(), executionRequest.getSubmitAsUserMode(), executionRequest.getUserName(), executionRequest.getUserPassword(), executionRequest.getRequestId()
            ));

			this.mifClient.executeJob(executionRequest);
		}

		return publishedJob;
	}

	private ExecutionRequest buildExecutionRequest(LocalJob job) {
		Preconditions.checkNotNull(job, "Job can't be null");
		
		// Determine whether to execute the job with specific user credentials
		boolean submitAsUserMode = false;
		String userName = DEFAULT_MIF_USERNAME;
		String userPassword = null;
		if (StringUtils.isNotBlank(this.mifUserName)) {
		    submitAsUserMode = true;
		    userName = this.mifUserName;
		    userPassword = this.mifUserPassword;
		}
		
		final ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder()
			.setRequestId(job.getId())
			.setName("FIS Service Job")
			.setExecutionType(job.getExecutionType())
			.setExecutionFile(job.getExecutionFile())
			.setSubmitAsUserMode(submitAsUserMode)
			.setUserName(userName)
			.setUserPassword(userPassword)
			.setExecutionParameters(job.getCommandParameters());
		requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE", getExecutionHostFileshare());
		requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE_REMOTE", getExecutionHostFileshareRemote());
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

	@Required
	public void setExecutionHostFileshare(String executionHostFileshare) {
		this.executionHostFileshare = executionHostFileshare;
	}
	
	public String getExecutionHostFileshare() {
		return this.executionHostFileshare;
	}
	
	@Required
	public void setExecutionHostFileshareRemote(String executionHostFileshareRemote) {
		this.executionHostFileshareRemote = executionHostFileshareRemote;
	}
	
	public String getExecutionHostFileshareRemote() {
		return this.executionHostFileshareRemote;
	}
	
	@Value("${fis.mif.userName}")
	public void setMifUserName(final String mifUserName) {
	    this.mifUserName = mifUserName;
	}
	
	public String getMifUserName() {
	    return this.mifUserName;
	}
	
    @Value("${fis.mif.userPassword}")
    public void setMifUserPassword(final String mifUserPassword) {
        this.mifUserPassword = mifUserPassword;
    }
    
    public String getMifUserPassword() {
        return this.mifUserPassword;
    }
	
}
