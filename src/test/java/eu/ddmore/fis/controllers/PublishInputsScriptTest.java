package eu.ddmore.fis.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.*;
import eu.ddmore.fis.domain.LocalJob;
import groovy.lang.Binding;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.mango.mif.core.exec.Invoker;


@RunWith(MockitoJUnitRunner.class)
public class PublishInputsScriptTest {

    private static final Logger LOG = Logger.getLogger(PublishInputsScriptTest.class);
    
    private static final File PATH_TO_CONVERTER_EXE = new File("/path/to/my/converter-exe");

    @Rule
    public TemporaryFolder testDirectory = new TemporaryFolder();

    private File testWorkingDir;
    private File mifWorkingDir;

    private Binding binding;

    private Executor mockExecutor;

    private JobProcessor jobProcessor;

    @Before
    public void setUp() {
        this.testWorkingDir = this.testDirectory.getRoot();
        LOG.debug(String.format("Test working dir %s", this.testWorkingDir));
        this.mifWorkingDir = new File(testWorkingDir, "MIF_JOB_ID");

        this.binding = new Binding();
        this.binding.setVariable("converter.toolbox.executable", PATH_TO_CONVERTER_EXE.getPath());
        this.binding.setVariable("converter.wrapperscript.mdl", ""); // set by the specific MDL tests
        this.binding.setVariable("fis.mdl.ext", "mdl");
        this.binding.setVariable("fis.pharmml.ext", "xml");
        this.binding.setVariable("invoker", mock(Invoker.class));
        this.mockExecutor = mock(Executor.class);
        this.binding.setVariable("ApacheCommonsExecExecutor", this.mockExecutor);

        this.jobProcessor = new JobProcessor(binding);
        this.jobProcessor.setScriptFile(FileUtils.toFile(PublishInputsScriptTest.class.getResource("/scripts/publishInputs.groovy")));
    }

    @Test
    public void shouldPublishCTLInputs() throws IOException {

        // Copy the files out of the testdata JAR file

        final String testDataDir = "/eu/ddmore/testdata/models/NM-TRAN/7.2.0/warfarin_PK_PRED/";

        final URL ctlFile = PublishInputsScriptTest.class.getResource(testDataDir + "warfarin_PK_PRED.ctl");
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + "warfarin_conc_pca.csv");

        FileUtils.copyURLToFile(ctlFile, new File(testWorkingDir, "warfarin_PK_PRED.ctl"));
        FileUtils.copyURLToFile(dataFile, new File(testWorkingDir, "warfarin_conc_pca.csv"));

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("warfarin_PK_PRED.ctl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("CTL file should be copied from the source", new File(mifWorkingDir, "warfarin_PK_PRED.ctl").exists());
        assertTrue("Data file should be copied from the source", new File(mifWorkingDir, "warfarin_conc_pca.csv").exists());
    }

    @Test
    public void shouldPublishPharmMLInputs() throws IOException {

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "example3.xml";
        final String DATA_FILE_NAME = "example3_data.csv"; // or _full_data_MDV.csv ?

        final String testDataDir = "/eu/ddmore/testdata/models/PharmML/0.3.0/example3/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(testWorkingDir, SCRIPT_FILE_NAME));
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(testWorkingDir, DATA_FILE_NAME));

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(SCRIPT_FILE_NAME);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("XML resource should be copied from the source", new File(mifWorkingDir, SCRIPT_FILE_NAME).exists());
        assertTrue("Data file should be copied from the source", new File(mifWorkingDir, DATA_FILE_NAME).exists());
        assertTrue("PharmML resource should be created", new File(mifWorkingDir, "example3.pharmml").exists());
    }
    
    /**
     * Common test functionality called by {@link #shouldPublishMDLInputs_Demonstrator10PlanAMode()}
     * and {@link #shouldPublishMDLInputs_Demonstrator10PlanBMode()}.
     * <p>
     * @param targetLang - target language i.e. PharmML or NMTRAN
     * @param outputModelFileExt - the file extension that the dummy created converter output file will be given (including the dot)
     * @throws IOException
     */
    private void shouldPublishMDLInputs(final String targetLang, final String outputModelFileExt) throws IOException {

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "warfarin_PK_PRED.mdl";
        final String DATA_FILE_NAME = "warfarin_conc_pca.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/mdl/warfarin_PK_PRED/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(testWorkingDir, SCRIPT_FILE_NAME));
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(testWorkingDir, DATA_FILE_NAME));

        // Set up the behaviour of the mock DefaultExecutor used to execute the conversion script
        when(this.mockExecutor.execute(isA(CommandLine.class))).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock invocation) throws Throwable {
                final CommandLine invokedCmdLine = (CommandLine) invocation.getArguments()[0];
                final String[] cmdLineArgs = invokedCmdLine.getArguments();
                assertEquals("Checking argument 0 to the call to the converter toolbox", PATH_TO_CONVERTER_EXE.getPath(), invokedCmdLine.getExecutable());
                assertEquals("Checking argument 1 to the call to the converter toolbox",
                    new File(mifWorkingDir, "warfarin_PK_PRED.mdl").getAbsolutePath(), cmdLineArgs[0]);
                assertEquals("Checking argument 2 to the call to the converter toolbox",
                    mifWorkingDir.getAbsolutePath(), cmdLineArgs[1]);
                assertEquals("Checking argument 3 to the call to the converter toolbox", "MDL", cmdLineArgs[2]);
                assertEquals("Checking argument 5 to the call to the converter toolbox", targetLang, cmdLineArgs[4]);

                // Simulate the conversion process generating the output PharmML model files
                FileUtils.touch(new File(mifWorkingDir.getAbsolutePath(), SCRIPT_FILE_NAME.replace(".mdl", outputModelFileExt)));

                return 0;
            }
        });

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn(SCRIPT_FILE_NAME);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);

        // Checking the working directory that is set for the call to the converter toolbox
        verify(this.mockExecutor).setWorkingDirectory(new File("/path/to/my"));

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("MDL file should be copied from the source", new File(mifWorkingDir, SCRIPT_FILE_NAME).exists());
        assertTrue("Data file should be copied from the source", new File(mifWorkingDir, DATA_FILE_NAME).exists());
    }

    /**
     * Demonstrator 1.0 "Plan A" mode: MDL to intermediate PharmML.
     * @throws IOException
     */
    @Test
    public void shouldPublishMDLInputs_Demonstrator10PlanAMode() throws IOException {

        this.binding.setVariable("converter.wrapperscript.mdl", "MdlToPharmML.groovy");

        shouldPublishMDLInputs("PharmML", ".xml");

        assertTrue("XML resource should be created", new File(mifWorkingDir, "warfarin_PK_PRED.xml").exists());
        assertTrue("PharmML resource should be created", new File(mifWorkingDir, "warfarin_PK_PRED.pharmml").exists());
    }

    /**
     * Demonstrator 1.0 "Plan B" mode: MDL direct to NMTRAN (NONMEM).
     * @throws IOException
     */
    @Test
    public void shouldPublishMDLInputs_Demonstrator10PlanBMode() throws IOException {

        this.binding.setVariable("converter.wrapperscript.mdl", "MdlToNmtran.groovy");

        shouldPublishMDLInputs("NMTRAN", ".ctl");

        assertTrue("Control file should be created", new File(mifWorkingDir, "warfarin_PK_PRED.ctl").exists());
    }

    @Test
    public void shouldPublishCTLInputsWhenModelFileWithinSubdirectory() throws IOException {

        // Copy the files out of the testdata JAR file

        final String testDataDir = "/eu/ddmore/testdata/models/NM-TRAN/7.2.0/warfarin_PK_PRED/";

        final URL ctlFile = PublishInputsScriptTest.class.getResource(testDataDir + "warfarin_PK_PRED.ctl");
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + "warfarin_conc_pca.csv");

        FileUtils.copyURLToFile(ctlFile, new File(testWorkingDir, "warfarin/warfarin_PK_PRED.ctl"));
        FileUtils.copyURLToFile(dataFile, new File(testWorkingDir, "warfarin/warfarin_conc_pca.csv"));

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("warfarin/warfarin_PK_PRED.ctl");
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("CTL file should be copied from the source to within the subdirectory",
            new File(mifWorkingDir, "warfarin/warfarin_PK_PRED.ctl").exists());
        assertTrue("Data file should be copied from the source to within the subdirectory",
            new File(mifWorkingDir, "warfarin/warfarin_conc_pca.csv").exists());
        assertFalse("CTL file should not be present within the main directory", new File(mifWorkingDir, "warfarin_PK_PRED.ctl").exists());
        assertFalse("Data file should not be present within the main directory", new File(mifWorkingDir, "warfarin_conc_pca.csv").exists());
    }

    @Test
    public void shouldPublishPharmMLInputsWhenModelFileWithinSubdirectory() throws IOException {

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "example3.xml";
        final String DATA_FILE_NAME = "example3_data.csv"; // or _full_data_MDV.csv ?

        final String testDataDir = "/eu/ddmore/testdata/models/PharmML/0.3.0/example3/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(testWorkingDir, "example3/" + SCRIPT_FILE_NAME));
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(testWorkingDir, "example3/" + DATA_FILE_NAME));

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("example3/" + SCRIPT_FILE_NAME);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("XML resource should be copied from the source to within the subdirectory",
            new File(mifWorkingDir, "example3/" + SCRIPT_FILE_NAME).exists());
        assertTrue("Data file should be copied from the source to within the subdirectory",
            new File(mifWorkingDir, "example3/" + DATA_FILE_NAME).exists());
        assertTrue("PharmML resource should be created within the subdirectory",
            new File(mifWorkingDir, "example3/example3.pharmml").exists());
        assertFalse("XML resource should not be present within the main directory",
            new File(mifWorkingDir, SCRIPT_FILE_NAME).exists());
        assertFalse("Data file should not be present within the main directory",
            new File(mifWorkingDir, DATA_FILE_NAME).exists());
        assertFalse("PharmML resource should not be created within the main directory",
            new File(mifWorkingDir, "example3.pharmml").exists());
    }

    @Test
    public void shouldPublishMDLInputsWhenModelFileWithinSubdirectory_Demonstrator10PlanAMode() throws IOException {
    
        // Demonstrator 1.0 "Plan A" mode: MDL -> intermediate PharmML 
        this.binding.setVariable("converter.wrapperscript.mdl", "MdlToPharmML.groovy");

        // Copy the files out of the testdata JAR file

        final String SCRIPT_FILE_NAME = "warfarin_PK_PRED.mdl";
        final String DATA_FILE_NAME = "warfarin_conc_pca.csv";

        final String testDataDir = "/eu/ddmore/testdata/models/mdl/warfarin_PK_PRED/";

        final URL scriptFile = PublishInputsScriptTest.class.getResource(testDataDir + SCRIPT_FILE_NAME);
        FileUtils.copyURLToFile(scriptFile, new File(testWorkingDir, "warfarin/" + SCRIPT_FILE_NAME));
        final URL dataFile = PublishInputsScriptTest.class.getResource(testDataDir + DATA_FILE_NAME);
        FileUtils.copyURLToFile(dataFile, new File(testWorkingDir, "warfarin/" + DATA_FILE_NAME));

        // Set up the behaviour of the mock DefaultExecutor used to execute the conversion script
        when(this.mockExecutor.execute(isA(CommandLine.class))).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock invocation) throws Throwable {
                final CommandLine invokedCmdLine = (CommandLine) invocation.getArguments()[0];
                final String[] cmdLineArgs = invokedCmdLine.getArguments();
                assertEquals("Checking argument 0 to the call to the converter toolbox", PATH_TO_CONVERTER_EXE.getPath(), invokedCmdLine.getExecutable());
                assertEquals("Checking argument 1 to the call to the converter toolbox",
                    new File(mifWorkingDir, "warfarin/warfarin_PK_PRED.mdl").getAbsolutePath(), cmdLineArgs[0]);
                assertEquals("Checking argument 2 to the call to the converter toolbox",
                    new File(mifWorkingDir, "warfarin").getAbsolutePath(), cmdLineArgs[1]);
                assertEquals("Checking argument 3 to the call to the converter toolbox", "MDL", cmdLineArgs[2]);
                assertEquals("Checking argument 5 to the call to the converter toolbox", "PharmML", cmdLineArgs[4]);

                // Simulate the conversion process generating the output PharmML model files
                FileUtils.touch(new File(mifWorkingDir.getAbsolutePath(), ("warfarin/" + SCRIPT_FILE_NAME).replace(".mdl", ".xml")));

                return 0;
            }
        });

        // Proceed with the test...

        LocalJob job = mock(LocalJob.class);
        when(job.getWorkingDirectory()).thenReturn(testWorkingDir.getAbsolutePath());
        when(job.getControlFile()).thenReturn("warfarin/" + SCRIPT_FILE_NAME);
        when(job.getId()).thenReturn("MIF_JOB_ID");
        jobProcessor.process(job);

        // Checking the working directory that is set for the call to the converter toolbox
        verify(this.mockExecutor).setWorkingDirectory(new File("/path/to/my"));

        assertTrue("MIF working directory should be created", mifWorkingDir.exists());
        assertTrue("MDL file should be copied from the source to within the subdirectory",
            new File(mifWorkingDir, "warfarin/" + SCRIPT_FILE_NAME).exists());
        assertTrue("Data file should be copied from the source to within the subdirectory",
            new File(mifWorkingDir, "warfarin/" + DATA_FILE_NAME).exists());
        assertTrue("XML resource should be created within the subdirectory",
            new File(mifWorkingDir, "warfarin/warfarin_PK_PRED.xml").exists());
        assertTrue("PharmML resource should be created within the subdirectory",
            new File(mifWorkingDir, "warfarin/warfarin_PK_PRED.pharmml").exists());
        assertFalse("MDL file should not be present within the main directory",
            new File(mifWorkingDir, SCRIPT_FILE_NAME).exists());
        assertFalse("Data file should not be present within the main directory",
            new File(mifWorkingDir, DATA_FILE_NAME).exists());
        assertFalse("XML resource should not be present within the main directory",
            new File(mifWorkingDir, "warfarin_PK_PRED.xml").exists());
        assertFalse("PharmML resource should not be present within the main directory",
            new File(mifWorkingDir, "warfarin_PK_PRED.pharmml").exists());
    }

}
