package minefarts.smarttube.utils.helpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stub implementation of LRU list.
 */
public class LruList<T> extends LinkedHashMap<Integer, T> {
    private final int maxSize;

    public LruList(int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, T> eldest) {
        return size() > maxSize;
    }
}