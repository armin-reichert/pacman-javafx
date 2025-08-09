/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.PacManGames_GameActions;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import java.util.List;

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

        ResourceManager rm = () -> GameUI.class;
        var iconPlay = new ImageView(rm.loadImage("graphics/icons/play.png"));
        var iconStop = new ImageView(rm.loadImage("graphics/icons/stop.png"));
        var iconStep = new ImageView(rm.loadImage("graphics/icons/step.png"));
        var tooltipPlay = new Tooltip("Play");
        var tooltipStop = new Tooltip("Stop");

        Button[] buttonsSimulationControl = addButtonList("Simulation", List.of("Play/Pause", "Step"));

        Button btnPlayPause = buttonsSimulationControl[0];
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");
        btnPlayPause.graphicProperty().bind(ui.theGameClock().pausedProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(ui.theGameClock().pausedProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setAction(btnPlayPause, PacManGames_GameActions.ACTION_TOGGLE_PAUSED);

        Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(ui.theGameClock().pausedProperty().not());
        setAction(btnStep, () -> ui.theGameClock().makeSteps(GameUI.PROPERTY_SIMULATION_STEPS.get(), true));

        addIntSpinner("Num Steps", 1, 50, GameUI.PROPERTY_SIMULATION_STEPS);
        var sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        setEditor(sliderTargetFPS, ui.theGameClock().targetFrameRateProperty());

        addDynamicLabeledValue("", () -> "Framerate: %.1f (Target: %.1f)".formatted(ui.theGameClock().lastTicksPerSecond(), ui.theGameClock().targetFrameRate()));
        addDynamicLabeledValue("Total Updates",  ui.theGameClock()::updateCount);

        addColorPicker("Canvas Color", GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR);
        addCheckBox("Image Smoothing", GameUI.PROPERTY_CANVAS_IMAGE_SMOOTHING);
        addCheckBox("Font Smoothing", GameUI.PROPERTY_CANVAS_FONT_SMOOTHING);
        addCheckBox("Show Debug Info", GameUI.PROPERTY_DEBUG_INFO_VISIBLE);
        addCheckBox("Time Measured", ui.theGameClock().timeMeasuredProperty());
    }
}