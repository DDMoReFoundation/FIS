package eu.ddmore.fis.controllers.utils
/*******************************************************************************
 * Copyright (C) 2015 Mango Solutions Ltd - All rights reserved.
 ******************************************************************************/
import java.io.File;
import java.util.Collection;

import org.apache.log4j.Logger
import eu.ddmore.converter.mdlprinting.MdlPrinter;
import eu.ddmore.fis.controllers.utils.MdlUtils;

import org.ddmore.mdl.mdl.DataObjectBlock
import org.ddmore.mdl.mdl.Mcl
import org.ddmore.mdl.mdl.MclObject
import org.ddmore.mdl.mdl.PropertyDeclaration
import com.google.inject.Injector
import org.ddmore.mdl.MdlStandaloneSetup
import org.ddmore.mdl.mdl.Mcl
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.Resource.Diagnostic
import org.eclipse.xtext.parser.ParseException
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.ddmore.mdl.mdl.Mcl;


/**
 * FIXME This implementation is based on code existing in mdl_json converter, this duplication could be avoided if mdl_json
 * is split into two modules - mdl-utils and mdl_json.
 */
public class MdlUtilsLocal implements MdlUtils {
    private static final Logger LOG = Logger.getLogger(MdlUtilsLocal.class);
    
    @Override
    public Collection<File> getDataFileFromMDL(File file) {
        File location = file.getParentFile()
        Mcl mcl = parseMdl(file)
        Collection<File> result = []
        final MdlPrinter mdlPrinter = MdlPrinter.getInstance();
        for (MclObject mclObj : mcl.getObjects()) {
            if(mclObj.getDataObject()) {
                LOG.debug("Data object found in MCL file ${file}.")
                for (DataObjectBlock block : mclObj.getDataObject().getBlocks()) {
                    if (block.getSourceBlock()) {
                        LOG.debug("Source block found in MCL file ${file}.")
                        result.addAll(block.getSourceBlock().getStatements().find { it.getPropertyName().getName() == 'file' }.collect{ PropertyDeclaration pd ->
                                def expr = pd.getExpression()
                                LOG.debug("File reference found ${mdlPrinter.toStr(expr)} in source block.")
                                return new File(location, mdlPrinter.toStr(expr)).getAbsoluteFile();
                            })
                    }
                }
            }
        }
        if(result.size()==0) {
            LOG.info("${file} doesn't reference any data files.");
        }
        return result;
    }
    
    /**
     * Parses given MDL file
     * @param mdlFile the MDL file to parse
     * @return the parsed MDL object
     * @throws ParseException if there were errors when parsing the file
     */
    private Mcl parseMdl(File mdlFile) {
            final Injector injector = new MdlStandaloneSetup().createInjectorAndDoEMFRegistration();
            final XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
            resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
            final Resource resource = resourceSet.getResource(URI.createURI("file:///" + mdlFile.getAbsolutePath()), true)
            EList<Diagnostic> errors = resource.getErrors()
            EList<Diagnostic> warnings = resource.getWarnings()
            if (errors) {
                LOG.error(errors.size() + " errors encountered in parsing MDL file " + mdlFile.getAbsolutePath())
                for (Diagnostic e : errors) {
                    LOG.error(e)
                }
                throw new ParseException("Unable to parse MDL file " + mdlFile.getAbsolutePath() + "; " + errors.size() + " error(s) encountered; see the log output.")
            }
            if (warnings) {
                LOG.error(warnings.size() + " warning(s) encountered in parsing MDL file " + mdlFile.getAbsolutePath())
                for (Diagnostic w : warnings) {
                    LOG.error(w)
                }
            }
            if (resource.getContents().isEmpty()) {
                throw new ParseException("There were no parsed MCL objects from ${mdlFile}")
            }
            return (Mcl) resource.getContents().get(0);
    }
    
}
