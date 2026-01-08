/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class OptionMenuEntry<T> {
    protected final ObjectProperty<T> value;
    protected final String text;
    protected final List<T> optionValues;
    protected int selectedValueIndex;
    protected boolean enabled;

    public OptionMenuEntry(String text, List<T> values, T initialValue) {
        requireNonNull(text);
        requireNonNull(initialValue);
        requireNonNull(values);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Menu entry values list is empty");
        }
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Menu entry values list contains NULL value");
        }

        this.value = new SimpleObjectProperty<>(initialValue);
        this.selectedValueIndex = values.indexOf(initialValue);
        if (this.selectedValueIndex == -1) {
            Logger.error("Initial value {} is not contained in values list, using first value instead");
            this.value.set(values.getFirst());
            this.selectedValueIndex = 0;
        }

        this.text = text;
        this.optionValues = List.copyOf(values);
        this.enabled = true;
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T value() {
        return valueProperty().get();
    }

    protected void onValueSelectionChange() {
        value.set(getSelectedValue());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void selectValue(T value) {
        requireNonNull(value);
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

    public String formatValue(T value) {
        return value != null ? String.valueOf(value) : "No value";
    }

    public String formatSelectedValue() {
        return formatValue(getSelectedValue());
    }
}
