/*******************************************************************************
 * Copyright (C) 2002 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.ddmore.fis.domain.LocalJob;
import eu.ddmore.fis.domain.LocalJobStatus;

@Repository
public interface LocalJobRepository extends CrudRepository<LocalJob, String>{
    /**
     * Finds job for given status
     * @param status
     * @return jobs with a given status
     */
    List<LocalJob> findByStatus(LocalJobStatus status);
}
