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
 * Wrapper for the conversion of MDL to PharmML.
 * <p>
 * @param origControlFile - references the control file with its relative path
 * @param controlFileInMifWorkingDir - references the control file in the MIF working directory
 * @param fisMetadataDir - into which the stdout and stderr will be written
 * @return the newModelFileName (i.e. the .xml version) if conversion was successful; null if conversion failed
 */
def doConvert(File origControlFile, File controlFileInMifWorkingDir, File fisMetadataDir) {

    def LOGGER = Logger.getLogger(getClass())

    def converterToolboxExecutable = binding.getVariable("converter.toolbox.executable")
    def PHARMML_FILE_EXT = binding.getVariable("fis.pharmml.ext");

    // Need to convert the MDL into PharmML using the Converter Toolbox command-line launch script

    // Build up the command line to execute
    CommandLine cmdLine = new CommandLine("cmd")
    cmdLine.addArgument("/c")
    cmdLine.addArgument(new File(converterToolboxExecutable).getName())
    cmdLine.addArgument(controlFileInMifWorkingDir.getAbsolutePath()) // Source MDL model file
    cmdLine.addArgument(controlFileInMifWorkingDir.getParentFile().getAbsolutePath()) // Destination directory for the converted PharmML file
    // TODO: Remove the hard-coding of these source and target converter versions
    cmdLine.addArgument("MDL")
    cmdLine.addArgument("5.1.6")
    cmdLine.addArgument("PharmML")
    cmdLine.addArgument("0.3.0")

    LOGGER.info("Invoking converter toolbox command : " + cmdLine); // This could be run from the command line for testing purposes

    // Set up some output streams to handle the standard out and standard error
    BufferedOutputStream stdoutOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL2PHARMML.stdout")))
    IOUtils.write("Invoking converter toolbox command : " + cmdLine + "\n\n", stdoutOS)
    BufferedOutputStream stderrOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL2PHARMML.stderr")))

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
        return null; // Failure
    } finally {
        stdoutOS.close()
        stderrOS.close()
    }

    // This hangs! :-
    //Process process = Runtime.getRuntime().exec(cmdLine.toStrings())
    //process.waitFor()
    //int exitValue = process.waitFor()

    def newModelFileName = FilenameUtils.removeExtension(origControlFile.getPath()) + "." + PHARMML_FILE_EXT

    // TODO: Do we really need both .xml and .pharmml copies?
    File xmlFileInMifWorkingDir = new File(FilenameUtils.removeExtension(controlFileInMifWorkingDir.getPath()) + "." + PHARMML_FILE_EXT)
    FileUtils.copyFile( xmlFileInMifWorkingDir, new File(FilenameUtils.removeExtension(xmlFileInMifWorkingDir.getPath()) + ".pharmml") )

    return newModelFileName;
}
