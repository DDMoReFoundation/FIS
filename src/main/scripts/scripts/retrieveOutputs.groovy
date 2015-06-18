
import java.util.regex.Pattern
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

import eu.ddmore.fis.domain.LocalJob

import groovy.transform.Field

@Field final Logger LOG = Logger.getLogger("eu.ddmore.fis.scripts.RetrieveOutputs") // getClass() doesn't return what you might expect, for a Groovy script

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
    LOG.error("MIF.stderr file did not exist")
}

File mifStdOut = new File(mifMetadataDir, "MIF.stdout");
if(mifStdErr.exists()) {
    FileUtils.copyFile(mifStdOut,new File(fisMetadataDir, "stdout"));
} else {
    LOG.error("MIF.stdout file did not exist")
}


// Copy output files
if (job.getResultsIncludeRegex() != null) {
	final Pattern includePattern = Pattern.compile(job.getResultsIncludeRegex())
    final Pattern excludePattern = Pattern.compile((job.getResultsExcludeRegex()==null)?"":job.getResultsExcludeRegex())

	// Copy back any files from the *execution folder* that pass the filename regular expression checks
    // and which aren't "hidden" e.g. .MIF directory.
    // Note the distinction between the *execution folder* and the *working directory":
    // The execution folder is the directory in which the model file lives, it will normally be the same
    // as the working folder, except in the case where the model file points to a data file in a parallel
    // directory (i.e. models/mymodel.mdl and data/mydata.csv), when the working folder will be the base
    // path that is common between the model file and the data file.
    // Outside of the execution directory, everything is copied unfiltered (should just contain data files).
    final Path executionDirectoryPath = mifWorkingDir.toPath().resolve( Paths.get(job.getControlFile()).getParent() ?: "" )
    LOG.debug("Recursively copying contents from ${mifWorkingDir} to ${workingDir}; applying filtering to ${executionDirectoryPath}")
	FileUtils.copyDirectory(mifWorkingDir, workingDir, new FileFilter() {
		boolean accept(final File file) {
            if (file.getName().startsWith(".")) { // .MIF hidden directory is at the top-level working directory
                return false
            }
            boolean accepted = true
            if (file.toPath().startsWith(executionDirectoryPath) && !file.toPath().equals(executionDirectoryPath)) {
    			accepted = (file.getName().matches(includePattern) && !file.getName().matches(excludePattern))
                getLogger().debug("The file ${file.getName()} passes inclusion/exclusion filtering = ${accepted}")
            } else if (getLogger().isDebugEnabled()) {
                getLogger().debug("Skipping filtering for file at path ${file.getPath()}")
            }
            return accepted
		}
	});
} else {
	FileUtils.write(new File(fisMetadataDir, "stderr"), "\n\nOutput Filenames Regular Expression was not specified in the MIF/TES Connector configuration; no output files will be copied back.\n", true); // true = append
}

/*
 * Because of: http://stackoverflow.com/questions/18725769/groovyscript-field-access-in-anonymous-class
 */
private Logger getLogger() {
    return LOG
}


