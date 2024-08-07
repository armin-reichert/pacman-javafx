/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.amr.games.pacman.ui2d.dashboard.InfoText.NO_INFO;

/**
 * Base class for area displaying UI info/editors.
 *
 * @author Armin Reichert
 */
public abstract class InfoBox extends TitledPane {

    public static final Color BACKGROUND_COLOR = new Color(0.2, 0.2, 0.4, 0.8);

    public static String fmtSpeed(byte percentage) {
        return String.format("%.2f px/s (%d%%)", GameModel.PPS_AT_100_PERCENT * percentage * 0.01f, percentage);
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
        grid.setBackground(Ufx.coloredBackground(BACKGROUND_COLOR));
        grid.setHgap(4);
        grid.setVgap(3);
        grid.setPadding(new Insets(5));
    }

    public abstract void init(GameContext context);

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

    protected Supplier<String> ifGameScene(Function<GameScene, String> infoSupplier) {
        return () -> context.currentGameScene().map(infoSupplier).orElse(NO_INFO);
    }

    protected Supplier<String> ifLevel(Function<GameLevel, String> infoSupplier) {
        return () -> context.game().level().map(infoSupplier).orElse(NO_INFO);
    }

    protected Supplier<String> ifWorld(Function<GameWorld, String> infoSupplier) {
        return () -> Optional.ofNullable(context.game().world()).map(infoSupplier).orElse(NO_INFO);
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

    protected InfoText addTextRow(String labelText, Supplier<?> fnValue) {
        var info = new InfoText(fnValue);
        info.setFill(textColor);
        info.setFont(textFont);
        infoTexts.add(info);
        addRow(labelText, info);
        return info;
    }

    protected void addTextRow(String labelText, String value) {
        addTextRow(labelText, () -> value);
    }

    protected void addEmptyRow() {
        addTextRow("", "");
    }

    protected Button[] addButtonListRow(String labelText, String... buttonTexts) {
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

    protected CheckBox checkBox(String text, Runnable callback) {
        var cb = new CheckBox(text);
        cb.setTextFill(textColor);
        cb.setFont(textFont);
        if (callback != null) {
            cb.setOnAction(e -> callback.run());
        }
        return cb;
    }

    protected CheckBox addCheckBoxRow(String labelText, Runnable callback) {
        var cb = checkBox("", callback);
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox checkBox(String labelText) {
        return addCheckBoxRow(labelText, null);
    }

    protected <T> ComboBox<T> addComboBoxRow(String labelText, T[] items) {
        var combo = new ComboBox<>(FXCollections.observableArrayList(items));
        combo.setStyle(fontCSS(textFont));
        addRow(labelText, combo);
        return combo;
    }

    protected ColorPicker addColorPickerRow(String labelText, Color color) {
        var colorPicker = new ColorPicker(color);
        addRow(labelText, colorPicker);
        return colorPicker;
    }

    protected Slider addSliderRow(String labelText, int min, int max, double initialValue) {
        var slider = new Slider(min, max, initialValue);
        slider.setMinWidth(context.assets().<Integer>get("infobox.min_col_width"));
        slider.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                slider.setValue(initialValue);
            }
        });
        addRow(labelText, slider);
        return slider;
    }

    protected Spinner<Integer> addIntSpinnerRow(String labelText, int min, int max, int initialValue) {
        var spinner = new Spinner<Integer>(min, max, initialValue);
        spinner.setStyle(fontCSS(textFont));
        addRow(labelText, spinner);
        return spinner;
    }
}