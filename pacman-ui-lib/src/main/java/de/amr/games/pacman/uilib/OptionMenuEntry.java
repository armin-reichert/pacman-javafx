/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static de.amr.games.pacman.Globals.assertNotNull;

public abstract class OptionMenuEntry<T> {
    protected final String text;
    protected final List<T> valueList;
    protected int selectedIndex;
    protected boolean enabled;

    @SafeVarargs
    public OptionMenuEntry(String text, T... values) {
        this.text = assertNotNull(text);
        if (assertNotNull(values).length == 0) {
            throw new IllegalArgumentException("Menu entry must provide at least one value");
        }
        if (Arrays.stream(values).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Menu entry contains NULL value");
        }
        valueList = List.of(values);
        enabled = true;
        selectedIndex = 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void selectValue(T value) {
        for (int i = 0; i < valueList.size(); ++i) {
            if (valueList.get(i).equals(value)) {
                selectedIndex = i;
                return;
            }
        }
        throw new IllegalArgumentException("Cannot select value " + value);
    }

    public T selectedValue() {
        return valueList.get(selectedIndex);
    }

    public String selectedValueText() {
        return String.valueOf(selectedValue());
    }

    protected void onSelect() {
    }

    protected abstract void onValueChanged(int index);
}
