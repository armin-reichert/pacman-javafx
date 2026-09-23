/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

public interface EnumMethods<T extends Enum<T>> {

    int ordinal();

    T[] enumValues();

    default int count() {
        return enumValues().length;
    }

    default T pred() {
        final int pred = ordinal() == 0 ? count() - 1 : ordinal() - 1;
        return enumValues()[pred];
    }

    default T succ() {
        final int succ = ordinal() + 1 == count() ? 0 : ordinal() + 1;
        return enumValues()[succ];
    }
}
