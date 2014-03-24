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
            case ~/\.MIF/:
                return true;
            default:
                return false;
        }
    }
});
