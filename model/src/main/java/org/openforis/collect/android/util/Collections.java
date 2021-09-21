package org.openforis.collect.android.util;

import java.util.ArrayList;
import java.util.List;

public abstract class Collections {

    public static <T> List<T> transform(List<T> list, Transformer<T> transformer) {
        List<T> result = new ArrayList<T>(list.size());
        for (T item : list) {
            result.add(transformer.transform(item));
        }
        return result;
    }

    public abstract interface Transformer<T> {
        T transform(T item);
    }
}
