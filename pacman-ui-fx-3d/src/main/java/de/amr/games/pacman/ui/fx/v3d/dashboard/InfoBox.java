/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.ActionHandler3D;
import de.amr.games.pacman.ui.fx.v3d.scene3d.PlayScene3D;
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
public abstract class InfoBox {

    public static String fmtSpeed(byte percentage) {
        return String.format("%.2f px/s (%d%%)", GameModel.PPS_AT_100_PERCENT * percentage * 0.01f, percentage);
    }

    protected final Theme theme;
    protected final List<InfoText> infoTexts = new ArrayList<>();
    protected final TitledPane root = new TitledPane();
    protected final GridPane content = new GridPane();

    private final int minLabelWidth;
    private final Color textColor;
    private final Font textFont;
    private final Font labelFont;

    private int row;

    protected GameSceneContext sceneContext;

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
        content.setBackground(ResourceManager.coloredBackground(new Color(0.2, 0.2, 0.4, 0.8)));
        content.setHgap(4);
        content.setVgap(3);
        content.setPadding(new Insets(5));
        root.setExpanded(false);
        root.setOpacity(0.7);
        root.setFocusTraversable(false);
        root.setText(title);
        root.setContent(content);
    }

    public void init(GameSceneContext sceneContext) {
        this.sceneContext = sceneContext;
        if (!(sceneContext.actionHandler() instanceof ActionHandler3D)) {
            throw new IllegalArgumentException("Action handler in scene context must be the 3D version");
        }
    }

    public TitledPane getRoot() {
        return root;
    }

    public void update() {
        infoTexts.forEach(InfoText::update);
    }

    protected ActionHandler3D actionHandler() {
        return (ActionHandler3D) sceneContext.actionHandler();
    }

    protected boolean isCurrentGameScene3D() {
        return sceneContext.currentGameScene().isPresent()
            && sceneContext.currentGameScene().get() instanceof PlayScene3D;
    }

    protected Supplier<String> ifLevelExists(Function<GameModel, String> infoSupplier) {
        return () -> sceneContext.gameLevel().isEmpty()
            ? InfoText.NO_INFO
            : infoSupplier.apply(sceneContext.game());
    }

    private void addRow(String labelText, Node child) {
        Label label = new Label(labelText);
        label.setTextFill(textColor);
        label.setFont(labelFont);
        label.setMinWidth(minLabelWidth);
        content.add(label, 0, row);
        content.add(child, 1, row);
        ++row;
    }

    protected InfoText addInfo(String labelText, Supplier<?> fnValue) {
        InfoText info = new InfoText(fnValue);
        info.setFill(textColor);
        info.setFont(textFont);
        infoTexts.add(info);
        addRow(labelText, info);
        return info;
    }

    protected void addEmptyLine() {
        addInfo("", "");
    }

    protected InfoText addInfo(String labelText, String value) {
        return addInfo(labelText, () -> value);
    }

    protected Button[] addButtonList(String labelText, String... buttonTexts) {
        HBox hbox = new HBox();
        Button[] buttons = new Button[buttonTexts.length];
        for (int i = 0; i < buttonTexts.length; ++i) {
            buttons[i] = new Button(buttonTexts[i]);
            buttons[i].setFont(textFont);
            hbox.getChildren().add(buttons[i]);
        }
        addRow(labelText, hbox);
        return buttons;
    }

    protected CheckBox addCheckBox(String labelText, Runnable callback) {
        CheckBox cb = new CheckBox();
        cb.setTextFill(textColor);
        cb.setFont(textFont);
        if (callback != null) {
            cb.setOnAction(e -> callback.run());
        }
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox addCheckBox(String labelText) {
        return addCheckBox(labelText, null);
    }

    protected <T> ComboBox<T> addComboBox(String labelText, T[] items) {
        var combo = new ComboBox<>(FXCollections.observableArrayList(items));
        combo.setStyle(style(textFont));
        addRow(labelText, combo);
        return combo;
    }

    protected ColorPicker addColorPicker(String labelText, Color color) {
        var colorPicker = new ColorPicker(color);
        addRow(labelText, colorPicker);
        return colorPicker;
    }

    protected Slider addSlider(String labelText, double min, double max, double initialValue) {
        Slider slider = new Slider(min, max, initialValue);
        slider.setMinWidth((int) theme.get("infobox.min_col_width"));
        slider.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getClickCount() == 2) {
                slider.setValue(initialValue);
            }
        });
        addRow(labelText, slider);
        return slider;
    }

    protected Spinner<Integer> addSpinner(String labelText, int min, int max, int initialValue) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initialValue);
        spinner.setStyle(style(textFont));
        addRow(labelText, spinner);
        return spinner;
    }

    private static String style(Font font) {
        return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
    }
}