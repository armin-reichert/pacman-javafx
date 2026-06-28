/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

public class DashboardSection extends TitledPane {

    public static final String NO_INFO = "n/a";

    protected final GridPane grid = new GridPane();
    protected int rowIndex;
    protected boolean displayedStandalone;

    public DashboardSection() {
        getStyleClass().add("dashboard-section");
        grid.getStyleClass().add("dashboard-section-grid");

        setContent(grid);
        setExpanded(false);
        setFocusTraversable(false);
        setDisplayedStandalone(false);

    }

    public boolean isDisplayedStandalone() {
        return displayedStandalone;
    }

    public void setDisplayedStandalone(boolean alone) {
        displayedStandalone = alone;
    }

    protected void clearSection() {
        grid.getChildren().clear();
        rowIndex = 0;
    }

    protected void addRow(Node content) {
        grid.add(content, 0, rowIndex, 2, 1);
        ++rowIndex;
    }

    protected void addRow(Node left, Node right) {
        if (left != null) {
            grid.add(left, 0, rowIndex);
        }
        if (right != null) {
            grid.add(right, 1, rowIndex);
        }
        if (left != null || right != null) {
            ++rowIndex;
        }
    }

    protected void addRow(String labelText, Node right) {
        addRow(createLabel(labelText, true), right);
    }

    protected void info(String label, String value) {
        addRow(label, new Text(value));
    }

    protected Label createLabel(String text, boolean enabled) {
        Label label = new Label(text);
        label.setDisable(!enabled);
        return label;
    }

    protected void emptyRow() {
        info("", "");
    }

    protected Button[] buttonList(String labelText, List<String> buttonTexts) {
        var hbox = new HBox();
        var buttons = new Button[buttonTexts.size()];
        for (int i = 0; i < buttonTexts.size(); ++i) {
            buttons[i] = new Button(buttonTexts.get(i));
            hbox.getChildren().add(buttons[i]);
        }
        addRow(labelText, hbox);
        return buttons;
    }

    protected CheckBox checkBox(String labelText) {
        var cb = new CheckBox("");
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox checkBox(String labelText, BooleanProperty property) {
        CheckBox checkBox = checkBox(labelText);
        checkBox.selectedProperty().bindBidirectional(property);
        return checkBox;
    }

    protected <T> ChoiceBox<T> choiceBox(String labelText, T[] items) {
        var selector = new ChoiceBox<>(FXCollections.observableArrayList(items));
        addRow(labelText, selector);
        return selector;
    }

    protected ColorPicker colorPicker(String labelText, ObjectProperty<Color> colorProperty) {
        var picker = new ColorPicker(colorProperty.get());
        addRow(labelText, picker);
        picker.setOnAction(_ -> colorProperty.set(picker.getValue()));
        return picker;
    }

    protected <T> void editProperty(ChoiceBox<T> selector, ObjectProperty<T> property) {
        selector.setOnAction(_ -> property.set(selector.getValue()));
    }

    protected void editProperty(Slider slider, Property<Number> property) {
        slider.valueProperty().bindBidirectional(property);
    }

    protected Slider slider(String labelText, double min, double max, double initialValue, boolean tickMarks, boolean tickLabels) {
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

    protected Spinner<Integer> intSpinner(String labelText, int min, int max, IntegerProperty valuePy) {
        var spinner = new Spinner<Integer>(min, max, valuePy.getValue());
        //TODO bidirectional binding does not work for me. Why? Is it me or is it a bug?
        spinner.getValueFactory().valueProperty().addListener((_, _, nv) -> valuePy.set(nv));
        valuePy.addListener((_, _, nv) -> spinner.getValueFactory().setValue(nv.intValue()));
        addRow(labelText, spinner);
        return spinner;
    }

    protected void setAction(Button button, Runnable action) {
        button.setOnAction(_ -> action.run());
    }

    protected void setAction(ChoiceBox<?> selector, Runnable action) {
        selector.setOnAction(_ -> action.run());
    }

    protected void setTooltip(Control control, ObservableValue<?> property, String pattern) {
        var tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.textProperty().bind(property.map(pattern::formatted));
        control.setTooltip(tooltip);
    }
}