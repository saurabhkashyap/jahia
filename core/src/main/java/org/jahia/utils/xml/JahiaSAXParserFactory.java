/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.xml;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A utility class that provides instances of SAX XML parser.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaSAXParserFactory extends BaseXMLParserFactory {

    private static JahiaSAXParserFactory instance;

    /**
     * Creates a new instance of a {@link SAXParserFactory} using the currently configured factory parameters.
     *
     * @return a new instance of an SAXParserFactory
     *
     * @throws ParserConfigurationException
     *             if a factory cannot be created which satisfies the requested configuration
     * @throws SAXException
     *             for SAX errors
     */
    public static SAXParserFactory newInstance() throws ParserConfigurationException, SAXException {
        if (instance == null) {
            throw new UnsupportedOperationException("This XML parser factory is not initialized yet");
        }
        return instance.create();
    }

    /**
     * Initializes an instance of this class.
     */
    public JahiaSAXParserFactory() {
        super();
        instance = this;
    }

    private SAXParserFactory create()
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException, SAXException {
        SAXParserFactory factory = new SAXParserFactoryImpl();

        factory.setNamespaceAware(isNamespaceAware());
        factory.setValidating(isValidating());
        factory.setXIncludeAware(isXIncludeAware());
        for (Map.Entry<String, Boolean> feature : getFeatures().entrySet()) {
            factory.setFeature(feature.getKey(), feature.getValue());
        }

        return factory;
    }
}
