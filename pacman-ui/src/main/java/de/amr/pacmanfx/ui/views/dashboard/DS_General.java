/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.ui.assets.ResourceManager;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
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

    public DS_General() {
        super(DashboardID.GENERAL);
    }

    @Override
    public void setExecutionContext(GameActionContext context) {
        final GameViewModel viewModel = context.ui().viewModel();

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
        btnPlayPause.graphicProperty().bind(context.clock().updatesDisabledProperty().map(paused -> paused ? iconPlay : iconStop));
        btnPlayPause.tooltipProperty().bind(context.clock().updatesDisabledProperty().map(paused -> paused ? tooltipPlay : tooltipStop));
        setGameAction(context, btnPlayPause, CommonGameActions.instance().simulationActions().actionTogglePaused());

        final Button btnStep = buttonsSimulationControl[1];
        btnStep.setGraphic(iconStep);
        btnStep.setStyle("-fx-background-color: transparent");
        btnStep.setText(null);
        btnStep.setTooltip(new Tooltip("Single Step Mode"));
        btnStep.disableProperty().bind(context.clock().updatesDisabledProperty().not());
        setAction(btnStep, () -> context.clock().makeSteps(viewModel.numSimulationStepsProperty().get(), true));

        intSpinner("Num Steps", 1, 50, viewModel.numSimulationStepsProperty());

        final var sliderTargetFPS = slider("Simulation Speed", MIN_FRAME_RATE, MAX_FRAME_RATE, 60, false, false);
        editPropertyWithSlider(sliderTargetFPS, context.clock().targetFrameRateProperty());

        addDynamicInfo("", () -> "FPS: %.1f (Target: %d)".formatted(context.clock().fps(), context.clock().targetFrameRate()));
        addDynamicInfo("Total Updates",  context.clock()::pausableUpdatesCount);
        addDynamicInfo("Render Queue Size: ", () -> context.renderManager().renderQueueSize());
        colorPicker("Canvas Color", viewModel.common2DSettings().canvasBackgroundColorProperty());
        //checkBox("Font Smoothing",  viewModel.common2DSettings().fontSmoothingOnProperty());
        checkBox("Show Debug Info", viewModel.debugModeOnProperty());
        checkBox("Time Measured", context.clock().timeMeasuredProperty());
    }
}