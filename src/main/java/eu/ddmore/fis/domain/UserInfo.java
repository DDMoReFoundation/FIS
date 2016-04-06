/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Holds user authentication information 
 */
@Entity
public class UserInfo {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String userName;
    private String password;
    private String identityFilePath;
    private String identityFilePassphrase;
    private boolean executeAsUser;
    
    /**
     * Empty
     */
    public UserInfo() {
        
    }
    /**
     * @param userName - user name
     * @param userPassword - user password
     * @param identityFilePath - user's identity (private key) file location on the execution host
     * @param identityFilePassphrase - user's identity (private key) file pass phrase
     * @param executeAsUser - should the execution be performed as the specified user
     */
    public UserInfo(String userName, String userPassword, String identityFilePath, String identityFilePassphrase, boolean executeAsUser) {
        super();
        this.userName = userName;
        this.password = userPassword;
        this.identityFilePath = identityFilePath;
        this.identityFilePassphrase = identityFilePassphrase;
        this.executeAsUser = executeAsUser;
    }

    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String userPassword) {
        this.password = userPassword;
    }
    
    public String getIdentityFilePassphrase() {
        return identityFilePassphrase;
    }
    
    public void setIdentityFilePassphrase(String identityFilePassphrase) {
        this.identityFilePassphrase = identityFilePassphrase;
    }
    
    public String getIdentityFilePath() {
        return identityFilePath;
    }
    
    public void setIdentityFilePath(String identityFilePath) {
        this.identityFilePath = identityFilePath;
    }

    public boolean isExecuteAsUser() {
        return executeAsUser;
    }

    public void setExecuteAsUser(boolean executeAsUser) {
        this.executeAsUser = executeAsUser;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
