/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class OptionMenuEntry<T> {
    protected final ObjectProperty<T> value;
    protected final String text;
    protected final List<T> optionValues;
    protected int selectedValueIndex;
    protected boolean enabled;

    public OptionMenuEntry(String text, List<T> values, T initialValue) {
        this.value = new SimpleObjectProperty<>(initialValue);
        this.text = requireNonNull(text);
        if (requireNonNull(values).isEmpty()) {
            throw new IllegalArgumentException("Menu entry must provide at least one value");
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Menu entry contains NULL value");
        }
        optionValues = List.copyOf(values);
        enabled = true;
        selectedValueIndex = 0;
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T value() {
        return valueProperty().get();
    }

    protected abstract void onValueChanged(int index);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void selectValue(T value) {
        for (int i = 0; i < optionValues.size(); ++i) {
            if (optionValues.get(i).equals(value)) {
                selectedValueIndex = i;
                return;
            }
        }
        throw new IllegalArgumentException("Cannot select value " + value);
    }

    public T getSelectedValue() {
        return optionValues.get(selectedValueIndex);
    }

    public String getSelectedValueText() {
        T selectedValue = getSelectedValue();
        return selectedValue != null ? String.valueOf(selectedValue) : "No value";
    }
}
