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
package eu.ddmore.fis.controllers;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.ddmore.fis.domain.LocalJob;

/**
 * Validator responsible for ensuring that just valid LocalJob entities are handled by {@link JobsController}
 */
public class SubmittedLocalJobValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return LocalJob.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		LocalJob job = (LocalJob)target;
		if(job.getStatus()!=null) {
			errors.rejectValue("status", "job.status.empty", "Status of the job should not be set.");
		}
		if(!StringUtils.isBlank(job.getId())) {
			errors.rejectValue("id", "job.id.empty", "Job id must not be set.");
		}
		if(job.getVersion()!=0) {
			errors.rejectValue("version", "job.version.empty", "Job version must not be set.");
		}

		if(!StringUtils.isBlank(job.getSubmitTime())) {
			errors.rejectValue("submitTime", "job.submitTime.empty", "Job submission time should not be set.");
		}
		}

}
