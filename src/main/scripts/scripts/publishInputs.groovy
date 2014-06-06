import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.launcher.CommandLauncherFactory
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
File origControlFileDir = origControlFile.getParentFile()!=null?origControlFile.getParentFile():new File("");

String modelName = FilenameUtils.getBaseName(origControlFile.getName());
String modelExt = FilenameUtils.getExtension(origControlFile.getName());

// Copying mock data
File mockDataDir = new File(scriptFile.getParentFile().getParentFile(),"mockData")

if (PHARMML_FILE_EXT.equals(modelExt)) {

    FileUtils.copyFile(
            new File(mifWorkingDir,origControlFile.getPath()),
            new File(new File(mifWorkingDir,origControlFileDir.getPath()), modelName + ".pharmml") );

} else if (MDL_FILE_EXT.equals(modelExt)) {

    String newModelFileName = modelName + "." + PHARMML_FILE_EXT
    File xmlVersion = new File(mockDataDir, newModelFileName)
    if (xmlVersion.exists()) {
        // Mock converted file exists in mockData dir
        FileUtils.copyFileToDirectory( xmlVersion, mifWorkingDir )
        FileUtils.copyFile( xmlVersion, new File(mifWorkingDir,modelName + ".pharmml") )
        File data = new File(mockDataDir, modelName + ".csv")
        if (data.exists()) {
            FileUtils.copyFile( data, new File(mifWorkingDir,modelName + "_data.csv") )
        }
    }
    else {
        // Need to convert the MDL into PharmML using the Converter Toolbox command-line launch script

        CommandLine cmd = new CommandLine("cmd")
        cmd.addArgument("/c")
        cmd.addArgument(new File(converterToolboxExecutable).getAbsolutePath())
        cmd.addArgument(new File(mifWorkingDir, origControlFile.getPath()).getAbsolutePath())
        cmd.addArgument(mifWorkingDir.getAbsolutePath())
        // TODO: Remove the hard-coding of these source and target converter versions
        cmd.addArgument("MDL")
        cmd.addArgument("5.1.6")
        cmd.addArgument("PharmML")
        cmd.addArgument("0.3.0")
        LOGGER.info("Invoking converter toolbox command : " + cmd);
        Process process = CommandLauncherFactory.createVMLauncher().exec(cmd, null)
        process.waitFor()

        // Write out the stdout from the converter toolbox call
        BufferedOutputStream stdoutOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL2PHARMML.stdout")))
        IOUtils.write("Invoking converter toolbox command : " + cmd + "\n\n", stdoutOS)
        IOUtils.copy(process.getInputStream(), stdoutOS)
        stdoutOS.flush()
        stdoutOS.close();

        if (process.exitValue() != 0) {
            // Write out the stderr from the converter toolbox call
            BufferedOutputStream stderrOS = new BufferedOutputStream(new FileOutputStream(new File(fisMetadataDir, "MDL2PHARMML.stderr")))
            IOUtils.write("Error code " + process.exitValue() + " returned from converter toolbox command : " + cmd + "\n\n", stderrOS)
            IOUtils.copy(process.getErrorStream(), stderrOS)
            stderrOS.flush()
            stderrOS.close()
            job.setStatus(LocalJobStatus.FAILED)
            return
            //throw new RuntimeException("MDD -> PharmML converter toolbox command failed with return code " + process.exitValue() + "; refer to the output files within the .fis directory")
        }

        // TODO: Do we really need both .xml and .pharmml copies?
        FileUtils.copyFile( new File(mifWorkingDir, newModelFileName), new File(mifWorkingDir, modelName + ".pharmml") )


        // WAS:
        // default to example3
        //FileUtils.copyFile(new File(mockDataDir,"example3_full_data_MDV.csv"), new File(mifWorkingDir,modelName + "_data.csv"));
        //FileUtils.copyFile(new File(mockDataDir,"example3.xml"), new File(mifWorkingDir,modelName + ".xml"));
        //FileUtils.copyFile(new File(mockDataDir,"example3.xml"), new File(mifWorkingDir,modelName + ".pharmml"));
    }

    job.setControlFile(new File(origControlFileDir, newModelFileName).getPath());
}
