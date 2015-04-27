/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
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
 * A proxy translating ConverterToolboxService interface calls into CTS REST API calls
 * 
 * This implementation uses the calling Thread to perform monitoring.
 * 
 * This implementation uses RestTemplate for REST communication, any RuntimeExceptions thrown by it
 * are passed directly to the client layer since they represent invalid system state (e.g. remote service
 * being unavailable). Clients should not capture these.
 * 
 */
@Service("converterToolboxService")
public class ConverterToolboxServiceProxy implements ConverterToolboxService {
    private static final Logger LOG = Logger.getLogger(ConverterToolboxServiceProxy.class);
    private final RestTemplate restTemplate;
    private final String ctsUrl;

    @Autowired(required=true)
    private ConversionToStringConverter conversionMapper;
    
    @Value("${fis.cts.pollingDelay:1}")
    private long pollingDelay;

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
        try {
            return convert(archive, from, to, archive.getArchiveFile());
        } catch (ArchiveException e) {
            throw new ConverterToolboxServiceException("Could not retrieve Archive file",e);
        }
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
        restTemplate.delete(conversion.getLink(LinkRelation.DELETE.getRelation()).getHref());
    }

    private void retrieveResult(File archiveFile, ConversionResource conversion) throws ConverterToolboxServiceException {
        Link resultLink = conversion.getLink(LinkRelation.RESULT.getRelation());
        if(resultLink!=null) {
            ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity(resultLink.getHref(), ByteArrayResource.class);
            checkStatus(response);
            dumpResultToFile(archiveFile, response.getBody());
        } else {
            LOG.warn(String.format("No result was generated for conversion %s", conversion.getId()));
        }
    }

    private void dumpResultToFile(File file, ByteArrayResource body) throws ConverterToolboxServiceException {
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
                LOG.error(e);
            }
            conversionResource = getConversionResource(conversionResource);
            status = conversionResource.getContent().getStatus();
        }
        return conversionResource;
    }

    private ConversionResource getConversionResource(ConversionResource conversionResource) throws ConverterToolboxServiceException {
        String conversionUrl = conversionResource.getLink(LinkRelation.SELF.getRelation()).getHref();
        LOG.debug(String.format("Retrieving conversion from %s.",conversionUrl));
        ResponseEntity<ConversionResource> response = restTemplate.getForEntity(conversionUrl, ConversionResource.class);
        checkStatus(response);
        return response.getBody();
    }

    private ConversionResource submit(ServiceDescriptorResource serviceDescriptorResource, Archive archive, Conversion conversion) throws ConverterToolboxServiceException {
        MultiValueMap<String,Object> requestParams = new LinkedMultiValueMap<String,Object>();
        try {
            requestParams.add("file", new FileSystemResource(archive.getArchiveFile()));
        } catch (ArchiveException e) {
            throw new ConverterToolboxServiceException("Could not get Archive file",e);
        }
        requestParams.add("conversion", conversionMapper.convert(conversion));
        ResponseEntity<ConversionResource> response = restTemplate.postForEntity(serviceDescriptorResource.getLink(LinkRelation.SUBMIT.getRelation()).getHref(), requestParams, ConversionResource.class);
        checkStatus(response);
        return response.getBody();
    }
    
    @VisibleForTesting
    Conversion prepareConversion(Archive archive, LanguageVersion from, LanguageVersion to) {
        if(archive.getMainEntries().size()==0) {
            throw new IllegalArgumentException("No main entries were specified in the Archive");
        }
        String mainEntry = archive.getMainEntries().get(0).getFilePath();
        if(archive.getMainEntries().size()>1) {
            LOG.warn(String.format("There are %s main entries in the archive, first (%s) will be used for conversion",archive.getMainEntries().size(),mainEntry));
        }
        Conversion conversion = new Conversion();
        conversion.setFrom(from);
        conversion.setTo(to);
        conversion.setInputFileName(mainEntry);
        return conversion;
    }

    private boolean isConversionSupported(ServiceDescriptor serviceDescriptor, final LanguageVersion from, final LanguageVersion to) {
        Collection<ConversionCapability> conversions = serviceDescriptor.getCapabilities();
        return conversions.size()==0||!Collections2.filter(conversions, new Predicate<ConversionCapability>() {
            @Override
            public boolean apply(ConversionCapability candidate) {
                return candidate.getSource().equals(from) && candidate.getTarget().contains(to);
            }
            
        }).isEmpty();
    }
    
    private ServiceDescriptorResource getServiceDescriptorResource() throws ConverterToolboxServiceException {
        ResponseEntity<ServiceDescriptorResource> response = restTemplate.getForEntity(ctsUrl, ServiceDescriptorResource.class);
        checkStatus(response);
        return response.getBody();
    }
    
    private void checkStatus(ResponseEntity<?> response) throws ConverterToolboxServiceException {
        if(!response.getStatusCode().is2xxSuccessful()||!response.hasBody()) {
            throw new ConverterToolboxServiceException(String.format("Converter Toolbox Service communication error. Request status %s : %s",response.getStatusCode().value(), response.getStatusCode().getReasonPhrase()));
        }
    }
    
    @VisibleForTesting
    void setConversionMapper(ConversionToStringConverter conversionMapper) {
        this.conversionMapper = conversionMapper;
    }

    @Override
    public boolean isConversionSupported(LanguageVersion from, LanguageVersion to) throws ConverterToolboxServiceException {
        Preconditions.checkNotNull(from, "Source language can't be null.");
        Preconditions.checkNotNull(to, "Target language can't be null.");
        ServiceDescriptorResource serviceDescriptorResource = getServiceDescriptorResource();
        return isConversionSupported(serviceDescriptorResource.getContent(), from, to);
    }

}
