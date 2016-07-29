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
package eu.ddmore.fis.controllers.utils

import org.apache.log4j.Logger
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.URIConverter
import org.eclipse.emf.ecore.resource.Resource.Diagnostic
import org.eclipse.xtext.parser.ParseException
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet

import com.google.inject.Injector

import eu.ddmore.mdl.MdlStandaloneSetup
import eu.ddmore.mdl.mdl.ListDefinition
import eu.ddmore.mdl.mdl.Mcl
import eu.ddmore.mdl.mdl.MclObject
import eu.ddmore.mdl.mdl.ValuePair
import eu.ddmore.mdl.provider.MogDefinitionProvider
import eu.ddmore.mdl.scoping.MdlImportURIGlobalScopeProvider
import eu.ddmore.mdl.utils.ExpressionConverter
import eu.ddmore.mdl.validation.MdlValidator

/**
 * FIXME This implementation is based on code existing in mdl_json converter, this duplication could be avoided if mdl_json
 * is split into two modules - mdl-utils and mdl_json.
 */
public class MdlUtilsLocal implements MdlUtils {
    private static final Logger LOG = Logger.getLogger(MdlUtilsLocal.class)
    
    private static MogDefinitionProvider MOG_DEF_PROVIDER = new MogDefinitionProvider();
    private static eu.ddmore.mdl.utils.MdlUtils MDL_UTILS = new eu.ddmore.mdl.utils.MdlUtils();
    private static ExpressionConverter EXPRESSION_CONVERTER = new ExpressionConverter()
    
    @Override
    public Collection<File> getDataFileFromMDL(final File mdlFile) {
        
        final Collection<File> result = []

        final Mcl mcl = parseMdl(mdlFile)
        final MclObject dataObject = MOG_DEF_PROVIDER.getFirstObjectOfType(mcl, MdlValidator.DATAOBJ)
            LOG.debug("Data object retrieved ${dataObject}")
        if (dataObject && MDL_UTILS.isDataObject(dataObject)) {
            LOG.debug("Data object found in MCL file ${mdlFile}, contents ${dataObject}")
            final ListDefinition dataSourceListDefn = MDL_UTILS.getDataSourceStmt(dataObject)
            if (dataSourceListDefn) {
                LOG.debug("SOURCE block found in MCL file ${mdlFile}")
                final ValuePair dataFileAttr = dataSourceListDefn.getList().getAttributes().find { "file".equals(it.getArgumentName()) }
                if (dataFileAttr) {
                    final String dataFileName = ExpressionConverter.convertToString(dataFileAttr.getExpression())
                    LOG.info("Found data file referenced in MCL file ${mdlFile}: " + dataFileName)
                    result.add(new File(mdlFile.getParentFile(), dataFileName).getAbsoluteFile())
                }
            }
        } else {
            LOG.debug("Data object not found in MCL file ${mdlFile}")
        }
        
        if (result.isEmpty()) {
            LOG.warn("${mdlFile} doesn't reference any data files.")
        }
        return result
    }
    
    /**
     * Parses given MDL file.
     * @param mdlFile the MDL file to parse
     * @return the parsed MDL object
     * @throws ParseException if there were errors when parsing the file
     */
    private Mcl parseMdl(final File mdlFile) {
            new eu.ddmore.mdllib.MdlLibStandaloneSetup().createInjectorAndDoEMFRegistration();
            final Injector injector = new MdlStandaloneSetup().createInjectorAndDoEMFRegistration();
            final XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
            resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
            registerURIMappingsForImplicitImports(resourceSet);

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
    
    private static void registerURIMappingsForImplicitImports(XtextResourceSet resourceSet) {
        URIConverter uriConverter = resourceSet.getURIConverter();
        Map<URI, URI> uriMap = uriConverter.getURIMap();
        registerPlatformToFileURIMapping(MdlImportURIGlobalScopeProvider.HEADER_URI, uriMap);
    }
 
    private static void registerPlatformToFileURIMapping(URI uri, Map<URI, URI> uriMap) {
        final URI newURI = createClasspathURIForHeaderFile(uri);
        uriMap.put(uri, newURI);
    }
    
    private static URI createClasspathURIForHeaderFile(URI uri) {
        String path = uri.path().replace("/plugin/", ""); // Eclipse RCP platform URL to a plugin resource starts with "/plugin/" so strip this off
        path = path.substring(path.indexOf("/")); // This skips past the plugin name, i.e. eu.ddmore.mdl.definitions/
        // Now we're just left with the path to the resource within the plugin; the built plugin JAR is available on the classpath, so create a classpath URI pointing at this resource
        return URI.createURI("classpath:" + path);
    }
}
