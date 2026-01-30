/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
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

public abstract class DashboardSection extends TitledPane {

    private static final int GRID_HGAP = 0;
    private static final int GRID_VGAP = 2;
    private static final double OPACITY = 0.8;

    public static final String NO_INFO = "n/a";

    protected static String fontCSS(Font font) {
        return "-fx-font: %.0fpx \"%s\";".formatted(font.getSize(), font.getFamily());
    }

    protected final Dashboard dashboard;
    protected final List<DynamicInfoText> infoTexts = new ArrayList<>();
    protected final GridPane grid = new GridPane(GRID_HGAP, GRID_VGAP);
    protected int rowIndex;
    protected boolean displayedMaximized;

    public DashboardSection(Dashboard dashboard) {
        this.dashboard = requireNonNull(dashboard);

        setContent(grid);
        setExpanded(false);
        setFocusTraversable(false);
        setPrefWidth(dashboard.style().width());
        setMinWidth(dashboard.style().width());
        setMaxWidth(dashboard.style().width());
        setOpacity(OPACITY);

        setContentBackground(Background.fill(dashboard.style().contentBackground()));
        setDisplayedMaximized(false);

        expandedProperty().addListener((_, _, expanded) -> {
            if (displayedMaximized) {
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

    public void init(GameUI ui) {}

    public void update(GameUI ui) {
        infoTexts.forEach(DynamicInfoText::update);
    }

    public void setDisplayedMaximized(boolean maximized) {
        displayedMaximized = maximized;
    }

    public void setContentBackground(Background background) {
        grid.setBackground(background);
    }

    protected Supplier<String> ifGameScenePresent(GameUI ui, Function<GameScene, String> fnInfo) {
        return () -> ui.views().playView().optGameScene().map(fnInfo).orElse(NO_INFO);
    }

    protected Supplier<String> ifGameLevel(Supplier<Game> gameSupplier, Function<GameLevel, String> fnInfo) {
        return () -> gameSupplier.get().optGameLevel().map(fnInfo).orElse(NO_INFO);
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
        dynamicInfoText.setFill(dashboard.style().textColor());
        dynamicInfoText.setFont(dashboard.style().contentFont());
        infoTexts.add(dynamicInfoText);
        addRow(label, dynamicInfoText);
    }

    protected void addStaticLabeledValue(String label, String value) {
        var staticText = new Text(value);
        staticText.setFill(dashboard.style().textColor());
        staticText.setFont(dashboard.style().contentFont());
        addRow(label, staticText);
    }

    protected Label createLabel(String text, boolean enabled) {
        Label label = new Label(text);
        label.setMinWidth(dashboard.style().labelWidth());
        label.setTextFill(enabled ? dashboard.style().textColor() : Color.DIMGRAY);
        label.setFont(dashboard.style().labelFont());
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
            buttons[i].setFont(dashboard.style().contentFont());
            hbox.getChildren().add(buttons[i]);
        }
        addRow(labelText, hbox);
        return buttons;
    }

    protected CheckBox createCheckBox(String text) {
        var cb = new CheckBox(text);
        cb.setTextFill(dashboard.style().textColor());
        cb.setFont(dashboard.style().contentFont());
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
        selector.setStyle(fontCSS(dashboard.style().contentFont()));
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
        slider.setPrefWidth(0.5 * dashboard.style().width());
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
        spinner.setStyle(fontCSS(dashboard.style().contentFont()));
        //TODO bidirectional binding does not work for me. Why? Is it me or is it a bug?
        spinner.getValueFactory().valueProperty().addListener((_, _, nv) -> valuePy.set(nv));
        valuePy.addListener((_, _, nv) -> spinner.getValueFactory().setValue(nv.intValue()));
        addRow(labelText, spinner);
        return spinner;
    }

    protected void setAction(Button button, Runnable action) {
        button.setOnAction(_ -> action.run());
    }

    protected void setAction(GameUI ui, Button button, GameAction gameAction) {
        button.setOnAction(_ -> gameAction.executeIfEnabled(ui));
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