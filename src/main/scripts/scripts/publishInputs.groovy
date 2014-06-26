import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

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

        GroovyShell shell = new GroovyShell(binding)
        def script = shell.parse( new File(scriptFile.getParentFile(), "MdlToPharmML.groovy") )
        boolean success = script.doConvert(controlFileInMifWorkingDir, fisMetadataDir)
        if (!success) {
            job.setStatus(LocalJobStatus.FAILED)
            return;
        }

        // TODO: Do we really need both .xml and .pharmml copies?
        File xmlFileInMifWorkingDir = new File(mifWorkingDir, newModelFileName)
        FileUtils.copyFile( xmlFileInMifWorkingDir, new File(FilenameUtils.removeExtension(xmlFileInMifWorkingDir.getPath()) + ".pharmml") )
    }

    job.setControlFile(newModelFileName);
}
