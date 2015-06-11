import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

import eu.ddmore.fis.domain.LocalJob


final Logger LOG = Logger.getLogger(getClass())
LocalJob job = binding.getVariable("job");
String executionHostFileshareLocal = binding.getVariable("execution.host.fileshare.local");

File workingDir = new File(job.getWorkingDirectory())
File mifWorkingDir = new File(executionHostFileshareLocal, job.getId());


// Ensure that the FIS metadata directory is created
File fisMetadataDir = new File(workingDir,".fis");
fisMetadataDir.mkdir();

File mifMetadataDir = new File(mifWorkingDir,".MIF");
File mifStdErr = new File(mifMetadataDir, "MIF.stderr");
if (mifStdErr.exists()) {
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


// Copy output files
if (job.getResultsIncludeRegex() != null) {
	final Pattern includePattern = Pattern.compile(job.getResultsIncludeRegex())
    final Pattern excludePattern = Pattern.compile((job.getResultsExcludeRegex()==null)?"":job.getResultsExcludeRegex())

	// Copy back any files that match the filename regular expression and which aren't "hidden" e.g. .MIF directory
	FileUtils.copyDirectory(mifWorkingDir, workingDir, new FileFilter() {
		boolean accept(File file) {
			boolean accepted = (file.getName().matches(includePattern) && !file.getName().startsWith(".") && !file.getName().matches(excludePattern));
            LOG.debug("The file ${file.getName()} passes inclusion/exclusion filtering ${accepted}");
            return accepted
		}
	});
} else {
	FileUtils.write(new File(fisMetadataDir, "stderr"), "\n\nOutput Filenames Regular Expression was not specified in the MIF/TES Connector configuration; no output files will be copied back.\n", true); // true = append
}
