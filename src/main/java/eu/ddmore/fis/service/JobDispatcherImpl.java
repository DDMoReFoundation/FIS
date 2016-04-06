/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.convert.ConversionService;

import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.core.exec.invoker.InvokerParameters;
import com.mango.mif.domain.ClientAvailableConnectorDetails;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;

import eu.ddmore.fis.configuration.Fileshare;
import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;

/**
 * Default implementation of {@link JobDispatcher}
 */
public class JobDispatcherImpl implements JobDispatcher {

    private final static Logger LOG = Logger.getLogger(JobDispatcherImpl.class);

    private MIFHttpRestClient mifClient;
    private JobResourceProcessor jobResourcePublisher;
    private CommandRegistry commandRegistry;
    private Fileshare fileshare;
    private ConversionService conversionService;

    public LocalJob dispatch(LocalJob job) {
        Preconditions.checkNotNull(job, "Job can't be null.");
        final ClientAvailableConnectorDetails clientAvailableConnectorDetails = this.commandRegistry
                .resolveClientAvailableConnectorDetailsFor(job.getExecutionType());

        Preconditions.checkNotNull(clientAvailableConnectorDetails,
            String.format("Could not retrieve connector details for execution type %s.", job.getExecutionType()));

        // Invoke the publishInputs Groovy script
        LOG.info(String.format(
            "About to publish inputs for job:  Execution File = %s  Working Directory = %s  Extra Input Files = %s",
            job.getExecutionFile(), job.getWorkingDirectory(), job.getExtraInputFiles()));
        final LocalJob publishedJob = this.jobResourcePublisher.process(job);

        LOG.info(String.format(
            "Published inputs for job:  Execution File = %s  Working Directory = %s  Extra Input Files = %s",
            job.getExecutionFile(), job.getWorkingDirectory(), job.getExtraInputFiles()));
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
            
            LOG.info(String
                    .format(
                        "About to submit job execution request: Request ID = %s Type = %s Execution File = %s Execution Parameters = %s  Submit As User Mode = %s  User Name = %s",
                        executionRequest.getRequestId(), executionRequest.getType(), executionRequest.getExecutionFile(), executionRequest.getExecutionParameters(),
                        executionRequest.getSubmitAsUserMode(), executionRequest.getUserName()
                        ));

            this.mifClient.executeJob(executionRequest);
        }

        return publishedJob;
    }

    private ExecutionRequest buildExecutionRequest(LocalJob job) {
        Preconditions.checkNotNull(job, "Job can't be null");
        final ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder().setRequestId(job.getId()).setName("FIS Service Job")
                .setExecutionType(job.getExecutionType()).setExecutionFile(job.getExecutionFile())
                .setSubmitAsUserMode(job.getUserInfo().isExecuteAsUser()).setUserName(job.getUserInfo().getUserName())
                .setUserPassword(job.getUserInfo().getPassword()).setExecutionParameters(job.getCommandParameters())
                .setInvokerParameters(conversionService.convert(job.getUserInfo(), InvokerParameters.class));
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE", fileshare.getMifHostPath());
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE_REMOTE", fileshare.getExecutionHostPath());
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
    public void setFileshare(Fileshare fileshare) {
        this.fileshare = fileshare;
    }

    public Fileshare getFileshare() {
        return fileshare;
    }

    @Required
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public ConversionService getConversionService() {
        return conversionService;
    }

}
