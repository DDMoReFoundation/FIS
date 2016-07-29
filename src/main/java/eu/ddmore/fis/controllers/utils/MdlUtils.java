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
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.util.Collection;


/**
 * Provides a set of utility methods specific to FIS that encapsulate MDL document parsing.
 */
public interface MdlUtils {
    /**
     * Retrieves a list of data files referenced by given MDL file.
     * @param mdlFile the MDL File
     * @return a collection of referenced data files
     */
    public Collection<File> getDataFileFromMDL(File mdlFile);
}
