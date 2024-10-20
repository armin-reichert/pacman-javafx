/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.LevelData;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableObjectValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.amr.games.pacman.ui2d.dashboard.InfoText.NO_INFO;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;

/**
 * Base class for area displaying UI info/editors.
 *
 * @author Armin Reichert
 */
public abstract class InfoBox extends TitledPane {

    public static String fmtSpeed(byte percentage) {
        return String.format("%.2f px/s (%d%%)", 1.25f * percentage * 0.01f, percentage);
    }

    protected static String fontCSS(Font font) {
        return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
    }

    protected final List<InfoText> infoTexts = new ArrayList<>();
    protected final GridPane grid = new GridPane();
    protected GameContext context;
    protected int minLabelWidth;
    protected Color textColor;
    protected Font textFont;
    protected Font labelFont;
    protected int rowIndex;

    public InfoBox() {
        setExpanded(false);
        setOpacity(0.7);
        setFocusTraversable(false);
        setContent(grid);
        grid.setHgap(4);
        grid.setVgap(3);
        grid.setPadding(new Insets(5));
    }

    public void init(GameContext context) {
        this.context = context;
        grid.setBackground(coloredBackground(context.assets().color("infobox.background_color")));
    }

    public void update() {
        infoTexts.forEach(InfoText::update);
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

    public void setTextFont(Font textFont) {
        this.textFont = textFont;
    }

    protected Supplier<String> ifGameScenePresent(Function<GameScene, String> fnInfo) {
        return () -> context.currentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> ifLevelPresent(Function<LevelData, String> fnInfo) {
        return () -> {
            if (context.game() instanceof PacManArcadeGame arcadeGame) {
                return arcadeGame.currentLevelData().map(fnInfo).orElse(NO_INFO);
            }
            if (context.game() instanceof MsPacManArcadeGame arcadeGame) {
                return arcadeGame.currentLevelData().map(fnInfo).orElse(NO_INFO);
            }
            if (context.game() instanceof TengenMsPacManGame tengenGame) {
                return tengenGame.currentLevelData().map(fnInfo).orElse(NO_INFO);
            }
            return NO_INFO;
        };
    }

    protected Supplier<String> ifWorldPresent(Function<GameWorld, String> fnInfo) {
        return () -> Optional.ofNullable(context.game().world()).map(fnInfo).orElse(NO_INFO);
    }

    protected void clearGrid() {
        grid.getChildren().clear();
        rowIndex = 0;
    }

    protected void addRow(Node rowNode) {
        grid.add(rowNode, 0, rowIndex, 2, 1);
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

    protected void labeledValue(String labelText, Supplier<?> fnValue) {
        var info = new InfoText(fnValue);
        info.setFill(textColor);
        info.setFont(textFont);
        infoTexts.add(info);
        addRow(labelText, info);
    }

    protected void labeledValue(String labelText, String value) {
        labeledValue(labelText, () -> value);
    }

    protected void emptyRow() {
        labeledValue("", "");
    }

    protected Button[] buttonList(String labelText, String... buttonTexts) {
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

    protected CheckBox checkBox(String labelText, String cbText) {
        var cb = createCheckBox(cbText);
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox checkBox(String labelText) {
        var cb = createCheckBox("");
        addRow(labelText, cb);
        return cb;
    }

    protected <T> ComboBox<T> comboBox(String labelText, T[] items) {
        var combo = new ComboBox<>(FXCollections.observableArrayList(items));
        combo.setStyle(fontCSS(textFont));
        addRow(labelText, combo);
        return combo;
    }

    protected ColorPicker colorPicker(String labelText, Color color) {
        var colorPicker = new ColorPicker(color);
        addRow(labelText, colorPicker);
        return colorPicker;
    }

    protected void assignEditor(CheckBox checkBox, BooleanProperty property) {
        checkBox.selectedProperty().bindBidirectional(property);
    }

    protected void assignEditor(ColorPicker picker, ObjectProperty<Color> property) {
        picker.setOnAction(e -> property.set(picker.getValue()));
    }

    protected <T> void assignEditor(ComboBox<T> combo, WritableObjectValue<T> property) {
        combo.setOnAction(e -> property.set(combo.getValue()));
    }

    protected void assignEditor(ComboBox<String> combo, StringProperty property) {
        combo.setOnAction(e -> property.set(combo.getValue()));
    }

    protected void assignEditor(Slider slider, Property<Number> property) {
        slider.valueProperty().bindBidirectional(property);
    }

    protected Slider slider(String labelText, int min, int max, double initialValue, boolean tickMarks,  boolean tickLabels) {
        var slider = new Slider(min, max, initialValue);
        slider.setShowTickMarks(tickMarks);
        slider.setShowTickLabels(tickLabels);
        slider.setMinWidth(context.assets().<Integer>get("infobox.min_col_width"));
        slider.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                slider.setValue(initialValue);
            }
        });
        addRow(labelText, slider);
        return slider;
    }

    protected Spinner<Integer> integerSpinner(String labelText, int min, int max, int initialValue) {
        var spinner = new Spinner<Integer>(min, max, initialValue);
        spinner.setStyle(fontCSS(textFont));
        addRow(labelText, spinner);
        return spinner;
    }

    protected Spinner<Integer> integerSpinner(String labelText, int min, int max, IntegerProperty property) {
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
    protected void setAction(ComboBox<?> combo, Runnable action) {
        combo.setOnAction(e -> action.run());
    }

    protected void setTooltip(Control control, ObservableValue<?> property, String pattern) {
        var tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.textProperty().bind(property.map(pattern::formatted));
        control.setTooltip(tooltip);
    }
}