import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import com.mango.mif.core.exec.Invoker
import eu.ddmore.fis.domain.LocalJob

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

File origControlFile = new File(job.getControlFile());
File origControlFileDir = origControlFile.getParentFile()!=null?origControlFile.getParentFile():new File("");

String modelName = FilenameUtils.getBaseName(origControlFile.getName());
String modelExt = FilenameUtils.getExtension(origControlFile.getName());
//Copying mock data
File mockDataDir = new File(scriptFile.getParentFile().getParentFile(),"mockData")

if(PHARMML_FILE_EXT.equals(modelExt)) {
    FileUtils.copyFile(
        new File(mifWorkingDir,origControlFile.getPath()), 
        new File(new File(mifWorkingDir,origControlFileDir.getPath()), modelName + ".pharmml") );
} else if(MDL_FILE_EXT.equals(modelExt)) {
	String newModelFileName = modelName + "." + PHARMML_FILE_EXT
	File xmlVersion = new File(mockDataDir, newModelFileName)
	if( xmlVersion.exists() ) {
		FileUtils.copyFileToDirectory( xmlVersion, mifWorkingDir )
		FileUtils.copyFile( xmlVersion, new File(mifWorkingDir,modelName + ".pharmml") )
	}
	else {
		// default to example3
		FileUtils.copyFile(new File(mockDataDir,"example3_full_data_MDV.csv"), new File(mifWorkingDir,modelName + "_data.csv"));
		FileUtils.copyFile(new File(mockDataDir,"example3.xml"), new File(mifWorkingDir,modelName + ".xml"));
		FileUtils.copyFile(new File(mockDataDir,"example3.xml"), new File(mifWorkingDir,modelName + ".pharmml"));
	}
    job.setControlFile(new File(origControlFileDir, newModelFileName).getPath());
}