/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.ui.game.Game;
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
    public void connect(Game game) {
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
        btnPlayPause.graphicProperty().bind(game.clock().updatesDisabledProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(game.clock().updatesDisabledProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setAction(game, btnPlayPause, CommonActions.ACTION_TOGGLE_PAUSED);

        final Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(game.clock().updatesDisabledProperty().not());
        setAction(btnStep, () -> game.clock().makeSteps(game.ui().settings().numSimulationStepsProperty.get(), true));

        addIntSpinner("Num Steps", 1, 50, game.ui().settings().numSimulationStepsProperty);

        final var sliderTargetFPS = addSlider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        setEditor(sliderTargetFPS, game.clock().targetFrameRateProperty());

        final GameClock gameClock = game.clock();
        addDynamicLabeledValue("", () -> "FPS: %.1f (Target: %d)".formatted(
            gameClock.fps(),
            gameClock.targetFrameRate()));

        addDynamicLabeledValue("Total Updates",  gameClock::pausableUpdatesCount);

        addColorPicker("Canvas Color", game.ui().settings().canvasBackgroundColorProperty);

        addCheckBox("Font Smoothing", game.ui().settings().canvasFontSmoothingProperty);
        addCheckBox("Show Debug Info", game.ui().settings().debugInfoVisibleProperty);
        addCheckBox("Time Measured", gameClock.timeMeasuredProperty());
    }
}