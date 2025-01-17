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
package org.jahia.services.history;

import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.version.InternalVersionHistory;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.InternalXAVersionManager;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version history utility class for purging all version entries of the specified node.
 *
 * @author Sergiy Shyrkov
 */
public final class NodeVersionHistoryHelper {

    private static boolean checkingOrphans;

    private static boolean checkingUnused;

    private static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryHelper.class);

    private static OrphanedVersionHistoryChecker orphanedChecker;

    protected static final int PURGE_HISTORY_CHUNK = Integer.getInteger(
            "org.jahia.services.history.purgeVersionHistoryBatchSize", 100);

    private static final String DATA_NODE_NAME = "unusedVersionChecker";

    private static final String LAST_CHECKED_ID_PROPERTY = "lastCheckedNodeId";

    private static UnusedVersionChecker unusedChecker;

    /**
     * Triggers the process of orphaned version histories check. If the <code>deleteOrphans</code> is set to <code>true</code> also performs
     * the purge of found orphaned version histories.
     *
     * This method ensures that only one check process runs at a time.
     *
     * @param maxOrphans
     *            the maximum number of orphaned histories found at which the process is stopped
     * @param deleteOrphans
     *            if set to <code>true</code> performs the purge of found orphaned version histories; in case of <code>false</code> only the
     *            found orphaned version history count is reported, but no removal is done
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized OrphanedVersionHistoryCheckStatus checkOrphaned(final long maxOrphans,
            final boolean deleteOrphans, final Writer statusOut) throws RepositoryException {
        if (checkingOrphans) {
            throw new IllegalStateException("The version history is currently beeing checked for orphans."
                    + " Cannot start the second process.");
        }
        checkingOrphans = true;
        long timer = System.currentTimeMillis();
        final OrphanedVersionHistoryCheckStatus status = new OrphanedVersionHistoryCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} orphaned version history", deleteOrphans ? "deleting" : "checking");

        orphanedChecker = new OrphanedVersionHistoryChecker(status, maxOrphans, deleteOrphans, out);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    orphanedChecker.perform(session);
                    return null;
                }
            });
        } finally {
            checkingOrphans = false;
            orphanedChecker = null;
            out.echo("Done checking orphaned version history in {} ms. Status: {}",
                    (System.currentTimeMillis() - timer), status.toString());
        }

        return status;
    }

    /**
     * Triggers the process of unused versions check. If the <code>deleteUnused</code> is set to <code>true</code> also performs the purge
     * of found unused versions.
     *
     * This method ensures that only one check process runs at a time.
     *
     * @param maxUnused
     *            the maximum number of unused versions found at which the process is stopped
     * @param deleteUnused
     *            if set to <code>true</code> performs the purge of found unused versions; in case of <code>false</code> only the found
     *            unused versions count is reported, but no removal is done
     * @param purgeOlderThanTimestamp
     *            if positive value is provided checks that the unused versions are older than the specified date (timestamp in milliseconds
     *            UTC); if <code>0</code> value is provided all found unused versions are considered in the process
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized UnusedVersionCheckStatus checkUnused(final long maxUnused, final boolean deleteUnused,
            final long purgeOlderThanTimestamp, final Writer statusOut) throws RepositoryException {
        if (checkingUnused) {
            throw new IllegalStateException("Unused versions are currently beeing checked."
                    + " Cannot start the second process.");
        }
        checkingUnused = true;
        long timer = System.currentTimeMillis();
        final UnusedVersionCheckStatus status = new UnusedVersionCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} unused versions{}", deleteUnused ? "deleting" : "checking",
                purgeOlderThanTimestamp <= 0 ? "" : (" older than " + new Date(purgeOlderThanTimestamp)));

        unusedChecker = new UnusedVersionChecker(status, maxUnused, deleteUnused, out);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    JCRNodeWrapper root = session.getRootNode();

                    NodeId lastCheckedNodeId = null;
                    if (root.hasNode(DATA_NODE_NAME)) {
                        JCRNodeWrapper data = root.getNode(DATA_NODE_NAME);
                        String lastCheckedNodeUuid = data.getPropertyAsString(LAST_CHECKED_ID_PROPERTY);
                        lastCheckedNodeId = (lastCheckedNodeUuid == null ? null : NodeId.valueOf(lastCheckedNodeUuid));
                    }

                    lastCheckedNodeId = unusedChecker.perform(session, purgeOlderThanTimestamp, lastCheckedNodeId);

                    JCRNodeWrapper data;
                    if (root.hasNode(DATA_NODE_NAME)) {
                        data = root.getNode(DATA_NODE_NAME);
                    } else {
                        data = root.addNode(DATA_NODE_NAME, Constants.NT_UNSTRUCTURED);
                    }
                    data.setProperty(LAST_CHECKED_ID_PROPERTY, (lastCheckedNodeId == null ? null : lastCheckedNodeId.toString()));
                    session.save();

                    return null;
                }
            });
        } finally {
            checkingUnused = false;
            unusedChecker = null;
            out.echo("Done checking unused versions in {} ms. Status: {}", (System.currentTimeMillis() - timer),
                    status.toString());
        }

        return status;
    }

    /**
     * Forces stop of the orphaned version history check process if it is currently running.
     */
    public static void forceStopOrphanedCheck() {
        if (orphanedChecker != null) {
            orphanedChecker.stop();
        }
    }

    /**
     * Forces stop of the unused versions check process if it is currently running.
     */
    public static void forceStopUnusedCheck() {
        if (unusedChecker != null) {
            unusedChecker.stop();
        }
    }

    static void internalPurgeVersionHistories(List<InternalVersionHistory> histories, JCRSessionWrapper session,
            VersionHistoryCheckStatus status) throws VersionException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        int[] result = null;
        if (vm instanceof InternalVersionManagerImpl) {
            result = ((InternalVersionManagerImpl) vm).purgeVersions(providerSession, histories);
        } else if (vm instanceof InternalXAVersionManager) {
            result = ((InternalXAVersionManager) vm).purgeVersions(providerSession, histories);
        } else {
            logger.warn("Unknown implemmentation of the InternalVersionManager: {}.", vm.getClass().getName());
        }

        if (result != null) {
            if (!(status instanceof OrphanedVersionHistoryCheckStatus)) {
                status.checked += histories.size();
            }
            status.deleted += result[0];
            status.deletedVersionItems += result[1];
        }
    }

    /**
     * Returns <code>true</code> if the process for checking orphans is currently running.
     *
     * @return <code>true</code> if the process for checking orphans is currently running; <code>false</code> otherwise
     */
    public static boolean isCheckingOrphans() {
        return checkingOrphans;
    }

    /**
     * Returns <code>true</code> if the process for checking unused versions is currently running.
     *
     * @return <code>true</code> if the process for checking unused versions is currently running; <code>false</code> otherwise
     */
    public static boolean isCheckingUnused() {
        return checkingUnused;
    }

    static void purgeUnusedVersions(List<NodeId> unusedVersions, JCRSessionWrapper session,
            UnusedVersionCheckStatus status) throws PathNotFoundException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        int result = 0;
        if (vm instanceof InternalVersionManagerImpl) {
            result = ((InternalVersionManagerImpl) vm).purgeUnusedVersions(providerSession, unusedVersions);
        } else if (vm instanceof InternalXAVersionManager) {
            result = ((InternalXAVersionManager) vm).purgeUnusedVersions(providerSession, unusedVersions);
        } else {
            logger.warn("Unknown implemmentation of the InternalVersionManager: {}.", vm.getClass().getName());
        }

        status.deletedVersionItems += result;
    }

    static void purgeVersionHistories(List<NodeId> historyIds, JCRSessionWrapper session,
            VersionHistoryCheckStatus status) throws VersionException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        List<InternalVersionHistory> histories = new LinkedList<InternalVersionHistory>();
        for (NodeId id : historyIds) {
            try {
                histories.add(vm.getVersionHistory(id));
            } catch (ItemNotFoundException e) {
                // no history found
            }
        }

        internalPurgeVersionHistories(histories, session, status);
    }

    private static void purgeVersionHistoryChunk(OutWrapper out, Set<String> ids, VersionHistoryCheckStatus status) {
        VersionHistoryCheckStatus result = purgeVersionHistoryForNodes(ids, out);
        ids.clear();
        status.checked += result.checked;
        status.deleted += result.deleted;
        status.deletedVersionItems += result.deletedVersionItems;
        out.echo(status.toString());
    }

    /**
     * Performs the removal of unused versions for the specified nodes. All unused versions are removed, no mater the "age" of the version.
     *
     * @param nodes
     *            an instance of {@link NodeIterator} for processing nodes
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(NodeIterator nodes, Writer statusOut)
            throws RepositoryException {
        long total = nodes.getSize();
        OutWrapper out = new OutWrapper(logger, statusOut);
        if (total > 0) {
            out.echo("Start checking version history for {} nodes", total);
        }
        Set<String> ids = new HashSet<String>();
        VersionHistoryCheckStatus status = null;
        if (nodes.getSize() <= PURGE_HISTORY_CHUNK) {
            for (; nodes.hasNext();) {
                ids.add(nodes.nextNode().getIdentifier());
            }
            status = purgeVersionHistoryForNodes(ids, out);
            out.echo(status.toString());
        } else {
            status = new VersionHistoryCheckStatus();
            for (; nodes.hasNext();) {
                ids.add(nodes.nextNode().getIdentifier());
                if (ids.size() >= PURGE_HISTORY_CHUNK) {
                    purgeVersionHistoryChunk(out, ids, status);
                }
            }
            if (ids.size() > 0) {
                // purge the rest
                purgeVersionHistoryChunk(out, ids, status);
            }
        }

        return status;
    }

    /**
     * Performs the removal of unused versions for the specified nodes. All unused versions are removed, no mater the "age" of the version.
     *
     * @param nodeIdentifiers
     *            a set of node IDs to process
     * @return the status object to indicate the result of the check
     */
    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers) {
        return purgeVersionHistoryForNodes(nodeIdentifiers, (Writer) null);
    }

    static void purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers, JCRSessionWrapper session,
            VersionHistoryCheckStatus status) throws VersionException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        List<InternalVersionHistory> histories = new LinkedList<InternalVersionHistory>();
        for (String id : nodeIdentifiers) {
            try {
                histories.add(vm.getVersionHistoryOfNode(NodeId.valueOf(id)));
            } catch (ItemNotFoundException e) {
                // no history found
            }
        }

        internalPurgeVersionHistories(histories, session, status);
    }

    private static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers,
            OutWrapper out) {
        final VersionHistoryCheckStatus status = new VersionHistoryCheckStatus();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {

                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    purgeVersionHistoryForNodes(nodeIdentifiers, session, status);
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return status;
    }

    /**
     * Performs the removal of unused versions for the specified nodes. All unused versions are removed, no mater the "age" of the version.
     *
     * @param nodeIdentifiers
     *            a set of node IDs to process
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     */
    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers,
            Writer statusOut) {
        final OutWrapper out = new OutWrapper(logger, statusOut);
        out.echo("Start checking version history for {} nodes", nodeIdentifiers.size());

        final VersionHistoryCheckStatus status = purgeVersionHistoryForNodes(nodeIdentifiers, out);

        out.echo("Done checking version history for nodes. Version history status: {}", status.toString());

        return status;
    }

    private NodeVersionHistoryHelper() {
        super();
    }
}
