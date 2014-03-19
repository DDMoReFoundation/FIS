/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.teis;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;

import eu.ddmore.miflocal.domain.LocalJob;
import eu.ddmore.miflocal.domain.LocalJobStatus;
import eu.ddmore.miflocal.domain.SubmissionRequest;
import eu.ddmore.miflocal.domain.SubmissionResponse;

/**
 * TEISHttpRestClient for interacting with TES IS REST services.
 */
public class TEISHttpRestClient {
    private static final Logger log = Logger.getLogger(TEISHttpRestClient.class);
    private String endPoint;
    private HttpClient client;
    
    /**
     * 
     * @param endPoint - TES endpoint
     */
    public TEISHttpRestClient(String endPoint) {
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


    private String buildEndpoint(String...parts) {
        StringBuilder builder = new StringBuilder();
        builder.append(endPoint);
        builder.append(Joiner.on("/").join(parts));
        return builder.toString();
    }
}
