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
package org.jahia.ajax.gwt.helper;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.utils.xml.JahiaTransformerFactory;
import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.XslFilter;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * HTML compare utility.
 * User: ktlili
 * Date: Mar 4, 2010
 * Time: 3:29:13 PM
 */
public class DiffHelper {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(DiffHelper.class);

    private static final Pattern LIVE_WS_PATTERN = Pattern.compile("/"+ Constants.LIVE_WORKSPACE+"/");

    public String getHighlighted(String original, String amendment) {
        final StringWriter sw = new StringWriter();

        try {

            final SAXTransformerFactory transformerFactory = (SAXTransformerFactory) JahiaTransformerFactory.newInstance();
            final TransformerHandler result = transformerFactory.newTransformerHandler();
            result.setResult(new StreamResult(sw));

            final XslFilter filter = new XslFilter();

            // replace /live/ by /default/ in href and src attributes as it represents same image
            if(original.contains("/files/"+Constants.EDIT_WORKSPACE+"/")||amendment.contains("/files/"+Constants.EDIT_WORKSPACE+"/")) {
                original = LIVE_WS_PATTERN.matcher(original).replaceAll("/"+Constants.EDIT_WORKSPACE+"/");
                amendment = LIVE_WS_PATTERN.matcher(amendment).replaceAll("/"+Constants.EDIT_WORKSPACE+"/");
            }
            original = new SourceFormatter(new Source(original)).toString();
            amendment = new SourceFormatter(new Source(amendment)).toString();
            final ContentHandler postProcess = filter.xsl(result, "jahiahtmlheader.xsl");

            final Locale locale = Locale.ENGLISH;
            final String prefix = "diff";

            final HtmlCleaner cleaner = new HtmlCleaner();

            final InputSource oldSource = new InputSource(new StringReader(original));
            final InputSource newSource = new InputSource(new StringReader(amendment));

            final DomTreeBuilder oldHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(oldSource, oldHandler);

            final TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

            final DomTreeBuilder newHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(newSource, newHandler);

            final TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

            postProcess.startDocument();
            postProcess.startElement("", "diffreport", "diffreport",new AttributesImpl());
            addDiffCss(postProcess);
            postProcess.startElement("", "diff", "diff", new AttributesImpl());

            final HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess,prefix);

            final HTMLDiffer differ = new HTMLDiffer(output);
            differ.diff(rightComparator, leftComparator);

            postProcess.endElement("", "diff", "diff");
            postProcess.endElement("", "diffreport", "diffreport");
            postProcess.endDocument();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return sw.toString();
    }

    /**
     * add css
     * @param handler
     * @throws org.xml.sax.SAXException
     */
    private void addDiffCss(ContentHandler handler) throws SAXException {
        handler.startElement("", "css", "css",new AttributesImpl());
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "href", "href", "CDATA", Jahia.getContextPath()+"/gwt/resources/css/diff.css");
        attr.addAttribute("", "type", "type", "CDATA", "text/css");
        attr.addAttribute("", "rel", "rel", "CDATA", "stylesheet");
        handler.startElement("", "link", "link",attr);
        handler.endElement("", "link", "link");
        handler.endElement("", "css", "css");
    }

}
