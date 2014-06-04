import java.io.File;

import org.apache.commons.io.FileUtils;
import eu.ddmore.fis.domain.LocalJob;

LocalJob job = binding.getVariable("job");
File workingDir = new File(job.getWorkingDirectory())
File mifWorkingDir = new File(workingDir, job.getId());

FileUtils.copyDirectory(mifWorkingDir, workingDir, new FileFilter() {
    boolean accept(File file) {
        switch(file.getName()) {
            case ~/.*\.(csv|ctl|xml|lst|pharmml)/:
                return true;
            default:
                return false;
        }
    }
});

File fisMetadataDir = new File(workingDir,".fis");
fisMetadataDir.mkdir();

File mifMetadataDir = new File(mifWorkingDir,".MIF");
File mifStdErr=new File(mifMetadataDir, "MIF.stderr");
if(mifStdErr.exists()) {
    FileUtils.copyFile(mifStdErr,new File(fisMetadataDir, "stderr"));
} else {
    println "MIF.stderr file did not exist" 
}

File mifStdOut = new File(mifMetadataDir, "MIF.stdout");
if(mifStdErr.exists()) {
    FileUtils.copyFile(mifStdOut,new File(fisMetadataDir, "stdout"));
} else {
    println "MIF.stdout file did not exist" 
}
