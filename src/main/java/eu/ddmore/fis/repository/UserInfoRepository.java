/*******************************************************************************
 * Copyright (C) 2016 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.ddmore.fis.domain.UserInfo;

@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long>{
}
