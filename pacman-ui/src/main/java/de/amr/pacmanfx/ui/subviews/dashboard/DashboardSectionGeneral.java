/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import java.util.List;

/**
 * General settings and simulation control.
 */
public class DashboardSectionGeneral extends DashboardSection {

    private static final int MIN_FRAME_RATE = 5;
    private static final int MAX_FRAME_RATE = 120;

    public DashboardSectionGeneral(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(GameUI ui) {
        addStaticLabeledValue("Java Version",   Runtime.version().toString());
        addStaticLabeledValue("JavaFX Version", System.getProperty("javafx.runtime.version"));

        // Simulation control

        final ResourceManager rm = () -> DashboardSectionGeneral.class;

        final var iconPlay = new ImageView(rm.loadImage("/de/amr/pacmanfx/ui/graphics/icons/play.png"));
        final var iconStop = new ImageView(rm.loadImage("/de/amr/pacmanfx/ui/graphics/icons/stop.png"));
        final var iconStep = new ImageView(rm.loadImage("/de/amr/pacmanfx/ui/graphics/icons/step.png"));

        final var tooltipPlay = new Tooltip("Play");
        final var tooltipStop = new Tooltip("Stop");

        final Button[] buttonsSimulationControl = addButtonList("Simulation", List.of("Play/Pause", "Step"));

        final Button btnPlayPause = buttonsSimulationControl[0];
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");
        btnPlayPause.graphicProperty().bind(ui.access().gameClock().updatesDisabledProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(ui.access().gameClock().updatesDisabledProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setAction(ui, btnPlayPause, CommonActions.ACTION_TOGGLE_PAUSED);

        final Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(ui.access().gameClock().updatesDisabledProperty().not());
        setAction(btnStep, () -> ui.access().gameClock().makeSteps(GameUI_Constants.PROPERTY_SIMULATION_STEPS.get(), true));

        addIntSpinner("Num Steps", 1, 50, GameUI_Constants.PROPERTY_SIMULATION_STEPS);

        final var sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        setEditor(sliderTargetFPS, ui.access().gameClock().targetFrameRateProperty());

        final GameClock gameClock = ui.access().gameClock();
        addDynamicLabeledValue("", () -> "FPS: %.1f (Target: %d)".formatted(
            gameClock.fps(),
            gameClock.targetFrameRate()));

        addDynamicLabeledValue("Total Updates",  gameClock::pausableUpdatesCount);

        addColorPicker("Canvas Color", GameUI_Constants.PROPERTY_CANVAS_BACKGROUND_COLOR);

        addCheckBox("Font Smoothing", GameUI_Constants.PROPERTY_CANVAS_FONT_SMOOTHING);
        addCheckBox("Show Debug Info", GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE);
        addCheckBox("Time Measured", gameClock.timeMeasuredProperty());
    }
}