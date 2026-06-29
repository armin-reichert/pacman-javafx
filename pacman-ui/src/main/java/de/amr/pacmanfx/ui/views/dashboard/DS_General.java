/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.model.GameUIViewModel;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import java.util.List;

/**
 * General settings and simulation control.
 */
public class DS_General extends GameDashboardSection {

    private static final int MIN_FRAME_RATE = 5;
    private static final int MAX_FRAME_RATE = 120;

    public DS_General() {}

    @Override
    public void connect(Game game) {
        final GameUIViewModel viewModel = game.ui().viewModel();
        final GameClock gameClock = game.clock();

        info("Java Version",   Runtime.version().toString());
        info("JavaFX Version", System.getProperty("javafx.runtime.version"));

        // Simulation control

        final ResourceManager rm = () -> DS_General.class;

        final var iconPlay = new ImageView(rm.loadImage("/de/amr/pacmanfx/ui/graphics/icons/play.png"));
        final var iconStop = new ImageView(rm.loadImage("/de/amr/pacmanfx/ui/graphics/icons/stop.png"));
        final var iconStep = new ImageView(rm.loadImage("/de/amr/pacmanfx/ui/graphics/icons/step.png"));

        final var tooltipPlay = new Tooltip("Play");
        final var tooltipStop = new Tooltip("Stop");

        final Button[] buttonsSimulationControl = buttonList("Simulation", List.of("Play/Pause", "Step"));

        final Button btnPlayPause = buttonsSimulationControl[0];
        btnPlayPause.setText(null);
        btnPlayPause.setStyle("-fx-background-color: transparent");
        btnPlayPause.graphicProperty().bind(gameClock.updatesDisabledProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(gameClock.updatesDisabledProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setGameAction(btnPlayPause, game.actions().simulationActions().actionTogglePaused());

        final Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(gameClock.updatesDisabledProperty().not());
        setAction(btnStep, () -> gameClock.makeSteps(viewModel.numSimulationStepsProperty.get(), true));

        intSpinner("Num Steps", 1, 50, viewModel.numSimulationStepsProperty);

        final var sliderTargetFPS = slider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        editPropertyWithSlider(sliderTargetFPS, gameClock.targetFrameRateProperty());

        addDynamicInfo("", () -> "FPS: %.1f (Target: %d)".formatted(gameClock.fps(), gameClock.targetFrameRate()));
        addDynamicInfo("Total Updates",  gameClock::pausableUpdatesCount);

        colorPicker("Canvas Color", viewModel.common2D.canvasBackgroundColorProperty);
        checkBox("Font Smoothing",  viewModel.common2D.fontSmoothingOnProperty);
        checkBox("Show Debug Info", viewModel.debugModeOnProperty);
        checkBox("Time Measured",   gameClock.timeMeasuredProperty());
    }
}