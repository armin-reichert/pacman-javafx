/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class OptionMenuEntry<T> {

    protected final String text;
    protected final ObjectProperty<T> value = new SimpleObjectProperty<>();
    protected final List<T> valueList;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

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
        final LinkedHashSet<T> valueSet = new LinkedHashSet<>(valueList);
        if (valueSet.size() != valueList.size()) {
            Logger.error("Menu entry values list contains duplicates. Use unique values only:");
            Logger.error("Original list: {}", valueList);
            valueList = valueSet.stream().toList();
            Logger.error("Corrected list: {}", valueList);
        }

        value.set(requireNonNull(initialValue));
        value.addListener((_, oldValue, newValue) -> onValueChanged(oldValue, newValue));

        final int selectedValueIndex = valueList.indexOf(initialValue);
        if (selectedValueIndex == -1) {
            Logger.error("Initial value {} is not contained in value list, select first value instead");
            value.set(valueList.getFirst());
        }
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T value() {
        return value.get();
    }

    public void setValue(T newValue) {
        value.set(requireNonNull(newValue));
    }

    public void selectNextValue() {
        final int index = valueList.indexOf(value());
        final int nextIndex = index < valueList.size() - 1 ? index + 1 : 0;
        setValue(valueAt(nextIndex));
    }

    public void onValueChanged(T oldValue, T newValue) {
        Logger.debug("Value changed from {} to {}", oldValue, newValue);
    }

    public void setValueFormatter(Function<T, String> formatter) {
        valueFormatter = requireNonNull(formatter);
    }

    public String valueFormatted() {
        return valueFormatter.apply(value());
    }

    public T valueAt(int index) {
        return valueList.get(index);
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean value) {
        enabled.set(value);
    }

    public String text() {
        return text;
    }
}
