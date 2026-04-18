/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics;

import java.util.Collection;

public interface Disposable {

    void dispose();

    default void disposeAll(Collection<?> collection) {
        for (Object element : collection) {
            if (element instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
    }
}
