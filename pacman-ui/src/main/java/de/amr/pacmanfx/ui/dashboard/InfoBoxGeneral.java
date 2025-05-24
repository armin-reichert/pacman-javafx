/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameActions;
import de.amr.pacmanfx.ui.PacManGamesUI;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import java.util.List;

import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

/**
 * General settings and simulation control.
 */
public class InfoBoxGeneral extends InfoBox {

    private static final int MIN_FRAME_RATE = 5;
    private static final int MAX_FRAME_RATE = 120;

    public void init() {
        addLabeledValue("Java Version",   Runtime.version().toString());
        addLabeledValue("JavaFX Version", System.getProperty("javafx.runtime.version"));

        // Simulation control

        ResourceManager rm = () -> PacManGamesUI.class;
        var iconPlay = new ImageView(rm.loadImage("graphics/icons/play.png"));
        var iconStop = new ImageView(rm.loadImage("graphics/icons/stop.png"));
        var iconStep = new ImageView(rm.loadImage("graphics/icons/step.png"));
        var tooltipPlay = new Tooltip("Play");
        var tooltipStop = new Tooltip("Stop");

        Button[] buttonsSimulationControl = addButtonList("Simulation", List.of("Play/Pause", "Step"));

        Button btnPlayPause = buttonsSimulationControl[0];
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");
        btnPlayPause.graphicProperty().bind(theClock().pausedProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(theClock().pausedProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setAction(btnPlayPause, GameActions.TOGGLE_PAUSED);

        Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(theClock().pausedProperty().not());
        setAction(btnStep, () -> theClock().makeSteps(PY_SIMULATION_STEPS.get(), true));

        addIntSpinner("Num Steps", 1, 50, PY_SIMULATION_STEPS);
        var sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        setEditor(sliderTargetFPS, theClock().targetFrameRateProperty());

        addLabeledValue("", () -> "FPS: %.1f (Tgt: %.1f)".formatted(theClock().getActualFrameRate(), theClock().getTargetFrameRate()));
        addLabeledValue("Total Updates",  theClock()::updateCount);

        addColorPicker("Canvas Color", PY_CANVAS_BG_COLOR);
        addCheckBox("Image Smoothing", PY_CANVAS_IMAGE_SMOOTHING);
        addCheckBox("Font Smoothing", PY_CANVAS_FONT_SMOOTHING);
        addCheckBox("Show Debug Info", PY_DEBUG_INFO_VISIBLE);
        addCheckBox("Time Measured", theClock().timeMeasuredProperty());
    }
}