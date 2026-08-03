package minefarts.smarttube.utils.helpers;

import java.util.ArrayList;

public class LruList<T> extends ArrayList<T> {

    private final int maxEntries;

    public LruList(int maxEntries) {
        super();

        this.maxEntries = maxEntries;
    }
    
    @Override
    public boolean add(T element) {

        if (contains(element))
            remove(element);

        if (size() >= maxEntries)
            remove(0);

        return super.add(element);
    }

};