/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.miflocal.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.ddmore.miflocal.domain.LocalJob;
import eu.ddmore.miflocal.domain.LocalJobStatus;

@Repository
public interface LocalJobRepository extends CrudRepository<LocalJob, String>{
    /**
     * Finds job for given status
     * @param status
     * @return jobs with a given status
     */
    List<LocalJob> findByStatus(LocalJobStatus status);
}
