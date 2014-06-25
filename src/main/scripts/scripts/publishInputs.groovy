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

import com.mango.mif.core.exec.Invoker

import eu.ddmore.fis.domain.LocalJob
import eu.ddmore.fis.domain.LocalJobStatus;

import org.apache.log4j.Logger


final Logger LOGGER = Logger.getLogger(getClass())

LocalJob job = binding.getVariable("job");
File scriptFile = binding.getVariable("scriptFile");
String converterToolboxExecutable = binding.getVariable("converter.toolbox.executable");
String MDL_FILE_EXT = binding.getVariable("fis.mdl.ext");
String PHARMML_FILE_EXT = binding.getVariable("fis.pharmml.ext");
Invoker invoker = binding.getVariable("invoker");

File workingDir = new File(job.getWorkingDirectory())
File mifWorkingDir = new File(workingDir, job.getId());

FileUtils.copyDirectory(workingDir, mifWorkingDir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !mifWorkingDir.equals(pathname);
            }
        });

// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(workingDir,".fis");
fisMetadataDir.mkdir();

File origControlFile = new File(job.getControlFile());

String modelName = FilenameUtils.getBaseName(origControlFile.getName());
String modelExt = FilenameUtils.getExtension(origControlFile.getName());

// We ensure that subdirectory structure is maintained
File controlFileInMifWorkingDir = new File(mifWorkingDir, origControlFile.getPath())

// If copying mock data
File mockDataDir = new File(scriptFile.getParentFile().getParentFile(),"mockData")

if (PHARMML_FILE_EXT.equals(modelExt)) {

    FileUtils.copyFile(
        controlFileInMifWorkingDir,
        new File(FilenameUtils.removeExtension(controlFileInMifWorkingDir.getPath()) + ".pharmml")
    );

} else if (MDL_FILE_EXT.equals(modelExt)) {

    String newModelFileName = FilenameUtils.removeExtension(origControlFile.getPath()) + "." + PHARMML_FILE_EXT

    File xmlVersionInMockDataDir = new File(mockDataDir, modelName + "." + PHARMML_FILE_EXT)
    if (xmlVersionInMockDataDir.exists()) {
        // Mock converted file exists in mockData dir
        FileUtils.copyFileToDirectory( xmlVersionInMockDataDir, mifWorkingDir )
        FileUtils.copyFile( xmlVersionInMockDataDir, new File(mifWorkingDir, modelName + ".pharmml") )
        File data = new File(mockDataDir, modelName + ".csv")
        if (data.exists()) {
            FileUtils.copyFile( data, new File(mifWorkingDir, modelName + "_data.csv") )
        }
    }
    else {
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
            job.setStatus(LocalJobStatus.FAILED)
            return;
        } finally {
            stdoutOS.close()
            stderrOS.close()
        }

        // This hangs! :-
        //Process process = Runtime.getRuntime().exec(cmdLine.toStrings())
        //process.waitFor()
        //int exitValue = process.waitFor()


        File xmlFileInMifWorkingDir = new File(mifWorkingDir, newModelFileName)

        // TODO: Do we really need both .xml and .pharmml copies?
        FileUtils.copyFile( xmlFileInMifWorkingDir, new File(FilenameUtils.removeExtension(xmlFileInMifWorkingDir.getPath()) + ".pharmml") )


        // WAS:
        // default to example3
        //FileUtils.copyFile(new File(mockDataDir,"example3_full_data_MDV.csv"), new File(mifWorkingDir,modelName + "_data.csv"));
        //FileUtils.copyFile(new File(mockDataDir,"example3.xml"), new File(mifWorkingDir,modelName + ".xml"));
        //FileUtils.copyFile(new File(mockDataDir,"example3.xml"), new File(mifWorkingDir,modelName + ".pharmml"));
    }

    job.setControlFile(newModelFileName);
}
