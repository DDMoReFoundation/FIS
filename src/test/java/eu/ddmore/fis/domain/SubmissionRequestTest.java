package eu.ddmore.fis.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubmissionRequestTest {

	private static final String COMMAND = "test-command";
	private static final String COMMAND_PARAMETERS = "test-command-parameters";
	private static final String EXECUTION_FILE = "test-execution-file";
	private static final String WORKING_DIRECTORY = "test-working-directory";

	private static final String EXPECTED_TO_STRING =
		"SubmissionRequest [" +
			"workingDirectory=" + WORKING_DIRECTORY + ", " +
			"command=" + COMMAND + ", " +
			"executionFile=" + EXECUTION_FILE + ", " +
			"commandParameters=" + COMMAND_PARAMETERS + "]";

    @Test
    public void shouldProduceCorrectToString() {
        SubmissionRequest sr = new SubmissionRequest();
        sr.setCommand(COMMAND);
        sr.setCommandParameters(COMMAND_PARAMETERS);
        sr.setExecutionFile(EXECUTION_FILE);
        sr.setWorkingDirectory(WORKING_DIRECTORY);

        assertEquals("toString should be correct.", EXPECTED_TO_STRING, sr.toString());
    }
}