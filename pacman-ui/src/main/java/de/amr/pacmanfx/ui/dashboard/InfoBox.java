/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.GameScene;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableObjectValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.optionalGameLevel;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theUI;

/**
 * Base class for area displaying UI info/editors.
 *
 * @author Armin Reichert
 */
public abstract class InfoBox extends TitledPane {

    protected static String fontCSS(Font font) {
        return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
    }

    protected final List<InfoText> infoTexts = new ArrayList<>();
    protected final GridPane grid = new GridPane();
    protected int minLabelWidth;
    protected Color textColor;
    protected Font textFont;
    protected Font labelFont;
    protected int rowIndex;

    public InfoBox() {
        grid.setVgap(2);
        grid.setHgap(3);
        setContent(grid);

        setExpanded(false);
        setFocusTraversable(false);
        setOpacity(0.9);
    }

    public void init() {}

    public void update() {
        infoTexts.forEach(InfoText::update);
    }

    public void setContentBackground(Background background) {
        grid.setBackground(background);
    }

    public void setLabelFont(Font labelFont) {
        this.labelFont = labelFont;
    }

    public void setMinLabelWidth(int minLabelWidth) {
        this.minLabelWidth = minLabelWidth;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setContentTextFont(Font textFont) {
        this.textFont = textFont;
    }

    protected Supplier<String> ifGameScenePresent(Function<GameScene, String> fnInfo) {
        return () -> theUI().currentGameScene().map(fnInfo).orElse(InfoText.NO_INFO);
    }

    protected Supplier<String> ifLevelPresent(Function<GameLevel, String> fnInfo) {
        return () -> optionalGameLevel().map(fnInfo).orElse(InfoText.NO_INFO);
    }

    protected void clearGrid() {
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
        var label = new Label(labelText);
        label.setTextFill(textColor);
        label.setFont(labelFont);
        label.setMinWidth(minLabelWidth);
        addRow(label, right);
    }

    protected void addLabeledValue(String labelText, Supplier<?> fnValue) {
        var info = new InfoText(fnValue);
        info.setFill(textColor);
        info.setFont(textFont);
        infoTexts.add(info);
        addRow(labelText, info);
    }

    protected void addLabeledValue(String labelText, String value) {
        addLabeledValue(labelText, () -> value);
    }

    protected void addEmptyRow() {
        addLabeledValue("", "");
    }

    protected Button[] addButtonList(String labelText, String... buttonTexts) {
        var hbox = new HBox();
        var buttons = new Button[buttonTexts.length];
        for (int i = 0; i < buttonTexts.length; ++i) {
            buttons[i] = new Button(buttonTexts[i]);
            buttons[i].setFont(textFont);
            hbox.getChildren().add(buttons[i]);
        }
        addRow(labelText, hbox);
        return buttons;
    }

    protected CheckBox createCheckBox(String text) {
        var cb = new CheckBox(text);
        cb.setTextFill(textColor);
        cb.setFont(textFont);
        return cb;
    }

    protected CheckBox addCheckBox(String labelText, String cbText) {
        var cb = createCheckBox(cbText);
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox addCheckBox(String labelText) {
        var cb = createCheckBox("");
        addRow(labelText, cb);
        return cb;
    }

    protected <T> ChoiceBox<T> addChoiceBox(String labelText, T[] items) {
        var selector = new ChoiceBox<>(FXCollections.observableArrayList(items));
        selector.setStyle(fontCSS(textFont));
        addRow(labelText, selector);
        return selector;
    }

    protected ColorPicker addColorPicker(String labelText, Color color) {
        var colorPicker = new ColorPicker(color);
        addRow(labelText, colorPicker);
        return colorPicker;
    }

    protected void setEditor(CheckBox checkBox, BooleanProperty property) {
        checkBox.selectedProperty().bindBidirectional(property);
    }

    protected void setEditor(ColorPicker picker, ObjectProperty<Color> property) {
        picker.setOnAction(e -> property.set(picker.getValue()));
    }

    protected <T> void setEditor(ChoiceBox<T> selector, WritableObjectValue<T> property) {
        selector.setOnAction(e -> property.set(selector.getValue()));
    }

    protected void setEditor(ChoiceBox<String> selector, StringProperty property) {
        selector.setOnAction(e -> property.set(selector.getValue()));
    }

    protected <T> void setEditor(ChoiceBox<T> selector, ObjectProperty<T> property) {
        selector.setOnAction(e -> property.set(selector.getValue()));
    }

    protected void setEditor(Slider slider, Property<Number> property) {
        slider.valueProperty().bindBidirectional(property);
    }

    protected Slider addSlider(String labelText, int min, int max, double initialValue, boolean tickMarks, boolean tickLabels) {
        var slider = new Slider(min, max, initialValue);
        slider.setShowTickMarks(tickMarks);
        slider.setShowTickLabels(tickLabels);
        slider.setMinWidth(Dashboard.INFOBOX_MIN_WIDTH);
        slider.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                slider.setValue(initialValue);
            }
        });
        addRow(labelText, slider);
        return slider;
    }

    protected Spinner<Integer> addIntSpinner(String labelText, int min, int max, int initialValue) {
        var spinner = new Spinner<Integer>(min, max, initialValue);
        spinner.setStyle(fontCSS(textFont));
        addRow(labelText, spinner);
        return spinner;
    }

    protected Spinner<Integer> addIntSpinner(String labelText, int min, int max, IntegerProperty property) {
        var spinner = new Spinner<Integer>(min, max, property.getValue());
        spinner.getValueFactory().valueProperty().bindBidirectional(property.asObject());
        spinner.valueProperty().addListener((py, ov, newValue) -> property.set(newValue));
        spinner.setStyle(fontCSS(textFont));
        addRow(labelText, spinner);
        return spinner;
    }

    protected void setAction(Button button, Runnable action) {
        button.setOnAction(e -> action.run());
    }

    protected void setAction(CheckBox checkBox, Runnable action) {
        checkBox.setOnAction(e -> action.run());
    }

    protected void setAction(ChoiceBox<?> selector, Runnable action) {
        selector.setOnAction(e -> action.run());
    }

    protected void setTooltip(Control control, ObservableValue<?> property, String pattern) {
        var tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.textProperty().bind(property.map(pattern::formatted));
        control.setTooltip(tooltip);
    }
}