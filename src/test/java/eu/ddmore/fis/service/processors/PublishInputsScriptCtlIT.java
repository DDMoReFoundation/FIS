package eu.ddmore.fis.service.processors;

/**
 * Integration Test for Publish CTL Inputs groovy script, that doesn't perform conversion.
 */
public class PublishInputsScriptCtlIT extends AbstractPublishInputsScriptTestBase {

    private static final String PUBLISH_CTL_INPUTS_SCRIPT = "/scripts/publishInputsCtl.groovy";
    
    private final static String TEST_DATA_DIR = "/test-models/NM-TRAN/7.2.0/Warfarin_ODE/";
    private final static String CONTROL_FILE_NAME = "Warfarin-ODE-latest.ctl";
    private final static String DATA_FILE_NAME = "warfarin_conc.csv";
    
    @Override
    protected String getPublishInputsScript() {
        return PUBLISH_CTL_INPUTS_SCRIPT;
    }
    
    @Override
    protected String getTestDataDir() {
        return TEST_DATA_DIR;
    }
    
    @Override
    protected String getModelFileName() {
        return CONTROL_FILE_NAME;
    }
    
    @Override
    protected String getDataFileName() {
        return DATA_FILE_NAME;
    }

}
