/**
 * 
 */
package eu.ddmore.fis.controllers.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.ddmore.fis.domain.SubmissionRequest;


/**
 * Tests for {@link SubmissionRequestToStringConverter} and {@link StringToSubmissionRequestConverter}.
 * Only calling this an integration test since it is testing two classes rather than just one.
 */
public class SubmissionRequestConversionIT {

    private final static Logger LOG = Logger.getLogger(SubmissionRequestConversionIT.class);

    /**
     * Set-up tasks prior to each test being run.
     */
    @Before
    public void setUp() {
    }

    @Test
    public void whenConvertSubmissionRequestToStringAndBackAgain_thenSameSubmissionRequestProduced() {
        
        final SubmissionRequest origSubmitReq = new SubmissionRequest();
        origSubmitReq.setCommand("NONMEM");
        origSubmitReq.setCommandParameters("-myparam1 -myparam2");
        origSubmitReq.setExecutionFile("C:\\path\\to\\my\\model.mdl");
        origSubmitReq.setWorkingDirectory("C:\\Temp\\fisworkingdir");
        origSubmitReq.setExtraInputFiles(Arrays.asList("C:\\path\\to\\my\\model.lst", "C:\\path\\to\\some\\other-file.txt"));
        
        final String json = new SubmissionRequestToStringConverter().convert(origSubmitReq);
        LOG.info(json);
        final SubmissionRequest resultSubmitReq = new StringToSubmissionRequestConverter().convert(json);
        
        assertEquals("Checking the command property is set correctly", origSubmitReq.getCommand(), resultSubmitReq.getCommand());
        assertEquals("Checking the commandParameters property is set correctly", origSubmitReq.getCommandParameters(), resultSubmitReq.getCommandParameters());
        assertEquals("Checking the executionFile parameter is set correctly", origSubmitReq.getExecutionFile(), resultSubmitReq.getExecutionFile());
        assertEquals("Checking the workingDirectory parameter is set correctly", origSubmitReq.getWorkingDirectory(), resultSubmitReq.getWorkingDirectory());
        assertEquals("Checking the extraInputFiles parameter is set correctly", origSubmitReq.getExtraInputFiles(), resultSubmitReq.getExtraInputFiles());
        
    }
    
    /**
     * The JSON used in this test has been copied directly from the output of the following R command (i.e. as would be created in TEL.submitJob() function):
     * <p><code>
     * rjson::toJSON(list(command="NONMEM", commandParameters="-myparam1 -myparam2", workingDirectory="C:/Temp/fisworkingdir", executionFile="C:/path/to/my/model.mdl", extraInputFiles=list("C:/path/to/my/model.lst", "C:/path/to/some/other-file.txt")))
     * </code>
     */
    @Test
    public void whenGivenJsonStringConvertedFromNamedListInR_multiple£xtraInputFiles_thenSubmissionRequestPopulatedCorrectly() {
        final String json = "{\"command\":\"NONMEM\",\"commandParameters\":\"-myparam1 -myparam2\",\"workingDirectory\":\"C:/Temp/fisworkingdir\",\"executionFile\":\"C:/path/to/my/model.mdl\",\"extraInputFiles\":[\"C:/path/to/my/model.lst\",\"C:/path/to/some/other-file.txt\"]}";
        final SubmissionRequest sr = new StringToSubmissionRequestConverter().convert(json);
        
        assertEquals("Checking the command property is set correctly", "NONMEM", sr.getCommand());
        assertEquals("Checking the commandParameters property is set correctly", "-myparam1 -myparam2", sr.getCommandParameters());
        assertEquals("Checking the executionFile parameter is set correctly", "C:/path/to/my/model.mdl", sr.getExecutionFile());
        assertEquals("Checking the workingDirectory parameter is set correctly", "C:/Temp/fisworkingdir", sr.getWorkingDirectory());
        assertEquals("Checking the extraInputFiles parameter is set correctly", Arrays.asList("C:/path/to/my/model.lst", "C:/path/to/some/other-file.txt"), sr.getExtraInputFiles());
        
    }
    
    /**
     * The JSON used in this test has been copied directly from the output of the following R command (i.e. as would be created in TEL.submitJob() function):
     * <p><code>
     * rjson::toJSON(list(command="NONMEM", commandParameters="-myparam1 -myparam2", workingDirectory="C:/Temp/fisworkingdir", executionFile="C:/path/to/my/model.mdl", extraInputFiles=list("C:/path/to/my/model.lst")))
     * </code>
     */
    @Test
    public void whenGivenJsonStringConvertedFromNamedListInR_singleExtraInputFile_thenSubmissionRequestPopulatedCorrectly() {
        final String json = "{\"command\":\"NONMEM\",\"commandParameters\":\"-myparam1 -myparam2\",\"workingDirectory\":\"C:/Temp/fisworkingdir\",\"executionFile\":\"C:/path/to/my/model.mdl\",\"extraInputFiles\":[\"C:/path/to/my/model.lst\"]}";
        final SubmissionRequest sr = new StringToSubmissionRequestConverter().convert(json);
        
        assertEquals("Checking the command property is set correctly", "NONMEM", sr.getCommand());
        assertEquals("Checking the commandParameters property is set correctly", "-myparam1 -myparam2", sr.getCommandParameters());
        assertEquals("Checking the executionFile parameter is set correctly", "C:/path/to/my/model.mdl", sr.getExecutionFile());
        assertEquals("Checking the workingDirectory parameter is set correctly", "C:/Temp/fisworkingdir", sr.getWorkingDirectory());
        assertEquals("Checking the extraInputFiles parameter is set correctly", Arrays.asList("C:/path/to/my/model.lst"), sr.getExtraInputFiles());
        
    }
    
    /**
     * The JSON used in this test has been copied directly from the output of the following R command (i.e. as would be created in TEL.submitJob() function):
     * <p><code>
     * rjson::toJSON(list(command="NONMEM", commandParameters="-myparam1 -myparam2", workingDirectory="C:/Temp/fisworkingdir", executionFile="C:/path/to/my/model.mdl", extraInputFiles=list()))
     * </code>
     */
    @Test
    public void whenGivenJsonStringConvertedFromNamedListInR_noExtraInputFiles_thenSubmissionRequestPopulatedCorrectly() {
        final String json = "{\"command\":\"NONMEM\",\"commandParameters\":\"-myparam1 -myparam2\",\"workingDirectory\":\"C:/Temp/fisworkingdir\",\"executionFile\":\"C:/path/to/my/model.mdl\",\"extraInputFiles\":[]}";
        final SubmissionRequest sr = new StringToSubmissionRequestConverter().convert(json);
        
        assertEquals("Checking the command property is set correctly", "NONMEM", sr.getCommand());
        assertEquals("Checking the commandParameters property is set correctly", "-myparam1 -myparam2", sr.getCommandParameters());
        assertEquals("Checking the executionFile parameter is set correctly", "C:/path/to/my/model.mdl", sr.getExecutionFile());
        assertEquals("Checking the workingDirectory parameter is set correctly", "C:/Temp/fisworkingdir", sr.getWorkingDirectory());
        assertTrue("Checking the extraInputFiles parameter is set correctly", sr.getExtraInputFiles().isEmpty());
        
    }
    
    /**
     * The JSON used in this test has been copied directly from the output of the following R command (i.e. as would be created in TEL.submitJob() function):
     * <p><code>
     * rjson::toJSON(list(command="NONMEM", commandParameters=NULL, workingDirectory="C:/Temp/fisworkingdir", executionFile="C:/path/to/my/model.mdl", extraInputFiles=NULL))
     * </code>
     */
    @Test
    public void whenGivenJsonStringConvertedFromNamedListInR_withSomeNullParameters_thenSubmissionRequestPopulatedCorrectly() {
        final String json = "{\"command\":\"NONMEM\",\"commandParameters\":null,\"workingDirectory\":\"C:/Temp/fisworkingdir\",\"executionFile\":\"C:/path/to/my/model.mdl\",\"extraInputFiles\":null}";
        final SubmissionRequest sr = new StringToSubmissionRequestConverter().convert(json);
        
        assertEquals("Checking the command property is set correctly", "NONMEM", sr.getCommand());
        assertNull("Checking the commandParameters property is not set", sr.getCommandParameters());
        assertEquals("Checking the executionFile parameter is set correctly", "C:/path/to/my/model.mdl", sr.getExecutionFile());
        assertEquals("Checking the workingDirectory parameter is set correctly", "C:/Temp/fisworkingdir", sr.getWorkingDirectory());
        assertNull("Checking the extraInputFiles parameter is set correctly", sr.getExtraInputFiles());
        
    }

}
