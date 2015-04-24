/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
package eu.ddmore.fis.service.cts;

import java.io.File;
import java.util.List;


/**
 * Temporary placeholder, to be replaced by eu.ddmore.Archive
 */
public interface Archive {
    
    interface Entry {
        String getFilePath();
    }
    
    List<Entry> getMainEntries();
    
    File getArchiveFile();
}
