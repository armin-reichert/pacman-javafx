/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.CommonGameActions;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import java.util.List;

import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;

/**
 * General settings and simulation control.
 */
public class InfoBoxGeneral extends InfoBox {

    private static final int MIN_FRAME_RATE = 5;
    private static final int MAX_FRAME_RATE = 120;

    public InfoBoxGeneral(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        addStaticLabeledValue("Java Version",   Runtime.version().toString());
        addStaticLabeledValue("JavaFX Version", System.getProperty("javafx.runtime.version"));

        // Simulation control

        ResourceManager rm = () -> GameUI_Implementation.class;
        var iconPlay = new ImageView(rm.loadImage("graphics/icons/play.png"));
        var iconStop = new ImageView(rm.loadImage("graphics/icons/stop.png"));
        var iconStep = new ImageView(rm.loadImage("graphics/icons/step.png"));
        var tooltipPlay = new Tooltip("Play");
        var tooltipStop = new Tooltip("Stop");

        Button[] buttonsSimulationControl = addButtonList("Simulation", List.of("Play/Pause", "Step"));

        Button btnPlayPause = buttonsSimulationControl[0];
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");
        btnPlayPause.graphicProperty().bind(ui.clock().pausedProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(ui.clock().pausedProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setAction(btnPlayPause, CommonGameActions.ACTION_TOGGLE_PAUSED);

        Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(ui.clock().pausedProperty().not());
        setAction(btnStep, () -> ui.clock().makeSteps(PROPERTY_SIMULATION_STEPS.get(), true));

        addIntSpinner("Num Steps", 1, 50, PROPERTY_SIMULATION_STEPS);
        var sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        setEditor(sliderTargetFPS, ui.clock().targetFrameRateProperty());

        addDynamicLabeledValue("", () -> "Frame Rate: %.1f (Target: %.1f)".formatted(ui.clock().lastTicksPerSecond(), ui.clock().targetFrameRate()));
        addDynamicLabeledValue("Total Updates",  ui.clock()::updateCount);

        addColorPicker("Canvas Color", PROPERTY_CANVAS_BACKGROUND_COLOR);
        addCheckBox("Font Smoothing", PROPERTY_CANVAS_FONT_SMOOTHING);
        addCheckBox("Show Debug Info", PROPERTY_DEBUG_INFO_VISIBLE);
        addCheckBox("Time Measured", ui.clock().timeMeasuredProperty());
    }
}