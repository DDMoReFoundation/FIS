import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import eu.ddmore.fis.domain.LocalJob
import eu.ddmore.fis.domain.LocalJobStatus;


LocalJob job = binding.getVariable("job");
File scriptFile = binding.getVariable("scriptFile");
String mdlConversionScript = "MdlToPharmML.groovy"
String converterToolboxExecutable = binding.getVariable("converter.toolbox.executable");
String MDL_FILE_EXT = binding.getVariable("fis.mdl.ext");
String PHARMML_FILE_EXT = binding.getVariable("fis.pharmml.ext");
String executionHostFileshareLocal = binding.getVariable("execution.host.fileshare.local");

File workingDir = new File(job.getWorkingDirectory())
File mifWorkingDir = new File(executionHostFileshareLocal, job.getId());

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

File mockDataDir = new File(scriptFile.getParentFile().getParentFile(),"mockData")
String newModelFileName;
File xmlVersionInMockDataDir = new File(mockDataDir, modelName + "." + PHARMML_FILE_EXT)
if (xmlVersionInMockDataDir.exists()) {
    // Mock converted file exists in mockData dir   
    newModelFileName = modelName + "." + PHARMML_FILE_EXT
    FileUtils.copyFile( xmlVersionInMockDataDir, new File(mifWorkingDir, newModelFileName) )
    File data = new File(mockDataDir, modelName + ".csv")
    if (data.exists()) {
        FileUtils.copyFile( data, new File(mifWorkingDir, modelName + "_data.csv") )
    }
} else {
    def convWrapper = this.class.classLoader.parseClass(new File(scriptFile.getParentFile(), mdlConversionScript));
    newModelFileName = convWrapper.newInstance(binding).doConvert(origControlFile, controlFileInMifWorkingDir, fisMetadataDir)
    if (newModelFileName == null) {
        // Conversion failed
        job.setStatus(LocalJobStatus.FAILED)
        return;
    }
}
job.setControlFile(newModelFileName);
