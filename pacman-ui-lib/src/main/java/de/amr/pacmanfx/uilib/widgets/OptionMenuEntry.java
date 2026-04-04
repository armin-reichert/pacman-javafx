/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class OptionMenuEntry<T> {

    protected final String text;
    protected final ObjectProperty<T> value = new SimpleObjectProperty<>();
    protected final List<T> valueList;
    protected int selectedValueIndex;
    protected boolean enabled;

    private Function<T, String> valueFormatter = value -> (value != null) ? String.valueOf(value) : "No value";

    public OptionMenuEntry(String text, List<T> valueList, T initialValue) {
        this.text = requireNonNull(text);

        this.valueList = List.copyOf(requireNonNull(valueList));
        if (valueList.isEmpty()) {
            throw new IllegalArgumentException("Menu entry values list is empty");
        }
        if (valueList.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Menu entry values list contains NULL value");
        }

        value.set(requireNonNull(initialValue));
        value.addListener((_, oldValue, newValue) -> onValueChanged(oldValue, newValue));

        selectedValueIndex = valueList.indexOf(initialValue);
        if (selectedValueIndex == -1) {
            Logger.error("Initial value {} is not contained in values list, using first value instead");
            value.set(valueList.getFirst());
            selectedValueIndex = 0;
        }

        enabled = true;
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T value() {
        return valueProperty().get();
    }

    public void onValueChanged(T oldValue, T newValue) {
        Logger.debug("Value changed from {} to {}", oldValue, newValue);
    }

    public void setValueFormatter(Function<T, String> formatter) {
        valueFormatter = requireNonNull(formatter);
    }

    public String selectedValueFormatted() {
        return valueFormatter.apply(getSelectedValue());
    }

    protected void onValueSelectionChange() {
        value.set(getSelectedValue());
    }

    public void selectValue(T value) {
        requireNonNull(value);
        for (int i = 0; i < valueList.size(); ++i) {
            if (valueList.get(i).equals(value)) {
                selectedValueIndex = i;
                return;
            }
        }
        throw new IllegalArgumentException("Cannot select value " + value);
    }

    public T getSelectedValue() {
        return valueList.get(selectedValueIndex);
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String text() {
        return text;
    }
}
