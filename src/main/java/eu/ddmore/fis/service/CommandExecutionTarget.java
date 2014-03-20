package eu.ddmore.fis.service;

public class CommandExecutionTarget {

    private String converterToolboxPath;
    private String environmentSetupScript;
    private String toolExecutablePath;
    private String executionType;
    private String command;

    public String getConverterToolboxPath() {
        return converterToolboxPath;
    }

    public String getEnvironmentSetupScript() {
        return environmentSetupScript;
    }

    public void setConverterToolboxPath(String converterToolboxPath) {
        this.converterToolboxPath = converterToolboxPath;
    }

    public void setEnvironmentSetupScript(String environmentSetupScript) {
        this.environmentSetupScript = environmentSetupScript;
    }
    
    public void setToolExecutablePath(String toolExecutablePath) {
        this.toolExecutablePath = toolExecutablePath;
    }
    
    public String getToolExecutablePath() {
        return toolExecutablePath;
    }
    
    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }
    
    public String getExecutionType() {
        return executionType;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public Object getCommand() {
        return command;
    }
}
