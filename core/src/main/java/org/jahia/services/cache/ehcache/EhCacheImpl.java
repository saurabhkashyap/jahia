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
package org.jahia.services.cache.ehcache;

import static org.jahia.services.cache.ehcache.DependenciesCacheEvictionPolicy.ALL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheStatistics;
import org.jahia.services.cache.GroupCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Ehcache based caching implementation.
 *
 * @author Serge Huber
 */
@SuppressWarnings("rawtypes")
public class EhCacheImpl implements CacheImplementation, CacheStatistics {

    final private static Logger logger = LoggerFactory.getLogger(EhCacheImpl.class);

    private String name;
    private Cache ehCache;
    private Cache ehCacheGroups;
    private int groupsSizeLimit;

    protected EhCacheImpl(String name, CacheManager ehCacheManager, EhCacheProvider provider) {
        super();
        this.name = name;
        this.groupsSizeLimit = provider.getGroupsSizeLimit();

        if (ehCacheManager.getCache(name) == null) {
            ehCacheManager.addCache(name);
        }
        ehCache = ehCacheManager.getCache(name);

        if (groupsSizeLimit > 0) {
            if (ehCacheManager.getCache(name + "Groups") == null) {
                ehCacheManager.addCache(name + "Groups");
            }
            ehCacheGroups = ehCacheManager.getCache(name + "Groups");
        }
    }

    @Deprecated
    public boolean containsKey(Object key) {
        // we cannot use EHCache's isKeyInCache because the element might have expired, so we use
        // an actual retrieval to test for expiration.
        return get(key) != null;
    }

    public Object get(Object key) {
        Element element = ehCache.get(key);
        if (element != null) {
            return element.getObjectValue();
        } else {
            return null;
        }
    }

    public void put(Object key, String[] groups, Object value) {

        Element element = new Element(key, value);
        ehCache.put(element);

        if (key instanceof GroupCacheKey) {
            addToGroups(key);
        }
    }

    public int size() {
        return ehCache.getSize();
    }

    public long getGroupsSize() {
        return ehCacheGroups != null ? ehCacheGroups.getSize() : 0;
    }

    public long getGroupsKeysTotal() {
        if (ehCacheGroups == null) {
            return 0;
        }
        long totalSize = 0;
        Iterator groupIterator = ehCacheGroups.getKeysWithExpiryCheck().iterator();
        while (groupIterator.hasNext()) {
            Object key = groupIterator.next();
            Element keySetElement = ehCacheGroups.get(key);
            if (keySetElement == null) continue;
            Set keySet = (Set) keySetElement.getValue();
            totalSize += keySet.size();
        }
        return totalSize;
    }

    public void flushAll(boolean propagate) {
        ehCache.removeAll();
        if (ehCacheGroups != null) {
            ehCacheGroups.removeAll();
        }
    }

    public void remove(Object key) {
        doRemove(key);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void flushGroup(String groupName) {
        if (ehCacheGroups == null) {
            return;
        }

        Set keysToFlush = null;
        Element element = ehCacheGroups.get(groupName);
        if (element == null) {
            return;
        }
        keysToFlush = (Set) element.getValue();
        if (keysToFlush == null) {
            return;
        } else {
            ehCacheGroups.remove(groupName);
            // we copy in order to avoid concurrent modifications
            keysToFlush = new HashSet(keysToFlush);
        }
        flushKeys(keysToFlush);
    }

    public Set getGroupKeys(String groupName) {
        if (ehCacheGroups == null) {
            return Collections.emptySet();
        }
        Set keysToFlush = null;
        Element element = ehCacheGroups.get(groupName);
        if (element == null) {
            return null;
        }
        keysToFlush = (Set) element.getValue();
        if (keysToFlush == null) {
            return null;
        } else {
            // we copy in order to avoid concurrent modifications
            keysToFlush = new HashSet(keysToFlush);
        }
        return keysToFlush;
    }

    protected void flushKeys(Set keysToFlush) {
        if (keysToFlush.contains(ALL)) {
            flushAll(true);
            logger.warn("Due to presence of big groups we are flushing the whole cache " + ehCache.getName());
        } else {
            Iterator keyToFlushIter = keysToFlush.iterator();
            while (keyToFlushIter.hasNext()) {
                Object curKeyToFlush = keyToFlushIter.next();
                doRemove(curKeyToFlush);
            }
        }
    }

    private void addToGroups(Object key) {
        if (ehCacheGroups == null || !(key instanceof GroupCacheKey)) {
            return;
        }
        GroupCacheKey groupCacheKey = (GroupCacheKey) key;
        synchronized (groupCacheKey) {
            Iterator groupIter = groupCacheKey.getGroups().iterator();
            while (groupIter.hasNext()) {
                String curGroup = (String) groupIter.next();
                addToGroup(curGroup, key);
            }
        }
    }

    private void addToGroup(String groupName, Object key) {
        if (ehCacheGroups == null) {
            return;
        }
        Element element = ehCacheGroups.get(groupName);
        Set currentKeys = null;
        if (element == null) {
            currentKeys = new HashSet();
        } else {
            currentKeys = (Set) element.getObjectValue();
        }
        if (currentKeys == null) {
            currentKeys = new HashSet();
        }

        if (!currentKeys.contains(ALL) && currentKeys.size() <= groupsSizeLimit)
            currentKeys.add(key);
        if (currentKeys.size() > groupsSizeLimit) {
            currentKeys = new HashSet();
            currentKeys.add(ALL);
            logger.warn("Number of keys for group " + groupName + "inside cache " + ehCache.getName() + " is exceeding " +
                    groupsSizeLimit + " entries so we are putting only one entries to tell jahia to flush all this cache when needed");
        }
        element = new Element(groupName, currentKeys);
        ehCacheGroups.put(element);
    }

    private void removeFromGroup(String groupName, Object key) {
        Element element = ehCacheGroups.get(groupName);
        Set currentKeys = null;
        if (element != null) {
            currentKeys = (Set) element.getObjectValue();
        }
        if (currentKeys == null) {
            return;
        }
        currentKeys.remove(key);
    }

    private void removeFromAllGroups(Object key) {
        if (key.getClass() == GroupCacheKey.class) {
            GroupCacheKey curGroupCacheKey = (GroupCacheKey) key;
            synchronized (curGroupCacheKey) {
                Iterator groupIter = curGroupCacheKey.getGroups().iterator();
                while (groupIter.hasNext()) {
                    String curGroup = (String) groupIter.next();
                    removeFromGroup(curGroup, key);
                }
            }
        } else {
            // this is possible if the global LRU map is used to flush entries
        }
    }

    private boolean doRemove(Object key) {
        try {
            boolean removedObject = false;
            removedObject = ehCache.remove(key);
            if (removedObject) {
                removeFromAllGroups(key);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Cannot remove cache entry " + key + " from cache " + toString(), e);
        }
        return false;
    }

    public boolean isEmpty() {
        return ehCache.getSize() == 0;
    }

    public Collection<Object> getKeys() {
        return ehCache.getKeys();
    }

    @Override
    public double getCacheEfficiency() {
        StatisticsGateway stats = ehCache.getStatistics();
        long hitCount = stats.cacheHitCount();
        long total = hitCount + stats.cacheMissCount();
        return total != 0 ? ((hitCount * 100.0) / total) : 0;
    }

    @Override
    public long getSuccessHits() {
        return ehCache.getStatistics().cacheHitCount();
    }

    @Override
    public long getTotalHits() {
        StatisticsGateway stats = ehCache.getStatistics();
        return stats.cacheHitCount() + stats.cacheMissCount();
    }
}
