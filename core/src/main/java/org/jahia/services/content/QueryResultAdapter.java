/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an adapter to support Jackrabbit's query result functionality.
 *
 * @author toto
 */
public class QueryResultAdapter implements QueryResult {

    private List<JCRWorkspaceWrapper.QueryResultWrapper> queryResults;
    private RowIteratorAdapter rowIterator;
    private NodeIteratorAdapter nodeIterator;

    /**
     * Wrapped query results that comes from different store
     *
     * @param queryResults
     */
    public QueryResultAdapter(List<JCRWorkspaceWrapper.QueryResultWrapper> queryResults) {
        this.queryResults = queryResults;
    }

    public QueryResultAdapter() {
        queryResults = new ArrayList<JCRWorkspaceWrapper.QueryResultWrapper>();
    }

    public String[] getColumnNames() throws RepositoryException {
        return new String[0];
    }

    public RowIterator getRows() throws RepositoryException {
        if (this.rowIterator == null) {
            List<Row> rows = new ArrayList<Row>();
            for (final JCRWorkspaceWrapper.QueryResultWrapper queryResult : queryResults) {
                RowIterator rowIterator = queryResult.getRows();
                while (rowIterator.hasNext()) {
                    final Row row = rowIterator.nextRow();
                    rows.add(new Row() {
                        public Value getValue(String s) throws ItemNotFoundException, RepositoryException {
                            if (s.equals(JcrConstants.JCR_PATH)) {
                                return new StringValue(getPath());
                            }
                            return row.getValue(s);
                        }

                        public Value[] getValues() throws RepositoryException {
                            return row.getValues();
                        }

                        public Node getNode() throws RepositoryException {
                            return queryResult.getProvider().getNodeWrapper(row.getNode(), queryResult.getSession());
                        }

                        public Node getNode(String selectorName) throws RepositoryException {
                            return queryResult.getProvider().getNodeWrapper(row.getNode(selectorName), queryResult.getSession());
                        }

                        private String getDerivedPath(String originalPath) throws RepositoryException {
                            originalPath = originalPath.replaceFirst(queryResult.getProvider().getRelativeRoot(),"");
                            String mountPoint = queryResult.getProvider().getMountPoint();
                            return mountPoint.equals("/") ? originalPath : mountPoint + originalPath;
                        }

                        public String getPath() throws RepositoryException {
                            return getDerivedPath(row.getPath());
                        }

                        public String getPath(String selectorName) throws RepositoryException {
                            return getDerivedPath(row.getPath(selectorName));
                        }

                        public double getScore() throws RepositoryException {
                            return row.getScore();
                        }

                        public double getScore(String selectorName) throws RepositoryException {
                            return row.getScore(selectorName);
                        }
                    });
                }
            }
            this.rowIterator = new RowIteratorAdapter(rows);
        }
        return this.rowIterator;
    }

    public NodeIterator getNodes() throws RepositoryException {
        if (this.nodeIterator == null) {
            List<Node> nodes = new ArrayList<Node>();
            for (QueryResult queryResult : queryResults) {
                NodeIterator nodeIterator = queryResult.getNodes();
                while (nodeIterator.hasNext()) {
                    nodes.add(nodeIterator.nextNode());
                }
            }
            this.nodeIterator = new NodeIteratorAdapter(nodes);
        }
        return this.nodeIterator;
    }

    public void addResults(List<JCRWorkspaceWrapper.QueryResultWrapper> queryResults) {
        this.queryResults.addAll(queryResults);
    }

    public void addResult(JCRWorkspaceWrapper.QueryResultWrapper result) {
        queryResults.add(result);
    }

    public String[] getSelectorNames() throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }
}
