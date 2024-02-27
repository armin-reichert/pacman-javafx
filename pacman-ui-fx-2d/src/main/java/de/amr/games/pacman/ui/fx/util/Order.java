/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

/**
 * @author Armin Reichert
 */
public class Order<T extends Enum<T>> {

    private final int[] reorderdIndex;

    @SuppressWarnings("unchecked")
    public Order(T... values) {
        reorderdIndex = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            reorderdIndex[values[i].ordinal()] = i;
        }
    }

    public int index(T enumValue) {
        return reorderdIndex[enumValue.ordinal()];
    }
}