/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class OptionMenuEntry<T> {
    protected final String text;
    protected final List<T> valueList;
    protected int selectedValueIndex;
    protected boolean enabled;

    @SafeVarargs
    public OptionMenuEntry(String text, T... values) {
        this.text = requireNonNull(text);
        if (requireNonNull(values).length == 0) {
            throw new IllegalArgumentException("Menu entry must provide at least one value");
        }
        if (Arrays.stream(values).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Menu entry contains NULL value");
        }
        valueList = List.of(values);
        enabled = true;
        selectedValueIndex = 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void selectValue(T value) {
        for (int i = 0; i < valueList.size(); ++i) {
            if (valueList.get(i).equals(value)) {
                selectedValueIndex = i;
                return;
            }
        }
        throw new IllegalArgumentException("Cannot select value " + value);
    }

    public T selectedValue() {
        return valueList.get(selectedValueIndex);
    }

    public String selectedValueText() {
        T selectedValue = selectedValue();
        return selectedValue != null ? String.valueOf(selectedValue) : "No value";
    }

    protected abstract void onValueChanged(int index);
}
