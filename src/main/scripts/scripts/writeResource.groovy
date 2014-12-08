import eu.ddmore.fis.domain.WriteMdlResponse

import javax.wsdl.Import;

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

final Logger LOGGER = Logger.getLogger(getClass())

    File scriptFile = binding.getVariable("scriptFile");
    String converterToolboxExecutable = binding.getVariable("converter.toolbox.executable");
	String fileContent = binding.getVariable("fileContent")
    String fileName = binding.getVariable("fileName");

	File workingDir = new File(fileName).parentFile

    // Ensure that the FIS metadata directory is created
    File fisMetadataDir = new File(workingDir,".fis");
    fisMetadataDir.mkdir();

	File jsonFileName = new File(FilenameUtils.removeExtension(fileName) + ".json")
	FileUtils.writeStringToFile(jsonFileName, fileContent)

    // Build up the command line to execute
    CommandLine cmdLine = new CommandLine("cmd")
    cmdLine.addArgument("/c")
    cmdLine.addArgument(new File(converterToolboxExecutable).getName())
    cmdLine.addArgument(jsonFileName.path) // Source JSON model file
    cmdLine.addArgument(new File(fileName).parent) // Destination directory for the MDL output file

    // TODO: Remove the hard-coding of these source and target converter versions
	cmdLine.addArgument("JSON")
	cmdLine.addArgument("0.0.0")
	cmdLine.addArgument("MDL")
    cmdLine.addArgument("5.1.6-interop")

    LOGGER.info("Invoking converter toolbox command : " + cmdLine); // This could be run from the command line for testing purposes

    // Set up some output streams to handle the standard out and standard error
    BufferedOutputStream stdoutOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "JSON2MDL.stdout")))
    IOUtils.write("Invoking converter toolbox command : " + cmdLine + "\n\n", stdoutOS)
    BufferedOutputStream stderrOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "JSON2MDL.stderr")))

    // Create the executor object, providing a stream handler that will avoid
    // the child process becoming blocked because nothing is consuming its output,
    // and also a timeout
    Executor executor = binding.hasVariable("ApacheCommonsExecExecutor") ? binding.getVariable("ApacheCommonsExecExecutor") /* to allow unit testing */ : new DefaultExecutor()
    executor.setWorkingDirectory(new File(converterToolboxExecutable).getParentFile())
    executor.setExitValue(0) // Required "success" return code
    ExecuteWatchdog watchdog = new ExecuteWatchdog(30000) // Will kill the process after 30 seconds
    executor.setWatchdog(watchdog)
    PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(stdoutOS, stderrOS)
    executor.setStreamHandler(pumpStreamHandler)

    try {
        executor.execute(cmdLine);
    } catch (ExecuteException eex) { // Command has failed or timed out
        IOUtils.write("\n\nError code " + eex.getExitValue() + " returned from converter toolbox command : " + cmdLine + "\n\n", stderrOS)
        IOUtils.write("ExecuteException cause: " + eex.getMessage(), stderrOS)
        return;
    } finally {
        stdoutOS.close()
        stderrOS.close()
    }

	FileUtils.deleteQuietly(jsonFileName)
	
	WriteMdlResponse response = new WriteMdlResponse("Successful")
	response