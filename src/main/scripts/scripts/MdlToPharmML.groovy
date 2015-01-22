import groovy.lang.Binding;

import java.io.File;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils


/**
 * Wrapper for the conversion of MDL to intermediate PharmML.
 */
final class MdlToPharmML extends AbstractConverterWrapper {

    private String pharmmlFileExt

    MdlToPharmML(final Binding binding) {
        super(binding, "MDL", "6.0.7", "PharmML", "0.3.1", binding.getVariable("fis.pharmml.ext")) // TODO: Remove the hard-coding of these source and target converter versions
        this.pharmmlFileExt = binding.getVariable("fis.pharmml.ext")
    }

}
