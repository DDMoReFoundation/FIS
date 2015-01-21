/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;
import eu.ddmore.fis.domain.SubmissionRequest;
import eu.ddmore.fis.domain.SubmissionResponse;
import eu.ddmore.fis.domain.WriteMdlRequest;
import eu.ddmore.fis.domain.WriteMdlResponse;

/**
 * TEISHttpRestClient for interacting with TES IS REST services.
 */
public class FISHttpRestClient {
    private static final Logger log = Logger.getLogger(FISHttpRestClient.class);
    private String endPoint;
    private HttpClient client;
    
    /**
     * 
     * @param endPoint - TES endpoint
     */
    public FISHttpRestClient(String endPoint) {
        this.endPoint = endPoint;
        client = new HttpClient();
    }

    /**
     * Submit an execution request to TES IS 
     * 
     * @return submission response 
     */
    public SubmissionResponse submitRequest(SubmissionRequest submissionRequest) {
        
        String endpoint = buildEndpoint("submit");
        PostMethod post = new PostMethod(endpoint);
        
        post.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(submissionRequest);
        } catch (Exception e) {
            throw new RuntimeException("Could not produce json", e);
        }
        post.setParameter("submissionRequest", json);
        
        log.info(String.format("Sending execution request: %s",json));
        
        String response = executeMethod(endpoint, post);
        try {
            return mapper.readValue(response, SubmissionResponse.class);
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
            throw new IllegalStateException(String.format("Unexpected response from TES IS Service: %s", result));
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
     * Retrieve a job from the TES IS service
     * @param job id
     */
    public LocalJob getJob(String jobId) {
        String endpoint = buildEndpoint("jobs", jobId);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        ObjectMapper mapper = new ObjectMapper();
        String response = executeMethod(endpoint, get);
        try {
            return mapper.readValue(response, LocalJob.class);
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
        String endpoint = buildEndpoint("healthcheck");
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        return executeMethod(endpoint,get);
    }

    public String readMdl(String fileName) {
    	String urlEncodedFilename;
        try {
	        urlEncodedFilename = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
	        throw new IllegalArgumentException("Unable to URL-encode file path: " + fileName);
        }
        String endpoint = buildEndpoint("readmdl?fileName=" + urlEncodedFilename);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        return executeMethod(endpoint,get);
    }

    public WriteMdlResponse writeMdl(WriteMdlRequest writeRequest) {
        String endpoint = buildEndpoint("writemdl");
        PostMethod req = new PostMethod(endpoint);
        req.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);

        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(writeRequest);
        } catch (Exception e) {
            throw new RuntimeException("Could not produce json", e);
        }
        req.setParameter("writeRequest", json);
        
        log.info(String.format("Sending execution request: %s",json));
        
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

	public String convertMdlToPharmML(String mdlFileFullPath, String outputFileFullPath) {
    	String urlEncodedFilename,urlEncodedOutputName;
    	
        try {
	        urlEncodedFilename = URLEncoder.encode(mdlFileFullPath, "UTF-8");
	        urlEncodedOutputName = URLEncoder.encode(outputFileFullPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
	        throw new IllegalArgumentException("Unable to URL-encode file path: " + mdlFileFullPath);
        }
        String endpoint = buildEndpoint("convertmdl?fileName=" + urlEncodedFilename+"&outputDir="+urlEncodedOutputName);
        GetMethod get = new GetMethod(endpoint);
        get.addRequestHeader("accept", MediaType.MEDIA_TYPE_WILDCARD);
        return executeMethod(endpoint,get);
	}
}
