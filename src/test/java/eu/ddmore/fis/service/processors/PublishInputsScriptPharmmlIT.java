package eu.ddmore.fis.service.processors;

/**
 * Integration Test for Publish PharmML Inputs groovy script, that doesn't perform conversion.
 */
public class PublishInputsScriptPharmmlIT extends AbstractPublishInputsScriptTestBase {

    private static final String PUBLISH_PHARMML_INPUTS_SCRIPT = "/scripts/publishInputsPharmML.groovy";
    
    private final static String TEST_DATA_DIR = "/test-models/PharmML/0.6.0/";
    private final static String PHARMML_FILE_NAME = "UseCase1.xml";
    private final static String DATA_FILE_NAME = "warfarin_conc.csv";
    
    @Override
    protected String getPublishInputsScript() {
        return PUBLISH_PHARMML_INPUTS_SCRIPT;
    }
    
    @Override
    protected String getTestDataDir() {
        return TEST_DATA_DIR;
    }
    
    @Override
    protected String getModelFileName() {
        return PHARMML_FILE_NAME;
    }
    
    @Override
    protected String getDataFileName() {
        return DATA_FILE_NAME;
    }

}
