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
package eu.ddmore.fis.service;

import eu.ddmore.fis.domain.LocalJob;

/**
 * Component responsible for dispatching job to appropriate execution environment
 */
public interface JobDispatcher {
    /**
     * dispatches a job to a given execution environment (TES)
     * @param localJob a job that should be executed
     * @return a copy of the passed parameter with updated state if needed
     */
    LocalJob dispatch(LocalJob localJob);
}
