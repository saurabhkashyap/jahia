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
package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Choice list initializer that uses values of the specified node property (multiple).
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6.1.0
 */
public class PropertyValuesChoiceListInitializer implements ChoiceListInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PropertyValuesChoiceListInitializer.class);

    private static final String CONTEXT_NODE = "contextNode";

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values,
            Locale locale, Map<String, Object> context) {
        if (param == null || !param.contains(";")) {
            throw new IllegalArgumentException(
                    "Parameter format is wrong. Expecting 'targetNode;targetProperty' or 'targetNode;targetProperty;valueType'");
        }

        List<ChoiceListValue> choices = null;

        if (context != null) {
            JCRNodeWrapper contextNode = context.containsKey(CONTEXT_NODE) && context.get(CONTEXT_NODE) != null ?
                    (JCRNodeWrapper) context.get(CONTEXT_NODE) :
                    (JCRNodeWrapper) context.get("contextParent");
            try {
                choices = contextNode != null ? getChoices(contextNode, param) : new LinkedList<>();
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return choices;
    }

    private List<ChoiceListValue> getChoices(JCRNodeWrapper contextNode, String param) throws RepositoryException {
        Value[] values = null;

        String targetNode = StringUtils.substringBefore(param, ";").trim();
        String targetProperty = StringUtils.substringAfter(param, ";").trim();
        String valueType = null;
        if (targetProperty.contains(";")) {
            valueType = StringUtils.substringAfter(targetProperty, ";").trim();
            targetProperty = StringUtils.substringBefore(targetProperty, ";").trim();
        }

        JCRNodeWrapper target = null;
        if ("this".equals(targetNode)) {
            target = contextNode;
        } else {
            try {
                target = contextNode.getSession().getNode(targetNode);
            } catch (PathNotFoundException e) {
                logger.warn("Node {} cannot be found. The choice list for the {} will be empty", targetNode, contextNode.getPath());
            }
        }

        JCRPropertyWrapper prop = null;
        if (target != null && target.hasProperty(targetProperty)) {
            prop = target.getProperty(targetProperty);
            values = prop.isMultiple() ? prop.getValues() : new Value[] { prop.getValue() };
        }

        List<ChoiceListValue> choices = null;

        if (values != null && values.length > 0) {
            choices = new LinkedList<>();
            boolean isReference = prop.getType() == PropertyType.REFERENCE || prop.getType() == PropertyType.WEAKREFERENCE;
            for (Value val : values) {
                if (isReference) {
                    JCRNodeWrapper referencedNode = ((JCRValueWrapper) val).getNode();
                    if (referencedNode != null) {
                        String listValue = null;
                        if (valueType != null) {
                            if ("name".equalsIgnoreCase(valueType)) {
                                listValue = referencedNode.getName();
                            } else if ("path".equalsIgnoreCase(valueType)) {
                                listValue = referencedNode.getPath();
                            }
                        }
                        listValue = listValue == null ? referencedNode.getIdentifier() : listValue;

                        choices.add(new ChoiceListValue(referencedNode.getDisplayableName(), listValue));
                    }
                } else {
                    String listValue = val.getString();
                    choices.add(new ChoiceListValue(listValue, listValue));
                }
            }
        }

        return choices;
    }
}
