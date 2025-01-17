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
package org.jahia.taglibs.query;

import javax.jcr.query.qom.Constraint;
import javax.servlet.jsp.JspException;

/**
 * Base query constraint tag class.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:36:14
 */
public abstract class ConstraintTag extends QOMBuildingTag {

    private static final long serialVersionUID = 4590901723948727733L;

    @Override
    public final int doEndTag() throws JspException {
        try {
            CompoundConstraintTag compoundConstraintTag = (CompoundConstraintTag) findAncestorWithClass(this, CompoundConstraintTag.class);
            if (compoundConstraintTag != null) {
                // this constraint is nested within a compound constraint tag (AND or OR) 
                compoundConstraintTag.addConstraint(getConstraint());
            } else {
                getQOMBuilder().andConstraint(getConstraint());
            }
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
            resetState();
        }
        return EVAL_PAGE;
    }

    protected abstract Constraint getConstraint() throws Exception;
}
