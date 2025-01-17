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
package org.jahia.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic collection utilities.
 * 
 * @author Sergiy Shyrkov
 */
public final class CollectionUtils {

    /**
     * Returns a bounded in size LRU map.
     * 
     * @param maxSize the maximum map size
     * @return a bounded in size LRU map
     */
    public static <K, V> Map<K, V> lruCache(final int maxSize) {
        return lruCache(16, maxSize);
    }

    /**
     * Returns a bounded in size LRU map.
     * 
     * @param initialCapacity the initial capacity of the map
     * @param maxSize the maximum map size
     * @return a bounded in size LRU map
     */
    public static <K, V> Map<K, V> lruCache(final int initialCapacity, final int maxSize) {
        return new LinkedHashMap<K, V>(initialCapacity, 0.75f, true) {

            private static final long serialVersionUID = 6773642631901363425L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }

    private CollectionUtils() {
        super();
    }
}
