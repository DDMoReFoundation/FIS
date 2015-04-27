/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.configuration;

import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import eu.ddmore.convertertoolbox.domain.LanguageVersion;
import eu.ddmore.convertertoolbox.domain.Version;


/**
 * Configuration that initiates common languages instances
 */
@Configuration
public class Languages {
    
    @Bean
    @Autowired(required=true)
    public LanguageVersion mdlLanguage(@Value("${fis.mdl.name}") String language, @Value("${fis.mdl.version}") String version) {
        return new LanguageVersion(language, parseVersion(version));
    }

    @Bean
    @Autowired(required=true)
    public LanguageVersion pharmmlLanguage(@Value("${fis.pharmml.name}") String language, @Value("${fis.pharmml.version}") String version) {
        return new LanguageVersion(language, parseVersion(version));
    }
    
    @VisibleForTesting
    Version parseVersion(String version) {
        Preconditions.checkNotNull(version, "Version can't be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(version), "Version can't be blank");
        StringTokenizer tokenizer = new StringTokenizer(version, ".-");
        Preconditions.checkArgument(tokenizer.countTokens()>=3, String.format("Invalid version number %s, there are missing version segments", version));
        int major = Integer.parseInt(tokenizer.nextToken());
        int minor = Integer.parseInt(tokenizer.nextToken());
        int micro = Integer.parseInt(tokenizer.nextToken());
        String qualifier = "";
        if(tokenizer.hasMoreTokens())
            qualifier = tokenizer.nextToken();
        return new Version(major, minor, micro, qualifier);
    }
}
