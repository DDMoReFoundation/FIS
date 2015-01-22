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
    String mdlFilePath = binding.getVariable("fileName");
	String outputDir = binding.getVariable("outputDir");
	String PHARMML_FILE_EXT = ".xml";
	
	File mdlFile = new File(mdlFilePath)
    File workingDir = mdlFile.parentFile
    // Ensure that the FIS metadata directory is created
    File fisMetadataDir = new File(workingDir,".fis");
    fisMetadataDir.mkdir();
	File outputDirectory = new File(outputDir);
	
	// Build up the command line to execute
	CommandLine cmdLine = new CommandLine(converterToolboxExecutable)
	// Set up some output streams to handle the standard out and standard error
	BufferedOutputStream stdoutOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL2PharmML.stdout")))
	IOUtils.write("Invoking converter toolbox command : " + cmdLine + "\n\n", stdoutOS)
	BufferedOutputStream stderrOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL2PharmML.stderr")))
	
	try {
		if (!(outputDirectory.exists() || outputDirectory.mkdir())) {
			IOUtils.write("Failed to access/create output directory at specified location : "+outputDirectory.getAbsolutePath(), stderrOS)
			return;
		}
	    // Build up the command line to execute
	    cmdLine.addArgument(mdlFile.getAbsolutePath()) // Source MDL model file
	    cmdLine.addArgument(outputDirectory.getAbsolutePath()) // Destination directory for the output file
	
	    // TODO: Remove the hard-coding of these source and target converter versions
	    cmdLine.addArgument("MDL")
	    cmdLine.addArgument("5.1.6-interop")
	    cmdLine.addArgument("PharmML")
	    cmdLine.addArgument("0.3.1")
	
	    LOGGER.info("Invoking converter toolbox command : " + cmdLine); // This could be run from the command line for testing purposes
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

        executor.execute(cmdLine);
		
		String outputFileName = FilenameUtils.removeExtension(mdlFile.getName())+PHARMML_FILE_EXT;
		File resultFile = new File(outputDirectory.getAbsolutePath()+File.separator+outputFileName);
		
		if(resultFile.exists()){
			resultFile.getAbsolutePath();
		}else{
			IOUtils.write("Failed to access/create output file at specified location : "+outputDirectory.getAbsolutePath(), stderrOS)
			return;
		}
    } catch (ExecuteException eex) { // Command has failed or timed out
        IOUtils.write("\n\nError code " + eex.getExitValue() + " returned from converter toolbox command : " + cmdLine + "\n\n", stderrOS)
        IOUtils.write("ExecuteException cause: " + eex.getMessage(), stderrOS)
        return;
    } finally {
        stdoutOS.close()
        stderrOS.close()
    }