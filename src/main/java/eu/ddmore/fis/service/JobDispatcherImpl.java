package eu.ddmore.fis.service;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.mango.mif.MIFHttpRestClient;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;

import eu.ddmore.fis.controllers.JobResourceProcessor;
import eu.ddmore.fis.domain.LocalJob;

/**
 * Default implementation of {@link JobDispatcher}
 */
public class JobDispatcherImpl implements JobDispatcher {
    private MIFHttpRestClient mifClient;
    private String mifUserName = "tel-user";
    private JobResourceProcessor jobResourcePublisher;
    private CommandRegistry commandRegistry;
    public LocalJob dispatch(LocalJob job) {
        LocalJob publishedJob = jobResourcePublisher.process(job);
        CommandExecutionTarget commandTarget = commandRegistry.resolveExecutionTargetFor(publishedJob.getCommand());
        ExecutionRequest executionRequest = buildExecutionRequest(publishedJob, commandTarget);
        mifClient.executeJob(executionRequest);
        return publishedJob;
    }

    @VisibleForTesting
    ExecutionRequest buildExecutionRequest(LocalJob job, CommandExecutionTarget commandTarget) {
        Preconditions.checkNotNull(job, "Job can't be null");
        Preconditions.checkNotNull(commandTarget, "Command Target can't be null");
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder().setRequestId(job.getId())
                .setName("FIS Service Job").setExecutionType(commandTarget.getExecutionType())
                .setExecutionFile(job.getControlFile()).setCommand(commandTarget.getToolExecutablePath())
                .setSubmitAsUserMode(false).setUserName(mifUserName);
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE", job.getWorkingDirectory());
        requestBuilder.addAttribute("EXECUTION_HOST_FILESHARE_REMOTE", job.getWorkingDirectory());
        requestBuilder.addAttribute("CONVERTER_TOOLBOX_PATH", commandTarget.getConverterToolboxPath());
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
        return commandRegistry;
    }
}