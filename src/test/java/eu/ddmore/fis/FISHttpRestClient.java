/*******************************************************************************
 * Copyright (C) 2014-2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;

import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;

/**
 * Http Rest Client for interacting with FIS REST services.
 */
public class FISHttpRestClient {

    private static final Logger LOG = Logger.getLogger(FISHttpRestClient.class);
    
    private String endPoint;
    private HttpClient client;
    private String managementEndpoint;
    
    /**
     * 
     * @param endPoint - FIS endpoint
     */
    public FISHttpRestClient(String endPoint, String managementEndpoint) {
        this.endPoint = endPoint;
        this.managementEndpoint = managementEndpoint;
        client = new HttpClient();
    }

    /**
     * Submit a job to FIS.
     * 
     * @return fis job
     */
    public ExternalJob submitRequest(ExternalJob job) {
        
        String endpoint = buildEndpoint("jobs");
        PostMethod post = new PostMethod(endpoint);
        
        post.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(job);
        } catch (Exception e) {
            throw new RuntimeException("Could not produce json", e);
        }
        try {
			post.setRequestEntity(new StringRequestEntity(json,MediaType.APPLICATION_JSON,"utf-8"));
		} catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("Could not create a request.", e1);
		}
        
        LOG.info(String.format("Sending execution request: %s",json));
        
        String response = executeMethod(endpoint, post);
        try {
            return mapper.readValue(response, ExternalJob.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not parse json %s",response), e);
        }
    }

    private String executeMethod(String endpoint, HttpMethod method) {
        int status = 0;
        String result = null;
        try {
            status = client.executeMethod(method);
            result = method.getResponseBodyAsString();
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Could not connect to server %s.",endpoint),e);
        }
        
        if(status!=200||result==null) {
            throw new IllegalStateException(String.format("Unexpected response from FIS Service: %s", result));
        }
        return result;
    }

    /**
     * Check status of a job.
     * Retrieve current job status
     * @param job id
     */
    public LocalJobStatus checkStatus(String jobId) {
        String endpoint = buildEndpoint("jobs", "status",jobId);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        ObjectMapper mapper = new ObjectMapper();
        String response = executeMethod(endpoint, get);
        try {
            return mapper.readValue(response, LocalJobStatus.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not parse json %s",response), e);
        }
    }

    /**
     * Retrieve a job from the FIS service.
     * @param job id
     */
    public ExternalJob getJob(String jobId) {
        String endpoint = buildEndpoint("jobs", jobId);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        String response = executeMethod(endpoint, get);
        try {
            return mapper.readValue(response, ExternalJob.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not parse json %s",response), e);
        }
    }
    
    public String shutdown() {
        String endpoint = buildEndpoint("shutdown");
        PostMethod get = new PostMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        return executeMethod(endpoint,get);
    }
    
    public String healthcheck() {
        String endpoint = buildManagementEndpoint("health");
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        return executeMethod(endpoint,get);
    }

    public String readMdl(String fileName) {
    	String urlEncodedFilename;
        try {
	        urlEncodedFilename = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
	        throw new IllegalArgumentException("Unable to URL-encode file path: " + fileName);
        }
        String endpoint = buildEndpoint("readmdl?filePath=" + urlEncodedFilename);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        return executeMethod(endpoint,get);
    }

    public WriteMdlResponse writeMdl(WriteMdlRequest writeRequest) {
        String endpoint = buildEndpoint("writemdl");
        PostMethod req = new PostMethod(endpoint);
        req.addRequestHeader("accept", MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(writeRequest);
        } catch (Exception e) {
            throw new RuntimeException("Could not produce json", e);
        }
        req.setParameter("writeRequest", json);
        
        LOG.info(String.format("Sending execution request: %s",json));
        
        String response = executeMethod(endpoint, req);

        try {
            return mapper.readValue(response, WriteMdlResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not parse json %s",response), e);
        }
    }

    private String buildEndpoint(String...parts) {
        StringBuilder builder = new StringBuilder();
        builder.append(endPoint);
        builder.append(Joiner.on("/").join(parts));
        return builder.toString();
    }

    private String buildManagementEndpoint(String...parts) {
        StringBuilder builder = new StringBuilder();
        builder.append(managementEndpoint);
        builder.append(Joiner.on("/").join(parts));
        return builder.toString();
    }
    
	public String convertMdlToPharmML(String mdlFileFullPath, String outputFileFullPath) {
    	String urlEncodedFilename,urlEncodedOutputName;
    	
        try {
	        urlEncodedFilename = URLEncoder.encode(mdlFileFullPath, "UTF-8");
	        urlEncodedOutputName = URLEncoder.encode(outputFileFullPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
	        throw new IllegalArgumentException("Unable to URL-encode file path: " + mdlFileFullPath);
        }
        String endpoint = buildEndpoint("convertmdl?filePath=" + urlEncodedFilename+"&outputDir="+urlEncodedOutputName);
        PostMethod postMethod = new PostMethod(endpoint);
        postMethod.addRequestHeader("accept", MediaType.APPLICATION_JSON);
        return executeMethod(endpoint,postMethod);
	}
}
