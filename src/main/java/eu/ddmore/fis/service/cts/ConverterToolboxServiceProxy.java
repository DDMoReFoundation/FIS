/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package eu.ddmore.fis.service.cts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.convertertoolbox.domain.Conversion;
import eu.ddmore.convertertoolbox.domain.ConversionCapability;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionStatus;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.convertertoolbox.domain.ServiceDescriptor;
import eu.ddmore.convertertoolbox.domain.hal.ConversionResource;
import eu.ddmore.convertertoolbox.domain.hal.LinkRelation;
import eu.ddmore.convertertoolbox.domain.hal.ServiceDescriptorResource;
import eu.ddmore.fis.service.cts.internal.ConversionToStringConverter;


/**
 * A proxy translating {@link ConverterToolboxService} interface calls into CTS REST API calls
 * 
 * This implementation uses the calling Thread to perform monitoring.
 * 
 * Note, this class is thread-safe and should be kept this way, since it can be invoked by concurrently by FIS HTTP request handlers.
 * 
 * This implementation uses {@link RestTemplate} for REST communication, any {@link RuntimeException} indicating HTTP communication error thrown by {@link RestTemplate}
 * are wrapped in {@link ConverterToolboxServiceException}.
 * 
 * Errors being a result of incorrect usage (e.g. null-pointer arguments, client requesting non-existing conversions etc.) result in Runtime Exceptions
 * 
 */
@Service("converterToolboxService")
public class ConverterToolboxServiceProxy implements ConverterToolboxService {
    private static final Logger LOG = Logger.getLogger(ConverterToolboxServiceProxy.class);
    private final RestTemplate restTemplate;
    private final String ctsUrl;
    
    @Value("${fis.cts.link.relation.template:ddmore:%s}")
    private String linkRelationTemplate;
    
    @Autowired(required=true)
    private ConversionToStringConverter conversionMapper;
    
    @Value("${fis.cts.pollingDelay:1}")
    private long pollingDelay;
    
    @Value("${fis.cts.debug:false}")
    private boolean skipConversionRemoval;

    @Autowired(required=true)
    public ConverterToolboxServiceProxy(@Qualifier("ctsRestTemplate") RestTemplate restTemplate, @Value("${fis.cts.url}") String ctsUrl) {
        this.restTemplate = restTemplate;
        this.ctsUrl = ctsUrl;
    }

    @Override
    public Collection<ConversionCapability> getConversionCapabilities() throws ConverterToolboxServiceException {
        return getServiceDescriptorResource().getContent().getCapabilities();
    }

    @Override
    public ConversionReport convert(Archive archive, LanguageVersion from, LanguageVersion to) throws ConverterToolboxServiceException {
        Preconditions.checkNotNull(archive, "Archive can't be null.");
        File archiveLocation = null;
        try {
            archiveLocation = archive.getArchiveFile();
        } catch (ArchiveException e) {
            throw new IllegalStateException("Could not retrieve input Archive's file.",e);
        }
        return convert(archive, from, to, archiveLocation);
    }

    @Override
    public ConversionReport convert(Archive archive, LanguageVersion from, LanguageVersion to, File outputFile)
            throws ConverterToolboxServiceException {
        Preconditions.checkNotNull(archive, "Archive can't be null.");
        Preconditions.checkNotNull(from, "Source language can't be null.");
        Preconditions.checkNotNull(to, "Target language can't be null.");
        Preconditions.checkNotNull(outputFile, "Output file location can't be null.");
        
        ServiceDescriptorResource serviceDescriptorResource = getServiceDescriptorResource();
        
        if(!isConversionSupported(serviceDescriptorResource.getContent(), from, to)) {
            throw new IllegalStateException(String.format("Conversion from %s to %s is not supported.", from, to));
        }
        
        Conversion conversion = prepareConversion(archive, from, to);
        ConversionResource submittedConversion = submit(serviceDescriptorResource, archive, conversion);
        
        submittedConversion = monitor(submittedConversion);
        retrieveResult(outputFile,submittedConversion);
        removeConversion(submittedConversion);
        
        return submittedConversion.getContent().getConversionReport();
    }
    
    private void removeConversion(ConversionResource conversion) {
        if(skipConversionRemoval) {
            LOG.warn("Skipping conversion removal");
            return;
        }
        Link deleteLink = conversion.getLink(toExternalForm(LinkRelation.DELETE.getRelation()));
        if(deleteLink!=null) {
            LOG.debug(String.format("Removing conversion at %s.",deleteLink.getHref()));
            try {
                restTemplate.delete(deleteLink.getHref());
            } catch (HttpStatusCodeException ex) {
                // this is non-critical
                LOG.error("Could not delete conversion on CTS",ex);
            }
        } else {
            LOG.warn(String.format("No %s relation link available for conversion %s", LinkRelation.DELETE.getRelation(), conversion.getId()));
        }
    }
    
    @VisibleForTesting
    void retrieveResult(File archiveFile, ConversionResource conversion) throws ConverterToolboxServiceException {
        Link resultLink = conversion.getLink(toExternalForm(LinkRelation.RESULT.getRelation()));
        if(resultLink!=null) {
            ResponseEntity<ByteArrayResource> response = null;
            try {
                response = restTemplate.getForEntity(resultLink.getHref(), ByteArrayResource.class);
            } catch (HttpStatusCodeException ex) {
                throw new ConverterToolboxServiceException(String.format("Couldn't retrieve result archive: [%s]",ex.getResponseBodyAsString()), ex);
            }
            checkStatus(response);
            dumpResultToFile(archiveFile, response.getBody());
        } else {
            LOG.warn(String.format("No result was generated for conversion %s", conversion.getId()));
        }
    }

    private void dumpResultToFile(File file, ByteArrayResource body) throws ConverterToolboxServiceException {
        LOG.debug(String.format("Dumping result Archive from CTS to %s", file));
        try (InputStream in = body.getInputStream(); OutputStream out = new FileOutputStream(file)){
            IOUtils.copy(in,out);
        } catch (IOException e) {
            throw new ConverterToolboxServiceException(String.format("Could not write conversion result to %s", file), e);
        }
    }

    private ConversionResource monitor(final ConversionResource conversion) throws ConverterToolboxServiceException {
        ConversionResource conversionResource = conversion;
        ConversionStatus status = conversion.getContent().getStatus();
        while(ConversionStatus.Completed.compareTo(status)>0) {
            LOG.debug(String.format("Conversion %s status is %s",conversionResource.getContent().getId(),status));
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(pollingDelay ));
            } catch (InterruptedException e) {
                throw new RuntimeException("Error when monitoring conversion status.",e);
            }
            conversionResource = getConversionResource(conversionResource);
            status = conversionResource.getContent().getStatus();
        }
        return conversionResource;
    }
    
    @VisibleForTesting
    ConversionResource getConversionResource(ConversionResource conversionResource) throws ConverterToolboxServiceException {
        Link conversionLink = conversionResource.getLink(LinkRelation.SELF.getRelation());
        Preconditions.checkState(conversionLink!=null, String.format("Link with %s relation was not found.", LinkRelation.SELF.getRelation()));
        String conversionUrl = conversionLink.getHref();
        LOG.debug(String.format("Retrieving conversion from %s.",conversionUrl));
        ResponseEntity<ConversionResource> response = null;
        try {
            response = restTemplate.getForEntity(conversionUrl, ConversionResource.class);
        } catch (HttpStatusCodeException ex) {
            throw new ConverterToolboxServiceException(String.format("Couldn't retrieve latest status of conversion: [%s]",ex.getResponseBodyAsString()), ex);
        }
        checkStatus(response);
        LOG.debug(response.getBody().getContent());
        LOG.debug(response.getBody());
        return response.getBody();
    }
    
    @VisibleForTesting
    ConversionResource submit(ServiceDescriptorResource serviceDescriptorResource, Archive archive, Conversion conversion) throws ConverterToolboxServiceException {
        MultiValueMap<String,Object> requestParams = new LinkedMultiValueMap<String,Object>();
        try {
            requestParams.add("file", new FileSystemResource(archive.getArchiveFile()));
        } catch (ArchiveException e) {
            throw new IllegalStateException("Could not prepare archive file for upload.",e);
        }
        requestParams.add("conversion", conversionMapper.convert(conversion));
        Link submitLink = serviceDescriptorResource.getLink(toExternalForm(LinkRelation.SUBMIT.getRelation()));
        Preconditions.checkState(submitLink!=null, String.format("Link with %s relation was not found.", LinkRelation.SUBMIT.getRelation()));
        LOG.debug(String.format("Submitting conversion to %s", submitLink.getHref()));
        ResponseEntity<ConversionResource> response = null;
        try {
            response = restTemplate.postForEntity(submitLink.getHref(), requestParams, ConversionResource.class);
        } catch (HttpStatusCodeException ex) {
            throw new ConverterToolboxServiceException(String.format("Couldn't submit conversion, CTS response: [%s]",ex.getResponseBodyAsString()), ex);
        }
        checkStatus(response);
        return response.getBody();
    }
    
    @VisibleForTesting
    Conversion prepareConversion(Archive archive, LanguageVersion from, LanguageVersion to) {
        String mainEntry = null;
        try {
            archive.open();
            if(archive.getMainEntries().size()==0) {
                throw new IllegalArgumentException("No main entries were specified in the Archive");
            }
            mainEntry = archive.getMainEntries().iterator().next().getFilePath();
            if(archive.getMainEntries().size()>1) {
                LOG.warn(String.format("There are %s main entries in the archive, first (%s) will be used as input for conversion",archive.getMainEntries().size(),mainEntry));
            }
        } finally {
            archive.close();
        }
        Conversion conversion = new Conversion();
        conversion.setFrom(from);
        conversion.setTo(to);
        conversion.setInputFileName(mainEntry);
        return conversion;
    }

    private boolean isConversionSupported(ServiceDescriptor serviceDescriptor, final LanguageVersion from, final LanguageVersion to) {
        Collection<ConversionCapability> capabilities = serviceDescriptor.getCapabilities();
        return capabilities.size()>0&&!Collections2.filter(capabilities, new Predicate<ConversionCapability>() {
            @Override
            public boolean apply(ConversionCapability candidate) {
                return candidate.getSource().equals(from) && candidate.getTarget().contains(to);
            }
        }).isEmpty();
    }
    
    private ServiceDescriptorResource getServiceDescriptorResource() throws ConverterToolboxServiceException {
        LOG.debug(String.format("Retrieving CTS Service Descriptor from %s", ctsUrl));
        ResponseEntity<ServiceDescriptorResource> response = null;
        try {
            response = restTemplate.getForEntity(ctsUrl, ServiceDescriptorResource.class);
        } catch (HttpStatusCodeException ex) {
            throw new ConverterToolboxServiceException(String.format("Couldn't contact Converter Toolbox Service, response: [%s]",ex.getResponseBodyAsString()), ex);
        }
        checkStatus(response);
        LOG.debug(response.getBody().getContent());
        LOG.debug(response.getBody());
        return response.getBody();
    }
    
    private void checkStatus(ResponseEntity<?> response) throws ConverterToolboxServiceException {
        if(!response.getStatusCode().is2xxSuccessful()||!response.hasBody()) {
            throw new ConverterToolboxServiceException(String.format("Converter Toolbox Service communication error. Request status %s : %s",response.getStatusCode().value(), response.getStatusCode().getReasonPhrase()));
        }
    }
    
    private String toExternalForm(String relation) {
        return String.format(linkRelationTemplate, relation);
    }
    
    @VisibleForTesting
    void setConversionMapper(ConversionToStringConverter conversionMapper) {
        this.conversionMapper = conversionMapper;
    }

    @VisibleForTesting
    void setLinkRelationTemplate(String linkRelationTemplate) {
        this.linkRelationTemplate = linkRelationTemplate;
    }

    @Override
    public boolean isConversionSupported(LanguageVersion from, LanguageVersion to) throws ConverterToolboxServiceException {
        Preconditions.checkNotNull(from, "Source language can't be null.");
        Preconditions.checkNotNull(to, "Target language can't be null.");
        ServiceDescriptorResource serviceDescriptorResource = getServiceDescriptorResource();
        return isConversionSupported(serviceDescriptorResource.getContent(), from, to);
    }

}
