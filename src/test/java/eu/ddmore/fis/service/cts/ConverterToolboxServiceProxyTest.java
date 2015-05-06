/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;

import eu.ddmore.archive.Archive;
import eu.ddmore.archive.Entry;
import eu.ddmore.archive.exception.ArchiveException;
import eu.ddmore.convertertoolbox.domain.Conversion;
import eu.ddmore.convertertoolbox.domain.ConversionCapability;
import eu.ddmore.convertertoolbox.domain.ConversionReport;
import eu.ddmore.convertertoolbox.domain.ConversionStatus;
import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.convertertoolbox.domain.ServiceDescriptor;
import eu.ddmore.convertertoolbox.domain.Version;
import eu.ddmore.convertertoolbox.domain.hal.ConversionResource;
import eu.ddmore.convertertoolbox.domain.hal.LinkRelation;
import eu.ddmore.convertertoolbox.domain.hal.ServiceDescriptorResource;
import eu.ddmore.fis.service.cts.internal.ConversionToStringConverter;

/**
 * Tests {@link ConverterToolboxServiceProxy}
 */
@RunWith(MockitoJUnitRunner.class)
public class ConverterToolboxServiceProxyTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ConversionToStringConverter conversionToStringConverter;

    private ConverterToolboxServiceProxy instance;

    private static final String MOCK_HOME_URL = "MOCK_HOME_URL";
    private static final String MOCK_SUBMIT_URL = "MOCK_SUBMIT_URL";
    private static final String MOCK_SELF_URL = "MOCK_SELF_URL";
    private static final String MOCK_DELETE_URL = "MOCK_DELETE_URL";
    private static final String MOCK_RESULT_URL = "MOCK_RESULT_URL";
    
    private static final String MOCK_ARCHIVE = "/eu/ddmore/fis/service/cts/mockArchive.txt";
    
    @Rule
    public TemporaryFolder tempArchiveLocation = new TemporaryFolder();
    
    @Before
    public void setUp() {
        instance = new ConverterToolboxServiceProxy(restTemplate, MOCK_HOME_URL);
        instance.setConversionMapper(conversionToStringConverter);
        instance.setLinkRelationTemplate("%s");
    }

    @Test(expected = ConverterToolboxServiceException.class)
    public void getConversions_shouldThrowExceptionIfThereIsACommunicationIssue() throws ConverterToolboxServiceException {
        ResponseEntity<ServiceDescriptorResource> notFoundResponse = new ResponseEntity<ServiceDescriptorResource>(HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(any(String.class), same(ServiceDescriptorResource.class))).thenReturn(notFoundResponse);
        instance.getConversionCapabilities();
    }

    @Test
    public void getConversions_shouldReturnListOfSupportedConversions() throws ConverterToolboxServiceException {
        ServiceDescriptorResource mockServiceDescriptorResource = createMockServiceDescriptorResource();
        ResponseEntity<ServiceDescriptorResource> mockResponse = new ResponseEntity<ServiceDescriptorResource>(
                mockServiceDescriptorResource, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_HOME_URL), same(ServiceDescriptorResource.class))).thenReturn(mockResponse);
        Collection<ConversionCapability> capabilities = instance.getConversionCapabilities();
        assertEquals(2, capabilities.size());
    }

    @Test(expected = NullPointerException.class)
    public void convert_shouldThrowRuntimeExceptionIfArchiveIsNull() throws ConverterToolboxServiceException {
        instance.convert(null, from("A"), to("B").iterator().next());
    }

    @Test(expected = NullPointerException.class)
    public void convert_shouldThrowRuntimeExceptionIfSourceLanguageIsNull() throws ConverterToolboxServiceException {
        instance.convert(mock(Archive.class), null, to("B").iterator().next());
    }

    @Test(expected = NullPointerException.class)
    public void convert_shouldThrowRuntimeExceptionIfTargetLanguageIsNull() throws ConverterToolboxServiceException {
        instance.convert(mock(Archive.class), from("A"), null);
    }

    @Test(expected = IllegalStateException.class)
    public void convert_shouldThrowRuntimeExceptionIfRequestedConversionIsNotSupported() throws ConverterToolboxServiceException, ArchiveException {
        ServiceDescriptorResource mockServiceDescriptorResource = createMockServiceDescriptorResource();
        ResponseEntity<ServiceDescriptorResource> mockResponse = new ResponseEntity<ServiceDescriptorResource>(
                mockServiceDescriptorResource, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_HOME_URL), same(ServiceDescriptorResource.class))).thenReturn(mockResponse);
        Archive archive = mock(Archive.class);
        when(archive.getArchiveFile()).thenReturn(mock(File.class));
        instance.convert(archive, from("A"), to("not-existing-language").iterator().next());
    }
    

    @Test(expected=ConverterToolboxServiceException.class)
    public void convert_shouldThrowConverterToolboxExceptionForArchiveException() throws ConverterToolboxServiceException, IOException, ArchiveException {
        ServiceDescriptorResource mockServiceDescriptorResource = createMockServiceDescriptorResource();
        ResponseEntity<ServiceDescriptorResource> mockResponse = new ResponseEntity<ServiceDescriptorResource>(
                mockServiceDescriptorResource, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_HOME_URL), same(ServiceDescriptorResource.class))).thenReturn(mockResponse);
        File archiveFile = tempArchiveLocation.newFile();
        Archive archive = mock(Archive.class);
        when(archive.getArchiveFile()).thenReturn(archiveFile);
        doThrow(ArchiveException.class).when(archive).open();
        instance.convert(archive, from("A"), to("B").iterator().next());
    }

    @Test
    public void convert_shouldPerformTheConversion() throws ConverterToolboxServiceException, IOException, ArchiveException {
        ServiceDescriptorResource mockServiceDescriptorResource = createMockServiceDescriptorResource();
        ResponseEntity<ServiceDescriptorResource> mockResponse = new ResponseEntity<ServiceDescriptorResource>(
                mockServiceDescriptorResource, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_HOME_URL), same(ServiceDescriptorResource.class))).thenReturn(mockResponse);
        File archiveFile = tempArchiveLocation.newFile();
        Archive archive = mock(Archive.class);
        when(archive.getArchiveFile()).thenReturn(archiveFile);
        Entry mainEntry = mock(Entry.class);
        when(mainEntry.getFilePath()).thenReturn("mock/path/to/file.ext");
        List<Entry> mainEntries = Lists.newArrayList(mainEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        
        //mocking submission
        {
            ConversionResource newConversionResource = mock(ConversionResource.class);
            Conversion newConversion = mock(Conversion.class);
            when(newConversion.getStatus()).thenReturn(ConversionStatus.New);
            when(newConversionResource.getContent()).thenReturn(newConversion);
            when(newConversionResource.getLink(LinkRelation.SELF.getRelation())).thenReturn(new Link(MOCK_SELF_URL));
            ResponseEntity<ConversionResource> submissionResponse = new ResponseEntity<ConversionResource>(newConversionResource, HttpStatus.OK);
            when(restTemplate.postForEntity(eq(MOCK_SUBMIT_URL), any(MultiValueMap.class), same(ConversionResource.class))).thenReturn(submissionResponse);
        }
        //mocking monitoring
        ConversionReport conversionReport = new ConversionReport();
        {
            ConversionResource runningConversionResource = mock(ConversionResource.class);
            Conversion runningConversion = mock(Conversion.class);
            when(runningConversion.getStatus()).thenReturn(ConversionStatus.New).thenReturn(ConversionStatus.Running)
                                                .thenReturn(ConversionStatus.Running).thenReturn(ConversionStatus.Running)
                                                .thenReturn(ConversionStatus.Completed);
            when(runningConversionResource.getContent()).thenReturn(runningConversion);
            when(runningConversionResource.getLink(LinkRelation.SELF.getRelation())).thenReturn(new Link(MOCK_SELF_URL));
            when(runningConversionResource.getLink(LinkRelation.DELETE.getRelation())).thenReturn(new Link(MOCK_DELETE_URL));
            when(runningConversionResource.getLink(LinkRelation.RESULT.getRelation())).thenReturn(new Link(MOCK_RESULT_URL));
            when(runningConversion.getConversionReport()).thenReturn(conversionReport);
            ResponseEntity<ConversionResource> statusResponse = new ResponseEntity<ConversionResource>(runningConversionResource, HttpStatus.OK);
            when(restTemplate.getForEntity(eq(MOCK_SELF_URL), same(ConversionResource.class))).thenReturn(statusResponse);
        }
        //mocking result handling
        
        ByteArrayResource resultReponse = mock(ByteArrayResource.class);
        when(resultReponse.getInputStream()).thenReturn(ConverterToolboxServiceProxyTest.class.getResourceAsStream(MOCK_ARCHIVE));
        ResponseEntity<ByteArrayResource> resultResponse = new ResponseEntity<ByteArrayResource>(resultReponse, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_RESULT_URL), same(ByteArrayResource.class))).thenReturn(resultResponse);
        
        ConversionReport outputReport = instance.convert(archive, from("A"), to("B").iterator().next());
        verify(restTemplate).delete(eq(MOCK_DELETE_URL));
        verify(restTemplate, times(5)).getForEntity(eq(MOCK_SELF_URL), same(ConversionResource.class));
        assertTrue(outputReport==conversionReport);
        assertTrue(FileUtils.sizeOf(archiveFile)>0);
        assertTrue(FileUtils.readFileToString(archiveFile).contains("This is mock archive"));
    }
    
    @Test
    public void prepareConversion_shouldUseFirstMainEntry() throws ArchiveException {
        Archive archive = mock(Archive.class);
        Entry firstMainEntry = mock(Entry.class);
        when(firstMainEntry.getFilePath()).thenReturn("first.ext");
        Entry secondMainEntry = mock(Entry.class);
        when(secondMainEntry.getFilePath()).thenReturn("second.ext");
        List<Entry> mainEntries = Lists.newArrayList(firstMainEntry,secondMainEntry);
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archive.getArchiveFile()).thenReturn(mock(File.class));
        Conversion conversion = instance.prepareConversion(archive, from("A"), to("B").iterator().next());
        
        assertEquals("first.ext",conversion.getInputFileName());
        verify(archive).open();
        verify(archive).close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void prepareConversion_shouldThrowExceptionIfNoMainEntries() throws ArchiveException {
        Archive archive = mock(Archive.class);
        List<Entry> mainEntries = Lists.newArrayList();
        when(archive.getMainEntries()).thenReturn(mainEntries);
        when(archive.getArchiveFile()).thenReturn(mock(File.class));
        instance.prepareConversion(archive, from("A"), to("B").iterator().next());
        verify(archive).open();
        verify(archive).close();
    }

    @Test
    public void isConversionSupported_shouldReturnTRUEIfConversionIsSupported() throws ConverterToolboxServiceException {
        ServiceDescriptorResource mockServiceDescriptorResource = createMockServiceDescriptorResource();
        ResponseEntity<ServiceDescriptorResource> mockResponse = new ResponseEntity<ServiceDescriptorResource>(
                mockServiceDescriptorResource, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_HOME_URL), same(ServiceDescriptorResource.class))).thenReturn(mockResponse);
        assertTrue("Conversion from A to B should be supported",instance.isConversionSupported(from("A"), to("B").iterator().next()));
    }

    @Test
    public void isConversionSupported_shouldReturnFALSEIfConversionIsNotSupported() throws ConverterToolboxServiceException {
        ServiceDescriptorResource mockServiceDescriptorResource = createMockServiceDescriptorResource();
        ResponseEntity<ServiceDescriptorResource> mockResponse = new ResponseEntity<ServiceDescriptorResource>(
                mockServiceDescriptorResource, HttpStatus.OK);
        when(restTemplate.getForEntity(eq(MOCK_HOME_URL), same(ServiceDescriptorResource.class))).thenReturn(mockResponse);
        assertFalse("Conversion from A to F should not be supported",instance.isConversionSupported(from("A"), to("F").iterator().next()));
    }
    
    
    private ServiceDescriptorResource createMockServiceDescriptorResource() {
        ServiceDescriptorResource mockServiceDescriptorResource = mock(ServiceDescriptorResource.class);
        ServiceDescriptor serviceDescriptor = mock(ServiceDescriptor.class);
        when(mockServiceDescriptorResource.getContent()).thenReturn(serviceDescriptor);
        when(serviceDescriptor.getCapabilities()).thenReturn(
            Arrays.asList(new ConversionCapability(from("A"), to("B", "C", "D")), new ConversionCapability(from("C"), to("B"))));
        
        when(mockServiceDescriptorResource.getLink(eq(LinkRelation.SUBMIT.getRelation()))).thenReturn(new Link(MOCK_SUBMIT_URL));
        return mockServiceDescriptorResource;
    }

    /**
     * Creates a { @link LanguageVersion } instance with the given name
     * @param languageName
     * @return language version
     */
    private static LanguageVersion from(String languageName) {
        return to(languageName).iterator().next();
    }

    /**
     * Creates a collection of { @link LanguageVersion } with given names
     * @param languageNames
     * @return
     */
    private static Collection<LanguageVersion> to(String... languageNames) {
        Collection<LanguageVersion> result = new ArrayList<LanguageVersion>();
        for (String language : languageNames) {
            result.add(new LanguageVersion(language, new Version(1, 0, 0, "Q")));
        }
        return result;
    }

}
