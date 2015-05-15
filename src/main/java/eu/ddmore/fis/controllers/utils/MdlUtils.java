/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.controllers.utils;

import java.io.File;
import java.util.Collection;


/**
 * Provides a set of utility methods specific to FIS that encapsulate MDL document parsing.
 */
public interface MdlUtils {
    /**
     * Retrieves a list of data files referenced by given MDL file.
     * @param file the MDL File
     * @return a collection of referenced data files
     */
    public Collection<File> getDataFileFromMDL(File file);
}
