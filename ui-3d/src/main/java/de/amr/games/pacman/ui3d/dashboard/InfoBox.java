/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.ActionHandler3D;
import de.amr.games.pacman.ui3d.scene.PlayScene3D;
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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for area displaying UI info/editors.
 *
 * @author Armin Reichert
 */
public class InfoBox extends TitledPane {

    public static final Color BACKGROUND_COLOR = new Color(0.2, 0.2, 0.4, 0.8);

    public static String fmtSpeed(byte percentage) {
        return String.format("%.2f px/s (%d%%)", GameModel.PPS_AT_100_PERCENT * percentage * 0.01f, percentage);
    }

    protected static String fontCSS(Font font) {
        return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
    }

    protected final Theme theme;
    protected final List<InfoText> infoTexts = new ArrayList<>();
    protected final GridPane content = new GridPane();

    private final int minLabelWidth;
    private final Color textColor;
    private final Font textFont;
    private final Font labelFont;

    private int row;

    protected GameSceneContext context;

    protected InfoBox(Theme theme, String title) {
        this(theme, title,
            theme.get("infobox.min_label_width"),
            theme.get("infobox.text_color"),
            theme.get("infobox.text_font"),
            theme.get("infobox.label_font"));
    }

    protected InfoBox(Theme theme, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
        this.theme = theme;
        this.minLabelWidth = minLabelWidth;
        this.textColor = textColor;
        this.textFont = textFont;
        this.labelFont = labelFont;
        content.setBackground(Ufx.coloredBackground(BACKGROUND_COLOR));
        content.setHgap(4);
        content.setVgap(3);
        content.setPadding(new Insets(5));
        setExpanded(false);
        setOpacity(0.7);
        setFocusTraversable(false);
        setText(title);
        setContent(content);
    }

    public void init(GameSceneContext sceneContext) {
        this.context = sceneContext;
        if (!(sceneContext.actionHandler() instanceof ActionHandler3D)) {
            throw new IllegalArgumentException("Action handler in scene context must be the 3D version");
        }
    }

    public void update() {
        infoTexts.forEach(InfoText::update);
    }

    protected ActionHandler3D actionHandler() {
        return (ActionHandler3D) context.actionHandler();
    }

    protected boolean isCurrentGameScene3D() {
        return context.currentGameScene().isPresent()
            && context.currentGameScene().get() instanceof PlayScene3D;
    }

    protected Supplier<String> ifLevelExists(Function<GameLevel, String> infoSupplier) {
        return () -> context.game().level().isEmpty()
            ? InfoText.NO_INFO
            : infoSupplier.apply(context.game().level().get());
    }

    protected void addRow(String labelText, Node child) {
        var label = new Label(labelText);
        label.setTextFill(textColor);
        label.setFont(labelFont);
        label.setMinWidth(minLabelWidth);
        content.add(label, 0, row);
        content.add(child, 1, row);
        ++row;
    }

    protected InfoText infoText(String labelText, Supplier<?> fnValue) {
        var info = new InfoText(fnValue);
        info.setFill(textColor);
        info.setFont(textFont);
        infoTexts.add(info);
        addRow(labelText, info);
        return info;
    }

    protected void infoText(String labelText, String value) {
        infoText(labelText, () -> value);
    }

    protected void emptyRow() {
        infoText("", "");
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

    protected CheckBox checkBox(String labelText, Runnable callback) {
        var cb = new CheckBox();
        cb.setTextFill(textColor);
        cb.setFont(textFont);
        if (callback != null) {
            cb.setOnAction(e -> callback.run());
        }
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox checkBox(String labelText) {
        return checkBox(labelText, null);
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

    protected Slider slider(String labelText, int min, int max, double initialValue) {
        var slider = new Slider(min, max, initialValue);
        slider.setMinWidth(theme.<Integer>get("infobox.min_col_width"));
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
}