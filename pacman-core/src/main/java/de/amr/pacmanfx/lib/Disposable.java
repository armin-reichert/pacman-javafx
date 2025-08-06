/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

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
