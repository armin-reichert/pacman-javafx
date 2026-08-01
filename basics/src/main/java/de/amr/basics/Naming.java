/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import static java.util.Objects.requireNonNull;

public interface Naming {

    String name();

    default boolean hasSameNameAs(Naming entity) {
        requireNonNull(entity);
        return entity.name().equals(name());
    }
}
