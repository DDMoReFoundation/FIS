import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import eu.ddmore.fis.domain.LocalJob
import eu.ddmore.fis.domain.LocalJobStatus;

LocalJob job = binding.getVariable("job");
File scriptFile = binding.getVariable("scriptFile");
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

