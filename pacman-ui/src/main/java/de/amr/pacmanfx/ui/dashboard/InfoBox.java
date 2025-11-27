/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Base class for area displaying UI info/editors.
 */
public abstract class InfoBox extends TitledPane {

    public static final String NO_INFO = "n/a";

    protected static String fontCSS(Font font) {
        return String.format("-fx-font: %.0fpx \"%s\";", font.getSize(), font.getFamily());
    }

    protected final GameUI ui;
    protected Dashboard dashboard;
    protected final List<DynamicInfoText> infoTexts = new ArrayList<>();
    protected final GridPane grid = new GridPane();
    protected int minLabelWidth;
    protected Color textColor;
    protected Font textFont;
    protected Font labelFont;
    protected int rowIndex;
    protected boolean showMaximized;

    public InfoBox(GameUI ui) {
        this.ui = requireNonNull(ui);
        grid.setVgap(2);
        grid.setHgap(3);
        setContent(grid);
        setExpanded(false);
        setFocusTraversable(false);
        setOpacity(0.9);
        setShowMaximized(false);

        expandedProperty().addListener((py,ov,expanded) -> {
            if (expanded && showMaximized) {
                dashboard.infoBoxes()
                        .filter(infoBox -> infoBox != this)
                        .forEach(infoBox -> infoBox.setVisible(false));
                dashboard.showOnlyVisibleInfoBoxes(true);
            }
            if (!expanded && showMaximized) {
                dashboard.infoBoxes().forEach(infoBox -> infoBox.setVisible(true));
                dashboard.showOnlyVisibleInfoBoxes(false);
            }
        });
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public Dashboard dashboard() {
        return dashboard;
    }

    public void setShowMaximized(boolean showMaximized) {
        this.showMaximized = showMaximized;
    }

    public boolean showMaximized() {
        return showMaximized;
    }

    public void init(GameUI ui) {}

    public void update() {
        infoTexts.forEach(DynamicInfoText::update);
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
        return () -> ui.currentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> ifGameLevelPresent(Function<GameLevel, String> fnInfo) {
        return () -> ui.context().currentGame().optGameLevel().map(fnInfo).orElse(NO_INFO);
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
        addRow(createLabel(labelText, true), right);
    }

    protected void addDynamicLabeledValue(String label, Supplier<?> infoSupplier) {
        var dynamicInfoText = new DynamicInfoText(infoSupplier);
        dynamicInfoText.setFill(textColor);
        dynamicInfoText.setFont(textFont);
        infoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void addStaticLabeledValue(String label, String value) {
        var staticText = new Text(value);
        staticText.setFill(textColor);
        staticText.setFont(textFont);
        addRow(label, staticText);
    }

    protected Label createLabel(String text, boolean enabled) {
        Label label = new Label(text);
        label.setMinWidth(minLabelWidth);
        label.setTextFill(enabled ? textColor : Color.DIMGRAY);
        label.setFont(textFont);
        return label;
    }

    protected void addEmptyRow() {
        addStaticLabeledValue("", "");
    }

    protected Button[] addButtonList(String labelText, List<String> buttonTexts) {
        var hbox = new HBox();
        var buttons = new Button[buttonTexts.size()];
        for (int i = 0; i < buttonTexts.size(); ++i) {
            buttons[i] = new Button(buttonTexts.get(i));
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

    protected CheckBox addCheckBox(String labelText) {
        var cb = createCheckBox("");
        addRow(labelText, cb);
        return cb;
    }

    protected CheckBox addCheckBox(String labelText, BooleanProperty property) {
        CheckBox checkBox = addCheckBox(labelText);
        checkBox.selectedProperty().bindBidirectional(property);
        return checkBox;
    }

    protected <T> ChoiceBox<T> addChoiceBox(String labelText, T[] items) {
        var selector = new ChoiceBox<>(FXCollections.observableArrayList(items));
        selector.setStyle(fontCSS(textFont));
        addRow(labelText, selector);
        return selector;
    }

    protected ColorPicker addColorPicker(String labelText, ObjectProperty<Color> colorProperty) {
        var picker = new ColorPicker(colorProperty.get());
        addRow(labelText, picker);
        picker.setOnAction(e -> colorProperty.set(picker.getValue()));
        return picker;
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

    protected Spinner<Integer> addIntSpinner(String labelText, int min, int max, IntegerProperty valuePy) {
        var spinner = new Spinner<Integer>(min, max, valuePy.getValue());
        spinner.setStyle(fontCSS(textFont));
        //TODO bidirectional binding does not work for me. Why? Is it me or is it a bug?
        spinner.getValueFactory().valueProperty().addListener((py,ov,nv) -> valuePy.set(nv));
        valuePy.addListener((py,ov,nv) -> spinner.getValueFactory().setValue(nv.intValue()));
        addRow(labelText, spinner);
        return spinner;
    }

    protected void setAction(Button button, Runnable action) {
        button.setOnAction(e -> action.run());
    }

    protected void setAction(Button button, GameAction gameAction) {
        button.setOnAction(e -> gameAction.executeIfEnabled(ui));
        //TODO add boolean property for enabled-state to game action and bind against it
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