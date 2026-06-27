/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class DashboardSection extends TitledPane {

    public static final String NO_INFO = "n/a";

    protected final Dashboard dashboard;
    protected final List<DynamicInfoText> infoTexts = new ArrayList<>();
    protected final GridPane grid = new GridPane();
    protected int rowIndex;
    protected boolean displayedStandalone;

    public DashboardSection(Dashboard dashboard) {
        this.dashboard = requireNonNull(dashboard);

        getStyleClass().add("dashboard-section");
        grid.getStyleClass().add("dashboard-section-grid");

        setContent(grid);
        setExpanded(false);
        setFocusTraversable(false);
        setDisplayedStandalone(false);

        expandedProperty().addListener((_, _, expanded) -> {
            if (displayedStandalone) {
                if (expanded) {
                    dashboard.sections().filter(infoBox -> infoBox != this).forEach(otherInfoBox -> otherInfoBox.setVisible(false));
                    dashboard.setCompactMode(true);
                } else {
                    dashboard.sections().forEach(infoBox -> infoBox.setVisible(true));
                    dashboard.setCompactMode(false);
                }
            }
        });
    }

    public void connect(Game game) {}

    public void update(Game game) {
        infoTexts.forEach(DynamicInfoText::update);
    }

    public void setDisplayedStandalone(boolean alone) {
        displayedStandalone = alone;
    }

    protected Supplier<String> gameSceneInfo(Game game, Function<AbstractGameScene, String> fnInfo) {
        return () -> game.ui().gameScenes().optCurrentGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> gameLevelInfo(Game game, Function<GameLevel, String> fnInfo) {
        return () -> game.currentGameContext().optCurrentLevel().map(fnInfo).orElse(NO_INFO);
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

    protected void addDynamicLabeledValue(String label, Supplier<?> infoSupplier) {
        var dynamicInfoText = new DynamicInfoText(infoSupplier);
        infoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void addStaticLabeledValue(String label, String value) {
        var staticText = new Text(value);
        addRow(label, staticText);
    }

    protected Label createLabel(String text, boolean enabled) {
        Label label = new Label(text);
        //label.setMinWidth(dashboard.config().labelWidth());
        label.setDisable(!enabled);
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
            hbox.getChildren().add(buttons[i]);
        }
        addRow(labelText, hbox);
        return buttons;
    }

    protected CheckBox createCheckBox(String text) {
        return new CheckBox(text);
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
        addRow(labelText, selector);
        return selector;
    }

    protected ColorPicker addColorPicker(String labelText, ObjectProperty<Color> colorProperty) {
        var picker = new ColorPicker(colorProperty.get());
        addRow(labelText, picker);
        picker.setOnAction(_ -> colorProperty.set(picker.getValue()));
        return picker;
    }

    protected <T> void setEditor(ChoiceBox<T> selector, ObjectProperty<T> property) {
        selector.setOnAction(_ -> property.set(selector.getValue()));
    }

    protected void setEditor(Slider slider, Property<Number> property) {
        slider.valueProperty().bindBidirectional(property);
    }

    protected Slider addSlider(String labelText, double min, double max, double initialValue, boolean tickMarks, boolean tickLabels) {
        var slider = new Slider(min, max, initialValue);
        slider.setShowTickMarks(tickMarks);
        slider.setShowTickLabels(tickLabels);
        //slider.setPrefWidth(0.5 * dashboard.config().width());
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
        //TODO bidirectional binding does not work for me. Why? Is it me or is it a bug?
        spinner.getValueFactory().valueProperty().addListener((_, _, nv) -> valuePy.set(nv));
        valuePy.addListener((_, _, nv) -> spinner.getValueFactory().setValue(nv.intValue()));
        addRow(labelText, spinner);
        return spinner;
    }

    protected void setAction(Button button, Runnable action) {
        button.setOnAction(_ -> action.run());
    }

    protected void setAction(Game game, Button button, GameAction gameAction) {
        button.setOnAction(_ -> gameAction.execute());
        //TODO add boolean property for enabled-state to game action and bind against it
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