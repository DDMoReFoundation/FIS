import groovy.lang.Binding;

import java.io.File;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils


/**
 * Wrapper for the conversion of MDL direct to NMTRAN.
 */
final class MdlToNMTRAN extends AbstractConverterWrapper {

    MdlToNMTRAN(final Binding binding) {
        super(binding, "MDL", "5.0.8", "NMTRAN", "7.2.0", "ctl") // TODO: Remove the hard-coding of these source and target converter versions
    }

}
