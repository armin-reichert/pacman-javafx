/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

/**
 * Mix-in with useful methods for building dashboard sections.

 * @param <S> dashboard section type
 */
public interface DashboardSectionCreator<S extends DashboardSection> {

    S section();
    
    default void addRow(String labelText, Node right) {
        section().addRow(createLabel(labelText, true), right);
    }

    default void info(String label, String value) {
        addRow(label, new Text(value));
    }

    default Label createLabel(String text, boolean enabled) {
        Label label = new Label(text);
        label.setDisable(!enabled);
        return label;
    }

    default void emptyRow() {
        info("", "");
    }

    default Button[] buttonList(String labelText, List<String> buttonTexts) {
        var hbox = new HBox();
        var buttons = new Button[buttonTexts.size()];
        for (int i = 0; i < buttonTexts.size(); ++i) {
            buttons[i] = new Button(buttonTexts.get(i));
            hbox.getChildren().add(buttons[i]);
        }
        addRow(labelText, hbox);
        return buttons;
    }

    default CheckBox checkBox(String labelText) {
        var cb = new CheckBox("");
        addRow(labelText, cb);
        return cb;
    }

    default CheckBox checkBox(String labelText, BooleanProperty property) {
        CheckBox checkBox = checkBox(labelText);
        checkBox.selectedProperty().bindBidirectional(property);
        return checkBox;
    }

    default <T> ChoiceBox<T> choiceBox(String labelText, T[] items) {
        var choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(items));
        addRow(labelText, choiceBox);
        return choiceBox;
    }

    default <T> void editPropertyWithChoiceBox(ChoiceBox<T> choiceBox, ObjectProperty<T> property) {
        choiceBox.setOnAction(_ -> property.set(choiceBox.getValue()));
    }

    default ColorPicker colorPicker(String labelText, ObjectProperty<Color> property) {
        var colorPicker = new ColorPicker(property.get());
        colorPicker.setOnAction(_ -> property.set(colorPicker.getValue()));
        addRow(labelText, colorPicker);
        return colorPicker;
    }

    default Slider slider(String labelText, double min, double max, double initialValue, boolean tickMarks, boolean tickLabels) {
        var slider = new Slider(min, max, initialValue);
        slider.setShowTickMarks(tickMarks);
        slider.setShowTickLabels(tickLabels);
        slider.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                slider.setValue(initialValue);
            }
        });
        addRow(labelText, slider);
        return slider;
    }

    default void editPropertyWithSlider(Slider slider, Property<Number> property) {
        slider.valueProperty().bindBidirectional(property);
    }

    default Spinner<Integer> intSpinner(String labelText, int min, int max, IntegerProperty valuePy) {
        var spinner = new Spinner<Integer>(min, max, valuePy.getValue());
        //TODO bidirectional binding does not work for me. Why? Is it me or is it a bug?
        spinner.getValueFactory().valueProperty().addListener((_, _, nv) -> valuePy.set(nv));
        valuePy.addListener((_, _, nv) -> spinner.getValueFactory().setValue(nv.intValue()));
        addRow(labelText, spinner);
        return spinner;
    }

    default void setAction(Button button, Runnable action) {
        button.setOnAction(_ -> action.run());
    }

    default void setAction(ChoiceBox<?> selector, Runnable action) {
        selector.setOnAction(_ -> action.run());
    }

    default void setTooltip(Control control, ObservableValue<?> property, String pattern) {
        var tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.textProperty().bind(property.map(pattern::formatted));
        control.setTooltip(tooltip);
    }
}
