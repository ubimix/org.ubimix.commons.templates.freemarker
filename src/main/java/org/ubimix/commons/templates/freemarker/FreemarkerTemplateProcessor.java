/*******************************************************************************
 * Copyright (c) 2005,2006 Cognium Systems SA and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Cognium Systems SA - initial API and implementation
 *******************************************************************************/
package org.ubimix.commons.templates.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ubimix.commons.templates.ITemplateProcessor;
import org.ubimix.commons.templates.ITemplateProvider;
import org.ubimix.commons.templates.TemplateException;
import org.ubimix.commons.templates.TemplateProcessor;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Freemarker-based (see http://freemarker.sourceforge.net/) implementation of
 * the {@link ITemplateProcessor} interface.
 *
 * @author kotelnikov
 */
public class FreemarkerTemplateProcessor extends TemplateProcessor {

    static class ResourceBasedTemplateLoader implements TemplateLoader {

        protected Properties fProperties;

        protected ITemplateProvider fTemplateProvider;

        /**
         * @param templateProvider
         * @param properties
         */
        public ResourceBasedTemplateLoader(
            ITemplateProvider templateProvider,
            Properties properties) {
            fTemplateProvider = templateProvider;
            fProperties = properties;
        }

        /**
         * @see freemarker.cache.TemplateLoader#closeTemplateSource(java.lang.Object)
         */
        public void closeTemplateSource(Object templateSource)
            throws IOException {
            //
        }

        /**
         * @see freemarker.cache.TemplateLoader#findTemplateSource(java.lang.String)
         */
        public Object findTemplateSource(String name) throws IOException {
            return fTemplateProvider.templateExists(name) ? name : null;
        }

        /**
         * @see freemarker.cache.TemplateLoader#getLastModified(java.lang.Object)
         */
        public long getLastModified(Object templateSource) {
            try {
                return fTemplateProvider
                    .getLastModified((String) templateSource);
            } catch (IOException e) {
                return -1;
            }
        }

        /**
         * @see freemarker.cache.TemplateLoader#getReader(java.lang.Object,
         *      java.lang.String)
         */
        public Reader getReader(Object templateSource, String encoding)
            throws IOException {
            InputStream input = fTemplateProvider
                .getTemplate((String) templateSource);
            if (input == null)
                return null;
            InputStreamReader reader = new InputStreamReader(input);
            return reader;
        }

    }

    private static Logger PROCESSOR_LOGGER = Logger
        .getLogger(ResourceBasedTemplateLoader.class.getName());

    protected Configuration fConfiguration = new Configuration();

    /**
     * @param properties
     * @param templateProvider
     * @throws IOException
     */
    public FreemarkerTemplateProcessor(
        ITemplateProvider templateProvider,
        Properties properties) {
        super(templateProvider, properties);
        fConfiguration.setTemplateLoader(new ResourceBasedTemplateLoader(
            fTemplateProvider,
            fProperties));
        fConfiguration.setObjectWrapper(new DefaultObjectWrapper());
        fConfiguration.setDefaultEncoding("UTF-8");
        fConfiguration.setTemplateUpdateDelay(10);
        try {
            freemarker.log.Logger
                .selectLoggerLibrary(freemarker.log.Logger.LIBRARY_LOG4J);
        } catch (ClassNotFoundException e) {
            PROCESSOR_LOGGER.log(
                Level.SEVERE,
                "Can not load log4j logger for FreeMarker.",
                e);
        }

    }

    /**
     * @see com.cogniumsystems.commons.templates.ITemplateProcessor#close()
     */
    public void close() {
        //
    }

    /**
     * @see com.cogniumsystems.commons.templates.ITemplateProcessor#render(java.lang.String,
     *      java.util.Map, java.io.Writer)
     */
    public void render(
        String templateName,
        Map<String, Object> params,
        Writer writer) throws TemplateException {
        try {
            Template temp = fConfiguration.getTemplate(templateName);
            temp.process(params, writer);
            writer.flush();
        } catch (IOException e) {
            throw new TemplateException(e);
        } catch (freemarker.template.TemplateException e) {
            throw new TemplateException(e);
        }

    }

}
