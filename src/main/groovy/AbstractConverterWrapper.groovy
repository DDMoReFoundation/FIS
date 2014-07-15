
import groovy.lang.Binding;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.launcher.CommandLauncherFactory
import org.apache.commons.exec.Executor
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.PumpStreamHandler

import org.apache.log4j.Logger


/**
 * Wrapper superclass to be extended by specific MDL -> PharmML and MDL -> NMTRAN conversion scripts.
 */
class AbstractConverterWrapper {

    private final static LOGGER = Logger.getLogger(AbstractConverterWrapper.class)

    private String converterToolboxExecutable
    private String srcLang
    private String srcVersion
    private String targetLang
    private String targetVersion
    private String outputModelFileExt
    private Executor executor

    /**
     * Construct a Converter Wrapper configured with the supplied parameters.
     * <p>
     * @param binding - the Groovy Binding passed through from the Java code that called the publishInputs script
     * @param srcLang
     * @param srcVersion
     * @param targetLang
     * @param targetVersion
     * @param outputModelFileExt - the file extension of the output converted model
     */
    AbstractConverterWrapper(Binding binding, String srcLang, String srcVersion, String targetLang, String targetVersion, String outputModelFileExt) {
        this.converterToolboxExecutable = binding.getVariable("converter.toolbox.executable")
        this.srcLang = srcLang
        this.srcVersion = srcVersion
        this.targetLang = targetLang
        this.targetVersion = targetVersion
        this.outputModelFileExt = outputModelFileExt
        this.executor = binding.hasVariable("ApacheCommonsExecExecutor") ? binding.getVariable("ApacheCommonsExecExecutor") /* to allow unit testing */ : new DefaultExecutor()
    }

    /**
     * Convert the MDL using the Converter Toolbox command-line launch script using the supplied parameters.
     * <p>
     * @param origModelFile - references the MDL model file in its original location
     * @param modelFileInMifWorkingDir - references the MDL model file in the MIF working directory
     * @param fisMetadataDir - into which the stdout and stderr will be written
     * @return the newModelFileName (i.e. the converted version of the original model file) if conversion was successful;
     *         null if conversion failed
     */
    final String doConvert(File origModelFile, File modelFileInMifWorkingDir, File fisMetadataDir) {

        // Build up the command line to execute
        CommandLine cmdLine = new CommandLine("cmd")
        cmdLine.addArgument("/c")
        cmdLine.addArgument(new File(this.converterToolboxExecutable).getName())
        cmdLine.addArgument(modelFileInMifWorkingDir.getAbsolutePath()) // Source MDL model file
        cmdLine.addArgument(modelFileInMifWorkingDir.getParentFile().getAbsolutePath()) // Destination directory for the output converted model file
        cmdLine.addArgument(this.srcLang)
        cmdLine.addArgument(this.srcVersion)
        cmdLine.addArgument(this.targetLang)
        cmdLine.addArgument(this.targetVersion)

        LOGGER.info("Invoking converter toolbox command : " + cmdLine); // This could be run from the command line for testing purposes

        // Set up some output streams to handle the standard out and standard error
        BufferedOutputStream stdoutOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL-conversion.stdout")))
        IOUtils.write("Invoking converter toolbox command : " + cmdLine + "\n\n", stdoutOS)
        BufferedOutputStream stderrOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL-conversion.stderr")))

        // Create the executor object, providing a stream handler that will avoid
        // the child process becoming blocked because nothing is consuming its output,
        // and also a timeout
        this.executor.setWorkingDirectory(new File(this.converterToolboxExecutable).getParentFile())
        this.executor.setExitValue(0) // Required "success" return code
        ExecuteWatchdog watchdog = new ExecuteWatchdog(30000) // Will kill the process after 30 seconds
        this.executor.setWatchdog(watchdog)
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(stdoutOS, stderrOS)
        this.executor.setStreamHandler(pumpStreamHandler)

        try {
            this.executor.execute(cmdLine);
        } catch (ExecuteException eex) { // Command has failed or timed out
            IOUtils.write("\n\nError code " + eex.getExitValue() + " returned from converter toolbox command : " + cmdLine + "\n\n", stderrOS)
            IOUtils.write("ExecuteException cause: " + eex.getMessage(), stderrOS)
            return null; // Failure
        } finally {
            stdoutOS.close()
            stderrOS.close()
        }

        // This hangs! :-
        //Process process = Runtime.getRuntime().exec(cmdLine.toStrings())
        //process.waitFor()
        //int exitValue = process.waitFor()

        def newModelFileName = FilenameUtils.removeExtension(origModelFile.getPath()) + "." + this.outputModelFileExt

        postConvert(modelFileInMifWorkingDir);

        return newModelFileName
    }

    /**
     * Perform any required post-conversion actions. Default is no-op, this can be overridden if required.
     * <p>
     * @param modelFileInMifWorkingDir - references the MDL model file in the MIF working directory
     */
    protected void postConvert(File modelFileInMifWorkingDir) {
    }

}
