/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.ddmore.fis.domain.UserInfo;

/**
 * Java Configuration file with FIS core beans definitions
 */
@Configuration
public class Core {

    @Bean
    public UserInfo invokerParameters(@Value("${fis.core.user.name}") String username, @Value("${fis.core.user.password}") String password, @Value("${fis.core.user.identityFile}") String identityFile,  @Value("${fis.core.user.identityFilePassphrase}") String passphrase, @Value("${fis.core.user.executeAsUser}") boolean executeAsUser ) {
        return new UserInfo(username, password, identityFile, passphrase, executeAsUser);
    }
}
